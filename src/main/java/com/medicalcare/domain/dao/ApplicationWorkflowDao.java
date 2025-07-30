package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.ApplicationWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationWorkflowDao extends JpaRepository<ApplicationWorkflow, Long> {
    
    // 申請IDによる検索
    List<ApplicationWorkflow> findByApplicationId(Long applicationId);
    
    // ステータスによる検索
    List<ApplicationWorkflow> findByCurrentStatus(String currentStatus);
    
    // 担当者IDによる検索
    List<ApplicationWorkflow> findByAssignedToUserId(Long assignedToUserId);
    
    // ワークフロータイプによる検索
    List<ApplicationWorkflow> findByWorkflowType(String workflowType);
    
    // 申請IDとステータスによる検索
    @Query("SELECT aw FROM ApplicationWorkflow aw WHERE aw.applicationId = :applicationId AND aw.currentStatus = :currentStatus")
    List<ApplicationWorkflow> findByApplicationIdAndCurrentStatus(@Param("applicationId") Long applicationId, @Param("currentStatus") String currentStatus);
    
    // ステータス別の件数
    long countByCurrentStatus(String currentStatus);
    
    // 担当者別の件数
    long countByAssignedToUserId(Long assignedToUserId);
    
    // ワークフロータイプ別の件数
    long countByWorkflowType(String workflowType);
    
    // 最近のワークフローを取得
    @Query("SELECT aw FROM ApplicationWorkflow aw ORDER BY aw.createdAt DESC")
    List<ApplicationWorkflow> findRecentWorkflows();
} 