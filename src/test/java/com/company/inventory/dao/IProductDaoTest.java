package com.company.inventory.dao;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del repositorio IProductDao.
 * Los datos base (3 categorías y 4 productos) se cargan desde
 * src/test/resources/import.sql al levantar el contexto @DataJpaTest.
 */
@DataJpaTest
class IProductDaoTest {

    @Autowired
    IProductDao productDao;

    /**
     * Prueba que un producto exista y tenga los datos correctos
     */
    @Test
    void testFindById() {
        Optional<Product> product = productDao.findById(1L);
        assertTrue(product.isPresent(), "El producto debería existir");
        assertEquals("Laptop", product.get().getName(), "El nombre del producto debe coincidir");
        assertEquals(1000, product.get().getPrice(), "El precio del producto debe coincidir");
    }

    /**
     * Prueba que la búsqueda de un id inexistente lance excepción al usar orElseThrow
     */
    @Test
    void testFindByIdThrowsException() {
        Optional<Product> product = productDao.findById(999L);
        assertFalse(product.isPresent(), "No debería encontrar un producto con id inexistente");
        assertThrows(NoSuchElementException.class, product::orElseThrow);
    }

    /**
     * Prueba que se listen todos los productos cargados
     */
    @Test
    void testFindAll() {
        List<Product> products = (List<Product>) productDao.findAll();
        assertFalse(products.isEmpty(), "La lista de productos no debería estar vacía");
        assertEquals(4, products.size(), "Deberían existir 4 productos cargados por import.sql");
    }

    /**
     * Prueba que se guarde un nuevo producto asociado a una categoría existente
     */
    @Test
    void testSaveProduct() {
        Category category = new Category();
        category.setId(2L);

        Product newProduct = new Product();
        newProduct.setName("Clean Code");
        newProduct.setPrice(45);
        newProduct.setAccount(30);
        newProduct.setCategory(category);

        Product savedProduct = productDao.save(newProduct);

        assertNotNull(savedProduct.getId(), "El producto guardado debería tener un id generado");
        assertEquals("Clean Code", savedProduct.getName(), "El nombre del producto guardado debe coincidir");
        assertEquals(2L, savedProduct.getCategory().getId(), "La categoría asociada debe coincidir");
    }

    /**
     * Prueba que se actualice un producto existente
     */
    @Test
    void testUpdateProduct() {
        Optional<Product> product = productDao.findById(1L);
        assertTrue(product.isPresent(), "El producto debería existir para poder actualizarlo");

        Product existingProduct = product.get();
        existingProduct.setPrice(1200);
        existingProduct.setAccount(3);

        Product updatedProduct = productDao.save(existingProduct);

        assertEquals(1200, updatedProduct.getPrice(), "El precio actualizado debe coincidir");
        assertEquals(3, updatedProduct.getAccount(), "El stock actualizado debe coincidir");
    }

    /**
     * Prueba que se elimine un producto mediante la entidad
     */
    @Test
    void testDeleteProduct() {
        Optional<Product> product = productDao.findById(1L);
        assertTrue(product.isPresent(), "El producto debería existir para poder eliminarlo");

        productDao.delete(product.get());

        Optional<Product> deletedProduct = productDao.findById(1L);
        assertFalse(deletedProduct.isPresent(), "El producto eliminado no debería encontrarse");
    }

    /**
     * Prueba que se elimine un producto por id
     */
    @Test
    void testDeleteById() {
        Long idToDelete = 2L;
        assertTrue(productDao.findById(idToDelete).isPresent(), "El producto debería existir antes de eliminarlo");

        productDao.deleteById(idToDelete);

        assertFalse(productDao.findById(idToDelete).isPresent(), "El producto eliminado no debería encontrarse");
    }


    /**
     * Prueba el método existsById heredado de CrudRepository
     */
    @Test
    void testExistsById() {
        assertTrue(productDao.existsById(1L), "El producto con id 1 debería existir");
        assertFalse(productDao.existsById(999L), "El producto con id 999 no debería existir");
    }

    /**
     * Prueba el método count heredado de CrudRepository
     */
    @Test
    void testCount() {
        assertEquals(4, productDao.count(), "Deberían existir 4 productos cargados por import.sql");
    }

    /**
     * Prueba la consulta personalizada findByNameLike (JPQL con LIKE)
     */
    @Test
    void testFindByNameLike() {
        List<Product> result = productDao.findByNameLike("phone");
        assertEquals(1, result.size(), "Debería encontrar exactamente 'Smartphone'");
        assertEquals("Smartphone", result.get(0).getName());
    }

    /**
     * Prueba que findByNameLike no encuentre coincidencias inexistentes
     */
    @Test
    void testFindByNameLikeNoMatch() {
        List<Product> result = productDao.findByNameLike("Bicicleta");
        assertTrue(result.isEmpty(), "No debería encontrar coincidencias para 'Bicicleta'");
    }

    /**
     * Prueba la consulta derivada findByNameContainingIgnoreCase (case-insensitive)
     */
    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Product> result = productDao.findByNameContainingIgnoreCase("LAPTOP");
        assertEquals(1, result.size(), "Debería encontrar 'Laptop' sin importar mayúsculas/minúsculas");
        assertEquals("Laptop", result.get(0).getName());
    }

    /**
     * Prueba que un producto recuperado conserve la relación con su categoría
     */
    @Test
    void testProductHasCategory() {
        Optional<Product> product = productDao.findById(3L);
        assertTrue(product.isPresent(), "El producto debería existir");
        assertNotNull(product.get().getCategory(), "El producto debería tener una categoría asociada");
        assertEquals("Books", product.get().getCategory().getName(), "La categoría asociada debe ser 'Books'");
    }

}