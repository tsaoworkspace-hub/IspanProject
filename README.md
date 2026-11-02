<p align="center">
  <img src="sneakermarket-frontend/public/images/brand/newLogo_rectangle.png" alt="酷客 Kicks" width="360">
</p>

# 酷客 Kicks｜球鞋商城、競標與活動通知平台

酷客 Kicks 是一個整合一般商城、即時競標、會員、廣告、活動行事曆與通知服務的全端電商平台。專案採 Monorepo 管理，前端使用 Vue 3，後端使用 Spring Boot 與 Microsoft SQL Server。

這是我的求職作品版本。原專案由五人團隊共同開發；我主要負責行事曆、通知、Google Calendar 整合、地圖功能及相關管理後台，並參與跨模組整合、品牌介面與營運 Dashboard。完整 Commit History 保留所有團隊成員的實際貢獻。

## 我的主要貢獻

- 設計並實作活動行事曆、會員關注與站內通知流程。
- 建立通知偏好、通知訂閱、未讀狀態與商品發售通知機制。
- 串接 Google Calendar OAuth 2.0，處理授權狀態驗證、事件同步與解除連結。
- 使用 AES-256-GCM 加密保存 Google Access Token 與 Refresh Token。
- 整合活動地圖、地點搜尋與會員個人行事曆介面。
- 建置活動／通知管理後台、發送紀錄與營運 Dashboard。
- 參與全站品牌調整、共用導覽、後台路由及跨模組整合。
- 補強 Session 授權、CSRF、防暴力嘗試、圖片上傳與敏感設定管理。

## 系統功能

| 模組 | 主要功能 |
| --- | --- |
| 會員 | 註冊、登入、個人資料、角色與管理員功能 |
| 一般商城 | 商品搜尋、規格、購物車、優惠券、結帳與訂單 |
| 競標商城 | 商品刊登、出價、自動結標、付款與棄標處理 |
| 廣告 | 多版位廣告、排程上下架、曝光與點擊統計 |
| 行事曆 | 活動瀏覽、地圖、關注、提醒與 Google Calendar 同步 |
| 通知 | 站內通知、Email、偏好設定、訂閱與背景重試 |
| 管理後台 | 會員、商品、競標、廣告、活動、通知與營運指標 |

## 技術重點

- 一般商城與競標交易共用會員系統，但保留獨立訂單與付款流程。
- 使用交易與悲觀鎖處理競標出價的併發一致性。
- 以 WebSocket／STOMP 推送競標狀態及會員私人通知。
- 使用 Scheduler 與可選的 Redis Queue 執行結標、提醒與 Email 重試。
- 透過 OAuth `state` 驗證與 AES-GCM 加密保護 Google Calendar Token。
- 使用 HttpSession、CSRF Token、BCrypt 與管理員授權保護狀態操作。
- 金流 Callback 驗證簽章，第三方憑證只由環境變數注入。

## 系統架構

```text
Browser
   │
   ▼
Vue 3 + Vite
Vue Router / Axios / Bootstrap / Leaflet
   │
   ├── REST API
   └── WebSocket / STOMP
            │
            ▼
Spring Boot 4
Member / Commerce / Auction / Advertisement
Calendar / Notification / Coupon / Admin
            │
            ▼
Spring Data JPA / Hibernate
            │
            ▼
Microsoft SQL Server

External Services
Google Calendar / Gmail SMTP / Gemini
LINE Pay / ECPay / OpenStreetMap / Redis
```

## 技術棧

### Frontend

- Vue 3、Vite、Vue Router
- Axios、Bootstrap 5、Font Awesome
- Leaflet、Chart.js、STOMP.js
- HTML5、CSS3、JavaScript

### Backend

- Java 17、Spring Boot 4
- Spring MVC、Spring Security、HttpSession
- Spring Data JPA、Hibernate、Validation
- Spring WebSocket、Spring Mail、Scheduler
- Spring Data Redis、Maven、Lombok

### Data & Integration

- Microsoft SQL Server
- Google Calendar OAuth 2.0
- LINE Pay、ECPay
- Gmail SMTP、Gemini API
- OpenStreetMap／Nominatim

## 安全設計

- API Key、資料庫密碼、SMTP 密碼與第三方 Secret 不提交至 Git。
- 所有敏感設定由部署環境變數提供，正式環境不使用預設帳密。
- 密碼使用 BCrypt Hash；登入成功後更新 Session ID。
- Session 型 API 使用 CSRF Token，管理端操作驗證管理員身分。
- Google Calendar Token 使用 AES-256-GCM 加密後再寫入資料庫。
- 登入與 AI 客服端點設有請求頻率限制。
- 上傳圖片驗證副檔名、實際格式、尺寸與儲存路徑。
- 金流 Callback 驗證 CheckMacValue，不在 Log 輸出完整付款參數。

## Repository 結構

```text
SneakerMarket/
├── sneakermarket-frontend/       # Vue 3 前端
│   ├── src/components/
│   ├── src/views/
│   ├── src/service/
│   └── src/router/
├── sneakermarket-backend/        # Spring Boot 後端
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── database/
├── docs/                         # 系統與協作文件
└── README.md
```

## 本機啟動

### 環境需求

- JDK 17
- Node.js 與 npm
- Microsoft SQL Server
- Redis（只有啟用通知 Queue 時需要）

### Backend

先在 IDE Run Configuration 或作業系統設定環境變數：

```text
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=SneakerMarketDB;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=<your-password>
```

依需要再設定 `MAIL_*`、`ECPAY_*`、`COMMERCE_LINE_PAY_*`、`GOOGLE_CALENDAR_*` 與 `GEMINI_API_KEY`。請勿將真實值寫入 Repository。

Windows：

```powershell
cd sneakermarket-backend
.\mvnw.cmd spring-boot:run
```

macOS／Linux：

```bash
cd sneakermarket-backend
./mvnw spring-boot:run
```

Backend 預設位址：`http://localhost:8080`

### Frontend

```bash
cd sneakermarket-frontend
npm install
npm run dev
```

Frontend 預設位址：`http://localhost:5173`

如需更換 Backend 位址，可建立不提交至 Git 的 `.env.local`：

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## 驗證指令

```powershell
# Frontend
cd sneakermarket-frontend
npm audit --omit=dev
npm run build

# Backend
cd ..\sneakermarket-backend
.\mvnw.cmd test
```

## 專案說明

- 本 Repository 是團隊專題的個人作品展示版本，不宣稱所有模組皆由單一開發者完成。
- Commit History 用於保留真實開發過程與團隊成員貢獻。
- 正式部署前仍應依實際網域、Secret Manager、TLS 與資料庫環境進行設定。

---

Portfolio maintained by **Frank Tsao**

[GitHub：tsaoworkspace-hub](https://github.com/tsaoworkspace-hub)
