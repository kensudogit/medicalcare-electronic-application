package com.medicalcare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ヘルスチェック用コントローラー
 */
@RestController
@RequestMapping("/")
public class HealthController {

    /**
     * ヘルスチェックエンドポイント
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "healthy");
        healthData.put("timestamp", LocalDateTime.now().toString());
        healthData.put("application", "medicalcare-electronic-application");
        healthData.put("version", "1.0.0");
        healthData.put("environment", "docker");
        
        return ResponseEntity.ok(healthData);
    }

    /**
     * 詳細ヘルスチェックエンドポイント
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "healthy");
        healthData.put("timestamp", LocalDateTime.now().toString());
        healthData.put("application", "medicalcare-electronic-application");
        healthData.put("version", "1.0.0");
        healthData.put("environment", "docker");
        
        // システム情報
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory());
        systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
        
        healthData.put("system", systemInfo);
        
        return ResponseEntity.ok(healthData);
    }
} 