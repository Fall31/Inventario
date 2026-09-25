package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.services.IProductService;
import com.company.inventory.util.Util;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.mockito.ArgumentCaptor;

@WebMvcTest(ProductRestController.class)
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @Test
    void saveProductMapsMultipartFieldsAndImage() throws Exception {
        byte[] image = {1, 2, 3, 4};
        when(productService.save(any(Product.class), eq(12L)))
                .thenReturn(response(product(8L, "Café", 25, 4, category(12L, "Bebidas")), HttpStatus.OK));

        mockMvc.perform(multipart("/api/v1/products")
                        .file(new MockMultipartFile("picture", "foto.bin", "application/octet-stream", image))
                        .param("name", "Café")
                        .param("price", "25")
                        .param("account", "4")
                        .param("categoryId", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].id").value(8))
                .andExpect(jsonPath("$.product.products[0].name").value("Café"))
                .andExpect(jsonPath("$.product.products[0].price").value(25));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productService).save(productCaptor.capture(), eq(12L));
        Product savedProduct = productCaptor.getValue();
        assertEquals("Café", savedProduct.getName());
        assertEquals(25, savedProduct.getPrice());
        assertEquals(4, savedProduct.getAccount());
        assertArrayEquals(image, Util.decompressZLib(savedProduct.getPicture()));
    }

    @Test
    void saveProductRejectsMissingMultipartParameter() throws Exception {
        mockMvc.perform(multipart("/api/v1/products")
                        .param("name", "Café")
                        .param("price", "25")
                        .param("account", "4")
                        .param("categoryId", "12"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveProductRejectsNonNumericPrice() throws Exception {
        mockMvc.perform(multipart("/api/v1/products")
                        .file(new MockMultipartFile("picture", "foto.bin", "application/octet-stream", new byte[]{1}))
                        .param("name", "Café")
                        .param("price", "no-numerico")
                        .param("account", "4")
                        .param("categoryId", "12"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveProductPropagatesServiceNotFound() throws Exception {
        when(productService.save(any(Product.class), eq(404L))).thenReturn(response(null, HttpStatus.NOT_FOUND));

        mockMvc.perform(multipart("/api/v1/products")
                        .file(new MockMultipartFile("picture", "foto.bin", "application/octet-stream", new byte[]{1}))
                        .param("name", "Café")
                        .param("price", "25")
                        .param("account", "4")
                        .param("categoryId", "404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());

        verify(productService).save(any(Product.class), eq(404L));
    }

    @Test
    void searchProductsReturnsProducts() throws Exception {
        when(productService.search()).thenReturn(response(product(1L, "Café", 25, 4, category(2L, "Bebidas")),
                HttpStatus.OK));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].name").value("Café"))
                .andExpect(jsonPath("$.product.products[0].category.name").value("Bebidas"));

        verify(productService).search();
    }

    @Test
    void searchProductsPropagatesNotFound() throws Exception {
        when(productService.search()).thenReturn(response(null, HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());

        verify(productService).search();
    }

    @Test
    void searchProductByIdForwardsId() throws Exception {
        when(productService.searchById(3L))
                .thenReturn(response(product(3L, "Café", 25, 4, category(2L, "Bebidas")), HttpStatus.OK));

        mockMvc.perform(get("/api/v1/products/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].id").value(3))
                .andExpect(jsonPath("$.product.products[0].name").value("Café"));

        verify(productService).searchById(3L);
    }

    @Test
    void searchProductByIdPropagatesNotFoundAndRejectsInvalidId() throws Exception {
        when(productService.searchById(404L)).thenReturn(response(null, HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/products/404"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/products/no-numerico"))
                .andExpect(status().isBadRequest());

        verify(productService).searchById(404L);
    }

    @Test
    void searchProductsByNameReturnsMatchingProduct() throws Exception {
        when(productService.searchByName("cafe"))
                .thenReturn(response(product(3L, "Café", 25, 4, category(2L, "Bebidas")), HttpStatus.OK));

        mockMvc.perform(get("/api/v1/products/filter/cafe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].name").value("Café"));

        verify(productService).searchByName("cafe");
    }

    @Test
    void searchProductsByNamePropagatesNotFound() throws Exception {
        when(productService.searchByName("inexistente")).thenReturn(response(null, HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/products/filter/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());

        verify(productService).searchByName("inexistente");
    }

    @Test
    void updateProductMapsMultipartFieldsImageAndId() throws Exception {
        byte[] image = {9, 8, 7};
        when(productService.update(any(Product.class), eq(12L), eq(6L)))
                .thenReturn(response(product(6L, "Café nuevo", 30, 5, category(12L, "Bebidas")), HttpStatus.OK));

        MockHttpServletRequestBuilder request = multipart("/api/v1/products/6")
                .file(new MockMultipartFile("picture", "foto.bin", "application/octet-stream", image))
                .param("name", "Café nuevo")
                .param("price", "30")
                .param("account", "5")
                .param("categoryId", "12");
        request = request.with(requestBuilder -> {
            requestBuilder.setMethod("PUT");
            return requestBuilder;
        });
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].name").value("Café nuevo"))
                .andExpect(jsonPath("$.product.products[0].price").value(30));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productService).update(productCaptor.capture(), eq(12L), eq(6L));
        assertEquals("Café nuevo", productCaptor.getValue().getName());
        assertEquals(5, productCaptor.getValue().getAccount());
        assertArrayEquals(image, Util.decompressZLib(productCaptor.getValue().getPicture()));
    }

    @Test
    void updateProductPropagatesServiceError() throws Exception {
        when(productService.update(any(Product.class), eq(12L), eq(404L)))
                .thenReturn(response(null, HttpStatus.INTERNAL_SERVER_ERROR));

        MockHttpServletRequestBuilder request = multipart("/api/v1/products/404")
                .file(new MockMultipartFile("picture", "foto.bin", "application/octet-stream", new byte[]{1}))
                .param("name", "Café")
                .param("price", "25")
                .param("account", "4")
                .param("categoryId", "12");
        request = request.with(requestBuilder -> {
            requestBuilder.setMethod("PUT");
            return requestBuilder;
        });

        mockMvc.perform(request)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.metadata").exists());

        verify(productService).update(any(Product.class), eq(12L), eq(404L));
    }

    @Test
    void deleteProductPropagatesServiceError() throws Exception {
        when(productService.deleteById(9L)).thenReturn(response(null, HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(delete("/api/v1/products/9"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.metadata").exists());

        verify(productService).deleteById(9L);
    }

    @Test
    void exportProductsReturnsExcelAttachment() throws Exception {
        when(productService.search()).thenReturn(response(
                product(5L, "Café", 25, 4, category(2L, "Bebidas")), HttpStatus.OK));

        byte[] excel = mockMvc.perform(get("/api/v1/products/export/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=result_product.xlsx"))
                .andReturn().getResponse().getContentAsByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertEquals("Resultado", workbook.getSheetAt(0).getSheetName());
            assertEquals("Nombre", workbook.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
            assertEquals("Café", workbook.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
            assertEquals(25, (int) workbook.getSheetAt(0).getRow(1).getCell(2).getNumericCellValue());
        }
        verify(productService).search();
    }

    private Product product(Long id, String name, int price, int account, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setAccount(account);
        product.setCategory(category);
        return product;
    }

    private Category category(Long id, String name) {
        return new Category(id, name, "Descripción");
    }

    private ResponseEntity<ProductResponseRest> response(Product product, HttpStatus status) {
        ProductResponseRest body = new ProductResponseRest();
        if (product != null) {
            body.getProduct().setProducts(List.of(product));
            body.setMetadata("Respuesta ok", "00", "Respuesta exitosa");
        } else {
            body.setMetadata("Error", "01", "Respuesta de error");
        }
        return new ResponseEntity<>(body, status);
    }
}
