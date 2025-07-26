package com.medicalcare.domain.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Column;
import org.seasar.doma.Version;

import java.time.LocalDateTime;

/**
 * 医療機関エンティティ
 */
@Entity(immutable = true)
@Table(name = "medical_institutions")
public class MedicalInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private final Long id;

    @Column(name = "institution_code")
    private final String institutionCode;

    @Column(name = "institution_name")
    private final String institutionName;

    @Column(name = "institution_type")
    private final String institutionType;

    @Column(name = "address")
    private final String address;

    @Column(name = "phone")
    private final String phone;

    @Column(name = "email")
    private final String email;

    @Column(name = "representative_name")
    private final String representativeName;

    @Column(name = "license_number")
    private final String licenseNumber;

    @Column(name = "status")
    private final String status;

    @Column(name = "created_at")
    private final LocalDateTime createdAt;

    @Column(name = "updated_at")
    private final LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private final Long version;

    public MedicalInstitution(
            Long id,
            String institutionCode,
            String institutionName,
            String institutionType,
            String address,
            String phone,
            String email,
            String representativeName,
            String licenseNumber,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long version) {
        this.id = id;
        this.institutionCode = institutionCode;
        this.institutionName = institutionName;
        this.institutionType = institutionType;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.representativeName = representativeName;
        this.licenseNumber = licenseNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    // Getters
    public Long getId() { return id; }
    public String getInstitutionCode() { return institutionCode; }
    public String getInstitutionName() { return institutionName; }
    public String getInstitutionType() { return institutionType; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRepresentativeName() { return representativeName; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
} 