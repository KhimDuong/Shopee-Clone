package com.shopeeclone.shopee_api.security;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HkdfTests {

    @Test
    void testExtractProducesDeterministicOutput() throws Exception {
        byte[] salt = new byte[16];
        byte[] ikm = new byte[32];
        new SecureRandom().nextBytes(salt);
        new SecureRandom().nextBytes(ikm);

        byte[] prk1 = Hkdf.extract(salt, ikm);
        byte[] prk2 = Hkdf.extract(salt, ikm);

        assertArrayEquals(prk1, prk2, "Extract should produce the same PRK for same inputs");
    }

    @Test
    void testExpandProducesCorrectLengthAndIsDeterministic() throws Exception {
        byte[] prk = new byte[32];
        new SecureRandom().nextBytes(prk);

        byte[] info = "test-info".getBytes();
        byte[] okm1 = Hkdf.expand(prk, info, 64);
        byte[] okm2 = Hkdf.expand(prk, info, 64);

        assertEquals(64, okm1.length, "OKM should have the requested length");
        assertArrayEquals(okm1, okm2, "Expand should produce the same OKM for same inputs");
    }

    @Test
    void testDeriveAesKeyIsDeterministic() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey masterKey = keyGen.generateKey();

        byte[] clientSalt = new byte[16];
        new SecureRandom().nextBytes(clientSalt);
        Long clientId = 12345L;

        SecretKey key1 = Hkdf.deriveAesKey(masterKey, clientSalt, clientId);
        SecretKey key2 = Hkdf.deriveAesKey(masterKey, clientSalt, clientId);

        assertArrayEquals(key1.getEncoded(), key2.getEncoded(), "Derived AES keys should match for same inputs");
        assertEquals(32, key1.getEncoded().length, "Derived AES-256 key should be 32 bytes");
    }

    @Test
    void testDifferentInputsProduceDifferentKeys() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey masterKey = keyGen.generateKey();

        byte[] salt1 = new byte[16];
        byte[] salt2 = new byte[16];
        new SecureRandom().nextBytes(salt1);
        new SecureRandom().nextBytes(salt2);

        SecretKey key1 = Hkdf.deriveAesKey(masterKey, salt1, 12345L);
        SecretKey key2 = Hkdf.deriveAesKey(masterKey, salt2, 12346L);

        assertFalse(Arrays.equals(key1.getEncoded(), key2.getEncoded()), "Different salt or clientId should produce different keys");
    }
}




// DUong Hieuuuuuuu 45154859159659645618596456