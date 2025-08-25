package com.shopeeclone.shopee_api.config;

import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crypto")
public class CryptoConfig {

    private Map<Integer, String> masterKeys; // version -> Base64
    private Gcm gcm = new Gcm();

    public static class Gcm {

        private int tagBits = 128;

        public int getTagBits() {
            return tagBits;
        }

        public void setTagBits(int tagBits) {
            this.tagBits = tagBits;
        }
    }

    public Gcm getGcm() {
        return gcm;
    }

    public void setGcm(Gcm gcm) {
        this.gcm = gcm;
    }

    public Map<Integer, String> getMasterKeys() {
        return masterKeys;
    }

    public void setMasterKeys(Map<Integer, String> masterKeys) {
        this.masterKeys = masterKeys;
    }

    // Fetch key by version
    public SecretKey getMasterKey(int version) {
        String b64 = masterKeys.get(version);
        if (b64 == null) {
            throw new IllegalArgumentException("No master key for version: " + version);
        }
        b64 = b64.trim();
        byte[] key = java.util.Base64.getDecoder().decode(b64);
        return new javax.crypto.spec.SecretKeySpec(key, "AES");
    }

    public Set<Integer> getAvailableVersions() {
        return masterKeys.keySet();
    }
}
