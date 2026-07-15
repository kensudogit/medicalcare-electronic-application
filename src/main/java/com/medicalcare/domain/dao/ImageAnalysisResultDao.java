package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.ImageAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageAnalysisResultDao extends JpaRepository<ImageAnalysisResult, Long> {
    List<ImageAnalysisResult> findByMedicalImageIdOrderByCreatedAtDesc(Long medicalImageId);
    Optional<ImageAnalysisResult> findFirstByMedicalImageIdOrderByCreatedAtDesc(Long medicalImageId);
}
