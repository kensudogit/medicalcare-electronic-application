package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.PhysicianReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicianReviewDao extends JpaRepository<PhysicianReview, Long> {
    List<PhysicianReview> findByMedicalImageIdOrderByCreatedAtDesc(Long medicalImageId);
    Optional<PhysicianReview> findFirstByAnalysisResultIdOrderByCreatedAtDesc(Long analysisResultId);
    List<PhysicianReview> findByDecisionOrderByCreatedAtDesc(String decision);
}
