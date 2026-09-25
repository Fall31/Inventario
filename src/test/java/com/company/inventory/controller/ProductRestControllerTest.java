package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.services.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
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
class ProductRestControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ProductRestController productRestController;

    @Mock
    private IProductService productService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(productRestController).build();
    }

    @Test
    void testSearch() throws Exception {
        ProductResponseRest responseRest = new ProductResponseRest();
        responseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(productService.search()).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testSearchById() throws Exception {
        Long id = 1L;
        ProductResponseRest responseRest = new ProductResponseRest();
        responseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(productService.searchById(id)).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testSearchByName() throws Exception {
        String name = "Laptop";
        ProductResponseRest responseRest = new ProductResponseRest();
        responseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(productService.searchByName(name)).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/products/filter/{name}", name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testSave() throws Exception {
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "img bytes".getBytes());
        ProductResponseRest responseRest = new ProductResponseRest();
        responseRest.setMetadata("Respuesta ok", "00", "Producto guardado");

        when(productService.save(any(Product.class), eq(1L))).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(multipart("/api/v1/products")
                        .file(picture)
                        .param("name", "Arroz")
                        .param("price", "10")
                        .param("account", "100")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testUpdate() throws Exception {
        Long id = 1L;
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "img bytes".getBytes());
        ProductResponseRest responseRest = new ProductResponseRest();
        responseRest.setMetadata("Respuesta ok", "00", "Producto actualizado");

        when(productService.update(any(Product.class), eq(1L), eq(id))).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(multipart("/api/v1/products/{id}", id)
                        .file(picture)
                        .param("name", "Arroz Premium")
                        .param("price", "12")
                        .param("account", "100")
                        .param("categoryId", "1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testDeleteById() throws Exception {
        Long id = 1L;
        ProductResponseRest responseRest = new ProductResponseRest();
        responseRest.setMetadata("Respuesta ok", "00", "Producto eliminado");

        when(productService.deleteById(id)).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(delete("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testExportToExcel() throws Exception {
        ProductResponseRest responseRest = new ProductResponseRest();
        List<Product> list = new ArrayList<>();

        // 1. Instanciamos la categoría requerida por el exportador
        Category category = new Category();
        category.setId(1L);
        category.setName("Tecnología");
        category.setDescription("Dispositivos electrónicos");

        // 2. Instanciamos el producto y le asignamos la categoría
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(1000);
        product.setAccount(10);
        product.setCategory(category); // <--- Evita el NullPointerException

        list.add(product);

        responseRest.getProduct().setProducts(list);

        when(productService.search()).thenReturn(new ResponseEntity<>(responseRest, HttpStatus.OK));

        this.mockMvc.perform(get("/api/v1/products/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=result_product.xlsx"));
    }
}