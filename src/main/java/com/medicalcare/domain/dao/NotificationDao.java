package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationDao extends JpaRepository<Notification, Long> {
    
    // ユーザーIDによる検索
    List<Notification> findByUserId(Long userId);
    
    // ユーザーIDと既読状態による検索
    List<Notification> findByUserIdAndIsRead(Long userId, boolean isRead);
    
    // 通知タイプによる検索
    List<Notification> findByNotificationType(String notificationType);
    
    // 優先度による検索
    List<Notification> findByPriority(String priority);
    
    // 関連エンティティによる検索
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityId = :relatedEntityId AND n.relatedEntityType = :relatedEntityType")
    List<Notification> findByRelatedEntityIdAndRelatedEntityType(@Param("relatedEntityId") Long relatedEntityId, @Param("relatedEntityType") String relatedEntityType);
    
    // 未読通知の検索
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);
    
    // スケジュール通知の検索
    @Query("SELECT n FROM Notification n WHERE n.scheduledAt IS NOT NULL AND n.scheduledAt <= :now")
    List<Notification> findScheduledNotifications(@Param("now") LocalDateTime now);
    
    // ユーザーID別の件数
    long countByUserId(Long userId);
    
    // 未読通知の件数
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    // 通知タイプ別の件数
    long countByNotificationType(String notificationType);
    
    // 最近の通知を取得
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications();
} 