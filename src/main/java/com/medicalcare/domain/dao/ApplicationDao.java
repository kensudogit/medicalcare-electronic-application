package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationDao extends JpaRepository<Application, Long> {
    
    // 申請番号による検索
    Optional<Application> findByApplicationNumber(String applicationNumber);
    
    // 医療機関IDによる検索
    List<Application> findByMedicalInstitutionId(Long medicalInstitutionId);
    
    // 申請タイプによる検索
    List<Application> findByApplicationTypeId(Long applicationTypeId);
    
    // ステータスによる検索
    List<Application> findByStatus(String status);
    
    // 提出日時範囲による検索
    List<Application> findBySubmittedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 申請番号の存在確認
    boolean existsByApplicationNumber(String applicationNumber);
    
    // ステータス別の件数
    long countByStatus(String status);
    
    // 申請タイプ別の件数
    long countByApplicationTypeId(Long applicationTypeId);
    
    // 医療機関IDとステータスによる検索
    @Query("SELECT a FROM Application a WHERE a.medicalInstitutionId = :medicalInstitutionId AND a.status = :status")
    List<Application> findByMedicalInstitutionIdAndStatus(@Param("medicalInstitutionId") Long medicalInstitutionId, @Param("status") String status);
    
    // 最近の申請を取得
    @Query("SELECT a FROM Application a ORDER BY a.createdAt DESC")
    List<Application> findRecentApplications();
} 