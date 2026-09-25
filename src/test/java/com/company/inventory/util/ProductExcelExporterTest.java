package com.company.inventory.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductExcelExporterTest {

    private Product product(Long id, String name, int price, int account, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setAccount(account);
        product.setCategory(category);
        return product;
    }

    private byte[] export(List<Product> products) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ProductExcelExporter(products).export(response);
        return response.getContentAsByteArray();
    }

    @Test
    void testExportHeaderLine() throws Exception {
        //Given al menos un producto
        Category category = new Category(1L, "Abarrotes", "Categoria de abarrotes");
        List<Product> products = List.of(product(1L, "Arroz", 100, 50, category));

        //When se exporta
        byte[] bytes = export(products);

        //Then el Excel tiene la hoja "Resultado" con las 5 columnas del encabezado
        assertTrue(bytes.length > 0, "El Excel no debe venir vacio");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet, "La hoja debe llamarse Resultado");

            Row header = sheet.getRow(0);
            assertNotNull(header);
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("Nombre", header.getCell(1).getStringCellValue());
            assertEquals("Precio", header.getCell(2).getStringCellValue());
            assertEquals("Cantidad", header.getCell(3).getStringCellValue());
            assertEquals("Categoría", header.getCell(4).getStringCellValue());
        }
    }

    @Test
    void testExportDataLines() throws Exception {
        //Given dos productos de distinta categoria
        Category abarrotes = new Category(1L, "Abarrotes", "Categoria de abarrotes");
        Category bebidas = new Category(2L, "Bebidas", "Categoria de bebidas");
        List<Product> products = List.of(
                product(10L, "Arroz", 100, 50, abarrotes),
                product(11L, "Agua", 25, 500, bebidas));

        //When se exporta
        byte[] bytes = export(products);

        //Then los datos aparecen en las filas 1 y 2 con sus tipos correctos
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheet("Resultado");
            assertEquals(2, sheet.getLastRowNum(), "Debe haber encabezado + 2 filas de datos");

            Row first = sheet.getRow(1);
            assertEquals("10", first.getCell(0).getStringCellValue(), "El ID se escribe como texto");
            assertEquals("Arroz", first.getCell(1).getStringCellValue());
            assertEquals(100.0, first.getCell(2).getNumericCellValue(), "El precio es numerico");
            assertEquals(50.0, first.getCell(3).getNumericCellValue(), "La cantidad es numerica");
            assertEquals("Abarrotes", first.getCell(4).getStringCellValue());

            Row second = sheet.getRow(2);
            assertEquals("11", second.getCell(0).getStringCellValue());
            assertEquals("Agua", second.getCell(1).getStringCellValue());
            assertEquals(25.0, second.getCell(2).getNumericCellValue());
            assertEquals(500.0, second.getCell(3).getNumericCellValue());
            assertEquals("Bebidas", second.getCell(4).getStringCellValue());
        }
    }

    @Test
    void testExportEmptyList() throws Exception {
        //Given ningun producto
        List<Product> products = Collections.emptyList();

        //When se exporta
        byte[] bytes = export(products);

        //Then solo existe la fila de encabezado
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet);
            assertEquals(0, sheet.getLastRowNum(), "Con lista vacia solo debe estar el encabezado");
            assertEquals("ID", sheet.getRow(0).getCell(0).getStringCellValue());
            assertNull(sheet.getRow(1), "No debe haber fila de datos");
        }
    }

    @Test
    void testExportUsesCategoryName() throws Exception {
        //Given un producto cuya categoria es la que debe mostrarse en la ultima columna
        Category categoria = new Category(3L, "Electrónica", "Dispositivos electrónicos");
        List<Product> products = List.of(product(20L, "Audifonos", 350, 12, categoria));

        //When se exporta
        byte[] bytes = export(products);

        //Then la ultima columna muestra el nombre de la categoria, no su id
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);
            assertEquals("Audifonos", row.getCell(1).getStringCellValue());
            assertEquals("Electrónica", row.getCell(4).getStringCellValue(),
                    "Debe usar el nombre de la categoria");
        }
    }

}
