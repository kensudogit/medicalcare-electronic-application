package com.medicalcare.service;

import com.medicalcare.domain.dao.NotificationDao;
import com.medicalcare.domain.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationDao notificationDao;

    /**
     * 通知作成
     */
    public Notification createNotification(Long userId, String title, String message, String notificationType) {
        Notification notification = new Notification(userId, title, message, notificationType, "MEDIUM");
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationDao.save(notification);
    }

    /**
     * エンティティ関連の通知作成
     */
    public Notification createNotificationWithEntity(Long userId, String title, String message, 
                                                   String notificationType, Long relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification(userId, title, message, notificationType, "MEDIUM");
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationDao.save(notification);
    }

    /**
     * 優先度付き通知作成
     */
    public Notification createPriorityNotification(Long userId, String title, String message, 
                                                 String notificationType, String priority) {
        Notification notification = new Notification(userId, title, message, notificationType, priority);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationDao.save(notification);
    }

    /**
     * スケジュール通知作成
     */
    public Notification createScheduledNotification(Long userId, String title, String message, 
                                                  String notificationType, LocalDateTime scheduledAt) {
        Notification notification = new Notification(userId, title, message, notificationType, "MEDIUM");
        notification.setScheduledAt(scheduledAt);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationDao.save(notification);
    }

    /**
     * 通知を既読にする
     */
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationDao.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());
            
            return notificationDao.save(notification);
        } else {
            throw new RuntimeException("通知が見つかりません: " + notificationId);
        }
    }

    /**
     * 複数通知を既読にする
     */
    public void markMultipleAsRead(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            try {
                markAsRead(notificationId);
            } catch (Exception e) {
                // 個別のエラーはログに記録するが、処理は続行
                System.err.println("通知既読エラー (ID: " + notificationId + "): " + e.getMessage());
            }
        }
    }

    /**
     * 通知削除
     */
    public void deleteNotification(Long notificationId) {
        Optional<Notification> notificationOpt = notificationDao.findById(notificationId);
        if (notificationOpt.isPresent()) {
            notificationDao.delete(notificationOpt.get());
        } else {
            throw new RuntimeException("通知が見つかりません: " + notificationId);
        }
    }

    /**
     * 期限切れ通知の削除
     */
    public void deleteExpiredNotifications() {
        List<Notification> allNotifications = notificationDao.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Notification notification : allNotifications) {
            if (notification.getExpiresAt() != null && notification.getExpiresAt().isBefore(now)) {
                notificationDao.delete(notification);
            }
        }
    }

    // 検索メソッド
    public List<Notification> getUserNotifications(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public List<Notification> getUserUnreadNotifications(Long userId) {
        return notificationDao.findUnreadByUserId(userId);
    }

    public List<Notification> getUserNotificationsByReadStatus(Long userId, boolean isRead) {
        return notificationDao.findByUserIdAndIsRead(userId, isRead);
    }

    public List<Notification> getNotificationsByType(String notificationType) {
        return notificationDao.findByNotificationType(notificationType);
    }

    public List<Notification> getNotificationsByPriority(String priority) {
        return notificationDao.findByPriority(priority);
    }

    public List<Notification> getNotificationsByRelatedEntity(Long relatedEntityId, String relatedEntityType) {
        return notificationDao.findByRelatedEntityIdAndRelatedEntityType(relatedEntityId, relatedEntityType);
    }

    public List<Notification> getScheduledNotifications() {
        return notificationDao.findScheduledNotifications(LocalDateTime.now());
    }

    // 統計メソッド
    public long getNotificationCountByUser(Long userId) {
        return notificationDao.countByUserId(userId);
    }

    public long getUnreadNotificationCountByUser(Long userId) {
        return notificationDao.countUnreadByUserId(userId);
    }

    public long getNotificationCountByType(String notificationType) {
        return notificationDao.countByNotificationType(notificationType);
    }

    /**
     * スケジュール通知の処理
     */
    public void processScheduledNotifications() {
        List<Notification> scheduledNotifications = getScheduledNotifications();
        for (Notification notification : scheduledNotifications) {
            // ここで実際の通知送信処理を行う
            // 例: メール送信、プッシュ通知など
            System.out.println("スケジュール通知送信: " + notification.getTitle() + " to user " + notification.getUserId());
        }
    }
} 