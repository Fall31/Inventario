package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import com.company.inventory.services.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(CategoryRestController.class)
class CategoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICategoryService service;

    @Test
    void searchCategoriesReturnsServiceResponse() throws Exception {
        Category category = category(1L, "Abarrotes", "Productos básicos");
        when(service.search()).thenReturn(response(category, HttpStatus.OK));

        mockMvc.perform(get("/api/v1/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryResponse.category[0].id").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes"))
                .andExpect(jsonPath("$.categoryResponse.category[0].description").value("Productos básicos"));

        verify(service).search();
    }

    @Test
    void searchCategoriesPropagatesServiceError() throws Exception {
        when(service.search()).thenReturn(response(null, HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.metadata[0].code").value("01"));

        verify(service).search();
    }

    @Test
    void searchCategoryByIdForwardsIdAndPropagatesNotFound() throws Exception {
        when(service.searchById(42L)).thenReturn(response(null, HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/categories/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());

        verify(service).searchById(42L);
    }

    @Test
    void searchCategoryByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/categories/no-es-numero"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveCategoryPassesJsonBodyToService() throws Exception {
        Category category = category(null, "Bebidas", "Tipos de bebidas");
        when(service.save(category)).thenReturn(response(category(3L, "Bebidas", "Tipos de bebidas"), HttpStatus.OK));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Bebidas"));

        verify(service).save(category);
    }

    @Test
    void saveCategoryRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveCategoryPropagatesServiceError() throws Exception {
        Category category = category(null, "Bebidas", "Tipos de bebidas");
        when(service.save(category)).thenReturn(response(null, HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.metadata").exists());

        verify(service).save(category);
    }

    @Test
    void updateCategoryPassesBodyAndIdToService() throws Exception {
        Category category = category(null, "Abarrotes actualizados", "Descripción actualizada");
        when(service.update(category, 7L)).thenReturn(response(
                category(7L, "Abarrotes actualizados", "Descripción actualizada"), HttpStatus.OK));

        mockMvc.perform(put("/api/v1/categories/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryResponse.category[0].id").value(7))
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes actualizados"));

        verify(service).update(category, 7L);
    }

    @Test
    void updateCategoryPropagatesNotFound() throws Exception {
        Category category = category(null, "No existe", "Descripción");
        when(service.update(category, 404L)).thenReturn(response(null, HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/v1/categories/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());

        verify(service).update(category, 404L);
    }

    @Test
    void deleteCategoryPropagatesServiceError() throws Exception {
        when(service.deleteById(9L)).thenReturn(response(null, HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(delete("/api/v1/categories/9"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.metadata").exists());

        verify(service).deleteById(9L);
    }

    @Test
    void exportCategoriesReturnsExcelAttachment() throws Exception {
        Category category = category(5L, "Lácteos", "Leche y derivados");
        when(service.search()).thenReturn(response(category, HttpStatus.OK));

        byte[] excel = mockMvc.perform(get("/api/v1/categories/export/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=result_category.xlsx"))
                .andReturn().getResponse().getContentAsByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertEquals("Resultado", workbook.getSheetAt(0).getSheetName());
            assertEquals("Nombre", workbook.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
            assertEquals("Lácteos", workbook.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
        }
        verify(service).search();
    }

    private Category category(Long id, String name, String description) {
        return new Category(id, name, description);
    }

    private ResponseEntity<CategoryResponseRest> response(Category category, HttpStatus status) {
        CategoryResponseRest body = new CategoryResponseRest();
        if (category != null) {
            body.getCategoryResponse().setCategory(List.of(category));
            body.setMetadata("Respuesta ok", "00", "Respuesta exitosa");
        } else {
            body.setMetadata("Error", "01", "Respuesta de error");
        }
        return new ResponseEntity<>(body, status);
    }
}
