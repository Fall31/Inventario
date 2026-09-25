package com.company.inventory.respnose;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductResponseRestTest {

    @Test
    void constructorSinArgumentosInicializaElProductResponse(){
        ProductResponseRest response = new ProductResponseRest();

        assertThat(response.getProduct()).isNotNull();
        assertThat(response.getProduct().getProducts()).isNull();
    }

    @Test
    void getterYSetterPermitenReemplazarElProductResponse(){
        ProductResponseRest response = new ProductResponseRest();
        ProductResponse nuevo = new ProductResponse();

        response.setProduct(nuevo);

        assertThat(response.getProduct()).isEqualTo(nuevo);
    }

    @Test
    void heredaElMetadataDeResponseRest(){
        ProductResponseRest response = new ProductResponseRest();

        response.setMetadata("exito", "200", "2026-09-25");

        assertThat(response.getMetadata()).hasSize(1);
        assertThat(response.getMetadata().get(0))
                .containsEntry("type", "exito")
                .containsEntry("code", "200")
                .containsEntry("date", "2026-09-25");
    }

}
