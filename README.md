# Nexus Arena — Esports Tournament Manager

Ứng dụng desktop JavaFX để quản lý giải đấu esports. Ứng dụng lưu dữ liệu nghiệp vụ trong Microsoft SQL Server; cần kết nối cơ sở dữ liệu thành công mới mở màn hình đăng nhập.

## Nội dung

- [Chức năng](#chức-năng)
- [Yêu cầu](#yêu-cầu)
- [Cài đặt cơ sở dữ liệu](#cài-đặt-cơ-sở-dữ-liệu)
- [Cấu hình kết nối](#cấu-hình-kết-nối)
- [Build và chạy](#build-và-chạy)
- [Cách sử dụng](#cách-sử-dụng)
- [Cấu trúc mã nguồn](#cấu-trúc-mã-nguồn)
- [Khắc phục lỗi thường gặp](#khắc-phục-lỗi-thường-gặp)
- [Lưu ý dữ liệu và bảo mật](#lưu-ý-dữ-liệu-và-bảo-mật)

## Chức năng

- Đăng ký tài khoản ban tổ chức và đăng nhập; mật khẩu được băm kèm salt, không lưu dạng văn bản.
- Quản lý giải đấu: game, thể thức, số đội, thời gian, địa điểm, giải thưởng và trạng thái.
- Quản lý đội tuyển, tuyển thủ, đăng ký tham gia và trạng thái xét duyệt.
- Tạo và cập nhật trận đấu, tỉ số, lịch thi đấu và trạng thái trận.
- Sinh/xem nhánh đấu, theo dõi nhà vô địch và tính bảng xếp hạng.
- Quản lý phân bổ giải thưởng, nhật ký hoạt động và một số thiết lập ứng dụng.
- Giao diện hỗ trợ tiếng Việt/tiếng Anh.

## Yêu cầu

- Windows 10/11 (các script hiện tại dùng PowerShell và đường dẫn Windows).
- JDK tương thích với JavaFX SDK được cài; cấu hình dự án hiện tham chiếu JavaFX **27.0.1.1**.
- Microsoft SQL Server đang chạy và cho phép kết nối TCP/IP nếu dùng xác thực SQL Server.
- SQL Server JDBC Driver (mssql-jdbc). Ikonli 12.3.1 được dùng cho biểu tượng giao diện.
- IntelliJ IDEA để chạy trực tiếp, hoặc PowerShell để dùng script `build.cmd` / `run.cmd`.

> Script build/run trong repo đang tham chiếu các đường dẫn máy phát triển: `C:\javafx-sdk-27.0.1.1\lib`, thư viện Ikonli trong `C:\Users\admin\.m2\repository`, và JDBC driver tại `C:\sqljdbc_13.6\enu\jars`. Nếu máy bạn cài thư viện ở nơi khác, sửa các biến/đường dẫn tương ứng trong `build.ps1`, `run.ps1` và cấu hình thư viện của IntelliJ (`untitled.iml`). Có thể đặt các JAR phụ trợ vào thư mục `lib` ở thư mục gốc; script đã tự thêm JAR trong thư mục này vào classpath.

## Cài đặt cơ sở dữ liệu

1. Cài/chạy SQL Server và mở SQL Server Management Studio (SSMS).
2. Tạo database nếu chưa có:

   ```sql
   CREATE DATABASE EsportsDB;
   GO
   ```

3. Chọn database `EsportsDB`, mở và chạy [`sql/create-all-tables.sql`](sql/create-all-tables.sql). Script tạo các bảng cần thiết và có các bước bổ sung cột cho một số schema cũ.
4. Tạo login SQL Server riêng cho ứng dụng (không nên dùng `sa`) và cấp quyền cần thiết trên `EsportsDB`. Nếu chọn Windows Authentication, tài khoản Windows chạy ứng dụng phải có quyền database.
5. Chạy ứng dụng và tạo tài khoản từ liên kết **Đăng ký** trên màn hình đăng nhập. Tài khoản đăng ký mới có vai trò `ORGANIZER`.

Các script bổ sung cho database cũ:

- [`sql/add-team-contact-email.sql`](sql/add-team-contact-email.sql): thêm cột email liên hệ của đội.
- [`sql/add-player-age-nationality.sql`](sql/add-player-age-nationality.sql): thêm tuổi và quốc tịch tuyển thủ.

Hai cột này cũng đã có trong script tạo schema đầy đủ. Chỉ chạy script bổ sung khi database hiện tại thiếu cột; mỗi script kiểm tra cột trước khi thêm.

[`sql/normalize-ids-and-seed-demo.sql`](sql/normalize-ids-and-seed-demo.sql) là script riêng để chuẩn hóa ID cũ và thêm dữ liệu demo. **Không chạy trên database có dữ liệu thật nếu chưa backup và chưa xác nhận mục đích**, vì nó thay đổi ID và chèn dữ liệu.

## Cấu hình kết nối

Sao chép [`config/db.properties.example`](config/db.properties.example) thành `config/db.properties`, rồi sửa giá trị cho máy của bạn:

```properties
db.host=localhost
db.port=1433
db.name=EsportsDB
db.authMode=SQL_SERVER
db.username=esports_app
db.password=THAY_BANG_MAT_KHAU_SQL_SERVER
db.encrypt=true
db.trustServerCertificate=true
db.loginTimeoutSeconds=5
```

`db.username` và `db.password` là thông tin đăng nhập **SQL Server**, không phải tài khoản đăng nhập vào ứng dụng.

Để dùng Windows Authentication, đặt `db.authMode=WINDOWS`; tài khoản Windows cần quyền vào database và JDBC driver cần thư viện native `mssql-jdbc_auth-x64.dll`. `run.ps1` hiện đặt thư mục DLL mặc định tại `C:\sqljdbc_13.6\enu\auth\x64`; sửa nếu cài driver ở đường dẫn khác.

Cũng có thể ghi đè cấu hình bằng các biến môi trường:

| Biến | Ý nghĩa |
|---|---|
| `ESPORTS_DB_HOST` | Tên máy chủ SQL Server |
| `ESPORTS_DB_PORT` | Cổng kết nối, mặc định `1433` |
| `ESPORTS_DB_NAME` | Tên database, mặc định `EsportsDB` |
| `ESPORTS_DB_AUTH_MODE` | `SQL_SERVER` hoặc `WINDOWS` |
| `ESPORTS_DB_USER` | Login SQL Server |
| `ESPORTS_DB_PASSWORD` | Mật khẩu SQL Server |
| `ESPORTS_DB_ENCRYPT` | Bật/tắt mã hóa JDBC |
| `ESPORTS_DB_TRUST_CERT` | Có tin cậy chứng chỉ máy chủ hay không |
| `ESPORTS_DB_TIMEOUT` | Thời gian chờ kết nối, tính bằng giây |

Không đưa `config/db.properties` có mật khẩu thật lên Git hoặc chia sẻ công khai.

## Build và chạy

Mở PowerShell tại thư mục gốc dự án:

```powershell
.\build.cmd
.\run.cmd
```

Hoặc chạy script PowerShell trực tiếp:

```powershell
.\build.ps1
.\run.ps1
```

Trong IntelliJ IDEA, mở dự án và cấu hình JDK, JavaFX SDK, Ikonli cùng SQL Server JDBC Driver theo các đường dẫn trên; entry point là `com.esports.manager.Launcher` hoặc `com.esports.manager.EsportsManagerApp`. Script dòng lệnh hiện build các file Java trong `src/main/java`, chép resource vào `out` rồi chạy ứng dụng từ đó.

## Cách sử dụng

1. Đảm bảo SQL Server và database hoạt động, rồi mở ứng dụng.
2. Chọn **Đăng ký** nếu chưa có tài khoản; nhập họ tên, username và mật khẩu theo hướng dẫn trên form.
3. Đăng nhập. Sau khi xác thực thành công, cửa sổ quản trị tự phóng to; nút đăng xuất quay về khung đăng nhập.
4. Tạo giải đấu, thêm đội tham dự và duyệt đăng ký. Sau đó tạo nhánh, quản lý trận và nhập kết quả để cập nhật thứ hạng.
5. Một số bảng có menu chuột phải trên dòng dữ liệu để sửa, xóa hoặc thay đổi trạng thái. Các thao tác xóa có thể ảnh hưởng dữ liệu liên quan do khóa ngoại.

> Khi toàn bộ các bảng giải đấu/đội/tuyển thủ/đăng ký/trận/giải thưởng đều trống, ứng dụng tự chèn dữ liệu mẫu vào SQL Server. Với database đã có dữ liệu, ứng dụng không tự nạp lại bộ demo mỗi lần khởi động.

## Cấu trúc mã nguồn

```text
src/main/java/com/esports/manager/
├── Launcher.java                 # Điểm vào khởi chạy
├── EsportsManagerApp.java        # Vòng đời JavaFX và chuyển Login/Admin
├── config/                       # Cấu hình DB, AppContext, transaction, hash mật khẩu
├── controller/                   # Controller cho từng giao diện
├── model/                        # Entity và enum nghiệp vụ
├── repository/                   # Hợp đồng repository, seed dữ liệu
│   └── sqlserver/                # Truy vấn SQL Server
├── service/                      # Nghiệp vụ giải đấu, đội, trận, đăng ký...
└── util/                         # Điều hướng, i18n, dialog, ID, toast

src/main/resources/com/esports/manager/
├── fxml/                         # Các màn hình JavaFX
└── css/                          # Theme giao diện

sql/                              # Script schema, cập nhật cột và dữ liệu demo
config/db.properties.example      # Mẫu cấu hình database
build.ps1, run.ps1                # Script build/chạy trên Windows
```

## Khắc phục lỗi thường gặp

- **Không tìm thấy `config/db.properties`**: sao chép file `.example` như hướng dẫn và điền cấu hình.
- **Login failed / SQL error 18456**: kiểm tra login, mật khẩu, chế độ SQL Server Authentication và quyền database.
- **Không kết nối được máy chủ / timeout**: kiểm tra service SQL Server, TCP/IP, hostname và cổng trong cấu hình/firewall.
- **Không mở được database / error 4060**: tạo database `EsportsDB`, chọn đúng database khi chạy schema và kiểm tra quyền của login ứng dụng.
- **Thiếu SQL Server JDBC Driver**: cài driver tương thích và đặt JAR vào `lib` hoặc cập nhật classpath trong script.
- **Lỗi Windows Authentication hoặc thiếu DLL**: kiểm tra quyền Windows, kiến trúc x64/x86 và `java.library.path` trong `run.ps1`.
- **Không tìm thấy JavaFX hoặc Ikonli**: sửa đường dẫn SDK/thư viện trong script và cấu hình IntelliJ cho đúng máy.
- **Lỗi thiếu cột/bảng**: chạy `sql/create-all-tables.sql` trên đúng database, kiểm tra schema và backup trước khi cập nhật database hiện hữu.

## Lưu ý dữ liệu và bảo mật

- SQL Server là nguồn lưu dữ liệu chính; app không chuyển sang chế độ lưu nghiệp vụ trong bộ nhớ khi SQL lỗi.
- Sao lưu database trước khi chạy script chuẩn hóa ID, seed demo thủ công hoặc thay đổi schema.
- Trong phần Cài đặt có nút khôi phục dữ liệu mẫu. Đây là thao tác phá hủy dữ liệu hiện tại và thay bằng dữ liệu demo; chỉ dùng trên database thử nghiệm sau khi xác nhận.
- `config/db.properties` chứa bí mật kết nối. Dùng tài khoản SQL có quyền tối thiểu cần thiết, không dùng `sa` cho cấu hình ứng dụng.
- Tài khoản ứng dụng lưu hash mật khẩu và salt; không ghi hoặc gửi mật khẩu dạng rõ trong script SQL.
