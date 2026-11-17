package com.example.ecom.core.data;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    public final long id;

    @SerializedName("email")
    public final String email;

    @SerializedName("fullName")
    public final String fullName;

    @SerializedName("name")
    public final String name;

    @SerializedName("phone")
    public final String phone;

    @SerializedName("address")
    public final String address;

    @SerializedName("role")
    public final String role; // ROLE_USER, ROLE_ADMIN

    @SerializedName("status")
    public final String status; // PENDING, ACTIVE, BLOCKED

    @SerializedName("createdAt")
    public final String createdAt;

    public User(long id, String email, String fullName, String phone, String role, String status, String createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.name = fullName;
        this.phone = phone != null ? phone : "";
        this.address = "";
        this.role = role != null ? role : "ROLE_USER";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt != null ? createdAt : "";
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.isEmpty()) return fullName;
        if (name != null && !name.isEmpty()) return name;
        if (email != null && !email.isEmpty()) return email;
        return "Người dùng";
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equalsIgnoreCase(role);
    }
}
