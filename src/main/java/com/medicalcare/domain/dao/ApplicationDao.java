package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.Application;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;

import java.util.List;
import java.util.Optional;

/**
 * 申請DAO
 */
@Dao
public interface ApplicationDao {

    @Select
    List<Application> selectAll();

    @Select
    Optional<Application> selectById(Long id);

    @Select
    Optional<Application> selectByApplicationNumber(String applicationNumber);

    @Select
    List<Application> selectByInstitutionId(Long institutionId);

    @Select
    List<Application> selectByStatus(String status);

    @Select
    List<Application> selectByApplicationType(String applicationType);

    @Insert
    int insert(Application application);

    @Update
    int update(Application application);

    @Delete
    int delete(Application application);
} 