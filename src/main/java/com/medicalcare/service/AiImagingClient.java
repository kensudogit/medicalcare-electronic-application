package com.medicalcare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * FastAPI 医療画像AIサービス クライアント
 */
@Service
public class AiImagingClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.imaging.base-url:http://localhost:8090}")
    private String baseUrl;

    public Map<String, Object> health() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/health", Map.class);
        return response.getBody() != null ? response.getBody() : Map.of();
    }

    public Map<String, Object> listProviders() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/providers", Map.class);
        return response.getBody() != null ? response.getBody() : Map.of();
    }

    public JsonNode analyze(File imageFile, String modality, String provider,
                            boolean generateFindings, String patientContext) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(imageFile));
            if (modality != null && !modality.isBlank()) {
                body.add("modality", modality);
            }
            if (provider != null && !provider.isBlank()) {
                body.add("provider", provider);
            }
            body.add("generate_findings", String.valueOf(generateFindings));
            if (patientContext != null && !patientContext.isBlank()) {
                body.add("patient_context", patientContext);
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/analyze", request, String.class);

            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("AI画像解析サービス呼び出しに失敗しました: " + e.getMessage(), e);
        }
    }

    public JsonNode extractDicomMetadata(File dicomFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(dicomFile));
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/dicom/metadata", request, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("DICOMメタデータ取得に失敗しました: " + e.getMessage(), e);
        }
    }

    public byte[] downloadPreview(String previewId) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    baseUrl + "/previews/" + previewId, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("プレビュー取得に失敗しました: " + e.getMessage(), e);
        }
    }

    public Path savePreviewLocally(String previewId, Path destination) {
        try {
            byte[] data = downloadPreview(previewId);
            Files.createDirectories(destination.getParent());
            Files.write(destination, data);
            return destination;
        } catch (Exception e) {
            throw new RuntimeException("プレビュー保存に失敗しました: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> toMap(JsonNode node) {
        try {
            return objectMapper.convertValue(node, HashMap.class);
        } catch (Exception e) {
            return Map.of("raw", node != null ? node.toString() : "");
        }
    }
}
