package com.company.inventory.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {


    @Test
    void testUtilConstructor() {
        //Given-When la clase se instancia
        Util util = new Util();

        //Then el constructor por defecto existe y funciona
        assertNotNull(util);
    }

    @Test
    void testCompressZLib() {
        String input = "Hello, this is a test string to be compressed using ZLib compression.";
        byte[] compressed = Util.compressZLib(input.getBytes());
        assertNotNull(compressed);
        assertTrue(compressed.length < input.length());
    }

    @Test
    void testCompressZLibEmptyData() {
        //Given
        byte [] data = new byte[0];

        //When
        byte[] compressed = Util.compressZLib(data);

        //Then
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
    }

    @Test
    void testDecompressZLib() {
        String input = "Hello, this is a test string to be compressed using ZLib compression.";
        byte[] compressed = Util.compressZLib(input.getBytes());
        byte[] decompressed = Util.decompressZLib(compressed);
        assertNotNull(decompressed);
        assertEquals(input, new String(decompressed));
    }

    @Test
    void testCompressZLibBinaryImage() {
        //Given una imagen simulada (bytes aleatorios ya comprimidos no se comprimen,
        //pero el algoritmo debe soportar datos binarios sin perderlos)
        byte[] image = new byte[50_000];
        new Random(42).nextBytes(image);

        //When
        byte[] compressed = Util.compressZLib(image);

        //Then
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
        assertArrayEquals(image, Util.decompressZLib(compressed),
                "El binario comprimido y descomprimido debe ser identico al original");
    }

    @Test
    void testCompressZLibLargeRepetitiveData() {
        //Given un buffer grande y muy repetitivo (caso real: imagen con zonas lisas)
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < 5_000; i++) {
            buffer.writeBytes("inventory-sp3 ".getBytes());
        }
        byte[] data = buffer.toByteArray();

        //When
        byte[] compressed = Util.compressZLib(data);

        //Then
        assertTrue(compressed.length < data.length / 10,
                "Datos muy repetitivos deben comprimirse al menos 10 veces");
        assertArrayEquals(data, Util.decompressZLib(compressed));
    }

    @Test
    void testCompressZLibDeterministic() {
        //Given el mismo input
        byte[] input = "Mismo contenido comprimido dos veces".getBytes();

        //When se comprime dos veces
        byte[] first = Util.compressZLib(input);
        byte[] second = Util.compressZLib(input);

        //Then la salida debe ser identica (mismo algoritmo, mismos bytes)
        assertArrayEquals(first, second, "Comprimir dos veces el mismo dato debe dar el mismo resultado");
    }

    @Test
    void testCompressZLibUnicodeData() {
        //Given texto con acentos y caracteres especiales (nombres de categorias reales)
        String input = "Bebidas frías – ¡Oferta! ¿Categoría nº 3? Ñuñoa ü";

        //When
        byte[] compressed = Util.compressZLib(input.getBytes());

        //Then
        assertEquals(input, new String(Util.decompressZLib(compressed)));
    }

    @Test
    void testRoundTripEmptyData() {
        //Given
        byte[] data = new byte[0];

        //When
        byte[] result = Util.decompressZLib(Util.compressZLib(data));

        //Then
        assertNotNull(result);
        assertEquals(0, result.length, "Descomprimir un vacio comprimido debe devolver vacio");
    }

    @Test
    void testRoundTripSingleByte() {
        //Given
        byte[] data = new byte[]{7};

        //When
        byte[] result = Util.decompressZLib(Util.compressZLib(data));

        //Then
        assertArrayEquals(data, result);
    }

    @Test
    void testDecompressZLibInvalidDataDoesNotThrow() {
        //Given bytes que NO son un stream zlib valido (cabecera incorrecta)
        byte[] corrupted = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};

        //When-Then no debe lanzar excepcion: el error se traga dentro de Util
        byte[] result = assertDoesNotThrow(() -> Util.decompressZLib(corrupted),
                "Util debe manejar datos corruptos sin propagar excepciones");
        assertNotNull(result);
        assertEquals(0, result.length, "Sin datos validos no puede devolver contenido");
    }

    @Test
    void testRoundTripRealisticImage() {
        //Given una imagen generada con zonas planas y zonas de ruido (patron PNG tipico)
        byte[] image = new byte[200_000];
        Random random = new Random(7);
        for (int i = 0; i < image.length; i++) {
            image[i] = (i % 256 < 200) ? (byte) 0xFF : (byte) random.nextInt(256);
        }

        //When
        byte[] compressed = Util.compressZLib(image);

        //Then
        assertTrue(compressed.length < image.length, "La imagen debe comprimirse");
        assertArrayEquals(image, Util.decompressZLib(compressed),
                "La imagen descomprimida debe ser byte a byte igual a la original");
    }

}
