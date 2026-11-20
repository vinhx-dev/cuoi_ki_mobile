# Hướng dẫn cài Ecom Android và backend cho người nhận và AI

Bộ hướng dẫn này dùng để cài dự án trên máy Windows mới: PostgreSQL, backend Spring Boot, Gmail gửi OTP và app Android. Làm theo thứ tự **database → Gmail → JWT → backend → Android → kiểm tra OTP/admin**. Người nhận tự nhập thông tin riêng vào `.env`; bộ bàn giao chỉ chứa mẫu và code.

## 1. Hai repository và trạng thái bàn giao

| Phần | Repository | Trạng thái tại lần bàn giao này |
|---|---|---|
| Android Java | https://github.com/vinhx-dev/cuoi_ki_mobile | Nhánh `Mạnh` rồi `main`: API admin, giao diện mới, chọn ảnh và hiển thị Base64 |
| Backend Spring Boot | https://github.com/vinhx-dev/cuoi_ki_mobile_be | Repo riêng, nhánh `Mạnh` rồi `main`: kiểm tra ảnh Base64, migration TEXT và launcher Windows. Gmail/JWT thật chỉ lưu local |

**Clone backend vẫn cần cấu hình chạy local.** Dùng các file mẫu trong thư mục `backend/` của bộ hướng dẫn này để tạo `.env` và `application.properties`. Launcher `run-local.ps1` có trong repo backend; không mang khóa thật từ máy người khác sang.

Một số mục trong README Android còn mô tả hợp đồng cũ. Luồng hiện tại đăng nhập bằng `email/password`; khi có khác biệt, đọc controller/DTO backend và `core/api/.../auth/AuthService.java`.

## 2. Công cụ cần có

| Thành phần | Yêu cầu của source hiện tại |
|---|---|
| Backend | JDK **25**, PostgreSQL đang chạy; Maven Wrapper đi kèm repo |
| Android | Android Studio hỗ trợ AGP `9.5.1`; dùng Gradle Wrapper `9.6.0` của repo |
| Android SDK | compileSdk/targetSdk `37`; minSdk `24` |
| Thiết bị kiểm tra | Android Emulator; backend mặc định cổng `8080` |
| Terminal trong hướng dẫn | PowerShell trên Windows |

Không tự đổi version thư viện chỉ để cài. Chọn Gradle JDK tương thích trong Android Studio; yêu cầu JDK 25 ở bảng trên áp dụng cho backend.

## 3. Lấy source và chuẩn bị PostgreSQL

Ví dụ thư mục gốc là `D:\Mobie`; có thể đổi sang vị trí khác.

```powershell
Set-Location D:\Mobie
git clone https://github.com/vinhx-dev/cuoi_ki_mobile.git
git clone https://github.com/vinhx-dev/cuoi_ki_mobile_be.git

```

Nếu đã có source, kiểm tra `git status` trước khi cập nhật; giữ các chỉnh sửa đang có.

Mở pgAdmin/Query Tool bằng tài khoản PostgreSQL của mình, tạo database một lần:

```sql
CREATE DATABASE mobile_ck;

```

Trong `.env`, `DB_USERNAME` và `DB_PASSWORD` phải khớp tài khoản PostgreSQL trên máy này. Không có một mật khẩu PostgreSQL chung cho mọi máy.

## 4. Phân biệt hai loại khóa

| Tên | Được tạo ở đâu | Mục đích |
|---|---|---|
| `MAIL_PASSWORD` | Google App Passwords | Backend đăng nhập SMTP để gửi email |
| `JWT_SECRET` | Tạo ngẫu nhiên trên máy bằng script ở bước 6 | Backend ký và kiểm tra phiên đăng nhập của app |

**Mật khẩu ứng dụng Gmail không dùng làm khóa JWT.** Backend hiện kiểm tra khóa JWT bằng UTF-8 và yêu cầu ít nhất 32 byte. Script tạo 32 byte ngẫu nhiên rồi lưu dạng Base64 vào `.env`.

