package com.medicalcare.service;

import com.medicalcare.domain.dao.ApplicationAttachmentDao;
import com.medicalcare.domain.entity.ApplicationAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileUploadService {

    @Autowired
    private ApplicationAttachmentDao attachmentDao;

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * ファイルアップロード（旧シグネチャ - 後方互換性のため保持）
     */
    public ApplicationAttachment uploadFile(Long applicationId, MultipartFile file, Long uploadedByUserId)
            throws IOException {
        return uploadFile(file, applicationId, "MANUAL", null, uploadedByUserId);
    }

    /**
     * ファイルアップロード（新シグネチャ）
     */
    public ApplicationAttachment uploadFile(MultipartFile file, Long applicationId, String uploadType,
            String description, Long uploadedByUserId) throws IOException {
        // ファイル名の生成
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // アップロードディレクトリの作成
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // ファイルの保存
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // データベースに記録
        ApplicationAttachment attachment = new ApplicationAttachment(
                applicationId, fileName, originalFileName, filePath.toString(), file.getSize(), file.getContentType());
        attachment.setUploadedBy(uploadedByUserId);
        attachment.setDescription(description);
        attachment.setUploadedAt(LocalDateTime.now());

        return attachmentDao.save(attachment);
    }

    /**
     * ファイル削除
     */
    public void deleteFile(Long attachmentId) throws IOException {
        Optional<ApplicationAttachment> attachmentOpt = attachmentDao.findById(attachmentId);
        if (attachmentOpt.isPresent()) {
            ApplicationAttachment attachment = attachmentOpt.get();

            // ファイルシステムから削除
            Path filePath = Paths.get(attachment.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // データベースから削除
            attachmentDao.delete(attachment);
        } else {
            throw new RuntimeException("ファイルが見つかりません: " + attachmentId);
        }
    }

    /**
     * 申請IDによるファイル取得
     */
    public List<ApplicationAttachment> getFilesByApplicationId(Long applicationId) {
        return attachmentDao.findByApplicationId(applicationId);
    }

    /**
     * ファイルIDによる取得
     */
    public Optional<ApplicationAttachment> getFileById(Long attachmentId) {
        return attachmentDao.findById(attachmentId);
    }

    /**
     * ファイル名による検索
     */
    public List<ApplicationAttachment> searchFilesByFileName(String fileName) {
        return attachmentDao.findByFileNameContaining(fileName);
    }

    /**
     * コンテンツタイプによる検索
     */
    public List<ApplicationAttachment> getFilesByContentType(String contentType) {
        return attachmentDao.findByApplicationIdAndContentType(null, contentType);
    }

    /**
     * ファイルサイズによる検索（指定サイズ以上）
     */
    public List<ApplicationAttachment> getFilesBySizeGreaterThan(Long fileSize) {
        return attachmentDao.findByFileSizeGreaterThan(fileSize);
    }

    /**
     * 全ファイル取得
     */
    public List<ApplicationAttachment> getAllFiles() {
        return attachmentDao.findAll();
    }

    /**
     * 申請IDによる添付ファイル取得（エイリアス）
     */
    public List<ApplicationAttachment> getAttachmentsByApplicationId(Long applicationId) {
        return getFilesByApplicationId(applicationId);
    }

    /**
     * アップロードタイプによる添付ファイル取得
     */
    public List<ApplicationAttachment> getAttachmentsByUploadType(String uploadType) {
        List<ApplicationAttachment> allAttachments = attachmentDao.findAll();
        return allAttachments.stream()
                .filter(att -> att.getDescription() != null && att.getDescription().contains("uploadType:" + uploadType))
                .collect(Collectors.toList());
    }

    /**
     * 検証ステータスによる添付ファイル取得
     */
    public List<ApplicationAttachment> getAttachmentsByVerificationStatus(String verificationStatus) {
        List<ApplicationAttachment> allAttachments = attachmentDao.findAll();
        return allAttachments.stream()
                .filter(att -> att.getDescription() != null && 
                        att.getDescription().contains("verificationStatus:" + verificationStatus))
                .collect(Collectors.toList());
    }

    /**
     * ファイルタイプによる添付ファイル取得
     */
    public List<ApplicationAttachment> getAttachmentsByFileType(String fileType) {
        return getFilesByContentType(fileType);
    }

    /**
     * 申請IDによる添付ファイル数取得
     */
    public long getAttachmentCountByApplicationId(Long applicationId) {
        return getFilesByApplicationId(applicationId).size();
    }

    /**
     * 検証ステータスによる添付ファイル数取得
     */
    public long getAttachmentCountByVerificationStatus(String verificationStatus) {
        return getAttachmentsByVerificationStatus(verificationStatus).size();
    }

    /**
     * ファイルタイプによる添付ファイル数取得
     */
    public long getAttachmentCountByFileType(String fileType) {
        return getAttachmentsByFileType(fileType).size();
    }

    /**
     * ファイル検証
     */
    public ApplicationAttachment verifyFile(Long attachmentId, String verificationStatus, String comments,
            Long verifiedByUserId) {
        Optional<ApplicationAttachment> attachmentOpt = attachmentDao.findById(attachmentId);
        if (attachmentOpt.isPresent()) {
            ApplicationAttachment attachment = attachmentOpt.get();
            String currentDesc = attachment.getDescription() != null ? attachment.getDescription() : "";
            String newDesc = currentDesc + "\n[検証] ステータス: " + verificationStatus + ", コメント: " + comments +
                    ", 検証者ID: " + verifiedByUserId;
            attachment.setDescription(newDesc);
            return attachmentDao.save(attachment);
        } else {
            throw new RuntimeException("ファイルが見つかりません: " + attachmentId);
        }
    }

    /**
     * ファイル拡張子を取得
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}