package com.lemon.lemonade.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Converter
public class TokenEncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";
    // 16 bytes secret key for AES. For production, load this from external properties!
    private static final byte[] KEY = "MySuperSecretKey".getBytes();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Key key = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Key key = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting attribute", e);
        }
    }
}
