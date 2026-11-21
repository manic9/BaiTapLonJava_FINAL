# Nexus Arena - Esports Tournament Manager

Ứng dụng JavaFX quản lý giải đấu esports.

## Yêu cầu hệ thống

- **JDK 17+**
- **JavaFX SDK 27.0.1.1+** (tải từ [gluonhq.com](https://gluonhq.com/products/javafx/))
- **IntelliJ IDEA** (khuyên dùng) hoặc command line

## Thiết lập JavaFX SDK

### Cách 1: IntelliJ IDEA (khuyên dùng)

1. **Tải JavaFX SDK** từ [gluonhq.com](https://gluonhq.com/products/javafx/) và giải nén vào một thư mục (ví dụ: `C:\javafx-sdk-27.0.1.1`)

2. **Thiết lập Path Variable trong IntelliJ IDEA**:
   - Mở `File` → `Settings` → `Appearance & Behavior` → `Path Variables`
   - Nhấn `+` để thêm path variable mới:
     - **Name**: `JavaFX_HOME`
     - **Path**: Chọn thư mục `lib` của JavaFX SDK (ví dụ: `C:\javafx-sdk-27.0.1.1\lib`)
   - Nhấn `OK`

3. **Mở dự án** trong IntelliJ IDEA
   - IDE sẽ tự động nhận diện `$JavaFX_HOME$` path variable
   - Nếu IDE hỏi về unresolved path variable, xác nhận đường dẫn đúng

4. **Chạy ứng dụng**:
   - Chọn Run Configuration: **Nexus Arena** (thanh công cụ trên cùng)
   - Nhấn **Run** (▶)

### Cách 2: Command Line

1. **Thiết lập environment variable**:
   ```powershell
   # PowerShell - tạm thời (cho session hiện tại)
   $env:JAVAFA_HOME = "C:\javafx-sdk-27.0.1.1\lib"

   # Hoặc thêm vào System Environment Variables để lưu vĩnh viễn
   ```

2. **Build**:
   ```powershell
   .\build.cmd
   ```

3. **Chạy**:
   ```powershell
   .\run.cmd
   ```

## Cấu trúc dự án

```
src/main/java/com/esports/manager/
├── Launcher.java              # Entry point
├── EsportsManagerApp.java     # JavaFX Application
├── config/AppContext.java     # Dependency injection
├── controller/                # FXML controllers
├── model/                     # Entity classes
├── repository/                # Data access layer
├── service/                   # Business logic layer
└── util/                      # Utilities
```

## Lưu ý

- Dự án dùng **In-Memory Repository** — không cần database
- Dữ liệu mẫu được nạp sẵn khi khởi động
- Path variable `$JavaFX_HOME$` giúp dự án hoạt động trên nhiều máy mà không cần sửa code
