package com.medicalcare.controller;

import com.medicalcare.domain.entity.MedicalInstitution;
import com.medicalcare.service.MedicalInstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医療機関コントローラー
 */
@RestController
@RequestMapping("/medical-institutions")
@CrossOrigin(origins = "*")
public class MedicalInstitutionController {

    private final MedicalInstitutionService medicalInstitutionService;

    public MedicalInstitutionController(MedicalInstitutionService medicalInstitutionService) {
        this.medicalInstitutionService = medicalInstitutionService;
    }

    /**
     * 全医療機関を取得
     */
    @GetMapping
    public ResponseEntity<List<MedicalInstitution>> getAllMedicalInstitutions() {
        List<MedicalInstitution> institutions = medicalInstitutionService.findAll();
        return ResponseEntity.ok(institutions);
    }

    /**
     * IDで医療機関を取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalInstitution> getMedicalInstitutionById(@PathVariable Long id) {
        return medicalInstitutionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 機関コードで医療機関を取得
     */
    @GetMapping("/code/{institutionCode}")
    public ResponseEntity<MedicalInstitution> getMedicalInstitutionByCode(@PathVariable String institutionCode) {
        return medicalInstitutionService.findByInstitutionCode(institutionCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ステータスで医療機関を取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MedicalInstitution>> getMedicalInstitutionsByStatus(@PathVariable String status) {
        List<MedicalInstitution> institutions = medicalInstitutionService.findByStatus(status);
        return ResponseEntity.ok(institutions);
    }

    /**
     * 医療機関を登録
     */
    @PostMapping
    public ResponseEntity<MedicalInstitution> createMedicalInstitution(@RequestBody MedicalInstitution medicalInstitution) {
        MedicalInstitution created = medicalInstitutionService.create(medicalInstitution);
        return ResponseEntity.ok(created);
    }

    /**
     * 医療機関を更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicalInstitution> updateMedicalInstitution(
            @PathVariable Long id,
            @RequestBody MedicalInstitution medicalInstitution) {
        try {
            MedicalInstitution updated = medicalInstitutionService.update(id, medicalInstitution);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 医療機関を削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalInstitution(@PathVariable Long id) {
        try {
            medicalInstitutionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 