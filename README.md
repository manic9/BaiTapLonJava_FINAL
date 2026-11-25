# Nexus Arena

Ứng dụng desktop JavaFX quản lý giải đấu esports. Dữ liệu nghiệp vụ và tài khoản được lưu trong Microsoft SQL Server. Ứng dụng cần kết nối SQL thành công để khởi động; không có tài khoản đăng nhập mặc định.

## Chức năng chính

- Đăng ký tài khoản ban tổ chức, đăng nhập và đăng xuất. Mật khẩu được lưu dưới dạng hash kèm salt.
- Quản lý giải đấu, đội tuyển, tuyển thủ, đăng ký tham dự, trận đấu và kết quả.
- Tạo/xem nhánh đấu, theo dõi nhà vô địch và cập nhật bảng xếp hạng.
- Quản lý phân bổ giải thưởng, thiết lập ứng dụng và nhật ký hoạt động.
- Giao diện tiếng Việt và tiếng Anh.

## Yêu cầu

- Windows 10/11.
- JDK tương thích với JavaFX SDK **27.0.1.1**.
- Microsoft SQL Server; bật TCP/IP nếu kết nối qua SQL Server Authentication.
- Microsoft SQL Server JDBC Driver và các thư viện Ikonli 12.3.1.
- IntelliJ IDEA hoặc PowerShell để chạy các script đi kèm.

### Đường dẫn thư viện

Script và cấu hình IntelliJ hiện có đường dẫn của máy phát triển:

- JavaFX: `C:\javafx-sdk-27.0.1.1\lib`
- Ikonli: `C:\Users\admin\.m2\repository\org\kordamp\ikonli\...\12.3.1`
- SQL Server JDBC: `C:\sqljdbc_13.6\enu\jars`

Nếu thư viện nằm chỗ khác, cập nhật đường dẫn trong `build.ps1`, `run.ps1` và cấu hình thư viện của IntelliJ (`untitled.iml`). Script tự thêm các JAR đặt trong thư mục `lib` ở thư mục gốc vào classpath.

## Cài đặt SQL Server

1. Khởi động SQL Server và mở SSMS.
2. Tạo database nếu chưa có:

   ```sql
   CREATE DATABASE EsportsDB;
   GO
   ```

3. Chọn `EsportsDB`, mở [`sql/create-all-tables.sql`](sql/create-all-tables.sql) và chạy để tạo/cập nhật các bảng ứng dụng, bao gồm `dbo.Users`.
4. Tạo login SQL riêng cho ứng dụng và cấp quyền cần thiết trên `EsportsDB` (không dùng `sa`). Nếu dùng Windows Authentication, cấp quyền cho tài khoản Windows chạy app.
5. Cấu hình kết nối theo phần dưới, build và chạy app. Tạo tài khoản đầu tiên bằng liên kết **Đăng ký** ở màn hình đăng nhập.

Các script cập nhật riêng cho database cũ:

- [`sql/add-team-contact-email.sql`](sql/add-team-contact-email.sql): thêm `ContactEmail` vào `dbo.Teams`.
- [`sql/add-player-age-nationality.sql`](sql/add-player-age-nationality.sql): thêm `Age` và `Nationality` vào `dbo.Players`.

Các cột này đã nằm trong script schema đầy đủ; chỉ cần chạy script bổ sung nếu database cũ đang thiếu chúng.

> `sql/normalize-ids-and-seed-demo.sql` chuẩn hóa ID hiện có và chèn dữ liệu demo. Đây không phải script cài đặt thông thường. Hãy backup database trước và chỉ chạy khi chủ động muốn thay đổi ID/thêm dữ liệu mẫu.

## Cấu hình kết nối database

Tạo file cấu hình từ mẫu:

```powershell
Copy-Item config/db.properties.example config/db.properties
```

Sửa `config/db.properties` với thông tin máy chủ và login SQL Server:

```properties
db.host=localhost
db.port=1433
db.name=EsportsDB
db.authMode=SQL_SERVER
db.username=esports_app
db.password=MAT_KHAU_SQL_SERVER
db.encrypt=true
db.trustServerCertificate=true
db.loginTimeoutSeconds=5
```

`db.username` và `db.password` là thông tin kết nối **SQL Server**, khác với username/mật khẩu đăng nhập ứng dụng. Không commit hoặc chia sẻ file cấu hình có mật khẩu thật.

Có thể ghi đè cấu hình bằng biến môi trường:

| Biến | Thuộc tính tương ứng |
|---|---|
| `ESPORTS_DB_HOST` | `db.host` |
| `ESPORTS_DB_PORT` | `db.port` |
| `ESPORTS_DB_NAME` | `db.name` |
| `ESPORTS_DB_AUTH_MODE` | `db.authMode` |
| `ESPORTS_DB_USER` | `db.username` |
| `ESPORTS_DB_PASSWORD` | `db.password` |
| `ESPORTS_DB_ENCRYPT` | `db.encrypt` |
| `ESPORTS_DB_TRUST_CERT` | `db.trustServerCertificate` |
| `ESPORTS_DB_TIMEOUT` | `db.loginTimeoutSeconds` |

**Windows Authentication:** đặt `db.authMode=WINDOWS`, cấp quyền database cho tài khoản Windows hiện tại và cài đúng native authentication DLL. `run.ps1` đang trỏ DLL x64 tới `C:\sqljdbc_13.6\enu\auth\x64`; điều chỉnh đường dẫn nếu cần.

