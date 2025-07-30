package com.medicalcare.controller;

import com.medicalcare.domain.entity.AuditLog;
import com.medicalcare.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {
    
    private final AuditService auditService;
    
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * ユーザー別監査ログ取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAuditLogsByUser(@PathVariable Long userId) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsByUser(userId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * アクション別監査ログ取得
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<?> getAuditLogsByAction(@PathVariable String action) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsByAction(action);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * エンティティタイプ別監査ログ取得
     */
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<?> getAuditLogsByEntityType(@PathVariable String entityType) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsByEntityType(entityType);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * エンティティID別監査ログ取得
     */
    @GetMapping("/entity-id/{entityId}")
    public ResponseEntity<?> getAuditLogsByEntityId(@PathVariable Long entityId) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsByEntityId(entityId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ステータス別監査ログ取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAuditLogsByStatus(@PathVariable String status) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsByStatus(status);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 日付範囲別監査ログ取得
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getAuditLogsByDateRange(@RequestParam String startDate, 
                                                    @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(start, end);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ユーザーと日付範囲別監査ログ取得
     */
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<?> getAuditLogsByUserAndDateRange(@PathVariable Long userId,
                                                           @RequestParam String startDate, 
                                                           @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<AuditLog> auditLogs = auditService.getAuditLogsByUserAndDateRange(userId, start, end);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * エンティティ別監査ログ取得
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<?> getAuditLogsByEntity(@PathVariable String entityType, 
                                                 @PathVariable Long entityId) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsByEntity(entityType, entityId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 最近の監査ログ取得
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        try {
            List<AuditLog> auditLogs = auditService.getRecentAuditLogs();
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    /**
     * 監査ログ統計取得
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<?> getAuditLogCountByUser(@PathVariable Long userId) {
        try {
            long count = auditService.getAuditLogCountByUser(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/action/{action}")
    public ResponseEntity<?> getAuditLogCountByAction(@PathVariable String action) {
        try {
            long count = auditService.getAuditLogCountByAction(action);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/entity-type/{entityType}")
    public ResponseEntity<?> getAuditLogCountByEntityType(@PathVariable String entityType) {
        try {
            long count = auditService.getAuditLogCountByEntityType(entityType);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/status/{status}")
    public ResponseEntity<?> getAuditLogCountByStatus(@PathVariable String status) {
        try {
            long count = auditService.getAuditLogCountByStatus(status);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * セキュリティ監査レポート生成
     */
    @GetMapping("/security-report")
    public ResponseEntity<String> generateSecurityAuditReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            String report = auditService.generateSecurityAuditReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("レポート生成エラー: " + e.getMessage());
        }
    }
    
    /**
     * 監査ログ統計サマリー取得
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<?> getAuditStatsSummary() {
        try {
            long totalLogs = auditService.getAuditLogCountByStatus("SUCCESS") + 
                           auditService.getAuditLogCountByStatus("ERROR");
            long errorLogs = auditService.getAuditLogCountByStatus("ERROR");
            long loginLogs = auditService.getAuditLogCountByAction("LOGIN");
            long logoutLogs = auditService.getAuditLogCountByAction("LOGOUT");
            long createLogs = auditService.getAuditLogCountByAction("CREATE");
            long updateLogs = auditService.getAuditLogCountByAction("UPDATE");
            long deleteLogs = auditService.getAuditLogCountByAction("DELETE");
            
            return ResponseEntity.ok(Map.of(
                "totalLogs", totalLogs,
                "errorLogs", errorLogs,
                "loginLogs", loginLogs,
                "logoutLogs", logoutLogs,
                "createLogs", createLogs,
                "updateLogs", updateLogs,
                "deleteLogs", deleteLogs,
                "errorRate", totalLogs > 0 ? (double) errorLogs / totalLogs : 0.0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 