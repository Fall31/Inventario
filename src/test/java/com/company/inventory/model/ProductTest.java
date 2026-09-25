package com.company.inventory.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProduct(){
        Category category = new Category(1L, "Abarrotes", "Categoria de abarrotes");
        byte[] image = new byte[]{1, 2, 3, 4, 5};

        Product product = new Product();
        product.setId(10L);
        product.setName("Arroz");
        product.setPrice(100);
        product.setAccount(50);
        product.setCategory(category);
        product.setPicture(image);

        assertEquals(10L, product.getId());
        assertEquals("Arroz", product.getName());
        assertEquals(100, product.getPrice());
        assertEquals(50, product.getAccount());
        assertEquals(category, product.getCategory());
        assertArrayEquals(image, product.getPicture(), "La imagen del producto debe ser igual a la imagen proporcionada");

    }

    private Product crear(Long id, String name, int price, int account, Category category, byte[] picture) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setAccount(account);
        product.setCategory(category);
        product.setPicture(picture);
        return product;
    }

    private Product base() {
        return crear(10L, "Arroz", 100, 50,
                new Category(1L, "Abarrotes", "Categoria de abarrotes"),
                new byte[]{1, 2, 3, 4, 5});
    }

    @Test
    void constructorSinArgumentosDejaLosValoresPorDefecto(){
        Product product = new Product();

        assertNull(product.getId());
        assertNull(product.getName());
        assertEquals(0, product.getPrice());
        assertEquals(0, product.getAccount());
        assertNull(product.getCategory());
        assertNull(product.getPicture());
    }

    @Test
    void equalsDetectaElMismoObjeto(){
        Product product = base();

        assertTrue(product.equals(product));
    }

    @Test
    void equalsConNuloDevuelveFalse(){
        assertFalse(base().equals(null));
    }

    @Test
    void equalsConOtraClaseDevuelveFalse(){
        assertFalse(base().equals("otra clase"));
    }

    @Test
    void equalsYHashCodeSonIgualesParaObjetosIguales(){
        Product a = base();
        Product b = base();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.toString(), b.toString());
    }

    @Test
    void equalsConTodosLosCamposNulos(){
        Product a = crear(null, null, 0, 0, null, null);
        Product b = crear(null, null, 0, 0, null, null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsComparaElId(){
        Product idNulo = crear(null, "Arroz", 100, 50, null, null);
        Product conId = crear(10L, "Arroz", 100, 50, null, null);
        Product conOtroId = crear(11L, "Arroz", 100, 50, null, null);

        assertFalse(idNulo.equals(conId));
        assertFalse(conId.equals(idNulo));
        assertFalse(conId.equals(conOtroId));
    }

    @Test
    void equalsComparaElName(){
        Product nameNulo = crear(10L, null, 100, 50, null, null);
        Product conName = crear(10L, "Arroz", 100, 50, null, null);
        Product conOtroName = crear(10L, "Azucar", 100, 50, null, null);

        assertFalse(nameNulo.equals(conName));
        assertFalse(conName.equals(nameNulo));
        assertFalse(conName.equals(conOtroName));
    }

    @Test
    void equalsComparaElPrice(){
        Product conPrice = base();
        Product priceDistinto = base();
        priceDistinto.setPrice(200);
        Product priceIgual = base();

        assertFalse(conPrice.equals(priceDistinto));
        assertTrue(conPrice.equals(priceIgual));
    }

    @Test
    void equalsComparaElAccount(){
        Product conAccount = base();
        Product accountDistinto = base();
        accountDistinto.setAccount(60);
        Product accountIgual = base();

        assertFalse(conAccount.equals(accountDistinto));
        assertTrue(conAccount.equals(accountIgual));
    }

    @Test
    void equalsComparaLaCategory(){
        Product categoryNula = crear(10L, "Arroz", 100, 50, null, new byte[]{1});
        Product conCategory = crear(10L, "Arroz", 100, 50,
                new Category(1L, "Abarrotes", "Categoria de abarrotes"), new byte[]{1});
        Product conOtraCategory = crear(10L, "Arroz", 100, 50,
                new Category(2L, "Lacteos", "Leche y derivados"), new byte[]{1});
        Product conCategoryIgual = crear(10L, "Arroz", 100, 50,
                new Category(1L, "Abarrotes", "Categoria de abarrotes"), new byte[]{1});

        assertFalse(categoryNula.equals(conCategory));
        assertFalse(conCategory.equals(categoryNula));
        assertFalse(conCategory.equals(conOtraCategory));
        assertTrue(conCategory.equals(conCategoryIgual));
    }

    @Test
    void equalsComparaElPicture(){
        Product pictureNula = crear(10L, "Arroz", 100, 50, null, null);
        Product conPicture = crear(10L, "Arroz", 100, 50, null, new byte[]{1, 2, 3});
        Product conOtraPicture = crear(10L, "Arroz", 100, 50, null, new byte[]{9});
        Product conPictureIgual = crear(10L, "Arroz", 100, 50, null, new byte[]{1, 2, 3});

        assertFalse(pictureNula.equals(conPicture));
        assertFalse(conPicture.equals(pictureNula));
        assertFalse(conPicture.equals(conOtraPicture));
        assertTrue(conPicture.equals(conPictureIgual));
    }

    @Test
    void canEqualDistingueLaMismaClaseDeOtra(){
        Product product = base();

        assertTrue(product.canEqual(product));
        assertTrue(product.canEqual(new Product()));
        assertFalse(product.canEqual("otra clase"));
    }

    @Test
    void equalsConSubclaseQueNoPuedeSerIgualDevuelveFalse(){
        Product product = base();
        Subproducto subproducto = new Subproducto();
        subproducto.setId(10L);
        subproducto.setName("Arroz");
        subproducto.setPrice(100);
        subproducto.setAccount(50);

        assertFalse(product.equals(subproducto));
    }

    static class Subproducto extends Product {

        @Override
        protected boolean canEqual(Object other) {
            return false;
        }
    }

    @Test
    void toStringIncluyeLosDatosDelProducto(){
        Product product = base();

        assertTrue(product.toString().contains("Arroz"));
        assertTrue(product.toString().contains("Abarrotes"));
    }

}
