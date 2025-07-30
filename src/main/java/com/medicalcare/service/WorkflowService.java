package com.medicalcare.service;

import com.medicalcare.domain.dao.ApplicationWorkflowDao;
import com.medicalcare.domain.dao.NotificationDao;
import com.medicalcare.domain.entity.ApplicationWorkflow;
import com.medicalcare.domain.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkflowService {
    
    @Autowired
    private ApplicationWorkflowDao workflowDao;
    
    @Autowired
    private NotificationDao notificationDao;

    /**
     * ワークフロー作成
     */
    public ApplicationWorkflow createWorkflow(Long applicationId, String initialStatus, Long assignedToUserId) {
        ApplicationWorkflow workflow = new ApplicationWorkflow(applicationId, initialStatus);
        workflow.setAssignedToUserId(assignedToUserId);
        workflow.setStatusChangedAt(LocalDateTime.now());
        workflow.setCreatedAt(LocalDateTime.now());
        
        return workflowDao.save(workflow);
    }

    /**
     * ワークフローステータス更新
     */
    public ApplicationWorkflow updateWorkflowStatus(Long workflowId, String newStatus, String comments, Long changedByUserId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            workflow.setPreviousStatus(workflow.getCurrentStatus());
            workflow.setCurrentStatus(newStatus);
            workflow.setComments(comments);
            workflow.setChangedByUserId(changedByUserId);
            workflow.setStatusChangedAt(LocalDateTime.now());
            workflow.setUpdatedAt(LocalDateTime.now());
            
            // 通知を送信
            sendWorkflowStatusNotification(workflow, newStatus);
            
            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * ワークフロー担当者変更
     */
    public ApplicationWorkflow reassignWorkflow(Long workflowId, Long newAssignedToUserId, String comments, Long changedByUserId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            Long oldAssignedToUserId = workflow.getAssignedToUserId();
            workflow.setAssignedToUserId(newAssignedToUserId);
            workflow.setComments(comments);
            workflow.setChangedByUserId(changedByUserId);
            workflow.setStatusChangedAt(LocalDateTime.now());
            workflow.setUpdatedAt(LocalDateTime.now());
            
            // 通知を送信
            sendWorkflowReassignmentNotification(workflow, oldAssignedToUserId);
            
            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    // 検索メソッド
    public List<ApplicationWorkflow> getWorkflowByApplicationId(Long applicationId) {
        return workflowDao.findByApplicationId(applicationId);
    }

    public List<ApplicationWorkflow> getWorkflowsByStatus(String status) {
        return workflowDao.findByCurrentStatus(status);
    }

    public List<ApplicationWorkflow> getWorkflowsByAssignedUser(Long assignedToUserId) {
        return workflowDao.findByAssignedToUserId(assignedToUserId);
    }

    public List<ApplicationWorkflow> getWorkflowsByType(String workflowType) {
        return workflowDao.findByWorkflowType(workflowType);
    }

    // 統計メソッド
    public long getWorkflowCountByStatus(String status) {
        return workflowDao.countByCurrentStatus(status);
    }

    public long getWorkflowCountByAssignedUser(Long assignedToUserId) {
        return workflowDao.countByAssignedToUserId(assignedToUserId);
    }

    // プライベートメソッド
    private void sendWorkflowStatusNotification(ApplicationWorkflow workflow, String newStatus) {
        String message = String.format("申請ID %d のステータスが %s に変更されました", 
                                     workflow.getApplicationId(), newStatus);
        
        Notification notification = new Notification(
            workflow.getAssignedToUserId(), 
            "ワークフロー通知", 
            message, 
            "APPLICATION_STATUS",
            "MEDIUM"
        );
        notification.setRelatedEntityId(workflow.getApplicationId());
        notification.setRelatedEntityType("APPLICATION");
        
        notificationDao.save(notification);
    }

    private void sendWorkflowReassignmentNotification(ApplicationWorkflow workflow, Long oldAssignedToUserId) {
        String message = String.format("申請ID %d の担当者が変更されました", workflow.getApplicationId());
        
        Notification notification = new Notification(
            workflow.getAssignedToUserId(),
            "ワークフロー担当変更通知",
            message,
            "APPLICATION_STATUS",
            "HIGH"
        );
        notification.setRelatedEntityId(workflow.getApplicationId());
        notification.setRelatedEntityType("APPLICATION");
        
        notificationDao.save(notification);
    }
} 