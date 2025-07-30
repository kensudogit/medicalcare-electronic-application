package com.medicalcare.controller;

import com.medicalcare.domain.entity.ApplicationWorkflow;
import com.medicalcare.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;

    /**
     * ワークフロー作成
     */
    @PostMapping
    public ResponseEntity<ApplicationWorkflow> createWorkflow(@RequestBody WorkflowRequest request) {
        try {
            ApplicationWorkflow workflow = workflowService.createWorkflow(
                request.getApplicationId(),
                request.getInitialStatus(),
                request.getAssignedToUserId()
            );
            return ResponseEntity.ok(workflow);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ワークフローステータス更新
     */
    @PutMapping("/{workflowId}/status")
    public ResponseEntity<ApplicationWorkflow> updateWorkflowStatus(
            @PathVariable Long workflowId,
            @RequestBody StatusUpdateRequest request) {
        try {
            ApplicationWorkflow workflow = workflowService.updateWorkflowStatus(
                workflowId,
                request.getNewStatus(),
                request.getComments(),
                request.getChangedByUserId()
            );
            return ResponseEntity.ok(workflow);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ワークフロー担当者変更
     */
    @PutMapping("/{workflowId}/reassign")
    public ResponseEntity<ApplicationWorkflow> reassignWorkflow(
            @PathVariable Long workflowId,
            @RequestBody ReassignmentRequest request) {
        try {
            ApplicationWorkflow workflow = workflowService.reassignWorkflow(
                workflowId,
                request.getNewAssignedToUserId(),
                request.getComments(),
                request.getChangedByUserId()
            );
            return ResponseEntity.ok(workflow);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請IDによるワークフロー取得
     */
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<ApplicationWorkflow>> getWorkflowsByApplicationId(@PathVariable Long applicationId) {
        try {
            List<ApplicationWorkflow> workflows = workflowService.getWorkflowByApplicationId(applicationId);
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ステータスによるワークフロー一覧取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApplicationWorkflow>> getWorkflowsByStatus(@PathVariable String status) {
        try {
            List<ApplicationWorkflow> workflows = workflowService.getWorkflowsByStatus(status);
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 担当者によるワークフロー一覧取得
     */
    @GetMapping("/assigned/{assignedToUserId}")
    public ResponseEntity<List<ApplicationWorkflow>> getWorkflowsByAssignedUser(@PathVariable Long assignedToUserId) {
        try {
            List<ApplicationWorkflow> workflows = workflowService.getWorkflowsByAssignedUser(assignedToUserId);
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ワークフロータイプによる一覧取得
     */
    @GetMapping("/type/{workflowType}")
    public ResponseEntity<List<ApplicationWorkflow>> getWorkflowsByType(@PathVariable String workflowType) {
        try {
            List<ApplicationWorkflow> workflows = workflowService.getWorkflowsByType(workflowType);
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ステータス別ワークフロー統計取得
     */
    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> getWorkflowCountByStatus(@PathVariable String status) {
        try {
            long count = workflowService.getWorkflowCountByStatus(status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 担当者別ワークフロー統計取得
     */
    @GetMapping("/stats/assigned/{assignedToUserId}")
    public ResponseEntity<Long> getWorkflowCountByAssignedUser(@PathVariable Long assignedToUserId) {
        try {
            long count = workflowService.getWorkflowCountByAssignedUser(assignedToUserId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // リクエストクラス
    public static class WorkflowRequest {
        private Long applicationId;
        private String initialStatus;
        private Long assignedToUserId;

        // Getters and Setters
        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        
        public String getInitialStatus() { return initialStatus; }
        public void setInitialStatus(String initialStatus) { this.initialStatus = initialStatus; }
        
        public Long getAssignedToUserId() { return assignedToUserId; }
        public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }
    }

    public static class StatusUpdateRequest {
        private String newStatus;
        private String comments;
        private Long changedByUserId;

        // Getters and Setters
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        
        public Long getChangedByUserId() { return changedByUserId; }
        public void setChangedByUserId(Long changedByUserId) { this.changedByUserId = changedByUserId; }
    }

    public static class ReassignmentRequest {
        private Long newAssignedToUserId;
        private String comments;
        private Long changedByUserId;

        // Getters and Setters
        public Long getNewAssignedToUserId() { return newAssignedToUserId; }
        public void setNewAssignedToUserId(Long newAssignedToUserId) { this.newAssignedToUserId = newAssignedToUserId; }
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        
        public Long getChangedByUserId() { return changedByUserId; }
        public void setChangedByUserId(Long changedByUserId) { this.changedByUserId = changedByUserId; }
    }
} 