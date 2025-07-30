package com.medicalcare.service;

import com.medicalcare.domain.dao.MedicalInstitutionDao;
import com.medicalcare.domain.entity.MedicalInstitution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalInstitutionService {
    
    @Autowired
    private MedicalInstitutionDao medicalInstitutionDao;

    /**
     * 全医療機関取得
     */
    public List<MedicalInstitution> getAllMedicalInstitutions() {
        return medicalInstitutionDao.findAll();
    }

    /**
     * IDによる医療機関取得
     */
    public Optional<MedicalInstitution> getMedicalInstitutionById(Long id) {
        return medicalInstitutionDao.findById(id);
    }

    /**
     * 機関コードによる医療機関取得
     */
    public Optional<MedicalInstitution> getMedicalInstitutionByCode(String institutionCode) {
        return medicalInstitutionDao.findByInstitutionCode(institutionCode);
    }

    /**
     * 機関種別による医療機関一覧取得
     */
    public List<MedicalInstitution> getMedicalInstitutionsByType(String institutionType) {
        return medicalInstitutionDao.findByInstitutionType(institutionType);
    }

    /**
     * 医療機関作成
     */
    public MedicalInstitution createMedicalInstitution(String institutionCode, String institutionName, 
                                                      String institutionType, String address, String phone, 
                                                      String email, String representativeName, String licenseNumber) {
        // 機関コードの重複チェック
        if (medicalInstitutionDao.existsByInstitutionCode(institutionCode)) {
            throw new RuntimeException("機関コードが既に使用されています: " + institutionCode);
        }
        
        MedicalInstitution newInstitution = new MedicalInstitution(institutionCode, institutionName, institutionType);
        newInstitution.setAddress(address);
        newInstitution.setPhone(phone);
        newInstitution.setEmail(email);
        newInstitution.setRepresentativeName(representativeName);
        newInstitution.setLicenseNumber(licenseNumber);
        newInstitution.setCreatedAt(LocalDateTime.now());
        
        return medicalInstitutionDao.save(newInstitution);
    }

    /**
     * 医療機関更新
     */
    public MedicalInstitution updateMedicalInstitution(Long id, String institutionName, String institutionType,
                                                      String address, String phone, String email, 
                                                      String representativeName, String licenseNumber) {
        Optional<MedicalInstitution> existing = medicalInstitutionDao.findById(id);
        if (existing.isPresent()) {
            MedicalInstitution current = existing.get();
            
            MedicalInstitution updated = new MedicalInstitution(
                current.getInstitutionCode(),
                institutionName,
                institutionType
            );
            updated.setId(current.getId());
            updated.setAddress(address);
            updated.setPhone(phone);
            updated.setEmail(email);
            updated.setRepresentativeName(representativeName);
            updated.setLicenseNumber(licenseNumber);
            updated.setCreatedAt(current.getCreatedAt());
            updated.setUpdatedAt(LocalDateTime.now());
            
            return medicalInstitutionDao.save(updated);
        } else {
            throw new RuntimeException("医療機関が見つかりません: " + id);
        }
    }

    /**
     * 医療機関削除
     */
    public void deleteMedicalInstitution(Long id) {
        Optional<MedicalInstitution> existing = medicalInstitutionDao.findById(id);
        if (existing.isPresent()) {
            medicalInstitutionDao.delete(existing.get());
        } else {
            throw new RuntimeException("医療機関が見つかりません: " + id);
        }
    }
} 