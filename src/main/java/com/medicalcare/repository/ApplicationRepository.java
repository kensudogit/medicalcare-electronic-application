package com.medicalcare.repository;

import com.medicalcare.domain.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 申請リポジトリ
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 医療機関IDで申請を検索
     */
    List<Application> findByMedicalInstitutionId(Long medicalInstitutionId);

    /**
     * ステータスで申請を検索
     */
    List<Application> findByStatus(String status);

    /**
     * 申請タイプIDで申請を検索
     */
    List<Application> findByApplicationTypeId(Long applicationTypeId);
} 