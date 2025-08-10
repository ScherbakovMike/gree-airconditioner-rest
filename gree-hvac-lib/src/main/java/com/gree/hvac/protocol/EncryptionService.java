package com.gree.hvac.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handles encryption and decryption of GREE HVAC messages */
public class EncryptionService {
  private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

  private final EcbCipher ecbCipher;
  private final GcmCipher gcmCipher;
  private AbstractCipher activeCipher;
  private int bindAttempt = 1;

  public EncryptionService() {
    this.ecbCipher = new EcbCipher();
    this.gcmCipher = new GcmCipher();
    this.activeCipher = ecbCipher;
  }

  public String getKey() {
    return activeCipher.getKey();
  }

  /** Decrypt UDP message */
  public JSONObject decrypt(JSONObject input) throws Exception {
    DecryptedMessage decrypted = activeCipher.decrypt(input);
    JSONObject payload = decrypted.getPayload();

    if (payload.has("t") && "bindok".equals(payload.getString("t"))) {
      activeCipher.setKey(payload.getString("key"));
    }

    logger.debug("Decrypt - input: {}, output: {}", input, decrypted);
    return payload;
  }

  /** Encrypt UDP message */
  public EncryptedMessage encrypt(JSONObject output) throws Exception {
    if (output.has("t") && "bind".equals(output.getString("t"))) {
      if (bindAttempt == 2) {
        activeCipher = gcmCipher;
      }
      bindAttempt++;
    }

    EncryptedMessage encrypted = activeCipher.encrypt(output);
    logger.debug("Encrypt - input: {}, output: {}", output, encrypted);
    return encrypted;
  }

  public static class EncryptedMessage {
    private final String payload;
    private final String tag;
    private final String cipher;
    private final String key;

    public EncryptedMessage(String payload, String tag, String cipher, String key) {
      this.payload = payload;
      this.tag = tag;
      this.cipher = cipher;
      this.key = key;
    }

    public String getPayload() {
      return payload;
    }

    public String getTag() {
      return tag;
    }

    public String getCipher() {
      return cipher;
    }

    public String getKey() {
      return key;
    }
  }

  public static class DecryptedMessage {
    private final JSONObject payload;
    private final String cipher;
    private final String key;

    public DecryptedMessage(JSONObject payload, String cipher, String key) {
      this.payload = payload;
      this.cipher = cipher;
      this.key = key;
    }

    public JSONObject getPayload() {
      return payload;
    }

    public String getCipher() {
      return cipher;
    }

    public String getKey() {
      return key;
    }
  }

  private abstract static class AbstractCipher {
    protected String key;

    public AbstractCipher(String defaultKey) {
      this.key = defaultKey;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }

    public abstract DecryptedMessage decrypt(JSONObject input) throws Exception;

    public abstract EncryptedMessage encrypt(JSONObject output) throws Exception;
  }

  /**
   * ECB cipher implementation for GREE HVAC protocol compatibility. Note: AES/ECB mode is required
   * by GREE device firmware and cannot be changed. The security risk is mitigated by: 1. Local
   * network communication only (no internet exposure) 2. Fixed protocol implementation required by
   * hardware 3. Limited data sensitivity (HVAC control commands)
   */
  @SuppressWarnings("java:S5542") // SonarQube: AES/ECB required for GREE protocol compatibility
  private static class EcbCipher extends AbstractCipher {
    public EcbCipher() {
      super("a3K8Bx%2r8Y7#xDh"); // NOSONAR - GREE protocol constant, not a secret
    }

    @Override
    public DecryptedMessage decrypt(JSONObject input) throws Exception {
      // SonarQube: AES/ECB required for GREE protocol - cannot use secure mode
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // NOSONAR
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
      cipher.init(Cipher.DECRYPT_MODE, secretKey);

      byte[] encryptedData = Base64.getDecoder().decode(input.getString("pack"));
      byte[] decryptedData = cipher.doFinal(encryptedData);
      String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);

      JSONObject payload = new JSONObject(decryptedString);
      return new DecryptedMessage(payload, "ecb", key);
    }

    @Override
    public EncryptedMessage encrypt(JSONObject output) throws Exception {
      // SonarQube: AES/ECB required for GREE protocol - cannot use secure mode
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // NOSONAR
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);

      byte[] data = output.toString().getBytes(StandardCharsets.UTF_8);
      byte[] encryptedData = cipher.doFinal(data);
      String payload = Base64.getEncoder().encodeToString(encryptedData);

      return new EncryptedMessage(payload, null, "ecb", key);
    }
  }

  private static class GcmCipher extends AbstractCipher {
    private static final byte[] GCM_NONCE = hexToBytes("5440784449675a516c5e6313");
    private static final byte[] GCM_AEAD = "qualcomm-test".getBytes(StandardCharsets.UTF_8);
    private static final int GCM_TAG_LENGTH = 16;

    public GcmCipher() {
      super("{yxAHAY_Lm6pbC/<");
    }

    @Override
    public DecryptedMessage decrypt(JSONObject input) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

      GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, GCM_NONCE);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
      cipher.updateAAD(GCM_AEAD);

      if (input.has("tag")) {
        // For GCM, we need to append the auth tag to the ciphertext
        byte[] encryptedData = Base64.getDecoder().decode(input.getString("pack"));
        byte[] tag = Base64.getDecoder().decode(input.getString("tag"));

        byte[] cipherWithTag = new byte[encryptedData.length + tag.length];
        System.arraycopy(encryptedData, 0, cipherWithTag, 0, encryptedData.length);
        System.arraycopy(tag, 0, cipherWithTag, encryptedData.length, tag.length);

        byte[] decryptedData = cipher.doFinal(cipherWithTag);
        String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);
        JSONObject payload = new JSONObject(decryptedString);
        return new DecryptedMessage(payload, "gcm", key);
      } else {
        byte[] encryptedData = Base64.getDecoder().decode(input.getString("pack"));
        byte[] decryptedData = cipher.doFinal(encryptedData);
        String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);
        JSONObject payload = new JSONObject(decryptedString);
        return new DecryptedMessage(payload, "gcm", key);
      }
    }

    @Override
    public EncryptedMessage encrypt(JSONObject output) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

      GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, GCM_NONCE);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
      cipher.updateAAD(GCM_AEAD);

      byte[] data = output.toString().getBytes(StandardCharsets.UTF_8);
      byte[] encryptedData = cipher.doFinal(data);

      // Split encrypted data and auth tag
      int cipherLength = encryptedData.length - GCM_TAG_LENGTH;
      byte[] ciphertext = new byte[cipherLength];
      byte[] authTag = new byte[GCM_TAG_LENGTH];

      System.arraycopy(encryptedData, 0, ciphertext, 0, cipherLength);
      System.arraycopy(encryptedData, cipherLength, authTag, 0, GCM_TAG_LENGTH);

      String payload = Base64.getEncoder().encodeToString(ciphertext);
      String tag = Base64.getEncoder().encodeToString(authTag);

      return new EncryptedMessage(payload, tag, "gcm", key);
    }

    private static byte[] hexToBytes(String hex) {
      int len = hex.length();
      byte[] data = new byte[len / 2];
      for (int i = 0; i < len; i += 2) {
        data[i / 2] =
            (byte)
                ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
      }
      return data;
    }
  }
}
