package com.company.inventory.model;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    void testCategory(){
       Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

       assertThat(category.getId()).isEqualTo(4L);
       assertThat(category.getName()).isEqualTo("Electronicas");
       assertThat(category.getDescription()).isEqualTo("Categoria de electronicos");
   }

    @Test
    void constructorSinArgumentosDejaLosCamposEnNulo(){
        Category category = new Category();

        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getDescription()).isNull();
    }

    @Test
    void settersPermitenActualizarLosTresCampos(){
        Category category = new Category();

        category.setId(9L);
        category.setName("Libros");
        category.setDescription("Libros impresos y digitales");

        assertThat(category.getId()).isEqualTo(9L);
        assertThat(category.getName()).isEqualTo("Libros");
        assertThat(category.getDescription()).isEqualTo("Libros impresos y digitales");
    }

    @Test
    void equalsDetectaElMismoObjeto(){
        Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

        assertThat(category).isEqualTo(category);
    }

    @Test
    void equalsConNuloDevuelveFalse(){
        Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

        assertThat(category.equals(null)).isFalse();
    }

    @Test
    void equalsConOtraClaseDevuelveFalse(){
        Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

        assertThat(category.equals("otra clase")).isFalse();
    }

    @Test
    void equalsYHashCodeSonIgualesParaObjetosIguales(){
        Category a = new Category(4L, "Electronicas", "Categoria de electronicos");
        Category b = new Category(4L, "Electronicas", "Categoria de electronicos");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).isEqualTo(b.toString());
    }

    @Test
    void equalsConTodosLosCamposNulos(){
        Category a = new Category(null, null, null);
        Category b = new Category(null, null, null);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void equalsComparaElId(){
        Category idNulo = new Category(null, "Electronicas", "Devices");
        Category conId = new Category(1L, "Electronicas", "Devices");
        Category conOtroId = new Category(2L, "Electronicas", "Devices");

        assertThat(idNulo).isNotEqualTo(conId);
        assertThat(conId).isNotEqualTo(idNulo);
        assertThat(conId).isNotEqualTo(conOtroId);
    }

    @Test
    void equalsComparaElName(){
        Category nameNulo = new Category(1L, null, "Devices");
        Category conName = new Category(1L, "Electronicas", "Devices");
        Category conOtroName = new Category(1L, "Libros", "Devices");

        assertThat(nameNulo).isNotEqualTo(conName);
        assertThat(conName).isNotEqualTo(nameNulo);
        assertThat(conName).isNotEqualTo(conOtroName);
    }

    @Test
    void equalsComparaLaDescription(){
        Category descriptionNula = new Category(1L, "Electronicas", null);
        Category conDescription = new Category(1L, "Electronicas", "Devices");
        Category conOtraDescription = new Category(1L, "Electronicas", "Gadgets");

        assertThat(descriptionNula).isNotEqualTo(conDescription);
        assertThat(conDescription).isNotEqualTo(descriptionNula);
        assertThat(conDescription).isNotEqualTo(conOtraDescription);
    }

    @Test
    void canEqualDistingueLaMismaClaseDeOtra(){
        Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

        assertThat(category.canEqual(category)).isTrue();
        assertThat(category.canEqual(new Category())).isTrue();
        assertThat(category.canEqual("otra clase")).isFalse();
    }

    @Test
    void equalsConSubclaseQueNoPuedeSerIgualDevuelveFalse(){
        Category category = new Category(1L, "Electronicas", "Devices");
        Subcategoria subcategoria = new Subcategoria(1L, "Electronicas", "Devices");

        assertThat(category.equals(subcategoria)).isFalse();
    }

    static class Subcategoria extends Category {

        Subcategoria(Long id, String name, String description) {
            super(id, name, description);
        }

        @Override
        protected boolean canEqual(Object other) {
            return false;
        }
    }

    @Test
    void toStringIncluyeLosTresCampos(){
        Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

        assertThat(category.toString()).contains("Electronicas", "Categoria de electronicos");
    }

}
