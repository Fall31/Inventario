package com.company.inventory.respnose;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResponseRestTest {

    @Test
    void constructorSinArgumentosInicializaElCategoryResponse(){
        CategoryResponseRest response = new CategoryResponseRest();

        assertThat(response.getCategoryResponse()).isNotNull();
        assertThat(response.getCategoryResponse().getCategory()).isNull();
    }

    @Test
    void getterYSetterPermitenReemplazarElCategoryResponse(){
        CategoryResponseRest response = new CategoryResponseRest();
        CategoryResponse nuevo = new CategoryResponse();

        response.setCategoryResponse(nuevo);

        assertThat(response.getCategoryResponse()).isEqualTo(nuevo);
    }

    @Test
    void heredaElMetadataDeResponseRest(){
        CategoryResponseRest response = new CategoryResponseRest();

        response.setMetadata("exito", "200", "2026-09-25");

        assertThat(response.getMetadata()).hasSize(1);
        assertThat(response.getMetadata().get(0))
                .containsEntry("type", "exito")
                .containsEntry("code", "200")
                .containsEntry("date", "2026-09-25");
    }

}
