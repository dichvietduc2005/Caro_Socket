# Hướng Dẫn Cải Đặt & Chạy Game Caro Socket (JavaFX)

Dự án này sử dụng giao diện **JavaFX** (không đi kèm sẵn trong JDK từ phiên bản 11 trở đi), nên bạn cần cấu hình thủ công SDK.

---

## BƯỚC 1: Tải JavaFX SDK 23

1. Truy cập trang chủ Gluon: [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
2. Tìm mục **JavaFX Windows SDK**.
3. Chọn phiên bản **23** (hoặc mới nhất) -> Nhấn **Download**.
4. Giải nén file vừa tải ra một thư mục cố định (Ví dụ: `C:\JavaFX\javafx-sdk-23`).
   *Lưu ý: Nhớ đường dẫn tới thư mục `lib` bên trong (VD: `C:\JavaFX\javafx-sdk-23\lib`).*

---

## BƯỚC 2: Thêm Thư Viện Vào IntelliJ IDEA

Để code hết báo lỗi đỏ:

1. Mở IntelliJ, nhấn `Ctrl + Alt + Shift + S` (hoặc vào **File** -> **Project Structure**).
2. Chọn mục **Libraries** ở cột bên trái.
3. Nhấn dấu `+` -> Chọn **Java**.
4. Tìm đến thư mục `lib` của JavaFX bạn vừa giải nén ở Bước 1.
5. Quét chọn toàn bộ các file `.jar` có trong đó -> Nhấn **OK**.
6. Nếu có hỏi "Add to module", hãy chọn module dự án của bạn (thường là `Caro_Socket`) -> Nhấn **OK**.
7. Nhấn **Apply** và **OK** để đóng cửa sổ.

---

## BƯỚC 3: Cấu Hình Chạy (VM Options) - QUAN TRỌNG NHẤT

Để chạy được game mà không bị lỗi thiếu module:

1. Nhìn lên góc trên bên phải IntelliJ, cạnh nút Run (tam giác xanh), nhấn vào tên cấu hình (ví dụ `MainClientFX`) -> Chọn **Edit Configurations...**
2. Trong cửa sổ hiện ra, tìm dòng **VM options**.
   *(Nếu không thấy, nhấn vào chữ **Modify options** màu xanh -> chọn **Add VM options**)*.
3. Dán dòng lệnh sau vào ô VM options (nhớ thay đổi đường dẫn cho đúng với máy bạn):

```bash
--module-path "ĐƯỜNG_DẪN_CỦA_BẠN\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

**Ví dụ, nếu bạn giải nén ở ổ C:**
```bash
--module-path "C:\JavaFX\javafx-sdk-23\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

4. Nhấn **OK** để lưu.
