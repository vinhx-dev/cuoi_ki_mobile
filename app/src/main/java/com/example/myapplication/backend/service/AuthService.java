package com.example.myapplication.backend.service;

import com.example.myapplication.backend.core.ApiException;
import com.example.myapplication.backend.entity.User;
import com.example.myapplication.backend.repository.UserRepository;
import com.example.myapplication.backend.security.TokenProvider;

/**
 * Tầng Service: chứa logic nghiệp vụ, validate, gọi Repository.
 */
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String phone, String password) {
        if (isBlank(phone) || isBlank(password)) {
            throw ApiException.badRequest("Vui lòng nhập số điện thoại và mật khẩu");
        }
        User user = userRepository.findByPhone(phone.trim());
        if (user == null || !user.getPassword().equals(password)) {
            throw ApiException.unauthorized("Sai số điện thoại hoặc mật khẩu");
        }
        return user;
    }

    public User register(String name, String phone, String password) {
        if (isBlank(name) || isBlank(phone) || isBlank(password)) {
            throw ApiException.badRequest("Vui lòng nhập đầy đủ thông tin");
        }
        if (!phone.trim().matches("0\\d{9}")) {
            throw ApiException.badRequest("Số điện thoại phải gồm 10 số, bắt đầu bằng 0");
        }
        if (password.length() < 6) {
            throw ApiException.badRequest("Mật khẩu tối thiểu 6 ký tự");
        }
        if (userRepository.findByPhone(phone.trim()) != null) {
            throw ApiException.badRequest("Số điện thoại đã được đăng ký");
        }
        return userRepository.save(new User(0, name.trim(), phone.trim(), password, ""));
    }

    public String issueToken(User user) {
        return TokenProvider.generate(user.getId());
    }

    /** Xác thực token và trả về user hiện tại. */
    public User authenticate(String token) {
        int userId = TokenProvider.verify(token);
        User user = userRepository.findById(userId);
        if (user == null) throw ApiException.unauthorized("Phiên đăng nhập hết hạn");
        return user;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
