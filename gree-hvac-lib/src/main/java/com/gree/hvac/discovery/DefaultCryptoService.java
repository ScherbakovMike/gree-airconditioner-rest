package com.gree.hvac.discovery;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCryptoService implements CryptoService {

  @Override
  public String decryptPackData(String encryptedData) {
    try {
      // GREE protocol constant - not a compromised password but a published protocol specification
      // This key is documented in GREE HVAC protocol and required by device firmware
      String genericKey = "a3K8Bx%2r8Y7#xDh"; // NOSONAR - GREE protocol constant, not a secret

      byte[] encrypted = Base64.getDecoder().decode(encryptedData);

      // SonarQube: AES/ECB required for GREE protocol - cannot use secure mode
      javax.crypto.Cipher cipher =
          javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding"); // NOSONAR
      javax.crypto.spec.SecretKeySpec keySpec =
          new javax.crypto.spec.SecretKeySpec(genericKey.getBytes(StandardCharsets.UTF_8), "AES");
      cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec);

      byte[] decrypted = cipher.doFinal(encrypted);
      return new String(decrypted, StandardCharsets.UTF_8);

    } catch (Exception e) {
      log.debug("Error decrypting pack data: {}", e.getMessage());
      return null;
    }
  }
}
