package com.medicalcare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalcare.domain.dao.AuditLogDao;
import com.medicalcare.domain.dao.UserDao;
import com.medicalcare.domain.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    @Autowired
    private AuditLogDao auditLogDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 基本的な監査ログを作成
     */
    public AuditLog createAuditLog(String username, String action, String details) {
        AuditLog auditLog = new AuditLog(action, username, details);
        return auditLogDao.save(auditLog);
    }

    /**
     * ユーザーIDによる監査ログ作成（オーバーロード）
     */
    public AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId) {
        String username = userDao.findById(userId)
                .map(user -> user.getUsername())
                .orElse("UNKNOWN");
        String details = String.format("%s (ID: %d)", entityType, entityId);
        AuditLog auditLog = new AuditLog(action, username, details);
        return auditLogDao.save(auditLog);
    }

    /**
     * 変更内容を含む監査ログを作成
     */
    public AuditLog createAuditLogWithChanges(String username, String action, Object oldValues, Object newValues) {
        try {
            String details = "変更前: " + (oldValues != null ? objectMapper.writeValueAsString(oldValues) : "null") +
                    ", 変更後: " + (newValues != null ? objectMapper.writeValueAsString(newValues) : "null");
            AuditLog auditLog = new AuditLog(action, username, details);
            return auditLogDao.save(auditLog);
        } catch (Exception e) {
            // JSON変換エラーの場合は、エラーログとして記録
            return createErrorAuditLog(username, action, "JSON変換エラー: " + e.getMessage());
        }
    }

    /**
     * エラー監査ログを作成
     */
    public AuditLog createErrorAuditLog(String username, String action, String errorMessage) {
        AuditLog auditLog = new AuditLog(action, username, "エラー: " + errorMessage);
        return auditLogDao.save(auditLog);
    }

    /**
     * 説明付きの監査ログを作成
     */
    public AuditLog createAuditLogWithDescription(String username, String action, String description) {
        AuditLog auditLog = new AuditLog(action, username, description);
        return auditLogDao.save(auditLog);
    }

    /**
     * HTTPリクエスト情報を含む監査ログを作成
     */
    public AuditLog createAuditLogWithRequest(String username, String action, String details,
            HttpServletRequest request) {
        AuditLog auditLog = new AuditLog(action, username, details);
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        return auditLogDao.save(auditLog);
    }

    // 検索メソッド
    public List<AuditLog> getAuditLogsByUsername(String username) {
        return auditLogDao.findByUsername(username);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogDao.findByAction(action);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogDao.findByTimestampBetween(startDate, endDate);
    }

    public List<AuditLog> getAuditLogsByUsernameAndAction(String username, String action) {
        return auditLogDao.findByUsernameAndAction(username, action);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogDao.findAll();
    }

    public long getAuditLogCountByUsername(String username) {
        return auditLogDao.countByUsername(username);
    }

    public long getAuditLogCountByAction(String action) {
        return auditLogDao.countByAction(action);
    }

    /**
     * ユーザーIDによる監査ログ取得
     */
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        String username = userDao.findById(userId)
                .map(user -> user.getUsername())
                .orElse(null);
        if (username == null) {
            return List.of();
        }
        return auditLogDao.findByUsername(username);
    }

    /**
     * エンティティタイプによる監査ログ取得
     */
    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        List<AuditLog> allLogs = auditLogDao.findAll();
        return allLogs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains(entityType))
                .collect(Collectors.toList());
    }

    /**
     * エンティティIDによる監査ログ取得
     */
    public List<AuditLog> getAuditLogsByEntityId(Long entityId) {
        List<AuditLog> allLogs = auditLogDao.findAll();
        String searchText = "ID: " + entityId;
        return allLogs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains(searchText))
                .collect(Collectors.toList());
    }

    /**
     * ステータスによる監査ログ取得
     */
    public List<AuditLog> getAuditLogsByStatus(String status) {
        List<AuditLog> allLogs = auditLogDao.findAll();
        return allLogs.stream()
                .filter(log -> status.equals(log.getAction()) || 
                        (log.getDetails() != null && log.getDetails().contains(status)))
                .collect(Collectors.toList());
    }

    /**
     * ユーザーと日付範囲による監査ログ取得
     */
    public List<AuditLog> getAuditLogsByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        List<AuditLog> userLogs = getAuditLogsByUser(userId);
        return userLogs.stream()
                .filter(log -> log.getTimestamp() != null &&
                        !log.getTimestamp().isBefore(start) &&
                        !log.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * エンティティによる監査ログ取得
     */
    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        List<AuditLog> allLogs = auditLogDao.findAll();
        String searchText = String.format("%s (ID: %d)", entityType, entityId);
        return allLogs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains(searchText))
                .collect(Collectors.toList());
    }

    /**
     * 最近の監査ログ取得
     */
    public List<AuditLog> getRecentAuditLogs() {
        List<AuditLog> allLogs = auditLogDao.findAll();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return allLogs.stream()
                .filter(log -> log.getTimestamp() != null && log.getTimestamp().isAfter(oneWeekAgo))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(100)
                .collect(Collectors.toList());
    }

    /**
     * ユーザーIDによる監査ログ数取得
     */
    public long getAuditLogCountByUser(Long userId) {
        return getAuditLogsByUser(userId).size();
    }

    /**
     * エンティティタイプによる監査ログ数取得
     */
    public long getAuditLogCountByEntityType(String entityType) {
        return getAuditLogsByEntityType(entityType).size();
    }

    /**
     * ステータスによる監査ログ数取得
     */
    public long getAuditLogCountByStatus(String status) {
        return getAuditLogsByStatus(status).size();
    }

    /**
     * セキュリティ監査レポートを生成
     */
    public String generateSecurityAuditReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = auditLogDao.findByTimestampBetween(startDate, endDate);

        long totalLogs = logs.size();
        long errorLogs = logs.stream().filter(log -> log.getDetails().contains("エラー")).count();
        long loginErrors = logs.stream()
                .filter(log -> "LOGIN".equals(log.getAction()) && log.getDetails().contains("エラー"))
                .count();

        return String.format("監査レポート (%s - %s)\n" +
                "総ログ数: %d\n" +
                "エラーログ数: %d\n" +
                "ログインエラー数: %d",
                startDate, endDate, totalLogs, errorLogs, loginErrors);
    }

    /**
     * クライアントIPアドレスを取得
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