### Tạo mật khẩu ứng dụng Gmail

1. Đăng nhập Google bằng Gmail muốn dùng làm người gửi.
2. Bật **Xác minh 2 bước** trong phần bảo mật tài khoản.
3. Mở [Google App Passwords](https://myaccount.google.com/apppasswords).
4. Tạo mật khẩu ứng dụng cho một tên dễ nhớ, ví dụ `Ecom local`.
5. Lưu mã 16 ký tự vào `MAIL_PASSWORD`; bỏ khoảng trắng phân nhóm khi dán. `MAIL_USERNAME` và `MAIL_FROM` là Gmail vừa dùng.

Nếu không có mục này, kiểm tra loại tài khoản cơ quan/trường học, chế độ Bảo vệ nâng cao hoặc thiết lập chỉ dùng khóa bảo mật. Đổi mật khẩu Google có thể thu hồi App Password; khi đó tạo lại. [Hướng dẫn chính thức của Google](https://support.google.com/accounts/answer/185833?hl=vi).

## 5. Tạo các file local của backend

Các đường dẫn bên dưới tính từ **gốc repo backend**, không phải repo Android:

```text
cuoi_ki_mobile_be/
├── .env
├── run-local.ps1
├── New-JwtSecret.ps1
└── src/main/resources/application.properties

```

Copy `backend/run-local.ps1` và `backend/New-JwtSecret.ps1` từ bộ bàn giao vào gốc backend. Với hai file cấu hình, **chỉ tạo mới nếu chưa tồn tại**:

```powershell
Set-Location D:\Mobie\cuoi_ki_mobile_be
# Sau khi copy các file mẫu vào đúng thư mục:
if (-not (Test-Path .env)) {
    Copy-Item .env.example .env
}
if (-not (Test-Path src\main\resources\application.properties)) {
    Copy-Item src\main\resources\application.properties.example src\main\resources\application.properties
}

```

Đặt bản mẫu `backend/.env.example` ở gốc backend; đặt bản mẫu `backend/application.properties.example` vào `src/main/resources/` trước khi chạy đoạn copy trên. Nếu file đã có, chỉnh từng khóa cần thiết; không ghi đè toàn bộ cấu hình đang dùng.

### Nội dung mẫu .env

```dotenv
# Copy to BACKEND/.env and fill in the blank values on your own computer.
DB_URL=jdbc:postgresql://localhost:5432/mobile_ck
DB_USERNAME=postgres
DB_PASSWORD=

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
MAIL_FROM_NAME=Ecom

# Run New-JwtSecret.ps1 once. Keep this value unchanged between restarts.
JWT_SECRET=

```

Điền mật khẩu PostgreSQL, Gmail gửi thư và App Password. Điền MAIL_FROM giống Gmail gửi; nếu để trống, launcher sẽ dùng MAIL_USERNAME. Để trống `JWT_SECRET` lần đầu, rồi chạy bước 6. Không dùng dấu ngoặc nhọn quanh giá trị thật. Với mẫu này, mỗi biến nằm trên một dòng; không thêm comment ở cuối giá trị.

### Nội dung mẫu application.properties

```properties
spring.application.name=ecom-backend
server.port=8080
server.servlet.context-path=/api

# Read the .env in the backend working directory.
spring.config.import=optional:file:./.env[.properties]

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/mobile_ck}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=false

spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
spring.mail.test-connection=true

app.mail.from=${MAIL_FROM:${MAIL_USERNAME}}
app.mail.from-name=${MAIL_FROM_NAME:Ecom}

app.jwt.secret=${JWT_SECRET}
app.jwt.access-token-ttl=1h
app.jwt.refresh-token-ttl=30d
app.jwt.cleanup-cron=0 0 3 * * ?

app.recommendation.url=${RECOMMENDATION_URL:}
app.recommendation.timeout=3s

```

Dòng `spring.config.import` cho Spring đọc `.env` như Java properties khi chạy ở gốc backend. Launcher còn nạp các biến vào môi trường tiến trình, giúp tránh phụ thuộc cấu hình terminal cũ. Biến môi trường bên ngoài có thể ghi đè giá trị trong file khi chạy trực tiếp từ IDE. [Cơ chế cấu hình của Spring Boot](https://docs.spring.io/spring-boot/4.1/reference/features/external-config.html).

`spring.mail.test-connection=true` bật kiểm tra kết nối mail khi khởi động. Nếu Gmail chưa hợp lệ hoặc không kết nối được SMTP, backend có thể dừng ngay; sửa cấu hình rồi chạy lại.

### Git ignore

Đảm bảo `.gitignore` backend có:

```gitignore
.env
src/main/resources/application.properties
target/

```

Chỉ gửi/commit `.env.example`, `application.properties.example` và script không chứa khóa thật.


## 6. Tạo JWT riêng một lần

Copy script `New-JwtSecret.ps1` vào gốc backend, rồi chạy:

```powershell
Set-Location D:\Mobie\cuoi_ki_mobile_be
.\New-JwtSecret.ps1

```

Nếu PowerShell chặn script, có thể chạy riêng file này bằng:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\New-JwtSecret.ps1

```

Lệnh trên chỉ áp dụng cho tiến trình chạy script đó.

Code của script:

```powershell
# Run once inside the backend folder, after creating .env.
$ErrorActionPreference = 'Stop'
$envPath = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $envPath)) { throw 'Create backend .env first.' }

$content = [IO.File]::ReadAllText($envPath)
$pattern = [regex]'(?m)^JWT_SECRET\s*=([^\r\n]*)'
$match = $pattern.Match($content)
$existing = if ($match.Success) { $match.Groups[1].Value.Trim() } else { '' }
if ($existing) {
    if ([Text.Encoding]::UTF8.GetByteCount($existing) -lt 32) {
        throw 'Existing JWT_SECRET is shorter than 32 bytes. Review it before replacing it.'
    }
    Write-Output 'JWT_SECRET already exists. Kept the current signing key.'
    exit 0
}

$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$secret = [Convert]::ToBase64String($bytes)
if ($match.Success) {
    $content = $pattern.Replace($content, ('JWT_SECRET=' + $secret), 1)
} else {
    $content = $content.TrimEnd() + [Environment]::NewLine + 'JWT_SECRET=' + $secret + [Environment]::NewLine
}
[IO.File]::WriteAllText($envPath, $content, (New-Object Text.UTF8Encoding $false))
Write-Output 'Created a local JWT signing key in .env. The key was not printed.'

```

Script giữ nguyên khóa đã có, không in khóa ra màn hình. Không tạo lại JWT mỗi lần mở app hoặc restart server. Nếu chủ hệ thống chủ động đổi khóa, các access token cũ sẽ không còn hợp lệ; người dùng cần đăng nhập lại.

## 7. Chạy backend trên Windows

Đặt `JAVA_HOME` đúng thư mục JDK 25 đã cài, rồi dùng launcher:

```powershell
Set-Location D:\Mobie\cuoi_ki_mobile_be
# Thay đường dẫn này nếu JDK 25 nằm ở nơi khác.
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'
& "$env:JAVA_HOME\bin\java.exe" -version
.\run-local.ps1

```

Giữ terminal đang chạy. Không mở thêm một backend nữa trên cùng cổng. Nếu PowerShell chặn launcher, chạy powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1.

Code launcher đầy đủ, không chứa khóa thật:

```powershell
# Run from any folder. Only this process and its Maven child receive the values.
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    $envPath = Join-Path $PSScriptRoot '.env'
    if (-not (Test-Path -LiteralPath $envPath)) { throw 'Create backend .env first.' }
    $lineNumber = 0
    foreach ($line in [IO.File]::ReadAllLines($envPath)) {
        $lineNumber++
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
        $pair = $trimmed -split '=', 2
        if ($pair.Count -ne 2 -or $pair[0].Trim() -notmatch '^[A-Za-z_][A-Za-z0-9_]*$') {
            throw "Invalid .env format at line $lineNumber."
        }
        $name = $pair[0].Trim()
        $value = $pair[1].Trim()
        if ($value.Length -ge 2 -and (
            ($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
    if (-not $env:DB_PASSWORD) { throw 'Fill in DB_PASSWORD in .env.' }
    if (-not $env:MAIL_USERNAME -or -not $env:MAIL_PASSWORD) { throw 'Fill in Gmail SMTP credentials in .env.' }
    $env:MAIL_PASSWORD = $env:MAIL_PASSWORD -replace '\s', ''
    if ($env:MAIL_PASSWORD.Length -ne 16) { throw 'Use the 16-character Gmail App Password.' }
    if ([string]::IsNullOrWhiteSpace($env:MAIL_FROM)) {
        $env:MAIL_FROM = $env:MAIL_USERNAME
    }

    if ([Text.Encoding]::UTF8.GetByteCount([string]$env:JWT_SECRET) -lt 32) {
        throw 'JWT_SECRET must be at least 32 bytes. Run New-JwtSecret.ps1 first.'
    }
    if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        throw 'Set JAVA_HOME to your JDK 25 installation in this terminal first.'
    }
    & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version
    if ($LASTEXITCODE -ne 0) { throw 'Could not run Java.' }
    & (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Backend failed. Read the error above.' }
} finally { Pop-Location }
```

Launcher luôn chuyển vào gốc backend, đọc `.env` và chạy `mvnw.cmd spring-boot:run`; Maven sẽ cập nhật tài nguyên build. Lần đầu cần mạng để tải phụ thuộc; chỉ thêm `--offline` khi máy đã có đủ cache.

Backend sẵn sàng khi log có `Started EcomBackendApplication`, không có lỗi khởi tạo. Nếu có thông báo Maven `BUILD SUCCESS` nhưng log phía trên có `Application run failed`, ứng dụng vẫn chưa chạy.

Có thể kiểm tra thêm:

```powershell
try {
    $r = Invoke-WebRequest http://localhost:8080/api/categories -UseBasicParsing
    $r.StatusCode
} catch {
    if ($_.Exception.Response) {
        [int]$_.Exception.Response.StatusCode
    } else {
        'Khong ket noi duoc backend'
    }
}

```
`401` ở API categories khi chưa gửi token là bình thường: server đã trả lời nhưng endpoint cần đăng nhập. Chỉ kiểm tra cổng trả lời chưa chứng minh email OTP đã được gửi.

Trong IntelliJ/IDE khác: chọn JDK 25, working directory là gốc backend và build/copy resources trước khi Run. Sau khi sửa source `application.properties`, phải để bản trong `target/classes` được cập nhật; không coi sửa tay file build là cách sửa lâu dài.

## 8. Chạy Android kết nối API thật

1. Mở repo `cuoi_ki_mobile` bằng Android Studio, đợi Gradle Sync và cài SDK thiếu theo cấu hình repo.
2. Tạo/chạy emulator.
3. Trong `local.properties` Android, giữ dòng `sdk.dir` của máy và thêm:

```properties
api.baseUrl=http://10.0.2.2:8080/api/

```

4. Build và chạy app:

```powershell
Set-Location D:\Mobie\cuoi_ki_mobile
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug

```

`10.0.2.2` là địa chỉ máy tính host nhìn từ emulator. Base URL đã có `/api/`; endpoint chỉ thêm `/auth/login`, `/admin/products`..., không thêm `/api` lần nữa.

Nếu dùng điện thoại thật qua USB:

```powershell
adb reverse tcp:8080 tcp:8080

```

Đặt `api.baseUrl=http://localhost:8080/api/` rồi build/cài lại. Nếu dùng Wi-Fi, cần IP LAN của máy chủ và cấu hình cleartext phù hợp trong `app/src/main/res/xml/network_security_config.xml`; bản hiện tại chỉ cho HTTP ở các host local đã khai báo.

## 9. Kiểm tra OTP và đăng nhập

Luồng đúng: **đăng ký → nhận OTP → xác thực → đăng nhập**. Backend nhận email, không phải số điện thoại làm tài khoản đăng nhập.

| Method | Endpoint tính sau /api | Body |
|---|---|---|
| POST | `/auth/register` | `{email,password,fullName,phone}` |
| POST | `/auth/resend-otp` | `{email}` |
| POST | `/auth/verify-otp` | `{email,otp}` |
| POST | `/auth/login` | `{email,password}` |
| GET | `/auth/me` | Bearer access token |

- Đăng ký dùng email nhận thư do người kiểm tra kiểm soát. Mật khẩu app từ 6–50 ký tự; số điện thoại nếu nhập phải 10 số, bắt đầu bằng 0.
- OTP có 6 số, hạn 5 phút; backend hiện hủy mã sau 5 lần nhập sai.
- Tài khoản đã `ACTIVE` không cần gửi lại OTP. Để kiểm tra đăng ký mới, dùng email chưa đăng ký; `resend-otp` cần tài khoản đã có và chưa kích hoạt.
- Login trả `{success,message,data}`; `data` chứa `accessToken`, `refreshToken` và `user`.
- `ROLE_ADMIN` mở khu quản trị; `ROLE_USER` theo luồng khách hàng. Role do backend trả về.

Email được gửi bất đồng bộ. API báo đã tạo/gửi lại mã **chưa đủ để khẳng định thư đã được giao**. Kiểm tra hộp thư/Spam và log `Đã gửi email SMTP thành công...`; nếu có `MailAuthenticationException`, sửa SMTP trước. Xác thực SMTP thành công là một bước riêng với việc nhận thư OTP thực tế.


## 10. Tạo quyền admin trên database local

Hướng dẫn này không phụ thuộc `AdminBootstrap.java`: file đó và thay đổi `UserRepository` liên quan đang là code local có từ trước, chưa nằm trong phần backend đã push.

Cách cài có thể lặp lại trên máy mới:

1. Đăng ký một tài khoản bằng app và xác thực OTP để có trạng thái `ACTIVE`.
2. Chủ database dùng pgAdmin, chọn đúng database `mobile_ck`.
3. Thay `EMAIL_TAI_KHOAN_DA_XAC_THUC` bên dưới bằng email tài khoản đó, rồi chạy:

```sql
SELECT id, email, role, status
FROM users
WHERE lower(email) = lower('EMAIL_TAI_KHOAN_DA_XAC_THUC');

UPDATE users
SET role = 'ROLE_ADMIN'
WHERE lower(email) = lower('EMAIL_TAI_KHOAN_DA_XAC_THUC')
  AND status = 'ACTIVE'
RETURNING id, email, role, status;

```

4. `UPDATE` phải trả đúng một tài khoản. Nếu không có dòng nào, kiểm tra email/trạng thái trước.
5. Đăng xuất và đăng nhập lại app để lấy token có role mới. App không tự cấp quyền admin.

Nếu dùng `AdminBootstrap` local đã có trên máy chủ, phải đọc code và profile của nó trước. Không giả định có sẵn tài khoản/mật khẩu admin trong repo.

## 11. Code admin đã được nối và vị trí tìm

| Vị trí trong Android | Vai trò |
|---|---|
| `feature/admin/.../data/AdminApi.java` | Hợp đồng API admin; các request bắt buộc server thật |
| `feature/admin/.../AdminViewModel.java` | Truy cập dữ liệu và chặn mutation đang xử lý |
| `feature/admin/.../fragments/` và `res/layout/` | Dashboard, sản phẩm, đơn, người dùng, doanh thu, form và chi tiết |
| `core/api/.../ApiClient.java` | `requestStrict`, envelope và lỗi mạng; không fallback mock cho admin |
| `core/api/.../auth/AuthNetwork.java` | OkHttp/Retrofit, Bearer và refresh token |
| `core/data/.../TokenManager.java` | Lưu phiên |
| `core/navigation/.../AppRouter.java` | Điều hướng theo role sau đăng nhập |
| `feature/auth/.../login/LoginFragment.java` | Login email/password |
| `feature/splash/.../SplashFragment.java` | Khôi phục phiên qua auth/me |

Dấu `...` là phần package trung gian; AI có thể dùng `rg --files` để tìm tên file trong repo.

| Chức năng admin | API chính |
|---|---|
| Sản phẩm | GET `/admin/products`, GET `/admin/products/{id}`; POST `/products`, PUT/DELETE `/products/{id}` |
| Danh mục trong form | GET `/categories` |
| Đơn hàng | GET `/admin/orders?status=...&userId=...`, GET `/admin/orders/{id}`, PATCH `/admin/orders/{id}/status` |
| Người dùng | GET `/admin/users?keyword=...&status=...`, GET `/admin/users/{id}`; đơn của người dùng qua `/admin/orders?userId=...` |
| Dashboard/doanh thu | GET `/admin/stats/dashboard`, `/admin/stats/top-products`, `/admin/stats/categories` |

Sản phẩm nhận `name, price, quantity, description, image, size, categoryId, soldCount, forAI`. `image` là Base64 JPEG/PNG (có thể kèm tiền tố `data:image/png;base64,` hoặc `data:image/jpeg;base64,`), `size` là mảng, `forAI` là JSON. Xóa thành công có thể trả `data:null`. Đơn chỉ đi `PENDING → SHIPPING/CANCELLED` hoặc `SHIPPING → DELIVERED/CANCELLED`. Chi tiết đơn có thông tin người nhận. Thống kê danh mục là số sản phẩm, không phải doanh thu danh mục.

**Thay đổi ảnh Base64 cần cập nhật đồng thời cả repo Android và backend.** Backend dùng `util/Base64Image.java` để kiểm tra ảnh thật và lưu data URI trong các cột TEXT đã có (sản phẩm, danh mục và bản sao ảnh trong đơn/lịch sử mua). Giới hạn 2 MiB sau giải mã, tối đa 4096 mỗi chiều và 16 triệu pixel. Không gửi `content://` hoặc URL cho ảnh mới. PUT bỏ `image`/gửi null giữ nguyên ảnh; chuỗi rỗng xóa ảnh. URL cũ còn hiển thị và được giữ khi không thay ảnh, không tự tải URL hàng loạt để chuyển dữ liệu.

**Database cũ:** các cột ảnh có thể vẫn là VARCHAR(2048) dù entity đã khai báo TEXT; `ddl-auto=update` không tự nới chúng. Trước khi gửi ảnh, chạy trong thư mục backend: `psql -h localhost -p 5432 -U postgres -d mobile_ck -v ON_ERROR_STOP=1 -f database/migrations/20261120_base64_images.sql`. Dùng thông tin DB trên máy nhận. Migration có transaction, chỉ nới bốn cột ảnh sang TEXT và giữ nguyên giá trị. Không xóa hay tạo lại database. Máy hiện tại đã áp dụng migration này.

Android: vào Sản phẩm → Thêm/Sửa → Chọn ảnh từ máy → Lưu. Form thu nhỏ cạnh dài tối đa 1600, chuẩn hóa hướng ảnh và mã hóa Base64. `AdminImageEncoder.java` xử lý ảnh; `core/ui/.../image/InlineImageLoader.java` đọc ảnh Base64 và URL cũ. Giữ nguyên trường JSON `image` và các endpoint. Không cần thêm API upload hay biến `.env` dành cho ảnh. Chạy `mvnw.cmd test` ở backend để kiểm tra codec, và `gradlew.bat :app:assembleDebug` ở Android.

UI hiện chưa có CRUD danh mục và nút khóa/mở khóa người dùng. Dashboard không dùng số mẫu hay chuỗi doanh thu giả. Luồng khách hàng được giữ riêng. Xem thêm `feature/admin/README.md` trong source Android.

## 12. Các lỗi hay gặp

| Biểu hiện | Kiểm tra và xử lý |
|---|---|
| `MailAuthenticationException / Authentication failed / 535` | App Password có đúng Gmail gửi thư không; đủ 16 ký tự và không có khoảng trắng phân nhóm; backend đã restart/nạp .env chưa. Nếu Google đã thu hồi mật khẩu ứng dụng, tạo lại |
| Khóa Gmail đúng nhưng Java vẫn lỗi | Kiểm tra working directory, bản `target/classes/application.properties`, biến MAIL_* hoặc SPRING_MAIL_* cũ trong cấu hình IDE/terminal. Launcher đọc lại .env cho tiến trình mới |
| `JWT Secret tối thiểu ... 32 bytes` | Tạo JWT riêng bằng script; không dùng App Password. Kiểm tra `app.jwt.secret` lấy đúng `JWT_SECRET` |
| `password authentication failed for user` | Sửa DB_USERNAME/DB_PASSWORD theo PostgreSQL của máy, không dùng mật khẩu Gmail |
| `database mobile_ck does not exist` | Tạo database trước; Hibernate tạo bảng, không tạo database |
| Cổng 8080 đang được dùng | Kiểm tra tiến trình hiện tại; dừng đúng backend cũ rồi chạy lại. Không tắt toàn bộ java.exe |
| Build backend báo release 25 không được hỗ trợ | Kiểm tra JAVA_HOME và Java Maven đang dùng; chọn JDK 25 |
| Mất phiên sau đổi JWT | Đăng nhập lại; giữ khóa mới ổn định ở các lần chạy sau |
| App không kết nối server | Backend sẵn sàng chưa; emulator dùng 10.0.2.2, không dùng localhost của chính emulator |
| HTTP 401 | Chưa đăng nhập hoặc token không hợp lệ; kiểm tra phiên/refresh |
| HTTP 403 ở admin | Tài khoản/token chưa có ROLE_ADMIN hoặc thao tác bị server từ chối |
| Danh sách admin trống | Kiểm tra dữ liệu database; không thêm dữ liệu giả để che trạng thái trống |

## 13. Kiểm tra trước khi bàn giao tiếp

- Backend có log Started, trả lời ở cổng 8080 và kiểm tra kết nối SMTP qua cấu hình đã bật.
- Đăng ký bằng email của người kiểm tra; thực sự nhận OTP, xác thực và đăng nhập.
- Đăng nhập admin: dashboard, danh sách sản phẩm, thêm/sửa/xóa một sản phẩm kiểm tra riêng rồi dọn bản ghi đó.
- Có đơn thật mới kiểm tra chi tiết người nhận và chuyển trạng thái; thiếu dữ liệu phải ghi chưa test.
- Mở lại app để kiểm tra khôi phục phiên; thử tài khoản thường để xác nhận đúng khu vực.
- Build Android:

```powershell
.\gradlew.bat :app:assembleDebug

```

- Smoke test cần emulator đã đăng nhập admin:

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest '-PsmokeTestRunner=com.example.ecom.AdminIntegrationSmokeTest'
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
adb shell am instrument -w -r com.example.ecom.test/com.example.ecom.AdminIntegrationSmokeTest

```

Khi có nhiều thiết bị, thêm `-s TEN_THIET_BI` cho adb. Mặc định test chỉ đọc; `-e productCrud true` là chế độ tạo/sửa/xóa sản phẩm QA riêng, không phải dữ liệu bán hàng.

Tại máy đã cấu hình trong lần bàn giao này: build Android và smoke test admin đã đạt; sản phẩm QA đã xóa. Đợt Base64: backend `mvnw.cmd package` và 9 unit test đạt; đã áp dụng migration TEXT. Emulator đã kiểm tra PNG/JPEG, thu nhỏ và giải mã hiển thị; API thật kiểm tra ảnh sản phẩm/danh mục qua POST/PUT/GET/DELETE, giữ/xóa ảnh, Base64 thuần và từ chối ảnh lỗi/quá giới hạn. Đã dọn cả hai bản ghi QA. Trình chọn ảnh hệ thống chưa kiểm tra bằng tay. Spring JavaMail đã xác thực SMTP, backend đã khởi động với kiểm tra mail lúc startup. Chưa xác nhận việc nhận email OTP thực tế sau lần khởi động cuối và chưa test chuyển trạng thái đơn vì DB lúc kiểm tra chưa có đơn.

## 14. Đoạn giao việc có thể gửi cho AI

```text
Hãy cài và kiểm tra dự án Ecom trên máy này theo note đi kèm.

- Android repo: vinhx-dev/cuoi_ki_mobile, nhánh main.
- Backend repo: vinhx-dev/cuoi_ki_mobile_be, nhánh main.
- Kiểm tra git status và giữ mọi thay đổi đang có.
- Đọc controller, DTO, SecurityConfig, build.gradle và pom.xml thực tế.
- Dùng JDK 25 cho backend, PostgreSQL mobile_ck, server cổng 8080 và prefix /api.
- Cấu hình local backend theo các file mẫu đi kèm; launcher run-local.ps1 có trong source backend, không chứa khóa thật.
- Chủ máy tự tạo App Password Gmail và nhập .env. Không in mật khẩu, token hoặc khóa JWT ra log/chat.
- MAIL_PASSWORD chỉ dành cho Gmail. JWT_SECRET là khóa riêng tối thiểu 32 byte; nếu đã có khóa hợp lệ thì giữ nguyên.
- Bật kiểm tra SMTP lúc startup và xác minh backend thực sự Started; không chỉ nhìn BUILD SUCCESS.
- Emulator Android dùng http://10.0.2.2:8080/api/. Không ghép lặp /api.
- Kiểm tra role từ backend; tạo quyền admin trên DB local theo đúng email đã xác thực.
- Giữ luồng khách hàng và API admin thật; không fallback mock cho thao tác quản trị.
- Đọc mục ảnh Base64: cập nhật cả hai repo cùng đợt; chạy migration TEXT nếu DB cũ còn VARCHAR(2048). Không đổi ảnh mới về URL.
- Báo rõ kiểm tra nào đã chạy, kiểm tra nào thiếu dữ liệu hoặc cần chủ máy thao tác.
- Chỉ gửi email kiểm tra/tạo dữ liệu khi chủ máy yêu cầu thao tác đó.
- Không commit hoặc push nếu chủ máy chưa yêu cầu cho đợt công việc này.

```

## 15. Nguồn và file mẫu

- [Google: mật khẩu ứng dụng](https://support.google.com/accounts/answer/185833?hl=vi).
- [Spring Boot: cấu hình ngoài và import file](https://docs.spring.io/spring-boot/4.1/reference/features/external-config.html).
- [Spring Boot: gửi email qua JavaMailSender](https://docs.spring.io/spring-boot/4.1/reference/io/email.html).
- Các endpoint, DTO, thời hạn OTP và cấu hình JDK/SDK ở note được đối chiếu source local của hai repo.
- Thư mục `backend/` đi kèm có `.env.example`, `application.properties.example`, `New-JwtSecret.ps1` và `run-local.ps1`. Tất cả là mẫu không chứa khóa thật.
