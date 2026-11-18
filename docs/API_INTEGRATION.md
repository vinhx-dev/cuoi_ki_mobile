# Gọi API theo cấu trúc của Minh

Bản tích hợp sử dụng nguyên phần API, xác thực và giao diện khách của `origin/Minh`
(commit `c1115ae`). Phần quản trị nằm trong `feature/admin` và không sửa các trang khách.

## Base URL

Giá trị mặc định được định nghĩa duy nhất trong `core/api/build.gradle.kts`:

```kotlin
val baseUrl: String = localProperties.getProperty("api.baseUrl")
    ?: "http://10.0.2.2:8080/api/"
```

Đổi địa chỉ cho máy đang dùng bằng `local.properties` ở gốc dự án, sau đó build lại:

```properties
api.baseUrl=http://10.0.2.2:8080/api/
```

- Android Emulator: `10.0.2.2` trỏ về máy tính chạy backend.
- Điện thoại USB: dùng `api.baseUrl=http://localhost:8080/api/` và
  `adb reverse tcp:8080 tcp:8080`.
- Backend triển khai ngoài: đặt URL HTTPS thật, bao gồm tiền tố API theo backend.
- `local.properties` là cấu hình riêng của máy và không được commit.

Luồng đọc cấu hình: `local.properties` → `core/api/build.gradle.kts` →
`core.api.BuildConfig.BASE_URL` → `core.api.ApiConfig.BASE_URL`.
Không sửa file `BuildConfig` được sinh tự động. Không còn base URL trong `core:data`.

## Nơi viết lời gọi API

```text
Fragment → ViewModel → feature/<tên>/data/<Tên>Api
                         ↓
                     core:api
                         ↓
                     HTTP server
```

- `core/api/.../ApiClient.java`: hàm GET/POST/PUT/DELETE và upload multipart dùng chung.
- `core/api/.../HttpTransport.java`: gửi HTTP qua OkHttp, dùng client xác thực chung.
- `feature/home/.../data/ProductApi.java`: ví dụ API theo feature, gọi `/products`.
- `feature/order/.../data/OrderApi.java`: API giỏ hàng/đơn hàng.
- `core/api/.../auth/AuthService.java`: endpoint Retrofit cho đăng ký, đăng nhập,
  OTP, refresh token, đăng xuất và `auth/me`.
- `core/api/.../auth/AuthNetwork.java`: cấu hình Retrofit, interceptor và refresh token.
- `core/data/`: lưu token và phiên đăng nhập; không giữ API/model nghiệp vụ admin.

Endpoint Retrofit xác thực dùng đường dẫn tương đối `auth/login`; API JSON chung dùng
đường dẫn bắt đầu bằng `/`, ví dụ `/products`. Không thêm lại `/api` nếu base URL đã có.

## Gói tin gửi và nhận

Ví dụ đăng nhập theo API của Minh (email, không dùng tài khoản điện thoại của bản cũ):

```http
POST /api/auth/login
Content-Type: application/json

{"email":"customer@example.com","password":"<mật khẩu>"}
```

Response có khung chung `{ success, message, data }`. Ví dụ minh họa:

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "<access token>",
    "refreshToken": "<refresh token>",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "refreshTokenExpiresIn": 604800,
    "user": {
      "id": 1,
      "email": "customer@example.com",
      "fullName": "Khách hàng",
      "phone": "0900000000",
      "role": "ROLE_CUSTOMER",
      "status": "ACTIVE",
      "createdAt": "2026-11-18T10:00:00Z"
    }
  }
}
```

Giá trị role, status và thời hạn trong ví dụ phải theo contract của backend thực tế.
`core/api/.../auth/ApiResponse.java` định nghĩa khung response;
`AuthResponse.java` định nghĩa dữ liệu token; `core/data/.../User.java` định nghĩa user.
Request xác thực đang được tạo từ `Map<String, String>` trong `auth/AuthRepository.java`.
Với API từng feature, model và hàm `fromJson()` nằm trong thư mục `data/` của feature.

Request cần đăng nhập được client chung tự gắn `Authorization: Bearer <access token>`.
Không tự lưu/gắn một bộ token khác trong feature. Khi access token hết hạn, bộ xử lý
xác thực dùng refresh token; khi phiên không còn hợp lệ, ứng dụng quay về đăng nhập.
Auth luôn gọi server thật. API JSON khác hiện có fallback `MockTransport` khi lỗi
kết nối, theo nguyên implementation của Minh; dữ liệu hiển thị chưa chứng minh server
đã được gọi thành công.

## Phần quản trị

```text
feature/admin/
├── src/main/java/com/example/ecom/feature/admin/
│   ├── AdminViewModel.java
│   ├── data/       AdminLocalStore, AdminProduct, AdminOrder, AdminCustomer, AdminOrderLine
│   ├── fragments/  các màn quản trị chỉ gọi ViewModel
│   ├── navigation/ AdminRouter
│   └── activities/ AdminActivity và AdminShellController
└── src/main/res/   layout, menu, ID và navigation của admin
```

Admin vẫn là bản mẫu dùng dữ liệu cục bộ trong `AdminLocalStore` (giữ nguyên khóa
SharedPreferences `shop_admin_demo_v1`, không mất dữ liệu đã lưu). Chưa có contract
backend quản trị trong repository nên chưa tự đặt các endpoint `/admin/...`.
Khi có contract: thêm `data/AdminApi.java` dùng `core.api.ApiClient` và
`core.api.ApiCallback`, đặt request/response model trong `data/`, rồi chuyển
`AdminViewModel` sang gọi API qua `fetch()`/`UiState` như các ViewModel của Minh.
Không thêm HTTP client, base URL hoặc kho token riêng cho admin.

## Chạy thử

```powershell
.\gradlew.bat :app:assembleDebug
```

- Biểu tượng ứng dụng chính: luồng khách của Minh, cần backend để đăng nhập email/OTP.
- Bản debug có thêm biểu tượng **Ecom Admin (mẫu)** để mở dữ liệu quản trị cục bộ.
- Có thể mở bản mẫu sau khi cài APK bằng:

```powershell
adb shell am start -n com.example.ecom/com.example.ecom.feature.admin.AdminPreview
```

Lối vào mẫu chỉ nằm trong source set `debug`, không được khai báo trong bản release.
`MainActivity`, màn đăng nhập, `AppRouter`, `AppPages` và layout khách của Minh được
giữ nguyên; admin sử dụng host và điều hướng riêng.

## Kiểm thử

- Token/refresh: `:core:api:testDebugUnitTest`.
- Smoke test khách mặc định: build `:app:assembleDebugAndroidTest`, cài APK test,
  chạy `adb shell am instrument -w com.example.ecom.test/com.example.ecom.CustomerUiSmokeTest`.
- Smoke test admin: build APK test với tham số PowerShell
  `'-PsmokeTestRunner=com.example.ecom.AdminIntegrationSmokeTest'`, cài lại APK test,
  chạy `adb shell am instrument -w com.example.ecom.test/com.example.ecom.AdminIntegrationSmokeTest`.
- Smoke test admin kiểm tra điều hướng các tab/chi tiết/form, xác thực form trống,
  header/footer và bảo toàn token khách; không sửa sản phẩm hay trạng thái đơn đã lưu.
