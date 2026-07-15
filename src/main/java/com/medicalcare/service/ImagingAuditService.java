package com.medicalcare.service;

import com.medicalcare.domain.dao.ImagingAuditLogDao;
import com.medicalcare.domain.dao.UserDao;
import com.medicalcare.domain.entity.ImagingAuditLog;
import com.medicalcare.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医療画像向け操作ログ・監査ログ
 */
@Service
public class ImagingAuditService {

    private final ImagingAuditLogDao imagingAuditLogDao;
    private final UserDao userDao;

    public ImagingAuditService(ImagingAuditLogDao imagingAuditLogDao, UserDao userDao) {
        this.imagingAuditLogDao = imagingAuditLogDao;
        this.userDao = userDao;
    }

    public ImagingAuditLog log(Long userId,
                               String role,
                               String action,
                               String entityType,
                               Long entityId,
                               Long medicalImageId,
                               String details,
                               boolean success,
                               HttpServletRequest request) {
        ImagingAuditLog log = new ImagingAuditLog();
        log.setUserId(userId);
        log.setUserRole(role);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMedicalImageId(medicalImageId);
        log.setDetails(details);
        log.setSuccess(success);
        if (userId != null) {
            userDao.findById(userId).map(User::getUsername).ifPresent(log::setUsername);
        }
        if (request != null) {
            log.setIpAddress(clientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
        return imagingAuditLogDao.save(log);
    }

    public List<ImagingAuditLog> listByImage(Long medicalImageId) {
        return imagingAuditLogDao.findByMedicalImageIdOrderByCreatedAtDesc(medicalImageId);
    }

    public List<ImagingAuditLog> listRecent() {
        return imagingAuditLogDao.findTop100ByOrderByCreatedAtDesc();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
