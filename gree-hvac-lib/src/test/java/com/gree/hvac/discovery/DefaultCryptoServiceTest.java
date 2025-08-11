package com.gree.hvac.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultCryptoServiceTest {

  private DefaultCryptoService cryptoService;

  @BeforeEach
  void setUp() {
    cryptoService = new DefaultCryptoService();
  }

  @Test
  void testDecryptPackDataWithValidData() throws Exception {
    // Arrange
    String originalText = "{\"t\":\"dev\",\"name\":\"TestDevice\"}";
    String encryptedData = encryptTestData(originalText);

    // Act
    String result = cryptoService.decryptPackData(encryptedData);

    // Assert
    assertEquals(originalText, result);
  }

  @Test
  void testDecryptPackDataWithInvalidData() {
    // Act
    String result = cryptoService.decryptPackData("invalid_base64_data");

    // Assert
    assertNull(result);
  }

  @Test
  void testDecryptPackDataWithEmptyData() {
    // Act
    String result = cryptoService.decryptPackData("");

    // Assert - Empty string input should return null or empty string
    assertTrue(result == null || result.isEmpty());
  }

  @Test
  void testDecryptPackDataWithNullData() {
    // Act
    String result = cryptoService.decryptPackData(null);

    // Assert
    assertNull(result);
  }

  private String encryptTestData(String plaintext) throws Exception {
    String genericKey = "a3K8Bx%2r8Y7#xDh";

    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
    javax.crypto.spec.SecretKeySpec keySpec =
        new javax.crypto.spec.SecretKeySpec(genericKey.getBytes(StandardCharsets.UTF_8), "AES");
    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec);

    byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
  }
}
