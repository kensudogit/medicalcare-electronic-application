package com.medicalcare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalcare.domain.dao.AuditLogDao;
import com.medicalcare.domain.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogDao auditLogDao;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 基本的な監査ログを作成
     */
    public AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId) {
        AuditLog auditLog = new AuditLog(userId, action, entityType, "SUCCESS");
        auditLog.setEntityId(entityId);
        return auditLogDao.save(auditLog);
    }

    /**
     * 変更内容を含む監査ログを作成
     */
    public AuditLog createAuditLogWithChanges(Long userId, String action, String entityType, Long entityId, 
                                            Object oldValues, Object newValues) {
        try {
            AuditLog auditLog = new AuditLog(userId, action, entityType, "SUCCESS");
            auditLog.setEntityId(entityId);
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
            return auditLogDao.save(auditLog);
        } catch (Exception e) {
            // JSON変換エラーの場合は、エラーログとして記録
            return createErrorAuditLog(userId, action, entityType, entityId, "JSON変換エラー: " + e.getMessage());
        }
    }

    /**
     * エラー監査ログを作成
     */
    public AuditLog createErrorAuditLog(Long userId, String action, String entityType, Long entityId, String errorMessage) {
        AuditLog auditLog = new AuditLog(userId, action, entityType, "ERROR");
        auditLog.setEntityId(entityId);
        auditLog.setErrorMessage(errorMessage);
        return auditLogDao.save(auditLog);
    }

    /**
     * 説明付きの監査ログを作成
     */
    public AuditLog createAuditLogWithDescription(Long userId, String action, String entityType, Long entityId, String description) {
        AuditLog auditLog = new AuditLog(userId, action, entityType, "SUCCESS");
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        return auditLogDao.save(auditLog);
    }

    /**
     * HTTPリクエスト情報を含む監査ログを作成
     */
    public AuditLog createAuditLogWithRequest(Long userId, String action, String entityType, Long entityId, 
                                            HttpServletRequest request) {
        AuditLog auditLog = new AuditLog(userId, action, entityType, "SUCCESS");
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setSessionId(request.getSession().getId());
        return auditLogDao.save(auditLog);
    }

    // 検索メソッド
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogDao.findByUserId(userId);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogDao.findByAction(action);
    }

    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        return auditLogDao.findByEntityType(entityType);
    }

    public List<AuditLog> getAuditLogsByEntityId(Long entityId) {
        return auditLogDao.findByEntityId(entityId);
    }

    public List<AuditLog> getAuditLogsByStatus(String status) {
        return auditLogDao.findByStatus(status);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogDao.findByCreatedAtBetween(startDate, endDate);
    }

    public List<AuditLog> getAuditLogsByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogDao.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
    }

    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogDao.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getRecentAuditLogs() {
        return auditLogDao.findRecentLogs();
    }

    // 統計メソッド
    public long getAuditLogCountByUser(Long userId) {
        return auditLogDao.countByUserId(userId);
    }

    public long getAuditLogCountByAction(String action) {
        return auditLogDao.countByAction(action);
    }

    public long getAuditLogCountByEntityType(String entityType) {
        return auditLogDao.countByEntityType(entityType);
    }

    public long getAuditLogCountByStatus(String status) {
        return auditLogDao.countByStatus(status);
    }

    /**
     * セキュリティ監査レポートを生成
     */
    public String generateSecurityAuditReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = auditLogDao.findByCreatedAtBetween(startDate, endDate);
        
        long totalLogs = logs.size();
        long errorLogs = logs.stream().filter(log -> "ERROR".equals(log.getStatus())).count();
        long loginAttempts = logs.stream().filter(log -> "LOGIN".equals(log.getAction())).count();
        long failedLogins = logs.stream()
                .filter(log -> "LOGIN".equals(log.getAction()) && "ERROR".equals(log.getStatus()))
                .count();
        
        return String.format(
            "セキュリティ監査レポート (%s - %s)\n" +
            "総ログ数: %d\n" +
            "エラーログ数: %d\n" +
            "ログイン試行数: %d\n" +
            "失敗ログイン数: %d\n" +
            "成功率: %.2f%%",
            startDate, endDate, totalLogs, errorLogs, loginAttempts, failedLogins,
            loginAttempts > 0 ? ((double)(loginAttempts - failedLogins) / loginAttempts) * 100 : 0
        );
    }

    /**
     * クライアントのIPアドレスを取得
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 