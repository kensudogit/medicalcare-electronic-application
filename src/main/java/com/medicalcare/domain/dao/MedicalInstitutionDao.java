package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.MedicalInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 医療機関データアクセスオブジェクト
 */
@Repository
public interface MedicalInstitutionDao extends JpaRepository<MedicalInstitution, Long> {

    /**
     * 医療機関コードによる検索
     */
    Optional<MedicalInstitution> findByInstitutionCode(String institutionCode);

    /**
     * 医療機関名による検索（部分一致）
     */
    List<MedicalInstitution> findByInstitutionNameContaining(String name);

    /**
     * 医療機関コードの存在確認
     */
    boolean existsByInstitutionCode(String institutionCode);
}