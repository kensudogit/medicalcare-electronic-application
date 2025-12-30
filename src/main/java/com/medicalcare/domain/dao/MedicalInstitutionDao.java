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
     * 医療機関番号による検索
     */
    Optional<MedicalInstitution> findByInstitutionNumber(String institutionNumber);

    /**
     * 医療機関名による検索（部分一致）
     */
    List<MedicalInstitution> findByNameContaining(String name);

    /**
     * 都道府県による検索
     */
    List<MedicalInstitution> findByPrefecture(String prefecture);

    /**
     * 医療機関番号の存在確認
     */
    boolean existsByInstitutionNumber(String institutionNumber);
}