package com.medicalcare.controller;

import com.medicalcare.domain.entity.MedicalInstitution;
import com.medicalcare.service.MedicalInstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medical-institutions")
public class MedicalInstitutionController {
    
    @Autowired
    private MedicalInstitutionService medicalInstitutionService;

    /**
     * 全医療機関取得
     */
    @GetMapping
    public ResponseEntity<List<MedicalInstitution>> getAllMedicalInstitutions() {
        try {
            List<MedicalInstitution> institutions = medicalInstitutionService.getAllMedicalInstitutions();
            return ResponseEntity.ok(institutions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * IDによる医療機関取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalInstitution> getMedicalInstitutionById(@PathVariable Long id) {
        try {
            return medicalInstitutionService.getMedicalInstitutionById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 機関コードによる医療機関取得
     */
    @GetMapping("/code/{institutionCode}")
    public ResponseEntity<MedicalInstitution> getMedicalInstitutionByCode(@PathVariable String institutionCode) {
        try {
            return medicalInstitutionService.getMedicalInstitutionByCode(institutionCode)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 機関種別による医療機関一覧取得
     */
    @GetMapping("/type/{institutionType}")
    public ResponseEntity<List<MedicalInstitution>> getMedicalInstitutionsByType(@PathVariable String institutionType) {
        try {
            List<MedicalInstitution> institutions = medicalInstitutionService.getMedicalInstitutionsByType(institutionType);
            return ResponseEntity.ok(institutions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 医療機関作成
     */
    @PostMapping
    public ResponseEntity<MedicalInstitution> createMedicalInstitution(@RequestBody MedicalInstitutionRequest request) {
        try {
            MedicalInstitution created = medicalInstitutionService.createMedicalInstitution(
                request.getInstitutionCode(),
                request.getInstitutionName(),
                request.getInstitutionType(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getRepresentativeName(),
                request.getLicenseNumber()
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 医療機関更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicalInstitution> updateMedicalInstitution(@PathVariable Long id, @RequestBody MedicalInstitutionRequest request) {
        try {
            MedicalInstitution updated = medicalInstitutionService.updateMedicalInstitution(
                id,
                request.getInstitutionName(),
                request.getInstitutionType(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getRepresentativeName(),
                request.getLicenseNumber()
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 医療機関削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalInstitution(@PathVariable Long id) {
        try {
            medicalInstitutionService.deleteMedicalInstitution(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // リクエストクラス
    public static class MedicalInstitutionRequest {
        private String institutionCode;
        private String institutionName;
        private String institutionType;
        private String address;
        private String phone;
        private String email;
        private String representativeName;
        private String licenseNumber;

        // Getters and Setters
        public String getInstitutionCode() { return institutionCode; }
        public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }
        
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        
        public String getInstitutionType() { return institutionType; }
        public void setInstitutionType(String institutionType) { this.institutionType = institutionType; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRepresentativeName() { return representativeName; }
        public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }
        
        public String getLicenseNumber() { return licenseNumber; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    }
} 