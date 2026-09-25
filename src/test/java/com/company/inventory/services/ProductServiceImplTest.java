package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.dao.IProductDao;
import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @InjectMocks
    ProductServiceImpl service;

    @Mock
    ICategoryDao categoryDao;

    @Mock
    IProductDao productDao;

    private static final byte[] IMAGE = "imagen de prueba del producto".getBytes();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Crea una categoria de prueba
     */
    private Category category() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Abarrotes");
        category.setDescription("Distintos tipos de abarrotes");
        return category;
    }

    /**
     * Crea un producto de prueba CON imagen comprimida
     * (los productos sin imagen rompen la descompresion)
     */
    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(100);
        product.setAccount(10);
        product.setPicture(Util.compressZLib(IMAGE));
        return product;
    }

    private String metadataType(ResponseEntity<ProductResponseRest> response) {
        return response.getBody().getMetadata().get(0).get("type");
    }

    // ---------------------------------------------------------
    // save
    // ---------------------------------------------------------

    @Test
    void testSaveProductSuccess() {
        //Given
        Product product = product(null, "Arroz");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category()));
        when(productDao.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        //When
        ResponseEntity<ProductResponseRest> response = service.save(product, 1L);

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertEquals("Arroz", response.getBody().getProduct().getProducts().get(0).getName());
        assertEquals("respuesta ok", metadataType(response));

        verify(categoryDao, times(1)).findById(1L);
        verify(productDao, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    void testSaveProductCategoryNotFound() {
        //Given la categoria no existe
        Product product = product(null, "Arroz");
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        //When
        ResponseEntity<ProductResponseRest> response = service.save(product, 99L);

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("respuesta nok", metadataType(response));

        verify(productDao, never()).save(ArgumentMatchers.any());
    }

    @Test
    void testSaveProductDaoReturnsNull() {
        //Given el DAO no guarda nada
        Product product = product(null, "Arroz");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category()));
        when(productDao.save(ArgumentMatchers.any())).thenReturn(null);

        //When
        ResponseEntity<ProductResponseRest> response = service.save(product, 1L);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El estado de la respuesta HTTP debe ser BAD_REQUEST");
        assertEquals("respuesta nok", metadataType(response));
    }

    @Test
    void testSaveProductException() {
        //Given el DAO lanza una excepcion
        Product product = product(null, "Arroz");
        when(categoryDao.findById(ArgumentMatchers.anyLong())).thenThrow(new RuntimeException("Error al consultar categoria"));

        //When
        ResponseEntity<ProductResponseRest> response = service.save(product, 1L);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("respuesta nok", metadataType(response));
    }

    // ---------------------------------------------------------
    // searchById
    // ---------------------------------------------------------

    @Test
    void testSearchByIdSuccess() {
        //Given un producto con imagen comprimida
        Product product = product(10L, "Laptop");
        when(productDao.findById(10L)).thenReturn(Optional.of(product));

        //When
        ResponseEntity<ProductResponseRest> response = service.searchById(10L);

        //Then la imagen viene descomprimida
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        Product found = response.getBody().getProduct().getProducts().get(0);
        assertEquals("Laptop", found.getName());
        assertArrayEquals(IMAGE, found.getPicture(), "La imagen debe venir descomprimida");
        assertEquals("Respuesta ok", metadataType(response));
    }

    @Test
    void testSearchByIdNotFound() {
        //Given un id inexistente
        when(productDao.findById(99L)).thenReturn(Optional.empty());

        //When
        ResponseEntity<ProductResponseRest> response = service.searchById(99L);

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("respuesta nok", metadataType(response));
    }

    @Test
    void testSearchByIdProductWithoutPictureReturns500() {
        //Given un producto SIN imagen (caso real: los datos de import.sql no traen picture)
        Product product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setPicture(null);
        when(productDao.findById(10L)).thenReturn(Optional.of(product));

        //When
        ResponseEntity<ProductResponseRest> response = service.searchById(10L);

        //Then la descompresion de un nulo revienta y el servicio responde 500
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "Un producto sin imagen provoca error interno");
        assertEquals("respuesta nok", metadataType(response));
    }

    @Test
    void testSearchByIdException() {
        //Given el DAO lanza una excepcion
        when(productDao.findById(ArgumentMatchers.anyLong())).thenThrow(new RuntimeException("Error al consultar"));

        //When
        ResponseEntity<ProductResponseRest> response = service.searchById(10L);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("respuesta nok", metadataType(response));
    }

    // ---------------------------------------------------------
    // searchByName
    // ---------------------------------------------------------

    @Test
    void testSearchByNameSuccess() {
        //Given
        List<Product> list = new ArrayList<>();
        list.add(product(10L, "Laptop"));
        when(productDao.findByNameContainingIgnoreCase("lap")).thenReturn(list);

        //When
        ResponseEntity<ProductResponseRest> response = service.searchByName("lap");

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertEquals(1, response.getBody().getProduct().getProducts().size());
        assertEquals("Respuesta ok", metadataType(response));
    }

    @Test
    void testSearchByNameNotFound() {
        //Given ningun producto coincide
        when(productDao.findByNameContainingIgnoreCase("noexiste")).thenReturn(new ArrayList<>());

        //When
        ResponseEntity<ProductResponseRest> response = service.searchByName("noexiste");

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("respuesta nok", metadataType(response));
    }

    @Test
    void testSearchByNameException() {
        //Given
        when(productDao.findByNameContainingIgnoreCase(ArgumentMatchers.anyString())).thenThrow(new RuntimeException("Error al buscar"));

        //When
        ResponseEntity<ProductResponseRest> response = service.searchByName("lap");

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("respuesta nok", metadataType(response));
    }

    // ---------------------------------------------------------
    // search
    // ---------------------------------------------------------

    @Test
    void testSearchSuccess() {
        //Given
        List<Product> list = new ArrayList<>();
        list.add(product(10L, "Laptop"));
        list.add(product(11L, "Smartphone"));
        when(productDao.findAll()).thenReturn(list);

        //When
        ResponseEntity<ProductResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertEquals(2, response.getBody().getProduct().getProducts().size());
        assertArrayEquals(IMAGE, response.getBody().getProduct().getProducts().get(0).getPicture());
        assertEquals("Respuesta ok", metadataType(response));
    }

    @Test
    void testSearchNotFoundEmptyList() {
        //Given la tabla esta vacia
        when(productDao.findAll()).thenReturn(new ArrayList<>());

        //When
        ResponseEntity<ProductResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("respuesta nok", metadataType(response));
    }

    @Test
    void testSearchProductsWithoutPictureReturns500() {
        //Given productos sin imagen como los que trae import.sql
        Product product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setPicture(null);
        List<Product> list = new ArrayList<>();
        list.add(product);
        when(productDao.findAll()).thenReturn(list);

        //When
        ResponseEntity<ProductResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "Listar productos sin imagen provoca error interno");
    }

    @Test
    void testSearchException() {
        //Given
        when(productDao.findAll()).thenThrow(new RuntimeException("Error al consultar"));

        //When
        ResponseEntity<ProductResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("respuesta nok", metadataType(response));
    }

    // ---------------------------------------------------------
    // deleteById
    // ---------------------------------------------------------

    @Test
    void testDeleteByIdSuccess() {
        //Given nada que configurar: deleteById no devuelve nada

        //When
        ResponseEntity<ProductResponseRest> response = service.deleteById(10L);

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertEquals("Respuesta ok", metadataType(response));
        verify(productDao, times(1)).deleteById(10L);
    }

    @Test
    void testDeleteByIdException() {
        //Given
        doThrow(new RuntimeException("Error al eliminar")).when(productDao).deleteById(ArgumentMatchers.anyLong());

        //When
        ResponseEntity<ProductResponseRest> response = service.deleteById(10L);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("respuesta nok", metadataType(response));
    }

    // ---------------------------------------------------------
    // update
    // ---------------------------------------------------------

    @Test
    void testUpdateSuccess() {
        //Given
        Product existing = product(10L, "Laptop");
        Product changes = new Product();
        changes.setName("Laptop Gamer");
        changes.setPrice(2500);
        changes.setAccount(3);
        changes.setPicture(Util.compressZLib(IMAGE));

        when(categoryDao.findById(1L)).thenReturn(Optional.of(category()));
        when(productDao.findById(10L)).thenReturn(Optional.of(existing));
        when(productDao.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        //When
        ResponseEntity<ProductResponseRest> response = service.update(changes, 1L, 10L);

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        Product updated = response.getBody().getProduct().getProducts().get(0);
        assertEquals("Laptop Gamer", updated.getName(), "El nombre debe actualizarse");
        assertEquals(2500, updated.getPrice(), "El precio debe actualizarse");
        assertEquals(3, updated.getAccount(), "La cantidad debe actualizarse");
        assertEquals("Abarrotes", updated.getCategory().getName(), "La categoria debe actualizarse");
        assertEquals("respuesta ok", metadataType(response));
    }

    @Test
    void testUpdateCategoryNotFound() {
        //Given la categoria no existe
        Product changes = new Product();
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        //When
        ResponseEntity<ProductResponseRest> response = service.update(changes, 99L, 10L);

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("respuesta nok", metadataType(response));
        verify(productDao, never()).findById(ArgumentMatchers.anyLong());
    }

    @Test
    void testUpdateProductNotFound() {
        //Given el producto no existe
        Product changes = new Product();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category()));
        when(productDao.findById(99L)).thenReturn(Optional.empty());

        //When
        ResponseEntity<ProductResponseRest> response = service.update(changes, 1L, 99L);

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("respuesta nok", metadataType(response));
        verify(productDao, never()).save(ArgumentMatchers.any());
    }

    @Test
    void testUpdateProductDaoReturnsNull() {
        //Given el DAO no devuelve nada al guardar
        Product existing = product(10L, "Laptop");
        Product changes = new Product();
        changes.setName("Laptop Gamer");

        when(categoryDao.findById(1L)).thenReturn(Optional.of(category()));
        when(productDao.findById(10L)).thenReturn(Optional.of(existing));
        when(productDao.save(ArgumentMatchers.any())).thenReturn(null);

        //When
        ResponseEntity<ProductResponseRest> response = service.update(changes, 1L, 10L);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El estado de la respuesta HTTP debe ser BAD_REQUEST");
        assertEquals("respuesta nok", metadataType(response));
    }

    @Test
    void testUpdateException() {
        //Given el DAO lanza una excepcion
        Product changes = new Product();
        when(categoryDao.findById(ArgumentMatchers.anyLong())).thenThrow(new RuntimeException("Error al actualizar"));

        //When
        ResponseEntity<ProductResponseRest> response = service.update(changes, 1L, 10L);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("respuesta nok", metadataType(response));
    }

}
