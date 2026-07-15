package com.medicalcare.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 患者氏名・患者IDの匿名化（PHI最小化）
 * 可逆な平文保管を避け、表示用トークンと照合用ハッシュを分離する。
 */
@Service
public class PhiAnonymizationService {

    private static final Pattern DIGITS = Pattern.compile("\\d");

    @Value("${security.phi.pepper:medicalcare-phi-pepper-change-me}")
    private String pepper;

    /**
     * 患者IDの照合用ハッシュ（一方向）
     */
    public String hashPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        return sha256(pepper + "|PID|" + patientId.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 患者氏名の照合用ハッシュ（一方向）
     */
    public String hashPatientName(String patientName) {
        if (patientName == null || patientName.isBlank()) {
            return null;
        }
        String normalized = patientName.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        return sha256(pepper + "|NAME|" + normalized);
    }

    /**
     * UI表示用の匿名ID（例: ANON-A1B2C3D4）
     */
    public String createDisplayAlias(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return "ANON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        }
        String digest = hmacSha256(patientId.trim().toUpperCase(Locale.ROOT));
        return "ANON-" + digest.substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * 氏名マスク（例: 山田 太郎 → 山** **郎）
     */
    public String maskPatientName(String patientName) {
        if (patientName == null || patientName.isBlank()) {
            return null;
        }
        String trimmed = patientName.trim();
        if (trimmed.length() <= 1) {
            return "*";
        }
        if (trimmed.length() == 2) {
            return trimmed.charAt(0) + "*";
        }
        return trimmed.charAt(0) + "*".repeat(Math.min(trimmed.length() - 2, 4)) + trimmed.charAt(trimmed.length() - 1);
    }

    /**
     * 患者IDマスク（中央を伏字）
     */
    public String maskPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        String id = patientId.trim();
        if (id.length() <= 4) {
            return DIGITS.matcher(id).replaceAll("*");
        }
        int keep = Math.min(2, id.length() / 4);
        return id.substring(0, keep) + "*".repeat(id.length() - keep * 2) + id.substring(id.length() - keep);
    }

    /**
     * DICOMメタデータJSONから氏名・患者IDを除去した匿名化JSON文字列を返す
     */
    public String anonymizeDicomMetadataJson(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        return json
                .replaceAll("(?i)\"patient_name\"\\s*:\\s*\"[^\"]*\"", "\"patient_name\":\"ANONYMIZED\"")
                .replaceAll("(?i)\"PatientName\"\\s*:\\s*\"[^\"]*\"", "\"PatientName\":\"ANONYMIZED\"")
                .replaceAll("(?i)\"patient_id\"\\s*:\\s*\"[^\"]*\"", "\"patient_id\":\"ANONYMIZED\"")
                .replaceAll("(?i)\"PatientID\"\\s*:\\s*\"[^\"]*\"", "\"PatientID\":\"ANONYMIZED\"");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("ハッシュ計算に失敗しました", e);
        }
    }

    private String hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC計算に失敗しました", e);
        }
    }
}
