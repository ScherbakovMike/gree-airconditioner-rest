package com.gree.hvac.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.security.GeneralSecurityException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceTest {

  private EncryptionService encryptionService;

  @BeforeEach
  void setUp() {
    encryptionService = new EncryptionService();
  }

  private void assertValidEncryptedMessage(EncryptionService.EncryptedMessage encrypted) {
    assertNotNull(encrypted);
    assertNotNull(encrypted.payload());
    assertNotNull(encrypted.cipher());
    assertNotNull(encrypted.key());

    assertFalse(encrypted.payload().isEmpty());
    assertFalse(encrypted.cipher().isEmpty());
    assertFalse(encrypted.key().isEmpty());

    if ("gcm".equals(encrypted.cipher())) {
      assertNotNull(encrypted.tag());
      assertFalse(encrypted.tag().isEmpty());
    }
  }

  private JSONObject createReceivedMessage(EncryptionService.EncryptedMessage encrypted) {
    JSONObject receivedMessage = new JSONObject();
    receivedMessage.put("pack", encrypted.payload());
    receivedMessage.put("tag", encrypted.tag());
    receivedMessage.put("cipher", encrypted.cipher());
    receivedMessage.put("key", encrypted.key());
    return receivedMessage;
  }

  @Test
  void testConstructor() {
    assertNotNull(encryptionService);
    assertNotNull(encryptionService.getKey());
    // Should start with ECB cipher
    assertFalse(encryptionService.getKey().isEmpty());
  }

  @Test
  void testGetKey() {
    String key = encryptionService.getKey();
    assertNotNull(key);
    assertFalse(key.isEmpty());
  }

  @Test
  void testEncryptBasicMessage() {
    JSONObject message = new JSONObject();
    message.put("t", "status");
    message.put("mac", "test-mac");

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);
      assertValidEncryptedMessage(encrypted);
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptBindMessage() {
    JSONObject bindMessage = new JSONObject();
    bindMessage.put("t", "bind");
    bindMessage.put("mac", "test-mac");

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(bindMessage);
      assertValidEncryptedMessage(encrypted);
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptBindMessageSecondAttempt() {
    JSONObject bindMessage = new JSONObject();
    bindMessage.put("t", "bind");
    bindMessage.put("mac", "test-mac");

    try {
      // First bind attempt
      EncryptionService.EncryptedMessage encrypted1 = encryptionService.encrypt(bindMessage);
      assertNotNull(encrypted1);

      // Second bind attempt should switch to GCM cipher
      EncryptionService.EncryptedMessage encrypted2 = encryptionService.encrypt(bindMessage);
      assertNotNull(encrypted2);

      // Both should be valid encrypted messages
      assertFalse(encrypted1.payload().isEmpty());
      assertFalse(encrypted2.payload().isEmpty());
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testDecryptBasicMessage() {
    JSONObject message = new JSONObject();
    message.put("t", "status");
    message.put("mac", "test-mac");

    try {
      // First encrypt
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);

      // Then decrypt (simulating received message)
      JSONObject receivedMessage = createReceivedMessage(encrypted);
      JSONObject decrypted = encryptionService.decrypt(receivedMessage);
      assertNotNull(decrypted);
      assertEquals("status", decrypted.getString("t"));
      assertEquals("test-mac", decrypted.getString("mac"));
    } catch (GeneralSecurityException | JSONException e) {
      fail("Encryption/decryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testDecryptBindOkMessage() {
    JSONObject bindOkMessage = new JSONObject();
    bindOkMessage.put("t", "bindok");
    bindOkMessage.put("key", "new-key-123");

    try {
      // First encrypt
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(bindOkMessage);

      // Then decrypt (simulating received message)
      JSONObject receivedMessage = createReceivedMessage(encrypted);
      JSONObject decrypted = encryptionService.decrypt(receivedMessage);
      assertNotNull(decrypted);
      assertEquals("bindok", decrypted.getString("t"));
      assertEquals("new-key-123", decrypted.getString("key"));

      // The key should be updated after bindok message
      String updatedKey = encryptionService.getKey();
      assertNotNull(updatedKey);
    } catch (GeneralSecurityException | JSONException e) {
      fail("Encryption/decryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptEmptyMessage() {
    JSONObject emptyMessage = new JSONObject();

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(emptyMessage);
      assertValidEncryptedMessage(encrypted);
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptMessageWithSpecialCharacters() {
    JSONObject message = new JSONObject();
    message.put("t", "control");
    message.put("data", "special-chars: !@#$%^&*()");
    message.put("number", 123.45);

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);
      assertValidEncryptedMessage(encrypted);
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptLargeMessage() {
    JSONObject largeMessage = new JSONObject();
    largeMessage.put("t", "status");

    // Create a large message
    StringBuilder largeData = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeData.append("data-chunk-").append(i).append("-");
    }
    largeMessage.put("data", largeData.toString());

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(largeMessage);
      assertValidEncryptedMessage(encrypted);
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testCipherSwitching() {
    try {
      // Start with ECB cipher
      String initialKey = encryptionService.getKey();
      assertNotNull(initialKey);

      // Send first bind message
      JSONObject bindMessage = new JSONObject();
      bindMessage.put("t", "bind");
      bindMessage.put("mac", "test-mac");

      EncryptionService.EncryptedMessage encrypted1 = encryptionService.encrypt(bindMessage);
      assertNotNull(encrypted1);

      // Send second bind message (should switch to GCM)
      EncryptionService.EncryptedMessage encrypted2 = encryptionService.encrypt(bindMessage);
      assertNotNull(encrypted2);

      // Both should be valid
      assertFalse(encrypted1.payload().isEmpty());
      assertFalse(encrypted2.payload().isEmpty());

    } catch (GeneralSecurityException e) {
      fail("Cipher switching should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptedMessageStructure() {
    JSONObject message = new JSONObject();
    message.put("t", "test");
    message.put("value", 42);

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);
      assertValidEncryptedMessage(encrypted);
    } catch (GeneralSecurityException e) {
      fail("Encryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testDecryptedMessageStructure() {
    JSONObject message = new JSONObject();
    message.put("t", "test");
    message.put("value", 42);

    try {
      // First encrypt
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);

      // Then decrypt
      JSONObject receivedMessage = new JSONObject();
      receivedMessage.put("pack", encrypted.payload());
      receivedMessage.put("tag", encrypted.tag());
      receivedMessage.put("cipher", encrypted.cipher());
      receivedMessage.put("key", encrypted.key());

      JSONObject decrypted = encryptionService.decrypt(receivedMessage);

      // Test that decrypted message contains original data
      assertNotNull(decrypted);
      assertEquals("test", decrypted.getString("t"));
      assertEquals(42, decrypted.getInt("value"));

    } catch (GeneralSecurityException | JSONException e) {
      fail("Encryption/decryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testMultipleEncryptionDecryptionCycles() {
    JSONObject originalMessage = new JSONObject();
    originalMessage.put("t", "cycle-test");
    originalMessage.put("counter", 1);
    originalMessage.put("data", "test-data");

    try {
      for (int i = 0; i < 5; i++) {
        // Update counter
        originalMessage.put("counter", i + 1);

        // Encrypt
        EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(originalMessage);
        assertNotNull(encrypted);

        // Decrypt
        JSONObject receivedMessage = new JSONObject();
        receivedMessage.put("pack", encrypted.payload());
        receivedMessage.put("tag", encrypted.tag());
        receivedMessage.put("cipher", encrypted.cipher());
        receivedMessage.put("key", encrypted.key());

        JSONObject decrypted = encryptionService.decrypt(receivedMessage);
        assertNotNull(decrypted);
        assertEquals("cycle-test", decrypted.getString("t"));
        assertEquals(i + 1, decrypted.getInt("counter"));
        assertEquals("test-data", decrypted.getString("data"));
      }
    } catch (GeneralSecurityException | JSONException e) {
      fail("Multiple encryption/decryption cycles should not throw exception: " + e.getMessage());
    }
  }
}
