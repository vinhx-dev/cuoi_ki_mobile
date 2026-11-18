# Báo cáo giao diện Customer Fashion Shop

## Ảnh tham chiếu đã đọc

USERPROFILE thực tế: `C:\Users\ASUS`. Desktop dùng OneDrive: `C:\Users\ASUS\OneDrive\Desktop`.
Đã kiểm tra Desktop thường và OneDrive, mở trực tiếp đủ sáu ảnh trước khi triển khai giao diện. `trangadmin.png` chưa có ở lần kiểm tra đầu, sau đó đã xuất hiện, được mở và phân tích trước khi viết code.

| Ảnh | Phân tích và áp dụng |
| --- | --- |
| trangadmin.png | Header và CTA cam tươi, nền trắng/xám nhạt, viền mảnh, card bo góc, icon nét, tiêu đề sans-serif đậm. Dùng palette cam #FF6500 và #F45100 từ đặc tả; font hệ thống sans. |
| 1.png | Search có camera/tìm kiếm/giỏ hàng, 8 danh mục 4 cột × 2 hàng, gợi ý 2 cột. |
| 2.png | Header có Back, tiêu đề theo danh mục, danh sách sản phẩm 2 cột cuộn dọc. |
| 3.png | Ảnh lớn, giá nổi bật, tên/mô tả, sản phẩm tương tự; thanh giỏ hàng/mua ngay cố định, không Bottom Navigation. |
| 4.png | Sheet trên nền tối, ảnh nhỏ/tổng tiền, bộ tăng giảm số lượng, size độc quyền, xác nhận; bo hai góc trên và thanh kéo. |
| 5.png | Thanh trắng, viền trên, 3 tab icon trên/chữ dưới: Trang chủ, Trạng thái, Tôi; cam khi chọn, xám khi chưa chọn. |

Không tìm thấy bộ ảnh tham chiếu hoặc ảnh sản phẩm trong project; chỉ có logo đăng nhập. Không tự cắt ảnh từ phác thảo để làm ảnh hàng hóa.

## Công nghệ và phạm vi

Giữ Java/XML, Fragment, AndroidX Navigation, Material Components, package và cấu trúc module hiện có. Không đổi Gradle Wrapper, AGP, SDK hay phiên bản thư viện. `app/build.gradle.kts` chỉ bổ sung runner kiểm thử instrumentation, không thêm dependencies. Home và Product List dùng chung `HomeFragment` qua hai route khác nhau. Orders/Profile và các chức năng đăng nhập, hủy đơn, đăng xuất được giữ lại. Không sửa layout Admin.

## Các màn hình và tương tác

- Home: 8 danh mục bấm được; 6 sản phẩm mẫu gợi ý; grid 2 cột, tìm kiếm tên, retry tải cửa hàng.
- Product List: route danh mục riêng, lọc chính xác theo category và từ khóa, Back và tab Trang chủ hoạt động; danh mục chưa có hàng hiển thị trạng thái trống.
- Product Detail: cùng nguồn dữ liệu và ID với danh sách; giá VNĐ, mô tả, sản phẩm cùng danh mục; hai CTA cố định; không Bottom Navigation.
- Product Option Bottom Sheet: hai chế độ thêm giỏ/mua ngay; giữ size/số lượng khi khôi phục sheet; size S/M/L/XL chỉ chọn một; bắt buộc size, số lượng tối thiểu 1, giới hạn tồn kho, tổng tiền cập nhật trực tiếp; hỗ trợ kéo/Back/chạm ngoài/nút Đóng.
- Giỏ hàng cục bộ dùng SharedPreferences theo tên phiên người dùng, gộp product + size, kiểm tra tổng số lượng các size theo tồn kho; icon giỏ header mở danh sách đã lưu.
- Mua ngay sản phẩm thật gọi `POST /orders` hiện có, gửi thêm `size`; chỉ phản hồi thành công và mở chi tiết đơn khi API thực sự thành công. Khóa thao tác khi đang gửi.
- Phát hiện ApiClient cũ tự fallback sang MockTransport. Bổ sung `getRemote`/`postRemote` cho catalogue và mua ngay mới để không nhận sản phẩm/đơn hàng giả khi mất mạng. Không đổi hành vi mock của các luồng cũ ngoài phạm vi.
- Safe area dùng xử lý system bars/IME có sẵn trong MainActivity, sheet xử lý inset riêng. Nội dung nằm trong vùng có trọng số phía trên footer và cuộn được.

## Kiểm tra đã thực hiện

Lệnh cuối: `gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --console=plain`.
Kết quả: **BUILD SUCCESSFUL**. Android Lint: **0 errors, 4 warnings**; cả bốn cảnh báo chỉ đề xuất nâng phiên bản AGP/Lifecycle đang có, đã giữ nguyên theo yêu cầu. `git diff --check` qua.

Đã cài APK và APK kiểm thử lên `emulator-5554`, chạy:

```powershell
adb shell am instrument -w com.example.ecom.test/com.example.ecom.CustomerUiSmokeTest
```

Kết quả **PASS**: lọc danh mục và giao từ khóa/danh mục; size bắt buộc và tồn kho giỏ giữa các size; Home → chi tiết; Home → danh mục → chi tiết; không footer ở chi tiết; sheet chọn size, tăng/giảm và tổng 150.000/300.000 ₫; không đặt mua giả; Back; nhấn tab Trang chủ từ danh sách.

Runner chỉ dùng trong APK kiểm thử, cung cấp trạng thái router đăng nhập cho kiểm tra UI mà không lưu phiên đăng nhập giả vào app. Kiểm thử giỏ dùng vùng SharedPreferences riêng. Chưa kiểm thử đăng nhập/đặt đơn/thanh toán thực với backend.

