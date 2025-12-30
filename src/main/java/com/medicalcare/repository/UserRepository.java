package com.medicalcare.repository;

import com.medicalcare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ユーザーリポジトリ
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ユーザー名でユーザーを検索
     */
    Optional<User> findByUsername(String username);

    /**
     * メールアドレスでユーザーを検索
     */
    Optional<User> findByEmail(String email);

    /**
     * ユーザー名またはメールアドレスでユーザーが存在するかチェック
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
} 