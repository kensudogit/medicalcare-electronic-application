package com.medicalcare.controller;

import com.medicalcare.domain.entity.Notification;
import com.medicalcare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;

    /**
     * 通知作成
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody CreateNotificationRequest request) {
        try {
            Notification notification = notificationService.createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getNotificationType()
            );
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * エンティティ関連の通知作成
     */
    @PostMapping("/with-entity")
    public ResponseEntity<Notification> createNotificationWithEntity(@RequestBody CreateNotificationWithEntityRequest request) {
        try {
            Notification notification = notificationService.createNotificationWithEntity(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getNotificationType(),
                request.getRelatedEntityId(),
                request.getRelatedEntityType()
            );
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 優先度付き通知作成
     */
    @PostMapping("/priority")
    public ResponseEntity<Notification> createPriorityNotification(@RequestBody CreatePriorityNotificationRequest request) {
        try {
            Notification notification = notificationService.createPriorityNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getNotificationType(),
                request.getPriority()
            );
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * スケジュール通知作成
     */
    @PostMapping("/scheduled")
    public ResponseEntity<Notification> createScheduledNotification(@RequestBody CreateScheduledNotificationRequest request) {
        try {
            Notification notification = notificationService.createScheduledNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getNotificationType(),
                request.getScheduledAt()
            );
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 通知を既読にする
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        try {
            Notification notification = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 複数通知を既読にする
     */
    @PostMapping("/read-multiple")
    public ResponseEntity<Void> markMultipleAsRead(@RequestBody MarkMultipleAsReadRequest request) {
        try {
            notificationService.markMultipleAsRead(request.getNotificationIds());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 通知削除
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 期限切れ通知の削除
     */
    @DeleteMapping("/expired")
    public ResponseEntity<Void> deleteExpiredNotifications() {
        try {
            notificationService.deleteExpiredNotifications();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの通知一覧取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの未読通知一覧取得
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUserUnreadNotifications(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUserUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの既読/未読通知一覧取得
     */
    @GetMapping("/user/{userId}/read-status/{isRead}")
    public ResponseEntity<List<Notification>> getUserNotificationsByReadStatus(
            @PathVariable Long userId, @PathVariable boolean isRead) {
        try {
            List<Notification> notifications = notificationService.getUserNotificationsByReadStatus(userId, isRead);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 通知タイプ別一覧取得
     */
    @GetMapping("/type/{notificationType}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable String notificationType) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByType(notificationType);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 優先度別通知一覧取得
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Notification>> getNotificationsByPriority(@PathVariable String priority) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByPriority(priority);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 関連エンティティ別通知一覧取得
     */
    @GetMapping("/entity/{relatedEntityId}/{relatedEntityType}")
    public ResponseEntity<List<Notification>> getNotificationsByRelatedEntity(
            @PathVariable Long relatedEntityId, @PathVariable String relatedEntityType) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByRelatedEntity(relatedEntityId, relatedEntityType);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * スケジュール通知一覧取得
     */
    @GetMapping("/scheduled")
    public ResponseEntity<List<Notification>> getScheduledNotifications() {
        try {
            List<Notification> notifications = notificationService.getScheduledNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザー別通知統計取得
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Long> getNotificationCountByUser(@PathVariable Long userId) {
        try {
            long count = notificationService.getNotificationCountByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザー別未読通知統計取得
     */
    @GetMapping("/stats/user/{userId}/unread")
    public ResponseEntity<Long> getUnreadNotificationCountByUser(@PathVariable Long userId) {
        try {
            long count = notificationService.getUnreadNotificationCountByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 通知タイプ別統計取得
     */
    @GetMapping("/stats/type/{notificationType}")
    public ResponseEntity<Long> getNotificationCountByType(@PathVariable String notificationType) {
        try {
            long count = notificationService.getNotificationCountByType(notificationType);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * スケジュール通知の処理
     */
    @PostMapping("/process-scheduled")
    public ResponseEntity<Void> processScheduledNotifications() {
        try {
            notificationService.processScheduledNotifications();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // リクエストクラス
    public static class CreateNotificationRequest {
        private Long userId;
        private String title;
        private String message;
        private String notificationType;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    }

    public static class CreateNotificationWithEntityRequest {
        private Long userId;
        private String title;
        private String message;
        private String notificationType;
        private Long relatedEntityId;
        private String relatedEntityType;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
        
        public Long getRelatedEntityId() { return relatedEntityId; }
        public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
        
        public String getRelatedEntityType() { return relatedEntityType; }
        public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    }

    public static class CreatePriorityNotificationRequest {
        private Long userId;
        private String title;
        private String message;
        private String notificationType;
        private String priority;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class CreateScheduledNotificationRequest {
        private Long userId;
        private String title;
        private String message;
        private String notificationType;
        private LocalDateTime scheduledAt;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
        
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    }

    public static class MarkMultipleAsReadRequest {
        private List<Long> notificationIds;

        // Getters and Setters
        public List<Long> getNotificationIds() { return notificationIds; }
        public void setNotificationIds(List<Long> notificationIds) { this.notificationIds = notificationIds; }
    }
} 