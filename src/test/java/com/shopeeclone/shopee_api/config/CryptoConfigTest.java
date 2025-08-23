package com.shopeeclone.shopee_api.config;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CryptoConfigTest {

    @Autowired
    private CryptoConfig config;

    @Test
    void testConfigBinding() {
        // Ensure master keys are loaded
        assertNotNull(config.getMasterKeys(), "Master keys map should not be null");
        assertFalse(config.getMasterKeys().isEmpty(), "Master keys map should not be empty");
        assertTrue(config.getMasterKeys().containsKey(1), "Master keys should contain version 1");

        // Validate SecretKey generation
        SecretKey key = config.getMasterKey(1);
        assertNotNull(key, "SecretKey should not be null");
        assertEquals("AES", key.getAlgorithm(), "Algorithm of the key should be AES");

        // Ensure GCM config is set correctly
        assertEquals(128, config.getGcm().getTagBits(), "GCM tagBits should be 128");
    }
}
