package com.company.inventory.respnose;

import java.util.ArrayList;
import java.util.List;

import com.company.inventory.model.Product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductResponseTest {

    @Test
    void constructorSinArgumentosDejaLaListaEnNulo(){
        ProductResponse response = new ProductResponse();

        assertThat(response.getProducts()).isNull();
    }

    @Test
    void getterYSetterPermitenGuardarLaLista(){
        ProductResponse response = new ProductResponse();
        List<Product> lista = new ArrayList<>();
        lista.add(new Product());

        response.setProducts(lista);

        assertThat(response.getProducts()).isEqualTo(lista);
    }

    @Test
    void equalsDetectaElMismoObjeto(){
        ProductResponse response = new ProductResponse();

        assertThat(response).isEqualTo(response);
    }

    @Test
    void equalsConNuloDevuelveFalse(){
        ProductResponse response = new ProductResponse();

        assertThat(response.equals(null)).isFalse();
    }

    @Test
    void equalsConOtraClaseDevuelveFalse(){
        ProductResponse response = new ProductResponse();

        assertThat(response.equals("otra clase")).isFalse();
    }

    @Test
    void equalsYHashCodeSonIgualesParaObjetosIguales(){
        ProductResponse a = new ProductResponse();
        ProductResponse b = new ProductResponse();
        List<Product> lista = new ArrayList<>();
        lista.add(new Product());
        a.setProducts(lista);
        b.setProducts(lista);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).isEqualTo(b.toString());
    }

    @Test
    void canEqualDistingueLaMismaClaseDeOtra(){
        ProductResponse response = new ProductResponse();

        assertThat(response.canEqual(response)).isTrue();
        assertThat(response.canEqual(new ProductResponse())).isTrue();
        assertThat(response.canEqual("otra clase")).isFalse();
    }

    @Test
    void equalsConSubclaseQueNoPuedeSerIgualDevuelveFalse(){
        ProductResponse response = new ProductResponse();
        SubProductResponse subrespuesta = new SubProductResponse();

        assertThat(response.equals(subrespuesta)).isFalse();
    }

    static class SubProductResponse extends ProductResponse {

        @Override
        protected boolean canEqual(Object other) {
            return false;
        }
    }

    @Test
    void equalsConLaListaEnNulo(){
        ProductResponse a = new ProductResponse();
        ProductResponse b = new ProductResponse();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void equalsComparaLaLista(){
        ProductResponse conListaNula = new ProductResponse();

        ProductResponse conLista = new ProductResponse();
        List<Product> lista = new ArrayList<>();
        Product product = new Product();
        product.setName("Arroz");
        lista.add(product);
        conLista.setProducts(lista);

        ProductResponse conOtraLista = new ProductResponse();
        List<Product> otraLista = new ArrayList<>();
        Product otroProduct = new Product();
        otroProduct.setName("Azucar");
        otraLista.add(otroProduct);
        conOtraLista.setProducts(otraLista);

        assertThat(conListaNula).isNotEqualTo(conLista);
        assertThat(conLista).isNotEqualTo(conListaNula);
        assertThat(conLista).isNotEqualTo(conOtraLista);
    }

}
