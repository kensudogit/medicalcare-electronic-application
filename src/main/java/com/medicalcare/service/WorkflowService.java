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
     * ワークフロー作成（旧シグネチャ - 後方互換性のため保持）
     */
    public ApplicationWorkflow createWorkflow(Long applicationId, String currentStatus) {
        return createWorkflow(applicationId, currentStatus, null);
    }

    /**
     * ワークフロー作成（新シグネチャ）
     */
    public ApplicationWorkflow createWorkflow(Long applicationId, String currentStatus, Long assignedToUserId) {
        ApplicationWorkflow workflow = new ApplicationWorkflow(applicationId, currentStatus);
        workflow.setAssignedToUserId(assignedToUserId);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());

        return workflowDao.save(workflow);
    }

    /**
     * ステータス更新
     */
    public ApplicationWorkflow updateStatus(Long workflowId, String newStatus, Long changedByUserId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            workflow.setPreviousStatus(workflow.getCurrentStatus());
            workflow.setCurrentStatus(newStatus);
            workflow.setChangedByUserId(changedByUserId);
            workflow.setStatusChangedAt(LocalDateTime.now());
            workflow.setUpdatedAt(LocalDateTime.now());

            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * 担当者割り当て
     */
    public ApplicationWorkflow assignToUser(Long workflowId, Long assignedToUserId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            workflow.setAssignedToUserId(assignedToUserId);
            workflow.setUpdatedAt(LocalDateTime.now());

            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * コメント追加
     */
    public ApplicationWorkflow addComment(Long workflowId, String comment) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            String currentComments = workflow.getComments();
            String newComments = currentComments != null ? currentComments + "\n" + comment : comment;
            workflow.setComments(newComments);
            workflow.setUpdatedAt(LocalDateTime.now());

            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * 申請IDによるワークフロー取得
     */
    public List<ApplicationWorkflow> getWorkflowsByApplicationId(Long applicationId) {
        return workflowDao.findByApplicationId(applicationId);
    }

    /**
     * ステータスによるワークフロー取得
     */
    public List<ApplicationWorkflow> getWorkflowsByStatus(String currentStatus) {
        return workflowDao.findByCurrentStatus(currentStatus);
    }

    /**
     * 担当者によるワークフロー取得
     */
    public List<ApplicationWorkflow> getWorkflowsByAssignee(Long assignedToUserId) {
        return workflowDao.findByAssignedToUserId(assignedToUserId);
    }

    /**
     * ワークフローIDによる取得
     */
    public Optional<ApplicationWorkflow> getWorkflowById(Long workflowId) {
        return workflowDao.findById(workflowId);
    }

    /**
     * 全ワークフロー取得
     */
    public List<ApplicationWorkflow> getAllWorkflows() {
        return workflowDao.findAll();
    }

    /**
     * ワークフロー削除
     */
    public void deleteWorkflow(Long workflowId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            workflowDao.delete(workflowOpt.get());
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * ワークフローステータス更新（拡張版）
     */
    public ApplicationWorkflow updateWorkflowStatus(Long workflowId, String newStatus, String comments,
            Long changedByUserId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            workflow.setPreviousStatus(workflow.getCurrentStatus());
            workflow.setCurrentStatus(newStatus);
            workflow.setChangedByUserId(changedByUserId);
            workflow.setStatusChangedAt(LocalDateTime.now());
            if (comments != null) {
                String currentComments = workflow.getComments();
                String newComments = currentComments != null ? currentComments + "\n" + comments : comments;
                workflow.setComments(newComments);
            }
            workflow.setUpdatedAt(LocalDateTime.now());

            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * ワークフロー担当者変更（拡張版）
     */
    public ApplicationWorkflow reassignWorkflow(Long workflowId, Long newAssignedToUserId, String comments,
            Long changedByUserId) {
        Optional<ApplicationWorkflow> workflowOpt = workflowDao.findById(workflowId);
        if (workflowOpt.isPresent()) {
            ApplicationWorkflow workflow = workflowOpt.get();
            workflow.setAssignedToUserId(newAssignedToUserId);
            workflow.setChangedByUserId(changedByUserId);
            if (comments != null) {
                String currentComments = workflow.getComments();
                String newComments = currentComments != null ? currentComments + "\n" + comments : comments;
                workflow.setComments(newComments);
            }
            workflow.setUpdatedAt(LocalDateTime.now());

            return workflowDao.save(workflow);
        } else {
            throw new RuntimeException("ワークフローが見つかりません: " + workflowId);
        }
    }

    /**
     * 申請IDによるワークフロー取得（エイリアス）
     */
    public List<ApplicationWorkflow> getWorkflowByApplicationId(Long applicationId) {
        return getWorkflowsByApplicationId(applicationId);
    }

    /**
     * 担当者によるワークフロー取得（エイリアス）
     */
    public List<ApplicationWorkflow> getWorkflowsByAssignedUser(Long assignedToUserId) {
        return getWorkflowsByAssignee(assignedToUserId);
    }

    /**
     * ワークフロータイプによる取得
     */
    public List<ApplicationWorkflow> getWorkflowsByType(String workflowType) {
        // ワークフロータイプは現在のステータスとして扱う
        return getWorkflowsByStatus(workflowType);
    }

    /**
     * ステータス別ワークフロー数取得
     */
    public long getWorkflowCountByStatus(String status) {
        return getWorkflowsByStatus(status).size();
    }

    /**
     * 担当者別ワークフロー数取得
     */
    public long getWorkflowCountByAssignedUser(Long assignedToUserId) {
        return getWorkflowsByAssignedUser(assignedToUserId).size();
    }
}