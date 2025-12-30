package com.medicalcare.controller;

import com.medicalcare.domain.entity.ApplicationAttachment;
import com.medicalcare.service.FileUploadService;
import com.medicalcare.service.AuditService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    private final AuditService auditService;
    
    public FileUploadController(FileUploadService fileUploadService, AuditService auditService) {
        this.fileUploadService = fileUploadService;
        this.auditService = auditService;
    }
    
    /**
     * ファイルアップロード
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                       @RequestParam("applicationId") Long applicationId,
                                       @RequestParam("uploadType") String uploadType,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam("uploadedByUserId") Long uploadedByUserId) {
        try {
            ApplicationAttachment attachment = fileUploadService.uploadFile(
                file, applicationId, uploadType, description, uploadedByUserId
            );
            
            // 監査ログ記録
            auditService.createAuditLog(uploadedByUserId, "CREATE", "ATTACHMENT", attachment.getId());
            
            return ResponseEntity.ok(Map.of(
                "message", "ファイルがアップロードされました",
                "attachment", attachment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ファイルダウンロード
     */
    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        try {
            // ファイル情報を取得（実際の実装では認証・認可チェックが必要）
            ApplicationAttachment attachment = fileUploadService.getFileById(attachmentId)
                .orElseThrow(() -> new RuntimeException("ファイルが見つかりません"));
            
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * ファイル削除
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long attachmentId,
                                       @RequestParam("deletedByUserId") Long deletedByUserId) {
        try {
            fileUploadService.deleteFile(attachmentId);
            
            // 監査ログ記録
            auditService.createAuditLog(deletedByUserId, "DELETE", "ATTACHMENT", attachmentId);
            
            return ResponseEntity.ok(Map.of("message", "ファイルが削除されました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ファイル検証
     */
    @PostMapping("/{attachmentId}/verify")
    public ResponseEntity<?> verifyFile(@PathVariable Long attachmentId,
                                       @RequestBody Map<String, Object> request) {
        try {
            String verificationStatus = (String) request.get("verificationStatus");
            String comments = (String) request.get("comments");
            Long verifiedByUserId = Long.valueOf(request.get("verifiedByUserId").toString());
            
            ApplicationAttachment attachment = fileUploadService.verifyFile(
                attachmentId, verificationStatus, comments, verifiedByUserId
            );
            
            // 監査ログ記録
            auditService.createAuditLog(verifiedByUserId, "UPDATE", "ATTACHMENT", attachmentId);
            
            return ResponseEntity.ok(Map.of(
                "message", "ファイル検証が完了しました",
                "attachment", attachment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 申請IDによる添付ファイル一覧取得
     */
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<?> getAttachmentsByApplicationId(@PathVariable Long applicationId) {
        try {
            List<ApplicationAttachment> attachments = fileUploadService.getAttachmentsByApplicationId(applicationId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * アップロードタイプ別添付ファイル一覧取得
     */
    @GetMapping("/upload-type/{uploadType}")
    public ResponseEntity<?> getAttachmentsByUploadType(@PathVariable String uploadType) {
        try {
            List<ApplicationAttachment> attachments = fileUploadService.getAttachmentsByUploadType(uploadType);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 検証ステータス別添付ファイル一覧取得
     */
    @GetMapping("/verification-status/{verificationStatus}")
    public ResponseEntity<?> getAttachmentsByVerificationStatus(@PathVariable String verificationStatus) {
        try {
            List<ApplicationAttachment> attachments = fileUploadService.getAttachmentsByVerificationStatus(verificationStatus);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ファイルタイプ別添付ファイル一覧取得
     */
    @GetMapping("/file-type/{fileType}")
    public ResponseEntity<?> getAttachmentsByFileType(@PathVariable String fileType) {
        try {
            List<ApplicationAttachment> attachments = fileUploadService.getAttachmentsByFileType(fileType);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ファイル統計取得
     */
    @GetMapping("/stats/application/{applicationId}")
    public ResponseEntity<?> getAttachmentCountByApplicationId(@PathVariable Long applicationId) {
        try {
            long count = fileUploadService.getAttachmentCountByApplicationId(applicationId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/verification-status/{verificationStatus}")
    public ResponseEntity<?> getAttachmentCountByVerificationStatus(@PathVariable String verificationStatus) {
        try {
            long count = fileUploadService.getAttachmentCountByVerificationStatus(verificationStatus);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/file-type/{fileType}")
    public ResponseEntity<?> getAttachmentCountByFileType(@PathVariable String fileType) {
        try {
            long count = fileUploadService.getAttachmentCountByFileType(fileType);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * ファイル統計サマリー取得
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<?> getFileStatsSummary() {
        try {
            long pendingCount = fileUploadService.getAttachmentCountByVerificationStatus("PENDING");
            long verifiedCount = fileUploadService.getAttachmentCountByVerificationStatus("VERIFIED");
            long rejectedCount = fileUploadService.getAttachmentCountByVerificationStatus("REJECTED");
            
            return ResponseEntity.ok(Map.of(
                "pending", pendingCount,
                "verified", verifiedCount,
                "rejected", rejectedCount,
                "total", pendingCount + verifiedCount + rejectedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 