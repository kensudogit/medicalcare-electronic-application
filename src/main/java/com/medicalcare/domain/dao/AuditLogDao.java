package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogDao extends JpaRepository<AuditLog, Long> {
    
    // ユーザーIDによる検索
    List<AuditLog> findByUserId(Long userId);
    
    // アクションによる検索
    List<AuditLog> findByAction(String action);
    
    // エンティティタイプによる検索
    List<AuditLog> findByEntityType(String entityType);
    
    // エンティティIDによる検索
    List<AuditLog> findByEntityId(Long entityId);
    
    // ステータスによる検索
    List<AuditLog> findByStatus(String status);
    
    // 日付範囲による検索
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // ユーザーIDと日付範囲による検索
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // エンティティタイプとエンティティIDによる検索
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId")
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    // 最近のログを取得
    @Query("SELECT al FROM AuditLog al ORDER BY al.createdAt DESC")
    List<AuditLog> findRecentLogs();
    
    // ユーザーID別の件数
    long countByUserId(Long userId);
    
    // アクション別の件数
    long countByAction(String action);
    
    // エンティティタイプ別の件数
    long countByEntityType(String entityType);
    
    // ステータス別の件数
    long countByStatus(String status);
} 