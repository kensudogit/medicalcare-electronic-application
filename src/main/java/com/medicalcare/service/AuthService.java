package com.medicalcare.service;

import com.medicalcare.domain.dao.UserDao;
import com.medicalcare.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * ユーザー登録（旧シグネチャ - 後方互換性のため保持）
     */
    public User registerUser(String username, String email, String password) {
        return registerUser(username, email, password, "USER", null, null);
    }

    /**
     * ユーザー登録（新シグネチャ）
     */
    public User registerUser(String username, String email, String password, String role, String firstName,
            String lastName) {
        // ユーザー名の重複チェック
        if (userDao.existsByUsername(username)) {
            throw new RuntimeException("ユーザー名が既に使用されています: " + username);
        }

        // メールアドレスの重複チェック
        if (userDao.existsByEmail(email)) {
            throw new RuntimeException("メールアドレスが既に使用されています: " + email);
        }

        // パスワードのハッシュ化
        String passwordHash = passwordEncoder.encode(password);

        // ユーザー作成
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role != null ? role : "USER");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userDao.save(user);
    }

    /**
     * ユーザー認証
     */
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                // 最終ログイン時刻を更新
                user.setUpdatedAt(LocalDateTime.now());
                userDao.save(user);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * ユーザー情報更新（旧シグネチャ - 後方互換性のため保持）
     */
    public User updateUser(Long userId, String email) {
        return updateUser(userId, null, null, null, email);
    }

    /**
     * ユーザー情報更新（新シグネチャ）
     */
    public User updateUser(Long userId, String firstName, String lastName, String phoneNumber, String email) {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (firstName != null) {
                user.setFirstName(firstName);
            }
            if (lastName != null) {
                user.setLastName(lastName);
            }
            if (phoneNumber != null) {
                user.setPhoneNumber(phoneNumber);
            }
            if (email != null) {
                user.setEmail(email);
            }
            user.setUpdatedAt(LocalDateTime.now());
            return userDao.save(user);
        }
        throw new RuntimeException("ユーザーが見つかりません: " + userId);
    }

    /**
     * パスワード変更
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userDao.save(user);
            } else {
                throw new RuntimeException("現在のパスワードが正しくありません");
            }
        } else {
            throw new RuntimeException("ユーザーが見つかりません: " + userId);
        }
    }

    /**
     * ユーザー削除
     */
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isPresent()) {
            userDao.delete(userOpt.get());
        } else {
            throw new RuntimeException("ユーザーが見つかりません: " + userId);
        }
    }

    /**
     * 全ユーザー取得
     */
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    /**
     * ユーザーIDによる取得
     */
    public Optional<User> getUserById(Long userId) {
        return userDao.findById(userId);
    }

    /**
     * ユーザー名による取得
     */
    public Optional<User> getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    /**
     * メールアドレスによる取得
     */
    public Optional<User> getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    /**
     * ユーザー無効化
     */
    public void deactivateUser(Long userId) {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userDao.save(user);
        } else {
            throw new RuntimeException("ユーザーが見つかりません: " + userId);
        }
    }

    /**
     * 医療機関別ユーザー取得
     */
    public List<User> getUsersByMedicalInstitution(Long medicalInstitutionId) {
        List<User> allUsers = userDao.findAll();
        return allUsers.stream()
                .filter(user -> medicalInstitutionId.equals(user.getMedicalInstitutionId()))
                .collect(Collectors.toList());
    }

    /**
     * ロール別ユーザー取得
     */
    public List<User> getUsersByRole(String role) {
        List<User> allUsers = userDao.findAll();
        return allUsers.stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }

    /**
     * アクティブユーザー取得
     */
    public List<User> getActiveUsers() {
        List<User> allUsers = userDao.findAll();
        return allUsers.stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }
}