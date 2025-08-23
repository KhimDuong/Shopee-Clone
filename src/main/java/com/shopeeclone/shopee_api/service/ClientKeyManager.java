package com.shopeeclone.shopee_api.service;

import java.security.SecureRandom;
import java.util.Collections;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.shopeeclone.shopee_api.config.CryptoConfig;
import com.shopeeclone.shopee_api.model.ClientCryptoMeta;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.repository.ClientCryptoMetaRepository;
import com.shopeeclone.shopee_api.repository.UserRepository;
import com.shopeeclone.shopee_api.security.Hkdf;

import jakarta.transaction.Transactional;

@Service
public class ClientKeyManager {

    private final ClientCryptoMetaRepository metaRepo;
    private final UserRepository userRepo;
    private final CryptoConfig crypto;

    public ClientKeyManager(ClientCryptoMetaRepository metaRepo, UserRepository userRepo, CryptoConfig crypto) {
        this.metaRepo = metaRepo;
        this.userRepo = userRepo;
        this.crypto = crypto;
    }

    /**
     * Ensures that a ClientCryptoMeta exists for the given userId. Creates one
     * if not present.
     */
    @Transactional
    public ClientCryptoMeta ensureMeta(Long clientId) {
        User user = userRepo.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + clientId));

        return metaRepo.findByUser(user).orElseGet(() -> {
            ClientCryptoMeta meta = new ClientCryptoMeta();
            meta.setUser(user);
            meta.setKeyVersion(Collections.max(crypto.getAvailableVersions()).shortValue());
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            meta.setSalt(salt);
            return metaRepo.save(meta);
        });
    }

    /**
     * Derives a data encryption key for the given user.
     */
    public SecretKey deriveDataKey(Long clientId) throws Exception {
        ClientCryptoMeta meta = ensureMeta(clientId);
        SecretKey mk = crypto.getMasterKey(meta.getKeyVersion());
        return Hkdf.deriveAesKey(mk, meta.getSalt(), clientId);
    }

    /**
     * Returns key version for a given user.
     */
    public short getKeyVersion(Long clientId) {
        return metaRepo.findByUserId(clientId)
                .map(ClientCryptoMeta::getKeyVersion)
                .orElseThrow(() -> new IllegalStateException("No crypto meta for user " + clientId));
    }

    /**
     * Returns salt for a given user.
     */
    public byte[] getSalt(Long clientId) {
        return metaRepo.findByUserId(clientId)
                .map(ClientCryptoMeta::getSalt)
                .orElseThrow(() -> new IllegalStateException("No crypto meta for user " + clientId));
    }
}
