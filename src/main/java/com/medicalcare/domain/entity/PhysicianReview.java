package com.medicalcare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 医師によるAI所見確認・承認
 * AI結果は「候補」であり、承認後も確定診断テキストとしては扱わない。
 */
@Entity
@Table(name = "physician_reviews")
public class PhysicianReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medical_image_id", nullable = false)
    private Long medicalImageId;

    @Column(name = "analysis_result_id", nullable = false)
    private Long analysisResultId;

    @Column(name = "reviewer_user_id", nullable = false)
    private Long reviewerUserId;

    @Column(name = "reviewer_role", length = 40)
    private String reviewerRole;

    /**
     * PENDING / APPROVED_AS_CANDIDATE / REJECTED / NEEDS_SECOND_LOOK / FALSE_POSITIVE / MISSED_FINDING
     */
    @Column(name = "decision", nullable = false, length = 40)
    private String decision = "PENDING";

    @Column(name = "physician_comment", columnDefinition = "TEXT")
    private String physicianComment;

    @Column(name = "confirmed_findings_text", columnDefinition = "TEXT")
    private String confirmedFindingsText;

    @Column(name = "false_positive_notes", columnDefinition = "TEXT")
    private String falsePositiveNotes;

    @Column(name = "missed_finding_notes", columnDefinition = "TEXT")
    private String missedFindingNotes;

    @Column(name = "acknowledged_non_diagnostic", nullable = false)
    private Boolean acknowledgedNonDiagnostic = false;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PhysicianReview() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMedicalImageId() { return medicalImageId; }
    public void setMedicalImageId(Long medicalImageId) { this.medicalImageId = medicalImageId; }
    public Long getAnalysisResultId() { return analysisResultId; }
    public void setAnalysisResultId(Long analysisResultId) { this.analysisResultId = analysisResultId; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long reviewerUserId) { this.reviewerUserId = reviewerUserId; }
    public String getReviewerRole() { return reviewerRole; }
    public void setReviewerRole(String reviewerRole) { this.reviewerRole = reviewerRole; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getPhysicianComment() { return physicianComment; }
    public void setPhysicianComment(String physicianComment) { this.physicianComment = physicianComment; }
    public String getConfirmedFindingsText() { return confirmedFindingsText; }
    public void setConfirmedFindingsText(String confirmedFindingsText) { this.confirmedFindingsText = confirmedFindingsText; }
    public String getFalsePositiveNotes() { return falsePositiveNotes; }
    public void setFalsePositiveNotes(String falsePositiveNotes) { this.falsePositiveNotes = falsePositiveNotes; }
    public String getMissedFindingNotes() { return missedFindingNotes; }
    public void setMissedFindingNotes(String missedFindingNotes) { this.missedFindingNotes = missedFindingNotes; }
    public Boolean getAcknowledgedNonDiagnostic() { return acknowledgedNonDiagnostic; }
    public void setAcknowledgedNonDiagnostic(Boolean acknowledgedNonDiagnostic) { this.acknowledgedNonDiagnostic = acknowledgedNonDiagnostic; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
