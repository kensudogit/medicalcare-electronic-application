package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.ApplicationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationAttachmentDao extends JpaRepository<ApplicationAttachment, Long> {
    
    // 申請IDによる検索
    List<ApplicationAttachment> findByApplicationId(Long applicationId);
    
    // アップロードタイプによる検索
    List<ApplicationAttachment> findByUploadType(String uploadType);
    
    // アップロード者IDによる検索
    List<ApplicationAttachment> findByUploadedByUserId(Long uploadedByUserId);
    
    // 検証ステータスによる検索
    List<ApplicationAttachment> findByVerificationStatus(String verificationStatus);
    
    // ファイルタイプによる検索
    List<ApplicationAttachment> findByFileType(String fileType);
    
    // 申請IDとアップロードタイプによる検索
    @Query("SELECT aa FROM ApplicationAttachment aa WHERE aa.applicationId = :applicationId AND aa.uploadType = :uploadType")
    List<ApplicationAttachment> findByApplicationIdAndUploadType(@Param("applicationId") Long applicationId, @Param("uploadType") String uploadType);
    
    // 申請ID別の件数
    long countByApplicationId(Long applicationId);
    
    // 検証ステータス別の件数
    long countByVerificationStatus(String verificationStatus);
    
    // ファイルタイプ別の件数
    long countByFileType(String fileType);
    
    // 最近の添付ファイルを取得
    @Query("SELECT aa FROM ApplicationAttachment aa ORDER BY aa.uploadedAt DESC")
    List<ApplicationAttachment> findRecentAttachments();
} 