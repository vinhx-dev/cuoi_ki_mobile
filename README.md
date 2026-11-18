# Ecom App (Android)

Base code app bán hàng Android viết bằng Java: Single Activity, chia nhiều module (multi-module), điều hướng bằng Jetpack Navigation.
App chỉ chứa **frontend**. Backend là một project Maven riêng, app gọi tới qua HTTP/JSON (xem mục [API](#5-api-contract)).

Tính năng có sẵn: đăng nhập/đăng ký, danh sách và tìm kiếm sản phẩm, chi tiết sản phẩm, đặt hàng, danh sách và chi tiết đơn, huỷ đơn, hồ sơ, đăng xuất.

---

## 1. Kiến trúc

```
Fragment (feature/*)
   │  gọi
   ▼
Repository (core/data)        ← UI chỉ gọi Repository, không gọi API trực tiếp
   │
   ▼
ApiClient → HttpTransport     ← gắn token, chạy nền, bóc { success, message, data }, xử lý 401
   │  HTTP/JSON
   ▼
Backend server (project riêng)
```

- **MainActivity** (`:app`) là "khung app" (App Shell), gồm header, nút back, bottom nav và loading dùng chung. Khung hiển thị thế nào do `LayoutType` của từng trang quyết định.
- Fragment kế thừa `BaseFragment`. Hàm `apiCallback(...)` tự bật/tắt loading, tự hiện toast khi lỗi, và bỏ qua kết quả nếu trang đã đóng.
- Mọi điều hướng đi qua `AppRouter`, Fragment không gọi `NavController` trực tiếp.
- Khi API trả **401**, `ApiClient` xoá phiên đăng nhập và `MainActivity` đưa người dùng về màn đăng nhập.

## 2. Cấu trúc module

```
ecom_app/
├── app/                       :app             MainActivity, nav_graph.xml, header, bottom nav, theme
├── core/
│   ├── data/                  :core:data       api/ (ApiClient, HttpTransport, ApiEndpoints), model/, repository/, session/
│   ├── ui/                    :core:ui         base/ (BaseFragment, AppShell), format/ (Formatter), colors.xml
│   └── navigation/            :core:navigation AppRouter, RouteConfig, LayoutType, ids.xml (id các trang)
└── feature/
    ├── splash/   auth/   home/   product/   order/   profile/
    └── admin/                  :feature:admin    Tổng quan, quản lý sản phẩm, đơn hàng, người dùng
```

Package trùng với namespace của module: `com.example.ecom` (app), `com.example.ecom.core.data`, `com.example.ecom.core.ui`, `com.example.ecom.core.navigation`, `com.example.ecom.feature.<tên>`.

**Quy tắc phụ thuộc:**

```
:app ──► :core:*  +  mọi :feature:*
:feature:* ──► :core:data, :core:ui, :core:navigation
:core:ui ──► :core:data          (BaseFragment dùng ApiCallback)
:core:navigation ──► (không phụ thuộc module nào của dự án)
```

- **Feature không được phụ thuộc feature khác.** Muốn mở trang của feature khác thì gọi `AppRouter`.
- Code, chuỗi hay màu dùng cho nhiều feature thì đặt ở `core`. Thứ chỉ một feature dùng thì đặt ngay trong feature đó.
- `nav_graph.xml` nằm ở `:app`, vì chỉ `:app` thấy được class của mọi feature. Id các trang khai báo ở `core/navigation/src/main/res/values/ids.xml` để `AppRouter` dùng được.

## 3. Yêu cầu môi trường

- Android Studio bản mới (hỗ trợ AGP 9.x), JDK 17+
- compileSdk / targetSdk 37, minSdk 24
- Gradle dùng wrapper đi kèm (`./gradlew`)
- Backend chạy ở cổng 8080 (xem README của backend)

## 4. Chạy app

1. Khởi động backend trước. Mặc định server chạy ở `http://localhost:8080/api`.
2. Mở thư mục `ecom_app` bằng Android Studio, chờ Gradle Sync xong rồi bấm Run. Hoặc chạy:
   ```bash
   ./gradlew installDebug
   ```

**Địa chỉ backend** lấy từ `BuildConfig.BASE_URL` (cấu hình trong `core/data/build.gradle.kts`):

| Chạy trên | Cấu hình |
|---|---|
| Emulator | Không cần làm gì, mặc định là `http://10.0.2.2:8080/api` |
| Máy thật cắm USB | Chạy `adb reverse tcp:8080 tcp:8080`, rồi đặt `api.baseUrl=http://localhost:8080/api` trong `local.properties` |
| Máy thật qua WiFi | Đặt `api.baseUrl=http://<IP-máy-tính>:8080/api` trong `local.properties`, và thêm IP đó vào `app/src/main/res/xml/network_security_config.xml` |

Sửa `local.properties` xong thì Sync lại Gradle. File này không được commit, mỗi máy tự cấu hình.

Tài khoản demo (dữ liệu mẫu của backend): `0123456789` / `123456`.

## 5. API contract

Mọi response đều có dạng:

```json
{ "success": true, "message": "OK", "data": { } }
```

Khi lỗi thì `success = false`, `data = null`, HTTP status là 4xx hoặc 5xx. API cần đăng nhập thì gửi kèm header `Authorization: Bearer <token>`.

| Method | Path | Token | Body | Mô tả |
|---|---|:---:|---|---|
| POST | `/auth/login` | | `{phone, password}` | Đăng nhập, trả `{token, user}` |
| POST | `/auth/register` | | `{name, phone, password}` | Đăng ký, trả `{token, user}` |
| GET | `/users/me` | ✔ | | Thông tin người dùng hiện tại |
| GET | `/products?keyword=` | | | Danh sách sản phẩm (keyword tuỳ chọn) |
| GET | `/products/{id}` | | | Chi tiết sản phẩm |
| GET | `/orders` | ✔ | | Đơn hàng của tôi |
| GET | `/orders/{id}` | ✔ | | Chi tiết đơn |
| POST | `/orders` | ✔ | `{address?, items:[{productId, quantity}]}` | Tạo đơn |
| POST | `/orders/{id}/cancel` | ✔ | | Huỷ đơn đang `PENDING` |

Path khai báo tập trung ở `core/data/.../api/ApiEndpoints.java`. `BASE_URL` đã gồm sẵn `/api`.

## 6. Hướng dẫn mở rộng

### Thêm một trang mới (ví dụ giỏ hàng)
1. Tạo module `feature/cart/`: copy `build.gradle.kts` của một feature có sẵn, đổi `namespace = "com.example.ecom.feature.cart"`.
2. `settings.gradle.kts`: thêm `include(":feature:cart")`. `app/build.gradle.kts`: thêm `implementation(project(":feature:cart"))`.
3. Tạo `CartFragment extends BaseFragment` cùng layout trong module mới.
4. Thêm `<item name="cartFragment" type="id" />` vào `core/navigation/src/main/res/values/ids.xml`.
5. Khai báo `<fragment android:id="@id/cartFragment" ...>` trong `app/src/main/res/navigation/nav_graph.xml`.
6. Đăng ký `LayoutType` cho trang ở `RouteConfig`, và thêm hàm `toCart(...)` vào `AppRouter`.

### Gọi thêm một API
1. Thêm path vào `ApiEndpoints`.
2. Thêm model (nếu cần) vào `core/data/model`, có hàm `fromJson(...)`.
3. Thêm hàm vào Repository tương ứng, gọi `api.get/post/put/delete(...)`.
4. Trong Fragment: `repository.xxx(apiCallback(data -> { ... }))`.

### Module quản trị (`:feature:admin`)

Giao diện Java + XML sử dụng Material Components và cùng `MainActivity` của ứng dụng. Trên màn đăng nhập, nhấn **Xem bản mẫu quản trị** để mở ngay, không cần backend.

```
feature/admin/
├── build.gradle.kts
└── src/main/
    ├── java/com/example/ecom/feature/admin/
    │   ├── activities/AdminShellController.java
    │   ├── fragments/   # Tổng quan, sản phẩm, form, đơn hàng, thống kê, khách hàng, cài đặt
    │   ├── adapters/    # RecyclerView sản phẩm, đơn hàng, khách hàng
    │   ├── models/      # Model hiển thị biểu đồ
    │   └── utils/       # Biểu đồ Canvas, định dạng, tải ảnh cục bộ
    └── res/
        ├── layout/     # Mỗi trang có file fragment_admin_*.xml riêng
        ├── drawable/   # Material vector icons, ảnh minh họa quần áo, background
        ├── menu/       # Menu và BottomNavigationView 5 mục
        ├── color/      # Màu trạng thái selected/focused
        └── values/     # Chuỗi tiếng Việt, màu, style, size/màu sắc
```

- `AdminFragment` kế thừa `BaseFragment`. Các trang điều hướng qua `AppRouter`; `LayoutType.ADMIN` và `ADMIN_DETAIL` dùng chung header cam và footer quản trị. Form/chi tiết ẩn footer, hiển thị nút quay lại.
- Mở bằng `AppRouter.toAdminDashboard(this)`. Chạy trực tiếp bản xem trước bằng `adb shell am start -n com.example.ecom/.MainActivity --ez admin_preview true` sau khi cài APK.
- Model nghiệp vụ ở `core/data/model/admin`, dữ liệu mẫu và lưu cục bộ bằng SharedPreferences ở `AdminRepository`. Feature chỉ gọi Repository, không phụ thuộc feature khác; khi có backend, thay phần triển khai Repository bằng API quản trị có phân quyền.
- Dữ liệu mẫu gồm 8 sản phẩm, 8 hồ sơ khách hàng và 24 đơn hàng. Tổng quan có số minh họa theo thiết kế (256 sản phẩm, 1.328 người dùng, 12.450.000 đ) và chuỗi doanh thu minh họa 09/11–15/11. Trang thống kê tính từ các đơn mẫu **đã giao**, theo bộ lọc ngày/tháng/năm, và cập nhật khi đổi trạng thái đơn.
- Thêm/sửa/xóa sản phẩm, tìm kiếm/bộ lọc, lịch sử khách hàng, cài đặt và quy trình đơn hàng đều hoạt động cục bộ. Chạm sản phẩm để xem chi tiết rồi sửa/xóa; xóa và hủy đơn có xác nhận. Quy trình: chờ xác nhận → đã xác nhận → đang xử lý → đang giao → đã giao; chỉ hủy khi chờ/đã xác nhận.
- Form kiểm tra tên, danh mục, giá, tồn kho, độ dài mô tả; hỗ trợ chọn size/màu và tối đa 6 ảnh qua trình chọn tài liệu Android. Ảnh được tải giảm kích thước ngoài main thread. Ảnh quần áo mặc định là vector đóng gói sẵn.
- Biểu đồ vẽ bằng `RevenueChartView` (Canvas), không dùng thư viện biểu đồ cũ hoặc repository bổ sung. Dùng phiên bản Material/AppCompat sẵn có trong version catalog của dự án.

### Quy ước
- Mỗi người hoặc nhóm phụ trách một module `feature/*`.
- Chuỗi hiển thị đặt trong `strings.xml` của module, không viết cứng trong Java. Chuỗi có tham số thì dùng `%1$s` / `%1$d`.
- Màu lấy từ `core/ui/src/main/res/values/colors.xml`.
- Đổi API (path, body, field) phải thống nhất với bên backend trước.

## 7. Sự cố thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| Code bị gạch đỏ hàng loạt nhưng build vẫn chạy | Chạy **File → Sync Project with Gradle Files**. Vẫn đỏ thì **File → Invalidate Caches → Restart** |
| `BuildConfig` bị đỏ | Build một lần (`./gradlew assembleDebug`) để sinh class |
| "Không thể kết nối tới máy chủ" | Backend chưa chạy, sai `api.baseUrl`, hoặc máy thật chưa chạy `adb reverse` |
| `CLEARTEXT communication ... not permitted` | Thêm host vào `network_security_config.xml` |
| Đang dùng thì bị đá về màn đăng nhập | Backend vừa khởi động lại nên token cũ hết hiệu lực. Đăng nhập lại là được |
