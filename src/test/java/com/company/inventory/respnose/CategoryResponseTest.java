package com.company.inventory.respnose;

import java.util.ArrayList;
import java.util.List;

import com.company.inventory.model.Category;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResponseTest {

    @Test
    void constructorSinArgumentosDejaLaListaEnNulo(){
        CategoryResponse response = new CategoryResponse();

        assertThat(response.getCategory()).isNull();
    }

    @Test
    void getterYSetterPermitenGuardarLaLista(){
        CategoryResponse response = new CategoryResponse();
        List<Category> lista = new ArrayList<>();
        lista.add(new Category(1L, "Libros", "Libros impresos y digitales"));

        response.setCategory(lista);

        assertThat(response.getCategory()).isEqualTo(lista);
    }

    @Test
    void equalsDetectaElMismoObjeto(){
        CategoryResponse response = new CategoryResponse();

        assertThat(response).isEqualTo(response);
    }

    @Test
    void equalsConNuloDevuelveFalse(){
        CategoryResponse response = new CategoryResponse();

        assertThat(response.equals(null)).isFalse();
    }

    @Test
    void equalsConOtraClaseDevuelveFalse(){
        CategoryResponse response = new CategoryResponse();

        assertThat(response.equals("otra clase")).isFalse();
    }

    @Test
    void equalsYHashCodeSonIgualesParaObjetosIguales(){
        CategoryResponse a = new CategoryResponse();
        CategoryResponse b = new CategoryResponse();
        List<Category> lista = new ArrayList<>();
        lista.add(new Category(1L, "Libros", "Libros impresos y digitales"));
        a.setCategory(lista);
        b.setCategory(lista);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).isEqualTo(b.toString());
    }

    @Test
    void canEqualDistingueLaMismaClaseDeOtra(){
        CategoryResponse response = new CategoryResponse();

        assertThat(response.canEqual(response)).isTrue();
        assertThat(response.canEqual(new CategoryResponse())).isTrue();
        assertThat(response.canEqual("otra clase")).isFalse();
    }

    @Test
    void equalsConSubclaseQueNoPuedeSerIgualDevuelveFalse(){
        CategoryResponse response = new CategoryResponse();
        SubCategoryResponse subrespuesta = new SubCategoryResponse();

        assertThat(response.equals(subrespuesta)).isFalse();
    }

    static class SubCategoryResponse extends CategoryResponse {

        @Override
        protected boolean canEqual(Object other) {
            return false;
        }
    }

    @Test
    void equalsConLaListaEnNulo(){
        CategoryResponse a = new CategoryResponse();
        CategoryResponse b = new CategoryResponse();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void equalsComparaLaLista(){
        CategoryResponse conListaNula = new CategoryResponse();

        CategoryResponse conLista = new CategoryResponse();
        List<Category> lista = new ArrayList<>();
        lista.add(new Category(1L, "Libros", "Libros impresos y digitales"));
        conLista.setCategory(lista);

        CategoryResponse conOtraLista = new CategoryResponse();
        List<Category> otraLista = new ArrayList<>();
        otraLista.add(new Category(2L, "Electronicas", "Devices and gadgets"));
        conOtraLista.setCategory(otraLista);

        assertThat(conListaNula).isNotEqualTo(conLista);
        assertThat(conLista).isNotEqualTo(conListaNula);
        assertThat(conLista).isNotEqualTo(conOtraLista);
    }

}
