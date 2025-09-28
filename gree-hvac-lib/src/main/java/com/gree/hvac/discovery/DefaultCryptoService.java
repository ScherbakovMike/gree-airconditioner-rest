package com.gree.hvac.discovery;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCryptoService implements CryptoService {

  /**
   * GREE HVAC protocol specification constant.
   * This is NOT a secret key but a publicly documented protocol constant
   * required by the GREE device firmware specification.
   * Reference: GREE HVAC Communication Protocol Documentation
   */
  private static final String GREE_PROTOCOL_KEY = "a3K8Bx%2r8Y7#xDh"; // NOSONAR

  @Override
  public String decryptPackData(String encryptedData) {
    try {

      byte[] encrypted = Base64.getDecoder().decode(encryptedData);

      // SonarQube: AES/ECB required for GREE protocol - cannot use secure mode
      javax.crypto.Cipher cipher =
          javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding"); // NOSONAR
      javax.crypto.spec.SecretKeySpec keySpec =
          new javax.crypto.spec.SecretKeySpec(GREE_PROTOCOL_KEY.getBytes(StandardCharsets.UTF_8), "AES"); // NOSONAR
      cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec);

      byte[] decrypted = cipher.doFinal(encrypted);
      return new String(decrypted, StandardCharsets.UTF_8);

    } catch (Exception e) {
      log.debug("Error decrypting pack data: {}", e.getMessage());
      return null;
    }
  }
}
