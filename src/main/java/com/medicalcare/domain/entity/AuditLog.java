package com.medicalcare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; // 操作実行ユーザーID
    
    @Column(name = "action", nullable = false)
    private String action; // CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT
    
    @Column(name = "entity_type", nullable = false)
    private String entityType; // USER, APPLICATION, MEDICAL_INSTITUTION, etc.
    
    @Column(name = "entity_id")
    private Long entityId; // 操作対象エンティティID
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON形式の変更前データ
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON形式の変更後データ
    
    @Column(name = "ip_address")
    private String ipAddress; // 操作元IPアドレス
    
    @Column(name = "user_agent")
    private String userAgent; // ブラウザ情報
    
    @Column(name = "session_id")
    private String sessionId; // セッションID
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 操作の詳細説明
    
    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILURE, ERROR
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // エラーメッセージ（失敗時）
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // デフォルトコンストラクタ
    public AuditLog() {}

    // コンストラクタ
    public AuditLog(Long userId, String action, String entityType, String status) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 