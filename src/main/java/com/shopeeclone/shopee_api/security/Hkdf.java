package com.shopeeclone.shopee_api.security;

import javax.crypto.SecretKey;

public final class Hkdf {

    private Hkdf() {
    }

    public static byte[] extract(byte[] salt, byte[] ikm) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(salt, "HmacSHA256"));
        return mac.doFinal(ikm);
    }

    public static byte[] expand(byte[] prk, byte[] info, int outLen) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(prk, "HmacSHA256"));
        byte[] t = new byte[0];
        java.io.ByteArrayOutputStream okm = new java.io.ByteArrayOutputStream();
        int counter = 1;
        while (okm.size() < outLen) {
            mac.reset();
            mac.update(t);
            mac.update(info);
            mac.update((byte) counter);
            t = mac.doFinal();
            okm.write(t);
            counter++;
        }
        byte[] out = okm.toByteArray();
        return java.util.Arrays.copyOf(out, outLen);
    }

    public static SecretKey deriveAesKey(SecretKey masterKey, byte[] clientSalt, Long clientId) throws Exception {
        // HKDF-Extract with salt = clientSalt, IKM = masterKey bytes
        byte[] prk = extract(clientSalt, masterKey.getEncoded());
        // HKDF-Expand with info = "client:<id>|AES-256"
        byte[] info = ("client:" + clientId + "|AES-256").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] dk = expand(prk, info, 32); // 32 bytes for AES-256
        return new javax.crypto.spec.SecretKeySpec(dk, "AES");
    }
}
