package com.medicalcare.domain.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Column;
import org.seasar.doma.Version;

import java.time.LocalDateTime;

/**
 * 申請エンティティ
 */
@Entity(immutable = true)
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private final Long id;

    @Column(name = "application_number")
    private final String applicationNumber;

    @Column(name = "institution_id")
    private final Long institutionId;

    @Column(name = "application_type")
    private final String applicationType;

    @Column(name = "title")
    private final String title;

    @Column(name = "description")
    private final String description;

    @Column(name = "status")
    private final String status;

    @Column(name = "submitted_at")
    private final LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private final LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private final LocalDateTime rejectedAt;

    @Column(name = "rejection_reason")
    private final String rejectionReason;

    @Column(name = "created_at")
    private final LocalDateTime createdAt;

    @Column(name = "updated_at")
    private final LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private final Long version;

    public Application(
            Long id,
            String applicationNumber,
            Long institutionId,
            String applicationType,
            String title,
            String description,
            String status,
            LocalDateTime submittedAt,
            LocalDateTime approvedAt,
            LocalDateTime rejectedAt,
            String rejectionReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long version) {
        this.id = id;
        this.applicationNumber = applicationNumber;
        this.institutionId = institutionId;
        this.applicationType = applicationType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
        this.rejectedAt = rejectedAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    // Getters
    public Long getId() { return id; }
    public String getApplicationNumber() { return applicationNumber; }
    public Long getInstitutionId() { return institutionId; }
    public String getApplicationType() { return applicationType; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
} 