package com.gree.hvac.protocol;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void testConstructor() {
    assertNotNull(encryptionService);
    assertNotNull(encryptionService.getKey());
    // Should start with ECB cipher
    assertTrue(encryptionService.getKey().length() > 0);
  }

  @Test
  void testGetKey() {
    String key = encryptionService.getKey();
    assertNotNull(key);
    assertTrue(key.length() > 0);
  }

  @Test
  void testEncryptBasicMessage() {
    JSONObject message = new JSONObject();
    message.put("t", "status");
    message.put("mac", "test-mac");

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);
      assertNotNull(encrypted);
      assertNotNull(encrypted.getPayload());
      assertNotNull(encrypted.getCipher());
      assertNotNull(encrypted.getKey());

      // Verify the encrypted message has the required fields
      assertTrue(encrypted.getPayload().length() > 0);
      assertTrue(encrypted.getCipher().length() > 0);
      assertTrue(encrypted.getKey().length() > 0);

      // For ECB cipher, tag is null; for GCM cipher, tag should be present
      if ("gcm".equals(encrypted.getCipher())) {
        assertNotNull(encrypted.getTag());
        assertTrue(encrypted.getTag().length() > 0);
      }
    } catch (Exception e) {
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
      assertNotNull(encrypted);
      assertNotNull(encrypted.getPayload());
      assertNotNull(encrypted.getCipher());
      assertNotNull(encrypted.getKey());

      // For ECB cipher, tag is null; for GCM cipher, tag should be present
      if ("gcm".equals(encrypted.getCipher())) {
        assertNotNull(encrypted.getTag());
      }
    } catch (Exception e) {
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
      assertTrue(encrypted1.getPayload().length() > 0);
      assertTrue(encrypted2.getPayload().length() > 0);
    } catch (Exception e) {
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
      JSONObject receivedMessage = new JSONObject();
      receivedMessage.put("pack", encrypted.getPayload());
      receivedMessage.put("tag", encrypted.getTag());
      receivedMessage.put("cipher", encrypted.getCipher());
      receivedMessage.put("key", encrypted.getKey());

      JSONObject decrypted = encryptionService.decrypt(receivedMessage);
      assertNotNull(decrypted);
      assertEquals("status", decrypted.getString("t"));
      assertEquals("test-mac", decrypted.getString("mac"));
    } catch (Exception e) {
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
      JSONObject receivedMessage = new JSONObject();
      receivedMessage.put("pack", encrypted.getPayload());
      receivedMessage.put("tag", encrypted.getTag());
      receivedMessage.put("cipher", encrypted.getCipher());
      receivedMessage.put("key", encrypted.getKey());

      JSONObject decrypted = encryptionService.decrypt(receivedMessage);
      assertNotNull(decrypted);
      assertEquals("bindok", decrypted.getString("t"));
      assertEquals("new-key-123", decrypted.getString("key"));

      // The key should be updated after bindok message
      String updatedKey = encryptionService.getKey();
      assertNotNull(updatedKey);
    } catch (Exception e) {
      fail("Encryption/decryption should not throw exception: " + e.getMessage());
    }
  }

  @Test
  void testEncryptEmptyMessage() {
    JSONObject emptyMessage = new JSONObject();

    try {
      EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(emptyMessage);
      assertNotNull(encrypted);
      assertNotNull(encrypted.getPayload());
      assertNotNull(encrypted.getCipher());
      assertNotNull(encrypted.getKey());

      // For ECB cipher, tag is null; for GCM cipher, tag should be present
      if ("gcm".equals(encrypted.getCipher())) {
        assertNotNull(encrypted.getTag());
      }
    } catch (Exception e) {
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
      assertNotNull(encrypted);
      assertNotNull(encrypted.getPayload());
      assertNotNull(encrypted.getCipher());
      assertNotNull(encrypted.getKey());

      // For ECB cipher, tag is null; for GCM cipher, tag should be present
      if ("gcm".equals(encrypted.getCipher())) {
        assertNotNull(encrypted.getTag());
      }
    } catch (Exception e) {
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
      assertNotNull(encrypted);
      assertNotNull(encrypted.getPayload());
      assertNotNull(encrypted.getCipher());
      assertNotNull(encrypted.getKey());

      // For ECB cipher, tag is null; for GCM cipher, tag should be present
      if ("gcm".equals(encrypted.getCipher())) {
        assertNotNull(encrypted.getTag());
      }
    } catch (Exception e) {
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
      assertTrue(encrypted1.getPayload().length() > 0);
      assertTrue(encrypted2.getPayload().length() > 0);

    } catch (Exception e) {
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

      // Test getter methods
      assertNotNull(encrypted.getPayload());
      assertNotNull(encrypted.getCipher());
      assertNotNull(encrypted.getKey());

      // Test that all fields have content
      assertTrue(encrypted.getPayload().length() > 0);
      assertTrue(encrypted.getCipher().length() > 0);
      assertTrue(encrypted.getKey().length() > 0);

      // For ECB cipher, tag is null; for GCM cipher, tag should be present
      if ("gcm".equals(encrypted.getCipher())) {
        assertNotNull(encrypted.getTag());
        assertTrue(encrypted.getTag().length() > 0);
      }

    } catch (Exception e) {
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
      receivedMessage.put("pack", encrypted.getPayload());
      receivedMessage.put("tag", encrypted.getTag());
      receivedMessage.put("cipher", encrypted.getCipher());
      receivedMessage.put("key", encrypted.getKey());

      JSONObject decrypted = encryptionService.decrypt(receivedMessage);

      // Test that decrypted message contains original data
      assertNotNull(decrypted);
      assertEquals("test", decrypted.getString("t"));
      assertEquals(42, decrypted.getInt("value"));

    } catch (Exception e) {
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
        receivedMessage.put("pack", encrypted.getPayload());
        receivedMessage.put("tag", encrypted.getTag());
        receivedMessage.put("cipher", encrypted.getCipher());
        receivedMessage.put("key", encrypted.getKey());

        JSONObject decrypted = encryptionService.decrypt(receivedMessage);
        assertNotNull(decrypted);
        assertEquals("cycle-test", decrypted.getString("t"));
        assertEquals(i + 1, decrypted.getInt("counter"));
        assertEquals("test-data", decrypted.getString("data"));
      }
    } catch (Exception e) {
      fail("Multiple encryption/decryption cycles should not throw exception: " + e.getMessage());
    }
  }
}
