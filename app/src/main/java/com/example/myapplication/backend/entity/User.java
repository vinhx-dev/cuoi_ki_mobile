package com.example.myapplication.backend.entity;

public class User {
    private int id;
    private String name;
    private String phone;
    private String password; // demo: lưu plain text, backend thật phải hash
    private String address;

    public User(int id, String name, String phone, String password, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getAddress() { return address; }
}
