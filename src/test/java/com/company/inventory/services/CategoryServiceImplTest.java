package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
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

class CategoryServiceImplTest {

    @InjectMocks
    CategoryServiceImpl service;

    @Mock
    ICategoryDao categoryDao;

    List<Category> list = new ArrayList<Category>();

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        this.chargeList();
    }

    /**
     * Test de la busqueda de categorias
     * Se espera que retorne una lista con 2 categorias
     * y el estado de la respuesta HTTP sea OK
     */
    @Test
    void testSearchSucess() {

        //Given
        when(categoryDao.findAll()).thenReturn(list);

        //When
        ResponseEntity<CategoryResponseRest> response = service.search();

        //Then
        assertEquals(2, response.getBody().getCategoryResponse().getCategory().size());
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respueesta HTTP debe ser OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Abarrotes", response.getBody().getCategoryResponse().getCategory().get(0).getName(), "El nombre de la primera categoria debe ser Abarrotes");

        //Optional
        verify(categoryDao, times(1)).findAll();

    }

    /**
     * Test de la busqueda de categorias
     * Se espera que retorne un error al consultar
     * y el estado de la respuesta HTTP sea INTERNAL_SERVER_ERROR
     */
    @Test
    void testSearchException() {

        //Given
        when(categoryDao.findAll()).thenThrow(new RuntimeException("Error al consultar"));

        //When
        ResponseEntity<CategoryResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");

        //Optional
        verify(categoryDao, times(1)).findAll();
    }

    /**
     * Test para probar guardar una categoria
     */
    @Test
    void testSaveCategorySuccess() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(category);

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Bebidas", response.getBody().getCategoryResponse().getCategory().get(0).getName(), "El nombre de la categoria guardada debe ser Bebidas");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    /**
     * Test para probar retorno nullo al guardar una categoria
     */
    @Test
    void testSaveCategoryDaoReturnsNull() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(null);

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El estado de la respuesta HTTP debe ser BAD_REQUEST");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    /**
     * Test para probar una excepción al guardar una categoria
     */
    @Test
    void testSaveCategoryException() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenThrow(new RuntimeException("Error al guardar"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }


    /**
     * HU-7: busqueda de una categoria por id existente
     */
    @Test
    void testSearchByIdSuccess() {
        // Given
        when(categoryDao.findById(1L)).thenReturn(Optional.of(list.get(0)));

        // When
        ResponseEntity<CategoryResponseRest> response = service.searchById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertEquals(1, response.getBody().getCategoryResponse().getCategory().size(), "Debe devolver una sola categoria");
        assertEquals("Abarrotes", response.getBody().getCategoryResponse().getCategory().get(0).getName());

        verify(categoryDao, times(1)).findById(1L);
    }

    /**
     * HU-7: busqueda de una categoria con id inexistente
     */
    @Test
    void testSearchByIdNotFound() {
        // Given
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<CategoryResponseRest> response = service.searchById(99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));

        verify(categoryDao, times(1)).findById(99L);
    }

    /**
     * HU-7: excepcion al buscar una categoria por id
     */
    @Test
    void testSearchByIdException() {
        // Given
        when(categoryDao.findById(ArgumentMatchers.anyLong())).thenThrow(new RuntimeException("Error al consultar por id"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.searchById(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
    }

    /**
     * HU-7: actualizacion exitosa de una categoria
     */
    @Test
    void testUpdateCategorySuccess() {
        // Given
        Category changes = new Category();
        changes.setName("Abarrotes y Lacteos");
        changes.setDescription("Nueva descripcion");

        when(categoryDao.findById(1L)).thenReturn(Optional.of(list.get(0)));
        when(categoryDao.save(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(changes, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        Category updated = response.getBody().getCategoryResponse().getCategory().get(0);
        assertEquals("Abarrotes y Lacteos", updated.getName(), "El nombre debe actualizarse");
        assertEquals("Nueva descripcion", updated.getDescription(), "La descripcion debe actualizarse");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    /**
     * HU-7: actualizacion de una categoria inexistente
     */
    @Test
    void testUpdateCategoryNotFound() {
        // Given
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(new Category(), 99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El estado de la respuesta HTTP debe ser NOT_FOUND");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));

        verify(categoryDao, never()).save(ArgumentMatchers.any());
    }

    /**
     * HU-7: el DAO no devuelve nada al guardar la actualizacion
     */
    @Test
    void testUpdateCategoryDaoReturnsNull() {
        // Given
        when(categoryDao.findById(1L)).thenReturn(Optional.of(list.get(0)));
        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(null);

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(new Category(), 1L);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El estado de la respuesta HTTP debe ser BAD_REQUEST");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
    }

    /**
     * HU-7: excepcion al actualizar una categoria
     */
    @Test
    void testUpdateCategoryException() {
        // Given
        when(categoryDao.findById(ArgumentMatchers.anyLong())).thenThrow(new RuntimeException("Error al actualizar"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(new Category(), 1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
    }

    /**
     * HU-7: eliminacion exitosa de una categoria
     */
    @Test
    void testDeleteByIdSuccess() {
        // When
        ResponseEntity<CategoryResponseRest> response = service.deleteById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertEquals("respuesta ok", response.getBody().getMetadata().get(0).get("type"));

        verify(categoryDao, times(1)).deleteById(1L);
    }

    /**
     * HU-7: excepcion al eliminar una categoria
     */
    @Test
    void testDeleteByIdException() {
        // Given
        doThrow(new RuntimeException("Error al eliminar")).when(categoryDao).deleteById(ArgumentMatchers.anyLong());

        // When
        ResponseEntity<CategoryResponseRest> response = service.deleteById(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
    }


    /**
     * Método que agrega datos a la lista de categorias
     */
    public void chargeList() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Abarrotes");
        category.setDescription("Distintos tipos de abarrotes");
        list.add(category);

        category = new Category();
        category.setId(2L);
        category.setName("Lacteos");
        category.setDescription("Distintos tipos de lacteos");
        list.add(category);
    }
}