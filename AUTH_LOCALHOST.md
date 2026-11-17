# Authentication API v1

Mặc định: `http://10.0.2.2:8080/api/` (Android Emulator).
Đổi tại `core/api/build.gradle.kts`, hoặc thêm vào `local.properties`:

```properties
api.baseUrl=http://localhost:8080/api/
```

Thiết bị thật qua USB: chạy `adb reverse tcp:8080 tcp:8080`, sau đó build lại với URL localhost ở trên.
Server phải chạy cổng 8080. Auth luôn dùng server thật; không fallback dữ liệu giả.

Kiểm thử: đăng ký email mới → OTP → đăng nhập → Cá nhân → đăng xuất → Back không quay về trang đã đăng nhập.
Kiểm tra mã OTP sai/hết hạn và gửi lại (khóa 60 giây, giữ khi xoay màn hình), email trùng, mật khẩu sai, tài khoản chưa kích hoạt và tài khoản bị khóa.
Mở lại app có token sẽ GET auth/me. Cho access token hết hạn rồi chạy nhiều request: chỉ một refresh với cả hai token; các request còn lại dùng cặp token mới. Refresh 401/403 xóa phiên và về đăng nhập.
Token lưu trong SharedPreferences riêng, loại khỏi backup. Logging BASIC không ghi nội dung request hoặc token.
