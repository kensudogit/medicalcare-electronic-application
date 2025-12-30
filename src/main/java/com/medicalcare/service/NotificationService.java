package com.medicalcare.service;

import com.medicalcare.domain.dao.NotificationDao;
import com.medicalcare.domain.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    /**
     * 通知作成
     */
    public Notification createNotification(Long userId, String title, String message, String type) {
        Notification notification = new Notification(userId, title, message, type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

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
        LocalDateTime now = LocalDateTime.now();
        List<Notification> allNotifications = notificationDao.findAll();

        for (Notification notification : allNotifications) {
            // 30日以上前の通知を削除
            if (notification.getCreatedAt().plusDays(30).isBefore(now)) {
                notificationDao.delete(notification);
            }
        }
    }

    /**
     * ユーザーの通知取得
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    /**
     * ユーザーの未読通知取得
     */
    public List<Notification> getUserUnreadNotifications(Long userId) {
        return notificationDao.findByUserIdAndReadFalse(userId);
    }

    /**
     * タイプ別通知取得
     */
    public List<Notification> getNotificationsByType(String type) {
        return notificationDao.findByType(type);
    }

    /**
     * 全通知取得
     */
    public List<Notification> getAllNotifications() {
        return notificationDao.findAll();
    }

    /**
     * 通知IDによる取得
     */
    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationDao.findById(notificationId);
    }

    /**
     * ユーザーIDとステータスによる通知取得
     */
    public List<Notification> getNotificationsByUserIdAndStatus(Long userId, String status) {
        return notificationDao.findByUserIdAndStatus(userId, status);
    }

    /**
     * エンティティ関連の通知作成
     */
    public Notification createNotificationWithEntity(Long userId, String title, String message,
            String notificationType, Long relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification(userId, title, message, notificationType);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        // エンティティ情報はメッセージに含める
        String entityInfo = String.format(" (関連エンティティ: %s, ID: %d)", relatedEntityType, relatedEntityId);
        notification.setMessage(message + entityInfo);
        return notificationDao.save(notification);
    }

    /**
     * 優先度付き通知作成
     */
    public Notification createPriorityNotification(Long userId, String title, String message,
            String notificationType, String priority) {
        Notification notification = new Notification(userId, title, message, notificationType);
        notification.setStatus(priority); // 優先度をステータスとして使用
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        return notificationDao.save(notification);
    }

    /**
     * スケジュール通知作成
     */
    public Notification createScheduledNotification(Long userId, String title, String message,
            String notificationType, LocalDateTime scheduledAt) {
        Notification notification = new Notification(userId, title, message, notificationType);
        notification.setStatus("SCHEDULED");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        // スケジュール情報はメッセージに含める
        String scheduleInfo = String.format(" (予定日時: %s)", scheduledAt);
        notification.setMessage(message + scheduleInfo);
        return notificationDao.save(notification);
    }

    /**
     * ユーザーの既読/未読通知取得
     */
    public List<Notification> getUserNotificationsByReadStatus(Long userId, boolean isRead) {
        List<Notification> allNotifications = notificationDao.findByUserId(userId);
        return allNotifications.stream()
                .filter(n -> n.isRead() == isRead)
                .collect(Collectors.toList());
    }

    /**
     * 優先度別通知取得
     */
    public List<Notification> getNotificationsByPriority(String priority) {
        List<Notification> allNotifications = notificationDao.findAll();
        return allNotifications.stream()
                .filter(n -> priority.equals(n.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 関連エンティティ別通知取得
     */
    public List<Notification> getNotificationsByRelatedEntity(Long relatedEntityId, String relatedEntityType) {
        List<Notification> allNotifications = notificationDao.findAll();
        String searchText = String.format("関連エンティティ: %s, ID: %d", relatedEntityType, relatedEntityId);
        return allNotifications.stream()
                .filter(n -> n.getMessage() != null && n.getMessage().contains(searchText))
                .collect(Collectors.toList());
    }

    /**
     * スケジュール通知取得
     */
    public List<Notification> getScheduledNotifications() {
        List<Notification> allNotifications = notificationDao.findAll();
        return allNotifications.stream()
                .filter(n -> "SCHEDULED".equals(n.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * ユーザー別通知数取得
     */
    public long getNotificationCountByUser(Long userId) {
        return notificationDao.findByUserId(userId).size();
    }

    /**
     * ユーザー別未読通知数取得
     */
    public long getUnreadNotificationCountByUser(Long userId) {
        return notificationDao.findByUserIdAndReadFalse(userId).size();
    }

    /**
     * 通知タイプ別通知数取得
     */
    public long getNotificationCountByType(String notificationType) {
        return notificationDao.findByType(notificationType).size();
    }

    /**
     * スケジュール通知の処理
     */
    public void processScheduledNotifications() {
        List<Notification> scheduledNotifications = getScheduledNotifications();
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : scheduledNotifications) {
            // スケジュール時刻が過ぎた通知を処理
            if (notification.getCreatedAt() != null && notification.getCreatedAt().isBefore(now)) {
                notification.setStatus("SENT");
                notification.setUpdatedAt(now);
                notificationDao.save(notification);
            }
        }
    }
}