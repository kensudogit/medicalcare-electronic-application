package com.medicalcare.service;

import com.medicalcare.domain.entity.ImageAnalysisResult;
import com.medicalcare.domain.entity.MedicalImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 電子カルテ（EHR）連携サービス（HL7 FHIR DocumentReference スタブ）
 */
@Service
public class EhrIntegrationService {

    @Value("${integration.ehr.enabled:false}")
    private boolean enabled;

    @Value("${integration.ehr.base-url:http://localhost:8089/fhir}")
    private String ehrBaseUrl;

    @Value("${integration.ehr.system:MedicalCareImaging}")
    private String systemName;

    public Map<String, Object> sendFindings(MedicalImage image, ImageAnalysisResult analysis) {
        Map<String, Object> result = new LinkedHashMap<>();
        String documentId = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        result.put("enabled", enabled);
        result.put("ehrBaseUrl", ehrBaseUrl);
        result.put("system", systemName);
        result.put("documentId", documentId);
        result.put("resourceType", "DocumentReference");
        result.put("patientId", image.getPatientId());
        result.put("modality", image.getModality());
        result.put("imageId", image.getId());
        result.put("syncedAt", LocalDateTime.now().toString());

        if (analysis != null) {
            result.put("analysisId", analysis.getId());
            result.put("findingsText", analysis.getFindingsText());
            result.put("provider", analysis.getProvider());
            result.put("detectionsJson", analysis.getDetectionsJson());
        }

        if (!enabled) {
            result.put("status", "STUB_SUCCESS");
            result.put("message", "電子カルテ連携はスタブモードです。integration.ehr.enabled=true で有効化してください。");
            return result;
        }

        // TODO: FHIR DocumentReference / DiagnosticReport POST
        result.put("status", "QUEUED");
        result.put("message", "電子カルテへ所見送信をキュー登録しました");
        return result;
    }
}
