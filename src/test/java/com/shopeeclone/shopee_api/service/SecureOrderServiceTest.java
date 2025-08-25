package com.shopeeclone.shopee_api.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shopeeclone.shopee_api.config.CryptoConfig;
import com.shopeeclone.shopee_api.model.Order;
import com.shopeeclone.shopee_api.repository.OrderRepository;
import com.shopeeclone.shopee_api.repository.UserRepository;
import com.shopeeclone.shopee_api.security.Hkdf;

public class SecureOrderServiceTest {

    private SecureOrderService service;
    private ClientKeyManager keyManager;
    private AesGcmService aes;
    private CryptoConfig config;
    private OrderRepository repo;
    private UserRepository userRepo;

    private SecretKey masterKey;
    private SecretKey derivedKey;

    @BeforeEach
    void setup() throws Exception {
        keyManager = mock(ClientKeyManager.class);
        repo = mock(OrderRepository.class);
        userRepo = mock(UserRepository.class);
        config = mock(CryptoConfig.class);
        CryptoConfig.Gcm gcm = mock(CryptoConfig.Gcm.class);

        // Mock GCM parameters
        when(config.getGcm()).thenReturn(gcm);
        when(gcm.getTagBits()).thenReturn(128); // typical GCM tag length (bits)

        // Initialize AES service with the mock config
        aes = new AesGcmService(config);
        service = new SecureOrderService(keyManager, aes, config, repo, userRepo);

        // Generate AES keys
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        masterKey = kg.generateKey();
        derivedKey = Hkdf.deriveAesKey(masterKey, "randomSalt1234".getBytes(StandardCharsets.UTF_8), 1L);

        // Mock key manager behavior
        when(keyManager.deriveDataKey(1L)).thenReturn(derivedKey);
        when(keyManager.getKeyVersion(1L)).thenReturn((short) 1);
        when(keyManager.getSalt(1L)).thenReturn("randomSalt1234".getBytes(StandardCharsets.UTF_8));

        // Mock config master key and versions
        when(config.getAvailableVersions()).thenReturn(Set.of(1));
        when(config.getMasterKey(1)).thenReturn(masterKey);
    }

    @Test
    void testSaveAndLoadOrder() throws Exception {
        // --- Step 1: Create order with plaintext details ---
        Order order = new Order();
        order.setId(100L);
        order.setDetailsJson("{\"item\":\"Phone\",\"price\":12000000}");

        // --- Step 2: Save order (encrypts details) ---
        service.saveOrder(1L, order);

        // Verify repository save called with encrypted content
        ArgumentCaptor<Order> savedCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repo).save(savedCaptor.capture());

        Order savedOrder = savedCaptor.getValue();
        assertNotNull(savedOrder.getEncryptedDetails(), "Encrypted data must not be null");
        assertNull(savedOrder.getDetailsJson(), "Plaintext details must be cleared");

        // --- Step 3: Simulate retrieving encrypted blob and decrypting ---
        String encryptedBase64 = Base64.getEncoder().encodeToString(savedOrder.getEncryptedDetails());
        String decrypted = service.loadOrderDetails(1L, encryptedBase64);

        assertEquals("{\"item\":\"Phone\",\"price\":12000000}", decrypted,
                "Decrypted order details must match original");
    }
}
