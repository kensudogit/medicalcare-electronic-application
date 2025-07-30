package com.medicalcare.service;

import com.medicalcare.domain.dao.ApplicationAttachmentDao;
import com.medicalcare.domain.entity.ApplicationAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileUploadService {
    
    @Autowired
    private ApplicationAttachmentDao attachmentDao;
    
    private static final String UPLOAD_DIR = "/app/uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx"};

    /**
     * ファイルアップロード
     */
    public ApplicationAttachment uploadFile(MultipartFile file, Long applicationId, String uploadType, 
                                          String description, Long uploadedByUserId) throws IOException {
        // ファイルサイズチェック
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("ファイルサイズが上限を超えています: " + file.getSize() + " bytes");
        }
        
        // ファイル拡張子チェック
        String originalFileName = file.getOriginalFilename();
        if (!isValidFileExtension(originalFileName)) {
            throw new RuntimeException("許可されていないファイル形式です: " + originalFileName);
        }
        
        // ファイル名の生成（UUID + 元の拡張子）
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
            applicationId, fileName, originalFileName, filePath.toString(), uploadType, uploadedByUserId
        );
        attachment.setDescription(description);
        attachment.setFileType(fileExtension);
        attachment.setFileSize(file.getSize());
        
        return attachmentDao.save(attachment);
    }

    /**
     * ファイル削除
     */
    public void deleteFile(Long attachmentId) {
        Optional<ApplicationAttachment> attachmentOpt = attachmentDao.findById(attachmentId);
        if (attachmentOpt.isPresent()) {
            ApplicationAttachment attachment = attachmentOpt.get();
            
            // 物理ファイルの削除
            try {
                Path filePath = Paths.get(attachment.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // ファイル削除エラーはログに記録するが、処理は続行
                System.err.println("ファイル削除エラー: " + e.getMessage());
            }
            
            // データベースから削除
            attachmentDao.delete(attachment);
        } else {
            throw new RuntimeException("添付ファイルが見つかりません: " + attachmentId);
        }
    }

    /**
     * ファイル検証
     */
    public ApplicationAttachment verifyFile(Long attachmentId, String verificationStatus, 
                                          String verificationComments, Long verifiedByUserId) {
        Optional<ApplicationAttachment> attachmentOpt = attachmentDao.findById(attachmentId);
        if (attachmentOpt.isPresent()) {
            ApplicationAttachment attachment = attachmentOpt.get();
            attachment.setVerificationStatus(verificationStatus);
            attachment.setVerificationComments(verificationComments);
            attachment.setVerifiedByUserId(verifiedByUserId);
            attachment.setVerifiedAt(LocalDateTime.now());
            attachment.setVerified("VERIFIED".equals(verificationStatus));
            attachment.setUpdatedAt(LocalDateTime.now());
            
            return attachmentDao.save(attachment);
        } else {
            throw new RuntimeException("添付ファイルが見つかりません: " + attachmentId);
        }
    }

    // 検索メソッド
    public List<ApplicationAttachment> getAttachmentsByApplicationId(Long applicationId) {
        return attachmentDao.findByApplicationId(applicationId);
    }

    public List<ApplicationAttachment> getAttachmentsByUploadType(String uploadType) {
        return attachmentDao.findByUploadType(uploadType);
    }

    public List<ApplicationAttachment> getAttachmentsByVerificationStatus(String verificationStatus) {
        return attachmentDao.findByVerificationStatus(verificationStatus);
    }

    public List<ApplicationAttachment> getAttachmentsByFileType(String fileType) {
        return attachmentDao.findByFileType(fileType);
    }

    // 統計メソッド
    public long getAttachmentCountByApplicationId(Long applicationId) {
        return attachmentDao.countByApplicationId(applicationId);
    }

    public long getAttachmentCountByVerificationStatus(String verificationStatus) {
        return attachmentDao.countByVerificationStatus(verificationStatus);
    }

    public long getAttachmentCountByFileType(String fileType) {
        return attachmentDao.countByFileType(fileType);
    }

    // ユーティリティメソッド
    private boolean isValidFileExtension(String fileName) {
        if (fileName == null) return false;
        String extension = getFileExtension(fileName).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }
} 