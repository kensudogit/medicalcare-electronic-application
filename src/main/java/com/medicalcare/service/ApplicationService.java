package com.medicalcare.service;

import com.medicalcare.domain.dao.ApplicationDao;
import com.medicalcare.domain.entity.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationDao applicationDao;

    /**
     * 全申請取得
     */
    public List<Application> getAllApplications() {
        return applicationDao.findAll();
    }

    /**
     * IDによる申請取得
     */
    public Optional<Application> getApplicationById(Long id) {
        return applicationDao.findById(id);
    }

    /**
     * 申請番号による申請取得
     */
    public Optional<Application> getApplicationByNumber(String applicationNumber) {
        return applicationDao.findByApplicationNumber(applicationNumber);
    }

    /**
     * 医療機関IDによる申請一覧取得
     */
    public List<Application> getApplicationsByInstitutionId(Long institutionId) {
        return applicationDao.findByMedicalInstitutionId(institutionId);
    }

    /**
     * ステータスによる申請一覧取得
     */
    public List<Application> getApplicationsByStatus(String status) {
        return applicationDao.findByStatus(status);
    }

    /**
     * 申請タイプによる申請一覧取得
     */
    public List<Application> getApplicationsByType(Long applicationTypeId) {
        return applicationDao.findByApplicationTypeId(applicationTypeId);
    }

    /**
     * 申請作成
     */
    public Application createApplication(String applicationNumber, Long medicalInstitutionId, 
                                       Long applicationTypeId, String title, String description) {
        // 申請番号の重複チェック
        if (applicationDao.existsByApplicationNumber(applicationNumber)) {
            throw new RuntimeException("申請番号が既に使用されています: " + applicationNumber);
        }
        
        Application newApplication = new Application(applicationNumber, medicalInstitutionId, applicationTypeId, title);
        newApplication.setDescription(description);
        newApplication.setStatus("DRAFT");
        newApplication.setCreatedAt(LocalDateTime.now());
        
        return applicationDao.save(newApplication);
    }

    /**
     * 申請更新
     */
    public Application updateApplication(Long id, String title, String description) {
        Optional<Application> existing = applicationDao.findById(id);
        if (existing.isPresent()) {
            Application current = existing.get();
            
            Application updated = new Application(
                current.getApplicationNumber(),
                current.getMedicalInstitutionId(),
                current.getApplicationTypeId(),
                title
            );
            updated.setId(current.getId());
            updated.setDescription(description);
            updated.setStatus(current.getStatus());
            updated.setSubmittedAt(current.getSubmittedAt());
            updated.setApprovedAt(current.getApprovedAt());
            updated.setRejectedAt(current.getRejectedAt());
            updated.setRejectionReason(current.getRejectionReason());
            updated.setCreatedAt(current.getCreatedAt());
            updated.setUpdatedAt(LocalDateTime.now());
            
            return applicationDao.save(updated);
        } else {
            throw new RuntimeException("申請が見つかりません: " + id);
        }
    }

    /**
     * 申請削除
     */
    public void deleteApplication(Long id) {
        Optional<Application> existing = applicationDao.findById(id);
        if (existing.isPresent()) {
            applicationDao.delete(existing.get());
        } else {
            throw new RuntimeException("申請が見つかりません: " + id);
        }
    }

    /**
     * 申請提出
     */
    public Application submitApplication(Long id) {
        Optional<Application> existing = applicationDao.findById(id);
        if (existing.isPresent()) {
            Application current = existing.get();
            
            Application submitted = new Application(
                current.getApplicationNumber(),
                current.getMedicalInstitutionId(),
                current.getApplicationTypeId(),
                current.getTitle()
            );
            submitted.setId(current.getId());
            submitted.setDescription(current.getDescription());
            submitted.setStatus("SUBMITTED");
            submitted.setSubmittedAt(LocalDateTime.now());
            submitted.setApprovedAt(current.getApprovedAt());
            submitted.setRejectedAt(current.getRejectedAt());
            submitted.setRejectionReason(current.getRejectionReason());
            submitted.setCreatedAt(current.getCreatedAt());
            submitted.setUpdatedAt(LocalDateTime.now());
            
            return applicationDao.save(submitted);
        } else {
            throw new RuntimeException("申請が見つかりません: " + id);
        }
    }

    /**
     * 申請承認
     */
    public Application approveApplication(Long id) {
        Optional<Application> existing = applicationDao.findById(id);
        if (existing.isPresent()) {
            Application current = existing.get();
            
            Application approved = new Application(
                current.getApplicationNumber(),
                current.getMedicalInstitutionId(),
                current.getApplicationTypeId(),
                current.getTitle()
            );
            approved.setId(current.getId());
            approved.setDescription(current.getDescription());
            approved.setStatus("APPROVED");
            approved.setSubmittedAt(current.getSubmittedAt());
            approved.setApprovedAt(LocalDateTime.now());
            approved.setRejectedAt(current.getRejectedAt());
            approved.setRejectionReason(current.getRejectionReason());
            approved.setCreatedAt(current.getCreatedAt());
            approved.setUpdatedAt(LocalDateTime.now());
            
            return applicationDao.save(approved);
        } else {
            throw new RuntimeException("申請が見つかりません: " + id);
        }
    }

    /**
     * 申請却下
     */
    public Application rejectApplication(Long id, String rejectionReason) {
        Optional<Application> existing = applicationDao.findById(id);
        if (existing.isPresent()) {
            Application current = existing.get();
            
            Application rejected = new Application(
                current.getApplicationNumber(),
                current.getMedicalInstitutionId(),
                current.getApplicationTypeId(),
                current.getTitle()
            );
            rejected.setId(current.getId());
            rejected.setDescription(current.getDescription());
            rejected.setStatus("REJECTED");
            rejected.setSubmittedAt(current.getSubmittedAt());
            rejected.setApprovedAt(current.getApprovedAt());
            rejected.setRejectedAt(LocalDateTime.now());
            rejected.setRejectionReason(rejectionReason);
            rejected.setCreatedAt(current.getCreatedAt());
            rejected.setUpdatedAt(LocalDateTime.now());
            
            return applicationDao.save(rejected);
        } else {
            throw new RuntimeException("申請が見つかりません: " + id);
        }
    }
} 