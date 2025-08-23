package com.shopeeclone.shopee_api.service;

import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.shopeeclone.shopee_api.config.CryptoConfig;
import com.shopeeclone.shopee_api.security.Hkdf;

@Service
public class AesGcmService {

    private static final int IV_LEN = 12; // 96-bit nonce
    private final int tagBits;

    public AesGcmService(CryptoConfig cfg) {
        this.tagBits = cfg.getGcm().getTagBits();
    }

    public String encrypt(SecretKey dataKey, String plaintext, byte[] aad,
            short keyVersion, byte[] clientSalt) throws Exception {
        byte[] iv = new byte[IV_LEN];
        new java.security.SecureRandom().nextBytes(iv);

        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        javax.crypto.spec.GCMParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(tagBits, iv);
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, dataKey, spec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        byte[] ct = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // pack: [ver][saltLen][salt][iv][ct]
        byte[] salt = clientSalt;
        int total = 1 + 1 + salt.length + IV_LEN + ct.length;
        byte[] out = new byte[total];
        int p = 0;
        out[p++] = (byte) keyVersion;
        out[p++] = (byte) salt.length;
        System.arraycopy(salt, 0, out, p, salt.length);
        p += salt.length;
        System.arraycopy(iv, 0, out, p, IV_LEN);
        p += IV_LEN;
        System.arraycopy(ct, 0, out, p, ct.length);
        return java.util.Base64.getEncoder().encodeToString(out);
    }

    public String decrypt(Map<Integer, SecretKey> masterKeys, String b64, Long clientId, byte[] aad) throws Exception {
        byte[] blob = java.util.Base64.getDecoder().decode(b64);
        int p = 0;
        int ver = blob[p++] & 0xFF;
        int saltLen = blob[p++] & 0xFF;
        byte[] salt = java.util.Arrays.copyOfRange(blob, p, p + saltLen);
        p += saltLen;
        byte[] iv = java.util.Arrays.copyOfRange(blob, p, p + IV_LEN);
        p += IV_LEN;
        byte[] ct = java.util.Arrays.copyOfRange(blob, p, blob.length);

        SecretKey mk = masterKeys.get(ver);
        if (mk == null) {
            throw new IllegalStateException("Unknown MK version: " + ver);
        }
        SecretKey dk = Hkdf.deriveAesKey(mk, salt, clientId);

        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        javax.crypto.spec.GCMParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(tagBits, iv);
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, dk, spec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
    }
}
