package com.medicalcare.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * リクエストヘッダベースのアクセス権限チェック
 * 本番では JWT / OIDC クレームへ置き換え可能。
 *
 * Headers:
 *   X-User-Id
 *   X-User-Role
 *   X-Institution-Id (optional)
 */
@Component
public class ImagingAccessGuard {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_INSTITUTION_ID = "X-Institution-Id";

    public void require(String role, Set<String> allowed, String action) {
        if (!ImagingRoles.allows(allowed, role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "権限がありません: " + action + "（role=" + ImagingRoles.normalize(role) + "）"
            );
        }
    }

    public Long requireUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id が必要です");
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-User-Id が不正です");
        }
    }

    public void requireInstitutionScope(String role, Long userInstitutionId, Long resourceInstitutionId) {
        String normalized = ImagingRoles.normalize(role);
        if (ADMIN_OR_GLOBAL.contains(normalized)) {
            return;
        }
        if (resourceInstitutionId == null) {
            return;
        }
        if (userInstitutionId == null || !userInstitutionId.equals(resourceInstitutionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "他医療機関の画像にはアクセスできません");
        }
    }

    private static final Set<String> ADMIN_OR_GLOBAL = Set.of(
            ImagingRoles.ADMIN
    );
}
