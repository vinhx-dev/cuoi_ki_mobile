# Quản trị dùng API thật

## Giao diện quản trị

Giao diện dùng nền xám sáng, chữ xanh than và điểm nhấn cam. Dashboard có thẻ doanh thu,
các chỉ số và lối tắt quản lý; thanh tab có dấu chọn rõ ràng. Thẻ sản phẩm/người dùng/đơn
dùng cùng kiểu chữ, khoảng cách và nhãn trạng thái có màu. Bộ lọc ID người dùng trong đơn
hàng được mở bằng nút lọc; form chia nhóm và có thanh Lưu/Hủy cố định, phần dữ liệu bổ sung
có thể thu gọn. Các màn chi tiết và cài đặt cũng dùng cùng bộ giao diện.

Smoke test hỗ trợ `-e screenshots true` để chụp các màn vào thư mục external files
`admin-ui-qa` của app. Ảnh kiểm tra local lưu ở `build/qa/admin-ui-qa`.

## API và phiên đăng nhập

`AdminApi` gọi qua `ApiClient.requestStrict`, dùng base URL và OkHttp của `AuthNetwork`
(Bearer token, refresh token), không dùng `MockTransport`. Base URL đang build:
`http://10.0.2.2:8080/api/`. Các đường dẫn trong admin không chứa thêm `/api`.

- Login và khôi phục phiên `/auth/me` dùng role backend trả về để mở `AdminActivity`.
- Sản phẩm: `/admin/products`, `/admin/products/{id}`; thêm/sửa/xóa ở `/products`.
  Form lấy ID danh mục từ `/categories`, chọn ảnh từ máy, giá thập phân, số lượng,
  mô tả, size tùy ý, lượt bán và JSON `forAI`. Xóa có xác nhận; `data:null` vẫn thành công.
- Đơn hàng: `/admin/orders` với `status` và `userId`, chi tiết có thông tin người nhận.
  PATCH status chỉ cho `PENDING → SHIPPING/CANCELLED`, `SHIPPING → DELIVERED/CANCELLED`.
  Giao diện nhận trạng thái từ response server sau thao tác.
- Người dùng: tìm kiếm/lọc `/admin/users`; chi tiết và thống kê `/admin/users/{id}`;
  đơn của người dùng lấy bằng `/admin/orders?userId=...`.
- Dashboard/doanh thu: `/admin/stats/dashboard`, `/admin/stats/top-products`,
  `/admin/stats/categories`. Thống kê danh mục là **số sản phẩm**, không phải doanh thu.
  Không hiển thị biểu đồ chuỗi doanh thu vì backend chưa có chuỗi này.

Không còn `AdminLocalStore` hoặc dữ liệu seed trong luồng quản trị.
Lỗi mạng/server hiển thị cùng nút thử lại; 401 đưa về đăng nhập, 403 giữ thông báo server.
Các thao tác đang gửi được chặn gửi trùng, kể cả khi Activity được tạo lại.
Cài đặt có nhãn lưu trên thiết bị; backend chưa có API cài đặt cửa hàng.
UI hiện chưa có màn CRUD danh mục hoặc nút khóa/mở khóa người dùng.
`AdminApi.userStatus` đã có hợp đồng PATCH để dùng khi bổ sung nút này.

## Kiểm tra

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest '-PsmokeTestRunner=com.example.ecom.AdminIntegrationSmokeTest' --offline --console=plain
```

Cài hai APK debug và androidTest lên emulator đã đăng nhập admin, rồi chạy:

```powershell
adb -s emulator-5554 shell am instrument -w -r com.example.ecom.test/com.example.ecom.AdminIntegrationSmokeTest
```

Test mặc định chỉ đọc và kiểm tra form trống. Thêm `-e productCrud true` trước tên runner
để tạo một sản phẩm QA riêng, kiểm tra POST/PUT/GET, mở chi tiết/form sửa,
rồi DELETE bản ghi vừa tạo; kiểm tra response xóa `data:null`.
Không có credential hoặc token được gán trong test.

Ảnh sản phẩm/danh mục dùng trường `image` chứa Base64 JPEG/PNG. Backend lưu data URI
`data:image/png;base64,...` hoặc `data:image/jpeg;base64,...` trong cột TEXT, tối đa
2 MiB sau giải mã, mỗi chiều tối đa 4096 và tổng tối đa 16 triệu pixel. Form tự sửa
chiều ảnh theo EXIF, thu nhỏ cạnh dài tối đa 1600 và nén trước khi gửi; không gửi URI
`content://`. Giữ nguyên ảnh khi PUT bỏ trường `image`, xóa ảnh bằng chuỗi rỗng.
Ảnh URL cũ vẫn đọc được; ảnh mới không nhận URL. Base64 không lưu vào Bundle của form.
Bộ hiển thị dùng chung đọc cả Base64 và URL cũ, không gửi Bearer sang host ảnh ngoài.
Database cũ cần chạy `database/migrations/20261120_base64_images.sql` trong repo
backend để nới cả bốn cột ảnh VARCHAR(2048) sang TEXT, giữ nguyên dữ liệu.

Kiểm tra xử lý ảnh local không cần đăng nhập bằng `-e imagesOnly true` trước tên runner.
`-e productCrud true` còn kiểm tra Base64 được POST, giữ qua PUT và đọc lại qua GET.

Đã kiểm tra local: phiên ROLE_ADMIN được server xác nhận, khôi phục vào admin,
dashboard, danh sách/bộ lọc, chi tiết/thống kê người dùng, doanh thu/top sản phẩm/danh mục,
và CRUD sản phẩm QA (đã xóa). Đợt Base64 kiểm tra thêm PNG/JPEG, thu nhỏ, giải mã
hiển thị trên emulator; API sản phẩm/danh mục giữ ảnh khi bỏ trường, xóa ảnh,
chuẩn hóa Base64 thuần; từ chối URL mới, content URI, ảnh lỗi/sai MIME (400),
ảnh quá 2 MiB (413). Đã xóa cả sản phẩm và danh mục QA. DB có 3 người dùng và chưa có đơn hàng.
Chưa kiểm tra đăng nhập bằng mật khẩu mới, chi tiết/chuyển trạng thái đơn thực tế,
mất mạng hoặc refresh token hết hạn trong lần chạy này. Trình chọn ảnh hệ thống
chưa được kiểm tra bằng tay; phần mã hóa/hiển thị dùng ảnh local được kiểm tra tự động.

Kiểm tra thủ công: đăng nhập ROLE_ADMIN → Dashboard → Sản phẩm → Thêm sản phẩm
→ chọn ảnh từ máy và nhập các trường → Lưu → Chi tiết → Sửa/Xóa (có xác nhận).
Sau khi có đơn thật: Đơn hàng → lọc trạng thái/ID người dùng → mở chi tiết
→ bàn giao vận chuyển/đánh dấu đã giao hoặc hủy.
Người dùng → tìm kiếm/lọc → mở tài khoản → xem thống kê và các đơn.
