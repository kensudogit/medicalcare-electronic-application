package com.medicalcare.service;

import com.medicalcare.domain.dao.MedicalInstitutionDao;
import com.medicalcare.domain.entity.MedicalInstitution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 医療機関サービス
 */
@Service
@Transactional
public class MedicalInstitutionService {

    private final MedicalInstitutionDao medicalInstitutionDao;

    public MedicalInstitutionService(MedicalInstitutionDao medicalInstitutionDao) {
        this.medicalInstitutionDao = medicalInstitutionDao;
    }

    /**
     * 全医療機関を取得
     */
    @Transactional(readOnly = true)
    public List<MedicalInstitution> findAll() {
        return medicalInstitutionDao.selectAll();
    }

    /**
     * IDで医療機関を取得
     */
    @Transactional(readOnly = true)
    public Optional<MedicalInstitution> findById(Long id) {
        return medicalInstitutionDao.selectById(id);
    }

    /**
     * 機関コードで医療機関を取得
     */
    @Transactional(readOnly = true)
    public Optional<MedicalInstitution> findByInstitutionCode(String institutionCode) {
        return medicalInstitutionDao.selectByInstitutionCode(institutionCode);
    }

    /**
     * ステータスで医療機関を取得
     */
    @Transactional(readOnly = true)
    public List<MedicalInstitution> findByStatus(String status) {
        return medicalInstitutionDao.selectByStatus(status);
    }

    /**
     * 医療機関を登録
     */
    public MedicalInstitution create(MedicalInstitution medicalInstitution) {
        LocalDateTime now = LocalDateTime.now();
        MedicalInstitution newInstitution = new MedicalInstitution(
                null,
                medicalInstitution.getInstitutionCode(),
                medicalInstitution.getInstitutionName(),
                medicalInstitution.getInstitutionType(),
                medicalInstitution.getAddress(),
                medicalInstitution.getPhone(),
                medicalInstitution.getEmail(),
                medicalInstitution.getRepresentativeName(),
                medicalInstitution.getLicenseNumber(),
                "ACTIVE",
                now,
                now,
                1L
        );
        
        medicalInstitutionDao.insert(newInstitution);
        return newInstitution;
    }

    /**
     * 医療機関を更新
     */
    public MedicalInstitution update(Long id, MedicalInstitution medicalInstitution) {
        Optional<MedicalInstitution> existing = medicalInstitutionDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Medical institution not found: " + id);
        }

        MedicalInstitution current = existing.get();
        MedicalInstitution updated = new MedicalInstitution(
                current.getId(),
                medicalInstitution.getInstitutionCode(),
                medicalInstitution.getInstitutionName(),
                medicalInstitution.getInstitutionType(),
                medicalInstitution.getAddress(),
                medicalInstitution.getPhone(),
                medicalInstitution.getEmail(),
                medicalInstitution.getRepresentativeName(),
                medicalInstitution.getLicenseNumber(),
                medicalInstitution.getStatus(),
                current.getCreatedAt(),
                LocalDateTime.now(),
                current.getVersion() + 1
        );

        medicalInstitutionDao.update(updated);
        return updated;
    }

    /**
     * 医療機関を削除
     */
    public void delete(Long id) {
        Optional<MedicalInstitution> existing = medicalInstitutionDao.selectById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Medical institution not found: " + id);
        }

        medicalInstitutionDao.delete(existing.get());
    }
} 