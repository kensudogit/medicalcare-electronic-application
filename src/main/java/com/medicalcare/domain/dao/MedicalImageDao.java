package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.MedicalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalImageDao extends JpaRepository<MedicalImage, Long> {
    List<MedicalImage> findByMedicalInstitutionIdOrderByCreatedAtDesc(Long medicalInstitutionId);
    List<MedicalImage> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    List<MedicalImage> findByModalityOrderByCreatedAtDesc(String modality);
    List<MedicalImage> findByStatusOrderByCreatedAtDesc(String status);
    List<MedicalImage> findByPatientIdOrderByCreatedAtDesc(String patientId);
}
