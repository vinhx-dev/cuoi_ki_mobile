# Ecom App (Android)

Base code app bán hàng Android viết bằng Java: Single Activity, chia nhiều module (multi-module), điều hướng bằng Jetpack Navigation.
App chỉ chứa **frontend**. Backend là một project Maven riêng, app gọi tới qua HTTP/JSON (xem mục [API](#5-api-contract)).

Tính năng có sẵn: đăng nhập/đăng ký, danh sách và tìm kiếm sản phẩm, chi tiết sản phẩm, đặt hàng, danh sách và chi tiết đơn, huỷ đơn, hồ sơ, đăng xuất.

---

## 1. Kiến trúc (đối chiếu với ReactJS)

| ReactJS | Android (dự án này) | Ở đâu |
|---|---|---|
| `<RouterProvider>` | `MainActivity`: **chỉ** chứa NavHost để hiển thị trang | `app/` |
| `<Routes>` + `<ProtectedRoute>` | `AppPages`: khai báo mọi trang, mỗi trang chọn **layout** và **có cần đăng nhập không** | `core/navigation/` |
| `useNavigate()` | `AppRouter.toXxx(...)` (tự chặn trang cần đăng nhập) | `core/navigation/` |
| 1 instance `axios` dùng chung | `ApiClient`: `get/post/put/delete` (JSON) + `upload` (ảnh, multipart) | `core/api/` |
| `localStorage` | `LocalStorage`, `SessionManager` | `core/data/` |
| `layouts/MainLayout/`, `AdminLayout/`... | `core/ui/src/main/layouts/<tên>/` | `core/ui/` |
| `features/home/pages/HomePage.jsx` | `HomeFragment` + `fragment_home.xml` | `feature/home/` |
| `features/home/hooks/useHome.js` | `HomeViewModel` | `feature/home/` |
| `features/home/api/products.js` | `data/ProductApi` + `data/Product` | `feature/home/data/` |
| `{ data, isLoading, error }` | `UiState<T>` | `core/ui/.../base/` |

Luồng hiển thị một trang:

```
MainActivity (NavHost)
   └─ AppPages: "home" → HomeFragment, layout MAIN, LOGIN_REQUIRED
         ├─ chưa đăng nhập → AppRouter tự chuyển sang "login"
         └─ BaseFragment bọc trang bằng layout:  layouts/main/layout_main.xml
               ├─ main_header.xml        (thanh tìm kiếm)
               ├─ part_content.xml       ← fragment_home.xml được đặt vào đây
               └─ main_bottom_bar.xml
```

Luồng dữ liệu của 1 trang:

```
HomeFragment (page) ─► HomeViewModel (hook) ─► data/ProductApi (feature) ─► ApiClient (core) ─HTTP─► Backend
      ▲                       │
      └── observe(state) ◄────┘   LiveData<UiState<T>>: tự loading, tự toast lỗi, giữ được khi xoay màn hình
```

- **Mỗi trang tự quản lý dữ liệu của nó** trong ViewModel riêng. Fragment không gọi API trực tiếp.
- **Mỗi feature tự giữ API và model của nó** trong `data/`. `core:api` chỉ có hàm gọi chung, không biết gì về sản phẩm hay đơn hàng.
- Hành động (đăng nhập, đặt hàng...): `vm.xxx(..., apiCallback(data -> ...))`, giống `mutation.mutate(data, { onSuccess })`.
- Khi API trả **401**, `ApiClient` xoá phiên đăng nhập và `MainActivity` đưa về Login.

## 2. Cấu trúc thư mục

```
ecom_app/
├── app/                                   :app   chỉ có MainActivity (hiển thị navigation)
├── core/
│   ├── navigation/                        :core:navigation
│   │   ├── AppPages.java                  ★ KHAI BÁO TRANG: đường dẫn, class, layout, cần đăng nhập?
│   │   ├── AppRouter.java                 hàm chuyển trang toXxx() + chặn trang cần đăng nhập
│   │   ├── LayoutType.java                NONE / MAIN / DETAIL / ADMIN
│   │   ├── Access.java                    PUBLIC / LOGIN_REQUIRED
│   │   └── RouteTable.java                bộ dựng NavGraph (không cần sửa)
│   ├── api/                               :core:api   ≈ axios
│   │   ├── ApiClient.java                 ★ get / post / put / delete / upload
│   │   ├── JsonBody.java, MultipartBody.java, FilePart.java   body JSON / form có ảnh
│   │   └── HttpTransport.java, ApiConfig.java, ApiCallback.java ...
│   ├── data/                              :core:data  ≈ localStorage
│   │   ├── LocalStorage.java              get / set / remove / clear
│   │   └── SessionManager.java            token + tên user đăng nhập
│   └── ui/                                :core:ui
│       ├── src/main/layouts/              ★ MỖI LAYOUT 1 THƯ MỤC
│       │   ├── none/    layout_none.xml
│       │   ├── main/    layout_main.xml, main_header.xml, main_bottom_bar.xml, menu, icon tab
│       │   ├── detail/  layout_detail.xml, detail_header.xml
│       │   └── admin/   layout_admin.xml, admin_header.xml
│       ├── src/main/res/                  phần dùng chung mọi layout: part_content.xml, view_search_bar.xml, màu, chuỗi
│       └── java/.../ui/
│           ├── layout/                    AppLayout (LayoutType → file XML), PageLayout (back, tìm kiếm, tab, loading)
│           ├── base/                      BaseFragment, BaseViewModel, UiState
│           └── format/Formatter.java
└── feature/                               mỗi feature: giao diện + Java + dữ liệu riêng
    ├── splash/   SplashFragment
    ├── auth/     data/AuthApi · login/ (Fragment + ViewModel) · register/ (Fragment + ViewModel)
    ├── home/     data/ProductApi, Product · HomeFragment, HomeViewModel, ProductAdapter
    ├── product/  data/ProductApi, Product · ProductDetailFragment, ProductDetailViewModel
    ├── order/    data/OrderApi, Order, OrderItem · list/ (...) · detail/ (...)
    └── profile/  data/ProfileApi, User · ProfileFragment, ProfileViewModel
```

**Quy tắc phụ thuộc:**

```
:app ──► :core:*  +  mọi :feature:*          (để đóng gói vào APK)
:feature:* ──► :core:api, :core:data, :core:ui, :core:navigation
:core:ui ──► :core:api, :core:navigation
:core:api ──► :core:data                     (lấy token từ LocalStorage)
:core:navigation, :core:data ──► (không phụ thuộc module nào của dự án)
```

- **Feature không phụ thuộc feature khác.** Muốn mở trang của feature khác thì gọi `AppRouter`.
- Hai feature cần cùng một kiểu dữ liệu (vd. `Product`) thì **mỗi feature tự khai báo bản của mình**, chỉ với các trường nó cần. Chấp nhận lặp một chút để các feature độc lập.
- `AppPages` khai báo trang bằng **tên class** (chuỗi) vì `core` không được phụ thuộc vào `feature`. Gõ sai tên thì app báo lỗi ngay khi mở, chỉ rõ trang nào sai.

## 2b. Chuyển dữ liệu giữa các trang

Quy tắc chung: **chỉ truyền ID, trang mới tự tải dữ liệu của nó.** Không truyền nguyên object qua lại.

| Tình huống | Cách làm | ≈ React | Trạng thái |
|---|---|---|---|
| Mở trang chi tiết (sản phẩm, đơn hàng) | Tham số trên đường dẫn: `product/{productId}`, đọc bằng `getArguments()` | `useParams()` | ✅ đang dùng |
| Mở trang kèm bộ lọc, từ khoá | Tham số tuỳ chọn: `home?query={query}` | `useSearchParams()` | ✅ đang dùng |
| Trang con trả kết quả về trang trước (chọn địa chỉ, chọn ảnh...) | `previousBackStackEntry.savedStateHandle` của Navigation | `navigate(-1)` kèm state | Gợi ý, chưa làm |
| Nhiều bước của 1 luồng (giỏ hàng → địa chỉ → thanh toán) | ViewModel dùng chung cho nhóm trang đó | Context bọc 1 nhóm route | Gợi ý, chưa làm |
| Dữ liệu cả app cùng dùng (user đăng nhập, số lượng giỏ hàng) | Store dùng chung trong `core` (`LiveData`) | Redux / Zustand / Context | Gợi ý, chưa làm (hiện chỉ có `SessionManager`) |

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

**Địa chỉ backend** lấy từ `BuildConfig.BASE_URL` (cấu hình trong `core/api/build.gradle.kts`):

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

Path nằm trong class API của từng feature (vd. `feature/order/.../data/OrderApi.java`). `BASE_URL` đã gồm sẵn `/api`.

## 6. Hướng dẫn mở rộng

### Thêm một trang mới (ví dụ giỏ hàng)
1. (Nếu là feature mới) Tạo module `feature/cart/`: copy `build.gradle.kts` của một feature có sẵn, đổi `namespace`. Thêm `include(":feature:cart")` vào `settings.gradle.kts` và `implementation(project(":feature:cart"))` vào `app/build.gradle.kts`.
2. Tạo trang trong feature:
   - `fragment_cart.xml`: chỉ phần nội dung, không có header/bottom bar.
   - `CartFragment`: `public CartFragment() { super(R.layout.fragment_cart); }`
   - `CartViewModel extends BaseViewModel`: dữ liệu của trang.
   - `data/CartApi` + model: các API trang này gọi.
3. `core/navigation/AppPages.java`: thêm đường dẫn và 1 dòng khai báo, **chọn layout và có cần đăng nhập không**:
   ```java
   public static final String CART = "cart";
   r.page(CART, FEATURE + "cart.CartFragment", LayoutType.DETAIL, LOGIN_REQUIRED);
   ```
4. `AppRouter`: thêm hàm `toCart(Fragment from)`.

### Thêm một layout mới
1. Tạo thư mục `core/ui/src/main/layouts/<tên>/res/layout/layout_<tên>.xml`. **Bắt buộc** có `<include layout="@layout/part_content"/>`. Header, menu, icon riêng của layout cũng đặt trong thư mục này. Đặt tên file có tiền tố `<tên>_` để không trùng với layout khác.
2. Thêm tên vào `core/navigation/.../LayoutType.java`.
3. Thêm 1 dòng vào `AppLayout.xmlOf()` trong `core/ui/.../layout/`.

### Gọi thêm một API
1. Trong feature cần dùng: thêm hàm vào `data/XxxApi.java`, gọi `api.get / post / put / delete(...)`. Body JSON tạo nhanh bằng `ApiClient.json("key", value, ...)`.
2. Gửi ảnh: `api.upload(path, fields, List.of(FilePart.fromUri(context, "avatar", uri)), parser, cb)`.
3. Thêm model vào `data/` (nếu cần), có hàm `fromJson(...)`.
4. ViewModel: `fetch(state, cb -> xxxApi.getXxx(cb))` để tải dữ liệu, hoặc nhận `ApiCallback` từ Fragment cho hành động.
5. Fragment: `observe(vm.getXxx(), data -> { ... })`, hoặc `vm.doXxx(apiCallback(data -> { ... }))`.

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
