package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.ApplicationWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 申請ワークフローデータアクセスオブジェクト
 */
@Repository
public interface ApplicationWorkflowDao extends JpaRepository<ApplicationWorkflow, Long> {

    /**
     * 申請IDによる検索
     */
    List<ApplicationWorkflow> findByApplicationId(Long applicationId);

    /**
     * 現在のステータスによる検索
     */
    List<ApplicationWorkflow> findByCurrentStatus(String currentStatus);

    /**
     * 申請IDと現在のステータスによる検索
     */
    List<ApplicationWorkflow> findByApplicationIdAndCurrentStatus(Long applicationId, String currentStatus);

    /**
     * 担当者IDによる検索
     */
    List<ApplicationWorkflow> findByAssignedToUserId(Long assignedToUserId);
}