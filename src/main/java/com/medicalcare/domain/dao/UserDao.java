package com.medicalcare.domain.dao;

import com.medicalcare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ユーザーデータアクセスオブジェクト
 */
@Repository
public interface UserDao extends JpaRepository<User, Long> {

    /**
     * ユーザー名による検索
     */
    Optional<User> findByUsername(String username);

    /**
     * メールアドレスによる検索
     */
    Optional<User> findByEmail(String email);

    /**
     * ユーザー名の存在確認
     */
    boolean existsByUsername(String username);

    /**
     * メールアドレスの存在確認
     */
    boolean existsByEmail(String email);
}