Ảnh chụp emulator đã mở và kiểm tra trực tiếp:

- [Home](customer-ui/screenshots/01-home.png)
- [Danh sách](customer-ui/screenshots/02-product-list.png)
- [Chi tiết](customer-ui/screenshots/03-detail.png)
- [Bottom Sheet](customer-ui/screenshots/04-sheet.png)
- [Sheet đã chọn size và chặn mua mẫu](customer-ui/screenshots/05-sheet-selected.png)

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Build log: `app/build/outputs/customer-ui-build.log`.
Báo cáo Lint: `app/build/reports/lint-results-debug.html`.

## Phần cần bổ sung / giới hạn

- Chưa có ảnh sản phẩm thật; tất cả ảnh đang dùng vector minh họa theo nhóm và có thông báo. Cần bổ sung ảnh hoodie, áo khoác váy, cotton sọc, váy nữ, caro, giày, khoác nhẹ, polo, sơ mi trắng và hoodie. Model giữ `imageUrl` để tích hợp ảnh backend sau; chưa có bộ tải ảnh URL.
- Backend thật chưa được xác minh. Cần trả category/categoryName để lọc chính xác và hỗ trợ trường size trong đơn hàng; danh mục thiếu metadata không tự suy đoán từ tên sản phẩm. Tồn kho hiện có ở cấp sản phẩm, chưa có tồn kho riêng theo size.
- Không có checkout mới hoặc thanh toán giả. Sản phẩm mẫu không được gửi lên API đơn hàng; mua thật còn phụ thuộc server, địa chỉ mặc định và cơ chế đặt nhanh có sẵn.
- Icon camera có phản hồi tính năng tìm ảnh chưa hỗ trợ; không dựng API nhận diện ảnh giả.
- Kiểm thử UI đã thực hiện trên emulator, chưa chạy trên máy Android vật lý hoặc toàn bộ kích thước/font hệ thống.

## File mới

- `app/src/androidTest/java/com/example/ecom/CustomerUiSmokeTest.java`
- `core/data/src/main/java/com/example/ecom/core/data/LocalCart.java`
- `core/data/src/main/java/com/example/ecom/core/data/ShopCatalog.java`
- `core/data/src/main/java/com/example/ecom/core/data/ShopProduct.java`
- `core/ui/src/main/java/com/example/ecom/core/ui/shop/CatalogRepository.java`
- `core/ui/src/main/java/com/example/ecom/core/ui/shop/ProductGridAdapter.java`
- `core/ui/src/main/java/com/example/ecom/core/ui/shop/ShopViews.java`
- `core/ui/src/main/res/drawable/shop_bag.xml`
- `core/ui/src/main/res/drawable/shop_camera.xml`
- `core/ui/src/main/res/drawable/shop_cap.xml`
- `core/ui/src/main/res/drawable/shop_cart.xml`
- `core/ui/src/main/res/drawable/shop_dress.xml`
- `core/ui/src/main/res/drawable/shop_glasses.xml`
- `core/ui/src/main/res/drawable/shop_nav_background.xml`
- `core/ui/src/main/res/drawable/shop_pants.xml`
- `core/ui/src/main/res/drawable/shop_shirt.xml`
- `core/ui/src/main/res/drawable/shop_shoe.xml`
- `core/ui/src/main/res/drawable/shop_watch.xml`
- `core/ui/src/main/res/values/shop_styles.xml`
- `docs/customer-ui/screenshots/01-home.png`
- `docs/customer-ui/screenshots/02-product-list.png`
- `docs/customer-ui/screenshots/03-detail.png`
- `docs/customer-ui/screenshots/04-sheet.png`
- `docs/customer-ui/screenshots/05-sheet-selected.png`
- `feature/product/src/main/java/com/example/ecom/feature/product/ProductOptionBottomSheet.java`
- `docs/CUSTOMER_UI.md` (báo cáo này)

## File đã sửa

- `app/build.gradle.kts`
- `app/src/main/res/values-night/themes.xml`
- `app/src/main/res/values/themes.xml`
- `core/api/src/main/java/com/example/ecom/core/api/ApiClient.java`
- `core/navigation/src/main/java/com/example/ecom/core/navigation/AppPages.java`
- `core/navigation/src/main/java/com/example/ecom/core/navigation/AppRouter.java`
- `core/ui/src/main/java/com/example/ecom/core/ui/base/BaseFragment.java`
- `core/ui/src/main/java/com/example/ecom/core/ui/layout/PageLayout.java`
- `core/ui/src/main/layouts/detail/res/layout/detail_header.xml`
- `core/ui/src/main/layouts/main/res/layout/main_bottom_bar.xml`
- `core/ui/src/main/layouts/main/res/layout/main_header.xml`
- `core/ui/src/main/layouts/main/res/values/strings.xml`
- `core/ui/src/main/res/color/bottom_nav_color_selector.xml`
- `core/ui/src/main/res/layout/view_search_bar.xml`
- `core/ui/src/main/res/values/colors.xml`
- `feature/home/src/main/java/com/example/ecom/feature/home/HomeFragment.java`
- `feature/home/src/main/java/com/example/ecom/feature/home/HomeViewModel.java`
- `feature/home/src/main/res/layout/fragment_home.xml`
- `feature/product/src/main/java/com/example/ecom/feature/product/ProductDetailFragment.java`
- `feature/product/src/main/java/com/example/ecom/feature/product/ProductDetailViewModel.java`
- `feature/product/src/main/java/com/example/ecom/feature/product/data/ProductApi.java`
- `feature/product/src/main/res/layout/fragment_product_detail.xml`
