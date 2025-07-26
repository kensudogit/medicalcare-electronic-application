package com.medicalcare.service;

import com.medicalcare.domain.dao.ApplicationDao;
import com.medicalcare.domain.entity.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 申請サービス
 */
@Service
@Transactional
public class ApplicationService {

    private final ApplicationDao applicationDao;

    public ApplicationService(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    /**
     * 全申請を取得
     */
    @Transactional(readOnly = true)
    public List<Application> findAll() {
        return applicationDao.selectAll();
    }

    /**
     * IDで申請を取得
     */
    @Transactional(readOnly = true)
    public Optional<Application> findById(Long id) {
        return applicationDao.selectById(id);
    }

    /**
     * 申請番号で申請を取得
     */
    @Transactional(readOnly = true)
    public Optional<Application> findByApplicationNumber(String applicationNumber) {
        return applicationDao.selectByApplicationNumber(applicationNumber);
    }

    /**
     * 医療機関IDで申請を取得
     */
    @Transactional(readOnly = true)
    public List<Application> findByInstitutionId(Long institutionId) {
        return applicationDao.selectByInstitutionId(institutionId);
    }

    /**
     * ステータスで申請を取得
     */
    @Transactional(readOnly = true)
    public List<Application> findByStatus(String status) {
        return applicationDao.selectByStatus(status);
    }

    /**
     * 申請タイプで申請を取得
     */
    @Transactional(readOnly = true)
    public List<Application> findByApplicationType(String applicationType) {
        return applicationDao.selectByApplicationType(applicationType);
    }

    /**
     * 申請を登録
     */
    public Application create(Application application) {
        LocalDateTime now = LocalDateTime.now();
        String applicationNumber = generateApplicationNumber();
        
        Application newApplication = new Application(
                null,
                applicationNumber,
                application.getInstitutionId(),
                application.getApplicationType(),
                application.getTitle(),
                application.getDescription(),
                "DRAFT",
                null,
                null,
                null,
                null,
                now,
                now,
                1L
        );
        
        applicationDao.insert(newApplication);
        return newApplication;
    }

    /**
     * 申請を更新
     */
    public Application update(Long id, Application application) {
        Optional<Application> existing = applicationDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Application not found: " + id);
        }

        Application current = existing.get();
        Application updated = new Application(
                current.getId(),
                current.getApplicationNumber(),
                application.getInstitutionId(),
                application.getApplicationType(),
                application.getTitle(),
                application.getDescription(),
                application.getStatus(),
                application.getSubmittedAt(),
                application.getApprovedAt(),
                application.getRejectedAt(),
                application.getRejectionReason(),
                current.getCreatedAt(),
                LocalDateTime.now(),
                current.getVersion() + 1
        );

        applicationDao.update(updated);
        return updated;
    }

    /**
     * 申請を削除
     */
    public void delete(Long id) {
        Optional<Application> existing = applicationDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Application not found: " + id);
        }

        applicationDao.delete(existing.get());
    }

    /**
     * 申請を提出
     */
    public Application submit(Long id) {
        Optional<Application> existing = applicationDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Application not found: " + id);
        }

        Application current = existing.get();
        if (!"DRAFT".equals(current.getStatus())) {
            throw new RuntimeException("Application is not in DRAFT status");
        }

        Application submitted = new Application(
                current.getId(),
                current.getApplicationNumber(),
                current.getInstitutionId(),
                current.getApplicationType(),
                current.getTitle(),
                current.getDescription(),
                "SUBMITTED",
                LocalDateTime.now(),
                null,
                null,
                null,
                current.getCreatedAt(),
                LocalDateTime.now(),
                current.getVersion() + 1
        );

        applicationDao.update(submitted);
        return submitted;
    }

    /**
     * 申請を承認
     */
    public Application approve(Long id) {
        Optional<Application> existing = applicationDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Application not found: " + id);
        }

        Application current = existing.get();
        if (!"SUBMITTED".equals(current.getStatus())) {
            throw new RuntimeException("Application is not in SUBMITTED status");
        }

        Application approved = new Application(
                current.getId(),
                current.getApplicationNumber(),
                current.getInstitutionId(),
                current.getApplicationType(),
                current.getTitle(),
                current.getDescription(),
                "APPROVED",
                current.getSubmittedAt(),
                LocalDateTime.now(),
                null,
                null,
                current.getCreatedAt(),
                LocalDateTime.now(),
                current.getVersion() + 1
        );

        applicationDao.update(approved);
        return approved;
    }

    /**
     * 申請を却下
     */
    public Application reject(Long id, String rejectionReason) {
        Optional<Application> existing = applicationDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Application not found: " + id);
        }

        Application current = existing.get();
        if (!"SUBMITTED".equals(current.getStatus())) {
            throw new RuntimeException("Application is not in SUBMITTED status");
        }

        Application rejected = new Application(
                current.getId(),
                current.getApplicationNumber(),
                current.getInstitutionId(),
                current.getApplicationType(),
                current.getTitle(),
                current.getDescription(),
                "REJECTED",
                current.getSubmittedAt(),
                null,
                LocalDateTime.now(),
                rejectionReason,
                current.getCreatedAt(),
                LocalDateTime.now(),
                current.getVersion() + 1
        );

        applicationDao.update(rejected);
        return rejected;
    }

    /**
     * 申請番号を生成
     */
    private String generateApplicationNumber() {
        return "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 