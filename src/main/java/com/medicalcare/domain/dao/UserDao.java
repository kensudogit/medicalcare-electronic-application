package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    
    // ユーザー名による検索
    Optional<User> findByUsername(String username);
    
    // メールアドレスによる検索
    Optional<User> findByEmail(String email);
    
    // 医療機関IDによる検索
    List<User> findByMedicalInstitutionId(Long medicalInstitutionId);
    
    // ロールによる検索
    List<User> findByRole(String role);
    
    // アクティブユーザーの検索
    List<User> findByIsActiveTrue();
    
    // ユーザー名の存在確認
    boolean existsByUsername(String username);
    
    // メールアドレスの存在確認
    boolean existsByEmail(String email);
    
    // 医療機関IDとロールによる検索
    @Query("SELECT u FROM User u WHERE u.medicalInstitutionId = :medicalInstitutionId AND u.role = :role")
    List<User> findByMedicalInstitutionIdAndRole(@Param("medicalInstitutionId") Long medicalInstitutionId, @Param("role") String role);
    
    // アクティブユーザー数
    long countByIsActiveTrue();
    
    // ロール別ユーザー数
    long countByRole(String role);
} 