package com.medicalcare.service;

import com.medicalcare.domain.dao.MedicalImageDao;
import com.medicalcare.domain.entity.MedicalImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * PACS連携 / DICOMweb 連携サービス
 * - DIMSE: C-STORE / C-FIND（スタブ）
 * - DICOMweb: QIDO-RS / WADO-RS / STOW-RS
 */
@Service
public class PacsIntegrationService {

    @Value("${integration.pacs.enabled:false}")
    private boolean enabled;

    @Value("${integration.pacs.ae-title:MEDICALCARE_SCU}")
    private String aeTitle;

    @Value("${integration.pacs.remote-host:localhost}")
    private String remoteHost;

    @Value("${integration.pacs.remote-port:11112}")
    private int remotePort;

    @Value("${integration.pacs.dicomweb-base-url:}")
    private String dicomwebBaseUrl;

    private final MedicalImageDao medicalImageDao;

    public PacsIntegrationService(MedicalImageDao medicalImageDao) {
        this.medicalImageDao = medicalImageDao;
    }

    public Map<String, Object> sendStudy(MedicalImage image) {
        Map<String, Object> result = new LinkedHashMap<>();
        String accession = image.getPacsAccessionNumber() != null
                ? image.getPacsAccessionNumber()
                : "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        result.put("enabled", enabled);
        result.put("protocol", dicomwebBaseUrl != null && !dicomwebBaseUrl.isBlank() ? "DICOMweb-STOW-RS" : "DIMSE-C-STORE");
        result.put("aeTitle", aeTitle);
        result.put("remoteHost", remoteHost);
        result.put("remotePort", remotePort);
        result.put("dicomwebBaseUrl", dicomwebBaseUrl);
        result.put("accessionNumber", accession);
        result.put("studyUid", image.getStudyUid() != null ? image.getStudyUid() : "1.2.840.10008." + image.getId());
        result.put("seriesUid", image.getSeriesUid());
        result.put("sopInstanceUid", image.getSopInstanceUid());
        result.put("modality", image.getModality());
        // 平文患者IDは送出しない（匿名エイリアスのみ）
        result.put("patientAlias", image.getPatientAlias());
        result.put("patientIdHash", image.getPatientIdHash());
        result.put("syncedAt", LocalDateTime.now().toString());

        if (!enabled) {
            result.put("status", "STUB_SUCCESS");
            result.put("message", "PACS連携はスタブモードです。integration.pacs.enabled=true で有効化してください。");
            return result;
        }

        result.put("status", "QUEUED");
        result.put("message", "PACS送信キューに登録しました（匿名化済みメタデータ）");
        return result;
    }

    public Map<String, Object> queryStudy(String patientAliasOrHash, String accessionNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("patientQuery", patientAliasOrHash);
        result.put("accessionNumber", accessionNumber);
        result.put("protocol", "DICOMweb-QIDO-RS / DIMSE-C-FIND");
        result.put("status", enabled ? "QUERY_OK" : "STUB_SUCCESS");
        result.put("studies", List.of());
        result.put("message", enabled ? "PACS照会を実行しました" : "PACS照会はスタブモードです");
        return result;
    }

    /**
     * DICOMweb QIDO-RS: Study検索
     */
    public Map<String, Object> qidoSearchStudies(String patientIdHash, String studyUid, String modality) {
        List<Map<String, Object>> studies = new ArrayList<>();
        List<MedicalImage> images = medicalImageDao.findAll();
        for (MedicalImage img : images) {
            if (!Boolean.TRUE.equals(img.getIsDicom()) && img.getStudyUid() == null) {
                continue;
            }
            if (patientIdHash != null && !patientIdHash.isBlank()
                    && (img.getPatientIdHash() == null || !img.getPatientIdHash().equals(patientIdHash))) {
                continue;
            }
            if (studyUid != null && !studyUid.isBlank()
                    && (img.getStudyUid() == null || !img.getStudyUid().equals(studyUid))) {
                continue;
            }
            if (modality != null && !modality.isBlank()
                    && (img.getModality() == null || !img.getModality().equalsIgnoreCase(modality))) {
                continue;
            }
            Map<String, Object> study = new LinkedHashMap<>();
            study.put("00080020", Map.of("vr", "DA", "Value", List.of(""))); // StudyDate
            study.put("00080050", Map.of("vr", "SH", "Value",
                    List.of(img.getPacsAccessionNumber() != null ? img.getPacsAccessionNumber() : "")));
            study.put("00080060", Map.of("vr", "CS", "Value", List.of(img.getModality())));
            study.put("00080061", Map.of("vr", "CS", "Value", List.of(img.getModality())));
            study.put("0020000D", Map.of("vr", "UI", "Value",
                    List.of(img.getStudyUid() != null ? img.getStudyUid() : "")));
            study.put("00100020", Map.of("vr", "LO", "Value",
                    List.of(img.getPatientAlias() != null ? img.getPatientAlias() : "ANONYMIZED")));
            study.put("resourceId", img.getId());
            studies.add(study);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resourceType", "Bundle");
        result.put("type", "searchset");
        result.put("total", studies.size());
        result.put("entry", studies);
        result.put("dicomweb", "QIDO-RS");
        result.put("note", "PatientIDは匿名エイリアスのみ返却します");
        return result;
    }

    /**
     * DICOMweb WADO-RS: インスタンス取得メタ情報
     */
    public Map<String, Object> wadoRetrieveInstance(Long imageId) {
        MedicalImage image = medicalImageDao.findById(imageId)
                .orElseThrow(() -> new RuntimeException("画像が見つかりません"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dicomweb", "WADO-RS");
        result.put("studyUid", image.getStudyUid());
        result.put("seriesUid", image.getSeriesUid());
        result.put("sopInstanceUid", image.getSopInstanceUid());
        result.put("contentType", Boolean.TRUE.equals(image.getIsDicom())
                ? "application/dicom"
                : image.getContentType());
        result.put("downloadPath", "/api/api/medical-images/" + imageId + "/download");
        result.put("previewPath", "/api/api/medical-images/" + imageId + "/preview");
        result.put("patientAlias", image.getPatientAlias());
        result.put("fileEncrypted", image.getFileEncrypted());
        return result;
    }

    /**
     * DICOMweb STOW-RS: 格納受付
     */
    public Map<String, Object> stowStore(MedicalImage image) {
        Map<String, Object> send = sendStudy(image);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dicomweb", "STOW-RS");
        result.put("status", send.get("status"));
        result.put("RetrieveURL", "/dicomweb/studies/" + send.get("studyUid"));
        result.put("accessionNumber", send.get("accessionNumber"));
        result.put("message", "STOW-RS 格納リクエストを受け付けました");
        return result;
    }
}
