package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.MedicalInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalInstitutionDao extends JpaRepository<MedicalInstitution, Long> {
    
    // 機関コードによる検索
    Optional<MedicalInstitution> findByInstitutionCode(String institutionCode);
    
    // 機関名による検索
    List<MedicalInstitution> findByInstitutionNameContaining(String institutionName);
    
    // 機関種別による検索
    List<MedicalInstitution> findByInstitutionType(String institutionType);
    
    // 都道府県による検索
    List<MedicalInstitution> findByPrefecture(String prefecture);
    
    // 機関コードの存在確認
    boolean existsByInstitutionCode(String institutionCode);
    
    // 機関種別別の件数
    long countByInstitutionType(String institutionType);
    
    // 都道府県別の件数
    long countByPrefecture(String prefecture);
    
    // 複数条件での検索
    @Query("SELECT mi FROM MedicalInstitution mi WHERE mi.institutionType = :institutionType AND mi.prefecture = :prefecture")
    List<MedicalInstitution> findByInstitutionTypeAndPrefecture(@Param("institutionType") String institutionType, @Param("prefecture") String prefecture);
} 