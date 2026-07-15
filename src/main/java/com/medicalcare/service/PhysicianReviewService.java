package com.medicalcare.service;

import com.medicalcare.domain.dao.ImageAnalysisResultDao;
import com.medicalcare.domain.dao.MedicalImageDao;
import com.medicalcare.domain.dao.PhysicianReviewDao;
import com.medicalcare.domain.entity.ImageAnalysisResult;
import com.medicalcare.domain.entity.MedicalImage;
import com.medicalcare.domain.entity.PhysicianReview;
import com.medicalcare.security.ImagingRoles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 医師による確認・承認ワークフロー
 * AI判定を確定診断として扱わないことを強制する。
 */
@Service
public class PhysicianReviewService {

    public static final String DISCLAIMER =
            "本表示はAIによる診断支援候補であり、確定診断ではありません。"
                    + "最終判断は必ず医師が行い、電子カルテ上の診断記録は医師が作成してください。";

    private static final Set<String> VALID_DECISIONS = Set.of(
            "APPROVED_AS_CANDIDATE",
            "REJECTED",
            "NEEDS_SECOND_LOOK",
            "FALSE_POSITIVE",
            "MISSED_FINDING"
    );

    private final PhysicianReviewDao physicianReviewDao;
    private final ImageAnalysisResultDao imageAnalysisResultDao;
    private final MedicalImageDao medicalImageDao;

    public PhysicianReviewService(PhysicianReviewDao physicianReviewDao,
                                  ImageAnalysisResultDao imageAnalysisResultDao,
                                  MedicalImageDao medicalImageDao) {
        this.physicianReviewDao = physicianReviewDao;
        this.imageAnalysisResultDao = imageAnalysisResultDao;
        this.medicalImageDao = medicalImageDao;
    }

    public PhysicianReview submitReview(Long analysisResultId,
                                        Long reviewerUserId,
                                        String reviewerRole,
                                        String decision,
                                        String physicianComment,
                                        String confirmedFindingsText,
                                        String falsePositiveNotes,
                                        String missedFindingNotes,
                                        boolean acknowledgedNonDiagnostic) {
        if (!ImagingRoles.allows(ImagingRoles.CAN_APPROVE, reviewerRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "医師承認の権限がありません");
        }
        if (!acknowledgedNonDiagnostic) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "「AI結果は確定診断ではない」ことの確認が必須です"
            );
        }
        if (decision == null || !VALID_DECISIONS.contains(decision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不正な判定コードです: " + decision);
        }

        ImageAnalysisResult analysis = imageAnalysisResultDao.findById(analysisResultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "解析結果が見つかりません"));
        MedicalImage image = medicalImageDao.findById(analysis.getMedicalImageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "画像が見つかりません"));

        PhysicianReview review = new PhysicianReview();
        review.setMedicalImageId(image.getId());
        review.setAnalysisResultId(analysisResultId);
        review.setReviewerUserId(reviewerUserId);
        review.setReviewerRole(ImagingRoles.normalize(reviewerRole));
        review.setDecision(decision);
        review.setPhysicianComment(physicianComment);
        // 確定診断文言の禁止: 医師コメントは「候補確認」として保存。診断確定ラベルは付けない
        if (confirmedFindingsText != null && !confirmedFindingsText.isBlank()) {
            review.setConfirmedFindingsText(
                    "[医師確認コメント・診断支援候補] " + confirmedFindingsText + "\n\n" + DISCLAIMER
            );
        }
        review.setFalsePositiveNotes(falsePositiveNotes);
        review.setMissedFindingNotes(missedFindingNotes);
        review.setAcknowledgedNonDiagnostic(true);
        review.setReviewedAt(LocalDateTime.now());

        analysis.setReviewStatus(decision);
        analysis.setIsDiagnosticClaim(false);
        analysis.setDisclaimerText(DISCLAIMER);
        imageAnalysisResultDao.save(analysis);

        image.setStatus(mapImageStatus(decision));
        medicalImageDao.save(image);

        return physicianReviewDao.save(review);
    }

    public List<PhysicianReview> listByImage(Long medicalImageId) {
        return physicianReviewDao.findByMedicalImageIdOrderByCreatedAtDesc(medicalImageId);
    }

    public String disclaimer() {
        return DISCLAIMER;
    }

    private String mapImageStatus(String decision) {
        return switch (decision) {
            case "APPROVED_AS_CANDIDATE" -> "PHYSICIAN_REVIEWED";
            case "REJECTED", "FALSE_POSITIVE" -> "PHYSICIAN_REJECTED";
            case "NEEDS_SECOND_LOOK", "MISSED_FINDING" -> "NEEDS_SECOND_LOOK";
            default -> "ANALYZED";
        };
    }
}
