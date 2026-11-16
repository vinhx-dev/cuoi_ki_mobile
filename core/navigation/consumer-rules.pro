# AppPages khai báo trang bằng TÊN CLASS (chuỗi) và tạo Fragment qua reflection.
# Khi bật isMinifyEnabled, R8 sẽ đổi tên / xoá các class này -> crash. Giữ nguyên tên mọi Fragment.
-keepnames class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.fragment.app.Fragment { public <init>(); }
