package com.medicalcare.controller;

import com.medicalcare.domain.entity.Application;
import com.medicalcare.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;

    /**
     * 全申請取得
     */
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        try {
            List<Application> applications = applicationService.getAllApplications();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * IDによる申請取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        try {
            return applicationService.getApplicationById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請番号による申請取得
     */
    @GetMapping("/number/{applicationNumber}")
    public ResponseEntity<Application> getApplicationByNumber(@PathVariable String applicationNumber) {
        try {
            return applicationService.getApplicationByNumber(applicationNumber)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 医療機関IDによる申請一覧取得
     */
    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<Application>> getApplicationsByInstitutionId(@PathVariable Long institutionId) {
        try {
            List<Application> applications = applicationService.getApplicationsByInstitutionId(institutionId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ステータスによる申請一覧取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getApplicationsByStatus(@PathVariable String status) {
        try {
            List<Application> applications = applicationService.getApplicationsByStatus(status);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請タイプによる申請一覧取得
     */
    @GetMapping("/type/{applicationTypeId}")
    public ResponseEntity<List<Application>> getApplicationsByType(@PathVariable Long applicationTypeId) {
        try {
            List<Application> applications = applicationService.getApplicationsByType(applicationTypeId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請作成
     */
    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody ApplicationRequest request) {
        try {
            Application created = applicationService.createApplication(
                request.getApplicationNumber(),
                request.getMedicalInstitutionId(),
                request.getApplicationTypeId(),
                request.getTitle(),
                request.getDescription()
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id, @RequestBody ApplicationRequest request) {
        try {
            Application updated = applicationService.updateApplication(id, request.getTitle(), request.getDescription());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請提出
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Application> submitApplication(@PathVariable Long id) {
        try {
            Application submitted = applicationService.submitApplication(id);
            return ResponseEntity.ok(submitted);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請承認
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Application> approveApplication(@PathVariable Long id) {
        try {
            Application approved = applicationService.approveApplication(id);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 申請却下
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Application> rejectApplication(@PathVariable Long id, @RequestBody RejectionRequest request) {
        try {
            Application rejected = applicationService.rejectApplication(id, request.getRejectionReason());
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // リクエストクラス
    public static class ApplicationRequest {
        private String applicationNumber;
        private Long medicalInstitutionId;
        private Long applicationTypeId;
        private String title;
        private String description;

        // Getters and Setters
        public String getApplicationNumber() { return applicationNumber; }
        public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
        
        public Long getMedicalInstitutionId() { return medicalInstitutionId; }
        public void setMedicalInstitutionId(Long medicalInstitutionId) { this.medicalInstitutionId = medicalInstitutionId; }
        
        public Long getApplicationTypeId() { return applicationTypeId; }
        public void setApplicationTypeId(Long applicationTypeId) { this.applicationTypeId = applicationTypeId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class RejectionRequest {
        private String rejectionReason;

        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
} 