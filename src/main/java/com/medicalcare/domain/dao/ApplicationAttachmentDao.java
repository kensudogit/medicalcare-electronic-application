package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.ApplicationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 申請添付ファイルデータアクセスオブジェクト
 */
@Repository
public interface ApplicationAttachmentDao extends JpaRepository<ApplicationAttachment, Long> {

    /**
     * 申請IDによる検索
     */
    List<ApplicationAttachment> findByApplicationId(Long applicationId);

    /**
     * ファイル名による検索（部分一致）
     */
    List<ApplicationAttachment> findByFileNameContaining(String fileName);

    /**
     * 申請IDとコンテンツタイプによる検索
     */
    List<ApplicationAttachment> findByApplicationIdAndContentType(Long applicationId, String contentType);

    /**
     * ファイルサイズによる検索（指定サイズ以上）
     */
    List<ApplicationAttachment> findByFileSizeGreaterThan(Long fileSize);
}