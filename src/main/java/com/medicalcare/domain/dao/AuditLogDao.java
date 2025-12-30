package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 監査ログデータアクセスオブジェクト
 */
@Repository
public interface AuditLogDao extends JpaRepository<AuditLog, Long> {

    /**
     * ユーザー名による検索
     */
    List<AuditLog> findByUsername(String username);

    /**
     * アクションによる検索
     */
    List<AuditLog> findByAction(String action);

    /**
     * 期間による検索
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * ユーザー名とアクションによる検索
     */
    List<AuditLog> findByUsernameAndAction(String username, String action);

    /**
     * ユーザー名によるカウント
     */
    long countByUsername(String username);

    /**
     * アクションによるカウント
     */
    long countByAction(String action);
}