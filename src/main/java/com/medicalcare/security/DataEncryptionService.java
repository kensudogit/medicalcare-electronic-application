package com.medicalcare.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 保存データの暗号化（AES-256-GCM）
 * 通信の暗号化は TLS（HTTPS）で担保する前提。本サービスは at-rest を担当。
 */
@Service
public class DataEncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;
    private final boolean enabled;

    public DataEncryptionService(
            @Value("${security.encryption.enabled:true}") boolean enabled,
            @Value("${security.encryption.key-base64:}") String keyBase64) {
        this.enabled = enabled;
        this.secretKey = resolveKey(keyBase64);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String encryptText(String plainText) {
        if (!enabled || plainText == null) {
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("テキスト暗号化に失敗しました", e);
        }
    }

    public String decryptText(String encrypted) {
        if (!enabled || encrypted == null) {
            return encrypted;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("テキスト復号に失敗しました", e);
        }
    }

    public Path encryptFile(Path source, Path destination) {
        if (!enabled) {
            return source;
        }
        try {
            byte[] plain = Files.readAllBytes(source);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plain);
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            Files.createDirectories(destination.getParent());
            Files.write(destination, buffer.array());
            return destination;
        } catch (Exception e) {
            throw new IllegalStateException("ファイル暗号化に失敗しました", e);
        }
    }

    public byte[] decryptFileBytes(Path encryptedPath) {
        try {
            byte[] decoded = Files.readAllBytes(encryptedPath);
            if (!enabled) {
                return decoded;
            }
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new IllegalStateException("ファイル復号に失敗しました", e);
        }
    }

    private SecretKey resolveKey(String keyBase64) {
        try {
            if (keyBase64 != null && !keyBase64.isBlank()) {
                byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
                return new SecretKeySpec(keyBytes, "AES");
            }
            // 開発用: 起動ごとに生成（本番では必ず固定キーを設定）
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("暗号化キーの初期化に失敗しました", e);
        }
    }
}
