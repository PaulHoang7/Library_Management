# MidleTest - Java Version (Spring Boot)

Project da duoc chuyen sang Java MVC voi:
- Spring Boot 3
- Thymeleaf
- Spring JDBC
- MySQL
- Login/Register bang email + mat khau (khong OTP)
- Dang nhap bang Google/Gmail (Google Identity)
- Email dong thoi la ten dang nhap (khong can nhap username rieng)
- Menu tai khoan + trang ca nhan (`/me`) de xem sach dang muon
- Scheduler tu dong danh dau phieu muon qua han
- Tu dong cap nhat ton kho: muon giam 1, tra tang 1; chan muon khi het sach
- User chon so ngay muon (1-30), he thong tinh han tra (khong co dinh 14 ngay)

## 1) Cau hinh database

Tao CSDL bang script:

```sql
source database/schema.sql
```

## 2) Build

```powershell
.\build-app.bat
```

## 3) Chay

```powershell
.\run-app.bat
```

Mo trinh duyet: `http://localhost:3000`

## 4) Role

- Guest: xem `/`, `/catalog`
- User: dang nhap de muon qua `/borrow/request`
- Admin: quan ly dashboard + CRUD + muon/tra

## 5) Cau hinh Google Login (khuyen nghi)

Cap nhat file `.env`:

```env
GOOGLE_CLIENT_ID=your_google_oauth_client_id.apps.googleusercontent.com
```

Neu khong cau hinh `GOOGLE_CLIENT_ID`, he thong van dang nhap bang email + mat khau binh thuong.

## 6) Cau hinh email thong bao muon sach (khuyen nghi)

Cap nhat file `.env`:

```env
MAIL_USER=your_gmail@gmail.com
MAIL_PASS=your_gmail_app_password
MAIL_FROM=your_gmail@gmail.com
```

Luu y: voi Gmail, `MAIL_PASS` can la App Password (khong phai mat khau thuong).
