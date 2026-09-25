package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponse;
import com.company.inventory.respnose.CategoryResponseRest;
import com.company.inventory.services.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryRestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CategoryRestController categoryRestController;

    @Mock
    private ICategoryService service;

    private final List<Category> list = new ArrayList<>();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(categoryRestController).build();
        this.chargeList();
    }

    @Test
    void testSearchCategories() throws Exception {
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(list);
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(service.search()).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchCategoriesError() throws Exception {
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al consultar categorias");

        when(service.search()).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        this.mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearchCategoriesById() throws Exception {
        Long id = 1L;
        Category category = list.get(0);
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(List.of(category));
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(service.searchById(id)).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchCategoriesByIdError() throws Exception {
        Long id = 1L;
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al consultar categoria por id");

        when(service.searchById(id)).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        this.mockMvc.perform(get("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSaveCategory() throws Exception {
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintos tipos de bebidas");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(List.of(category));
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Categoria guardada exitosamente");

        when(service.save(any(Category.class))).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.OK));

        this.mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Bebidas"))
                .andExpect(status().isOk());
    }

    @Test
    void testSaveCategoryError() throws Exception {
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al guardar la categoria");

        when(service.save(any(Category.class))).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        this.mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateCategory() throws Exception {
        Long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Abarrotes Actualizado");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(List.of(category));
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Categoria actualizada exitosamente");

        when(service.update(any(Category.class), eq(id))).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.OK));

        this.mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes Actualizado"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCategoryError() throws Exception {
        Long id = 1L;
        Category category = new Category();
        category.setId(id);

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al actualizar la categoria");

        when(service.update(any(Category.class), eq(id))).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        this.mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteCategory() throws Exception {
        Long id = 1L;
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Categoria eliminada exitosamente");

        when(service.deleteById(id)).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.OK));

        this.mockMvc.perform(delete("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCategoryError() throws Exception {
        Long id = 1L;
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al eliminar la categoria");

        when(service.deleteById(id)).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        this.mockMvc.perform(delete("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testExportToExcel() throws Exception {
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(list);

        when(service.search()).thenReturn(new ResponseEntity<>(categoryResponseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/categories/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=result_category.xlsx"));
    }

    public void chargeList() {
        list.clear();
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Abarrotes");
        category1.setDescription("Distintos tipos de abarrotes");
        list.add(category1);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Lacteos");
        category2.setDescription("Distintos tipos de lacteos");
        list.add(category2);
    }
}