package com.example.myapplication.backend.repository;

import com.example.myapplication.backend.database.FakeDatabase;
import com.example.myapplication.backend.entity.User;

/**
 * Tầng Repository: chỉ làm việc với dữ liệu (CRUD), KHÔNG chứa logic nghiệp vụ.
 */
public class UserRepository {

    private final FakeDatabase db = FakeDatabase.getInstance();

    public User findById(int id) {
        synchronized (db.users) {
            for (User u : db.users) if (u.getId() == id) return u;
        }
        return null;
    }

    public User findByPhone(String phone) {
        synchronized (db.users) {
            for (User u : db.users) if (u.getPhone().equals(phone)) return u;
        }
        return null;
    }

    public User save(User user) {
        user.setId(db.userSeq.incrementAndGet());
        db.users.add(user);
        return user;
    }
}
