package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.ImagingAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagingAuditLogDao extends JpaRepository<ImagingAuditLog, Long> {
    List<ImagingAuditLog> findByMedicalImageIdOrderByCreatedAtDesc(Long medicalImageId);
    List<ImagingAuditLog> findTop100ByOrderByCreatedAtDesc();
}
