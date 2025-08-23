package com.shopeeclone.shopee_api.service;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shopeeclone.shopee_api.config.CryptoConfig;
import com.shopeeclone.shopee_api.security.Hkdf;

class AesGcmServiceTest {

    private AesGcmService aesGcmService;
    private SecretKey masterKey;
    private Map<Integer, SecretKey> masterKeys;

    @BeforeEach
    void setUp() throws Exception {
        // Prepare CryptoConfig with tagBits = 128
        CryptoConfig config = new CryptoConfig();
        CryptoConfig.Gcm gcmConfig = new CryptoConfig.Gcm();
        gcmConfig.setTagBits(128);
        config.setGcm(gcmConfig);

        aesGcmService = new AesGcmService(config);

        // Generate a 256-bit AES master key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        masterKey = keyGen.generateKey();

        masterKeys = new HashMap<>();
        masterKeys.put(1, masterKey);
    }

    @Test
    void testEncryptDecrypt_success() throws Exception {
        String plaintext = "Sensitive Data!";
        Long clientId = 123L;
        byte[] aad = ("client:" + clientId + "|type:order").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);

        // Derive a data key using HKDF
        SecretKey dataKey = Hkdf.deriveAesKey(masterKey, salt, clientId);

        // Encrypt
        String ciphertext = aesGcmService.encrypt(dataKey, plaintext, aad, (short) 1, salt);
        assertNotNull(ciphertext);
        assertFalse(ciphertext.isEmpty());

        // Decrypt
        String decrypted = aesGcmService.decrypt(masterKeys, ciphertext, clientId, aad);

        // Verify correctness
        assertEquals(plaintext, decrypted, "Decrypted text should match original plaintext");
    }

    @Test
    void testDecrypt_withWrongKey_shouldFail() throws Exception {
        String plaintext = "Secret Message!";
        Long clientId = 123L;
        byte[] aad = null;
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);

        SecretKey dataKey = Hkdf.deriveAesKey(masterKey, salt, clientId);
        String ciphertext = aesGcmService.encrypt(dataKey, plaintext, aad, (short) 1, salt);

        // Use an invalid master key (version mismatch)
        Map<Integer, SecretKey> wrongMasterKeys = new HashMap<>();

        Exception ex = assertThrows(IllegalStateException.class,
                () -> aesGcmService.decrypt(wrongMasterKeys, ciphertext, clientId, aad));

        assertTrue(ex.getMessage().contains("Unknown MK version"));
    }
}
