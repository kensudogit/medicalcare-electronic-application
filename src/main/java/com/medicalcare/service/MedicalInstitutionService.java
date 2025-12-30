package com.medicalcare.service;

import com.medicalcare.domain.dao.MedicalInstitutionDao;
import com.medicalcare.domain.entity.MedicalInstitution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * 医療機関コードによる取得
     */
    public Optional<MedicalInstitution> getMedicalInstitutionByCode(String institutionCode) {
        return medicalInstitutionDao.findByInstitutionCode(institutionCode);
    }

    /**
     * 医療機関番号による取得（エイリアス - コードで検索）
     */
    public Optional<MedicalInstitution> getMedicalInstitutionByNumber(String institutionNumber) {
        return medicalInstitutionDao.findByInstitutionCode(institutionNumber);
    }

    /**
     * 医療機関名による検索
     */
    public List<MedicalInstitution> searchMedicalInstitutionsByName(String name) {
        return medicalInstitutionDao.findByInstitutionNameContaining(name);
    }

    /**
     * 機関種別による医療機関取得
     */
    public List<MedicalInstitution> getMedicalInstitutionsByType(String institutionType) {
        return medicalInstitutionDao.findAll().stream()
                .filter(inst -> institutionType.equals(inst.getInstitutionType()))
                .collect(Collectors.toList());
    }

    /**
     * 都道府県による検索（住所から検索）
     */
    public List<MedicalInstitution> getMedicalInstitutionsByPrefecture(String prefecture) {
        return medicalInstitutionDao.findAll().stream()
                .filter(inst -> inst.getAddress() != null && inst.getAddress().contains(prefecture))
                .collect(Collectors.toList());
    }

    /**
     * 医療機関作成
     */
    public MedicalInstitution createMedicalInstitution(String institutionCode, String institutionName,
            String institutionType, String address, String phone, String email, String representativeName,
            String licenseNumber) {
        // 医療機関コードの重複チェック
        if (medicalInstitutionDao.existsByInstitutionCode(institutionCode)) {
            throw new RuntimeException("医療機関コードが既に使用されています: " + institutionCode);
        }

        MedicalInstitution institution = new MedicalInstitution();
        institution.setInstitutionCode(institutionCode);
        institution.setInstitutionName(institutionName);
        institution.setInstitutionType(institutionType);
        institution.setAddress(address);
        institution.setPhone(phone);
        institution.setEmail(email);
        institution.setRepresentativeName(representativeName);
        institution.setLicenseNumber(licenseNumber);
        institution.setCreatedAt(LocalDateTime.now());
        institution.setUpdatedAt(LocalDateTime.now());

        return medicalInstitutionDao.save(institution);
    }

    /**
     * 医療機関更新
     */
    public MedicalInstitution updateMedicalInstitution(Long id, String institutionName, String institutionType,
            String address, String phone, String email, String representativeName, String licenseNumber) {
        Optional<MedicalInstitution> institutionOpt = medicalInstitutionDao.findById(id);
        if (institutionOpt.isPresent()) {
            MedicalInstitution institution = institutionOpt.get();
            institution.setInstitutionName(institutionName);
            institution.setInstitutionType(institutionType);
            institution.setAddress(address);
            institution.setPhone(phone);
            institution.setEmail(email);
            institution.setRepresentativeName(representativeName);
            institution.setLicenseNumber(licenseNumber);
            institution.setUpdatedAt(LocalDateTime.now());

            return medicalInstitutionDao.save(institution);
        } else {
            throw new RuntimeException("医療機関が見つかりません: " + id);
        }
    }

    /**
     * 医療機関削除
     */
    public void deleteMedicalInstitution(Long id) {
        Optional<MedicalInstitution> institutionOpt = medicalInstitutionDao.findById(id);
        if (institutionOpt.isPresent()) {
            medicalInstitutionDao.delete(institutionOpt.get());
        } else {
            throw new RuntimeException("医療機関が見つかりません: " + id);
        }
    }
}