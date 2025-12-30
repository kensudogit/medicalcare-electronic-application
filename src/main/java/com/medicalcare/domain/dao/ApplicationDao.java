package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 申請データアクセスオブジェクト
 */
@Repository
public interface ApplicationDao extends JpaRepository<Application, Long> {

    /**
     * 申請番号による検索
     */
    Optional<Application> findByApplicationNumber(String applicationNumber);

    /**
     * 医療機関IDによる検索
     */
    List<Application> findByMedicalInstitutionId(Long medicalInstitutionId);

    /**
     * ステータスによる検索
     */
    List<Application> findByStatus(String status);

    /**
     * 申請タイプIDによる検索
     */
    List<Application> findByApplicationTypeId(Long applicationTypeId);

    /**
     * 申請番号の存在確認
     */
    boolean existsByApplicationNumber(String applicationNumber);
}