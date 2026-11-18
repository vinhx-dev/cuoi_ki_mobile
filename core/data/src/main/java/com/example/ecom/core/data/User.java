package com.example.ecom.core.data;
public class User {
    public long id;
    public String email, fullName, phone, role, status, createdAt;
    public String name, address;
    public User() { }
    public User(long id,String email,String fullName,String phone,String role,String status,String createdAt) {
        this.id=id;this.email=email;this.fullName=fullName;this.name=fullName;this.phone=phone;this.role=role;this.status=status;this.createdAt=createdAt;
    }
    public String getDisplayName() { if(fullName!=null&&!fullName.isEmpty())return fullName;if(name!=null&&!name.isEmpty())return name;return email!=null?email:""; }
    public boolean isAdmin() { return "ROLE_ADMIN".equals(role); }
}
