package com.shopeeclone.shopee_api.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.shopeeclone.shopee_api.config.CryptoConfig;
import com.shopeeclone.shopee_api.model.Order;
import com.shopeeclone.shopee_api.repository.OrderRepository;

@Service
public class SecureOrderService {

    private final ClientKeyManager keyManager;
    private final AesGcmService aes;
    private final CryptoConfig config;
    private final OrderRepository orderRepo;

    public SecureOrderService(ClientKeyManager keyManager,
            AesGcmService aes,
            CryptoConfig config,
            OrderRepository orderRepo) {
        this.keyManager = keyManager;
        this.aes = aes;
        this.config = config;
        this.orderRepo = orderRepo;
    }

    public Order saveOrder(Long clientId, Order order) throws Exception {
        String details = order.getDetailsJson();
        if (details == null || details.isBlank()) {
            throw new IllegalArgumentException("Order detailsJson is required for encryption");
        }

        SecretKey dk = keyManager.deriveDataKey(clientId);
        short ver = keyManager.getKeyVersion(clientId);
        byte[] salt = keyManager.getSalt(clientId);
        byte[] aad = ("client:" + clientId + "|type:order").getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // aes.encrypt -> Base64 string; store as bytes in DB
        byte[] encrypted = Base64.getDecoder().decode(aes.encrypt(dk, details, aad, ver, salt));
        order.setEncryptedDetails(encrypted);

        // Clear plaintext to avoid persisting sensitive data
        order.setDetailsJson(null);

        return orderRepo.save(order);
    }

    /**
     * Decrypt when you already have a Base64 blob (e.g., from API).
     */
    public String loadOrderDetails(Long clientId, String encryptedBlobB64) throws Exception {
        Map<Integer, SecretKey> mks = buildMasterKeyMap();
        byte[] aad = ("client:" + clientId + "|type:order").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return aes.decrypt(mks, encryptedBlobB64, clientId, aad);
    }

    /**
     * Decrypt straight from what you store in DB (byte[]).
     */
    public String loadOrderDetails(Long clientId, byte[] encryptedBlob) throws Exception {
        if (encryptedBlob == null) {
            throw new IllegalArgumentException("Encrypted blob is null");
        }
        String b64 = Base64.getEncoder().encodeToString(encryptedBlob);
        return loadOrderDetails(clientId, b64);
    }

    private Map<Integer, SecretKey> buildMasterKeyMap() {
        Map<Integer, SecretKey> mks = new HashMap<>();
        for (Integer v : config.getAvailableVersions()) {
            mks.put(v, config.getMasterKey(v));
        }
        return mks;
    }
}
