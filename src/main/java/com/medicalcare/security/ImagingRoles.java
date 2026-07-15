package com.medicalcare.security;

import java.util.Locale;
import java.util.Set;

/**
 * 医療画像機能のアクセス権限ロール
 */
public final class ImagingRoles {

    public static final String ADMIN = "ADMIN";
    public static final String INSTITUTION_ADMIN = "INSTITUTION_ADMIN";
    public static final String PHYSICIAN = "PHYSICIAN";
    public static final String RADIOLOGIST = "RADIOLOGIST";
    public static final String TECHNICIAN = "TECHNICIAN";
    public static final String USER = "USER";

    public static final Set<String> CAN_UPLOAD = Set.of(
            ADMIN, INSTITUTION_ADMIN, PHYSICIAN, RADIOLOGIST, TECHNICIAN
    );
    public static final Set<String> CAN_ANALYZE = Set.of(
            ADMIN, INSTITUTION_ADMIN, PHYSICIAN, RADIOLOGIST, TECHNICIAN
    );
    public static final Set<String> CAN_VIEW = Set.of(
            ADMIN, INSTITUTION_ADMIN, PHYSICIAN, RADIOLOGIST, TECHNICIAN, USER
    );
    public static final Set<String> CAN_APPROVE = Set.of(
            ADMIN, PHYSICIAN, RADIOLOGIST
    );
    public static final Set<String> CAN_SYNC_EXTERNAL = Set.of(
            ADMIN, INSTITUTION_ADMIN, PHYSICIAN, RADIOLOGIST
    );
    public static final Set<String> CAN_VIEW_PHI = Set.of(
            ADMIN, PHYSICIAN, RADIOLOGIST
    );
    public static final Set<String> CAN_VIEW_AUDIT = Set.of(
            ADMIN, INSTITUTION_ADMIN
    );

    private ImagingRoles() {}

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean allows(Set<String> allowed, String role) {
        return allowed.contains(normalize(role));
    }
}
