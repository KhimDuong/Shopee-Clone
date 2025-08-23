package com.shopeeclone.shopee_api.service;

import java.security.SecureRandom;
import java.util.Optional;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shopeeclone.shopee_api.config.CryptoConfig;
import com.shopeeclone.shopee_api.model.ClientCryptoMeta;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.repository.ClientCryptoMetaRepository;
import com.shopeeclone.shopee_api.repository.UserRepository;

class ClientKeyManagerTest {

    private ClientCryptoMetaRepository metaRepo;
    private UserRepository userRepo;
    private CryptoConfig crypto;
    private ClientKeyManager manager;

    @BeforeEach
    void setup() {
        metaRepo = mock(ClientCryptoMetaRepository.class);
        userRepo = mock(UserRepository.class);
        crypto = mock(CryptoConfig.class);
        manager = new ClientKeyManager(metaRepo, userRepo, crypto);
    }

    @Test
    void testEnsureMetaCreatesNew() {
        User user = new User();
        user.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(metaRepo.findByUser(user)).thenReturn(Optional.empty());
        when(crypto.getAvailableVersions()).thenReturn(java.util.Set.of(1, 2));

        ClientCryptoMeta saved = new ClientCryptoMeta();
        saved.setUser(user);
        saved.setKeyVersion((short) 2);
        saved.setSalt(new byte[16]);

        when(metaRepo.save(any(ClientCryptoMeta.class))).thenReturn(saved);

        ClientCryptoMeta result = manager.ensureMeta(1L);

        assertNotNull(result);
        assertEquals((short) 2, result.getKeyVersion());
        verify(metaRepo).save(any(ClientCryptoMeta.class));
    }

    @Test
    void testEnsureMetaThrowsWhenUserNotFound() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> manager.ensureMeta(99L)
        );

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void testDeriveDataKeyWorks() throws Exception {
        User user = new User();
        user.setId(1L);

        ClientCryptoMeta meta = new ClientCryptoMeta();
        meta.setUser(user);
        meta.setKeyVersion((short) 1);
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        meta.setSalt(salt);

        SecretKey masterKey = mock(SecretKey.class);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(metaRepo.findByUser(user)).thenReturn(Optional.of(meta));
        when(crypto.getMasterKey((short) 1)).thenReturn(masterKey);

        SecretKey derived = manager.deriveDataKey(1L);

        assertNotNull(derived);
        assertEquals("AES", derived.getAlgorithm());
    }

    @Test
    void testGetKeyVersionThrowsIfNoMeta() {
        when(metaRepo.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> manager.getKeyVersion(1L)
        );

        assertTrue(ex.getMessage().contains("No crypto meta"));
    }

    @Test
    void testGetSaltThrowsIfNoMeta() {
        when(metaRepo.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> manager.getSalt(1L)
        );

        assertTrue(ex.getMessage().contains("No crypto meta"));
    }
}
