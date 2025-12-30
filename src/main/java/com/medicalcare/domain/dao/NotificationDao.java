package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知データアクセスオブジェクト
 */
@Repository
public interface NotificationDao extends JpaRepository<Notification, Long> {

    /**
     * ユーザーIDによる検索
     */
    List<Notification> findByUserId(Long userId);

    /**
     * ユーザーIDと未読状態による検索
     */
    List<Notification> findByUserIdAndReadFalse(Long userId);

    /**
     * タイプによる検索
     */
    List<Notification> findByType(String type);

    /**
     * ユーザーIDとステータスによる検索
     */
    List<Notification> findByUserIdAndStatus(Long userId, String status);
}