## Build và chạy

Mở PowerShell ở thư mục dự án:

```powershell
.\build.cmd
.\run.cmd
```

Hoặc chạy trực tiếp `build.ps1` rồi `run.ps1`. Build biên dịch mã nguồn trong `src/main/java` vào `out` và chép resource; run khởi chạy ứng dụng từ thư mục này.

Trong IntelliJ IDEA, cấu hình JDK, JavaFX, Ikonli và JDBC driver theo đường dẫn trên. Entry point: `com.esports.manager.Launcher` (hoặc `com.esports.manager.EsportsManagerApp`). Dự án hiện không kèm Maven `pom.xml` hay Gradle wrapper; phụ thuộc được tham chiếu qua cấu hình IDE và các script.

## Luồng sử dụng

1. Đăng ký tài khoản: họ tên tối đa 100 ký tự; username 3–50 ký tự, chỉ gồm chữ không dấu, số, `.`, `_`, `-`; mật khẩu 8–128 ký tự. Tài khoản đăng ký được tạo với vai trò `ORGANIZER`.
2. Đăng nhập. Cửa sổ quản trị sẽ phóng to; đăng xuất đưa cửa sổ về form đăng nhập.
3. Tạo giải đấu, thêm đội tham dự, xét duyệt đăng ký, tạo nhánh và quản lý trận/kết quả.
4. Một số thao tác sửa/xóa/trạng thái nằm trong menu chuột phải trên dòng dữ liệu.

Các mục trong thanh điều hướng: **Tổng quan**, **Quản lý giải đấu**, **Hồ sơ đăng ký**, **Quản lý trận đấu**, **Sơ đồ nhánh đấu**, **Bảng xếp hạng**, **Quản lý đội tuyển**, **Quản lý tuyển thủ**, **Cơ cấu giải thưởng** và **Cài đặt hệ thống**.

## Dữ liệu mẫu và thao tác nguy hiểm

- Khi các bảng giải đấu, đội, tuyển thủ, đăng ký, trận và giải thưởng đều trống, ứng dụng tự thêm dữ liệu demo vào SQL Server. Khi database đã có dữ liệu, app không tự thêm lại demo mỗi lần chạy.
- Nút **Khôi phục dữ liệu mẫu** trong Cài đặt sẽ xóa dữ liệu hiện tại ở các bảng nghiệp vụ rồi chèn lại demo. Chỉ dùng trên database thử nghiệm; backup trước khi thao tác.
- Xóa dữ liệu có thể bị ràng buộc bởi khóa ngoại hoặc ảnh hưởng bản ghi liên quan.

## Cấu trúc dự án

```text
src/main/java/com/esports/manager/
  Launcher.java, EsportsManagerApp.java   # Điểm vào và vòng đời JavaFX
  config/                                 # AppContext, SQL config, transaction, password hash
  controller/                             # Điều khiển từng giao diện
  model/                                  # Entity và enum
  repository/sqlserver/                   # Truy cập SQL Server
  service/                                # Quy tắc nghiệp vụ
  util/                                   # Điều hướng, i18n, dialog, toast, ID
src/main/resources/com/esports/manager/
  fxml/                                   # Giao diện
  css/                                    # Theme
sql/                                      # Schema và script cập nhật dữ liệu
config/db.properties.example              # Mẫu cấu hình kết nối
build.ps1, run.ps1                        # Script Windows
```

## Lỗi thường gặp

| Lỗi | Kiểm tra |
|---|---|
| Không tìm thấy `config/db.properties` | Copy file `.example` và điền cấu hình. |
| SQL Server error 18456 / Login failed | Username, mật khẩu, chế độ xác thực và quyền database. |
| Timeout / không kết nối được máy chủ | SQL Server Service, TCP/IP, host, port và firewall. |
| Error 4060 / không mở được database | Database `EsportsDB` đã tạo chưa; login có quyền truy cập chưa. |
| Không tìm thấy JDBC driver | Đặt mssql-jdbc JAR trong `lib` hoặc cập nhật classpath/đường dẫn trong script. |
| Thiếu JavaFX hoặc Ikonli | Kiểm tra SDK và các đường dẫn phụ thuộc trong script/IntelliJ. |
| Windows Authentication báo thiếu DLL | Kiểm tra native DLL đúng kiến trúc và đường dẫn trong `run.ps1`. |
| Thiếu bảng/cột hoặc lỗi schema | Chạy schema trên đúng database; backup trước khi cập nhật database hiện hữu. |

## Các script SQL

- [`sql/create-all-tables.sql`](sql/create-all-tables.sql): tạo/cập nhật schema cho ứng dụng.
- [`sql/create-users-table.sql`](sql/create-users-table.sql): tạo riêng bảng tài khoản nếu chỉ cần bootstrap bảng Users.
- [`sql/add-team-contact-email.sql`](sql/add-team-contact-email.sql): bổ sung email đội trên schema cũ.
- [`sql/add-player-age-nationality.sql`](sql/add-player-age-nationality.sql): bổ sung tuổi/quốc tịch trên schema cũ.
- [`sql/normalize-ids-and-seed-demo.sql`](sql/normalize-ids-and-seed-demo.sql): chuẩn hóa ID và thêm dữ liệu demo; xem cảnh báo phía trên trước khi chạy.
