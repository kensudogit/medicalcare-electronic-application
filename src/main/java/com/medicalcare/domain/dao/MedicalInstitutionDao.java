package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.MedicalInstitution;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;

import java.util.List;
import java.util.Optional;

/**
 * 医療機関DAO
 */
@Dao
public interface MedicalInstitutionDao {

    @Select
    List<MedicalInstitution> selectAll();

    @Select
    Optional<MedicalInstitution> selectById(Long id);

    @Select
    Optional<MedicalInstitution> selectByInstitutionCode(String institutionCode);

    @Select
    List<MedicalInstitution> selectByStatus(String status);

    @Insert
    int insert(MedicalInstitution medicalInstitution);

    @Update
    int update(MedicalInstitution medicalInstitution);

    @Delete
    int delete(MedicalInstitution medicalInstitution);
} 