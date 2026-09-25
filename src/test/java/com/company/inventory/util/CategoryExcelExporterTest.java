package com.company.inventory.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.company.inventory.model.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryExcelExporterTest {

    private byte[] export(List<Category> categories) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CategoryExcelExporter(categories).export(response);
        return response.getContentAsByteArray();
    }

    @Test
    void testExportHeaderLine() throws Exception {
        //Given al menos una categoria
        List<Category> categories = List.of(new Category(1L, "Abarrotes", "Categoria de abarrotes"));

        //When se exporta
        byte[] bytes = export(categories);

        //Then el Excel tiene la hoja "Resultado" con las 3 columnas del encabezado
        assertTrue(bytes.length > 0, "El Excel no debe venir vacio");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet, "La hoja debe llamarse Resultado");

            Row header = sheet.getRow(0);
            assertNotNull(header);
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("Nombre", header.getCell(1).getStringCellValue());
            assertEquals("Descripción", header.getCell(2).getStringCellValue());
        }
    }

    @Test
    void testExportDataLines() throws Exception {
        //Given dos categorias
        List<Category> categories = List.of(
                new Category(1L, "Abarrotes", "Categoria de abarrotes"),
                new Category(2L, "Bebidas", "Categoria de bebidas"));

        //When se exporta
        byte[] bytes = export(categories);

        //Then las filas 1 y 2 contienen los datos en el mismo orden
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheet("Resultado");
            assertEquals(2, sheet.getLastRowNum(), "Debe haber encabezado + 2 filas de datos");

            Row first = sheet.getRow(1);
            assertEquals("1", first.getCell(0).getStringCellValue());
            assertEquals("Abarrotes", first.getCell(1).getStringCellValue());
            assertEquals("Categoria de abarrotes", first.getCell(2).getStringCellValue());

            Row second = sheet.getRow(2);
            assertEquals("2", second.getCell(0).getStringCellValue());
            assertEquals("Bebidas", second.getCell(1).getStringCellValue());
            assertEquals("Categoria de bebidas", second.getCell(2).getStringCellValue());
        }
    }

    @Test
    void testExportEmptyList() throws Exception {
        //Given ninguna categoria
        List<Category> categories = Collections.emptyList();

        //When se exporta
        byte[] bytes = export(categories);

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
    void testExportIsReadableAsXlsx() throws Exception {
        //Given una lista con una categoria de nombre con acentos
        List<Category> categories = List.of(new Category(9L, "Electrónica", "Dispositivos electrónicos"));

        //When se exporta
        byte[] bytes = export(categories);

        //Then el contenido es un XLSX valido que Apache POI puede abrir
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            assertEquals("Resultado", sheet.getSheetName());
            assertEquals("Electrónica", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Dispositivos electrónicos", sheet.getRow(1).getCell(2).getStringCellValue());
        }
    }

}
