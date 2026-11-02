/* ============================================================
   SneakerMarketDB - Microsoft SQL Server
   用途：第一次開發時建立member專題資料庫
   ============================================================ */

CREATE TABLE dbo.members (
    member_id BIGINT IDENTITY(1,1) NOT NULL,
    account NVARCHAR(50) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    deleted_at DATETIME2 NULL,
    email NVARCHAR(100) NOT NULL,
    name NVARCHAR(50) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    role NVARCHAR(20) NOT NULL DEFAULT 'ROLE_MEMBER',
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT PK_member PRIMARY KEY (member_id),
    CONSTRAINT UQ_member_account UNIQUE (account),
    CONSTRAINT UQ_member_email UNIQUE (email)
);

/* ============================================================
   SneakerMarketDB - Microsoft SQL Server
   用途：建立時可執行
   ============================================================ */
   SELECT * FROM dbo.members;
   
   
/* ============================================================
   SneakerMarketDB - Microsoft SQL Server
   用途：手動新增會員資料
   ============================================================ */
   INSERT INTO dbo.member
(
    account,
    email,
    name,
    password_hash,
    phone,
    role
)
VALUES
(
    'test001',
    'test001@example.com',
    N'測試會員',
    'test_password_hash',
    '0900000000',
    'ROLE_MEMBER'
);
