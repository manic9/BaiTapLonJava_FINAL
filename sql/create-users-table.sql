IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        Id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Users PRIMARY KEY,
        UserName NVARCHAR(100) NOT NULL,
        PasswordHash NVARCHAR(128) NOT NULL,
        PasswordSalt NVARCHAR(64) NOT NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT (1),
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT UQ_Users_UserName UNIQUE (UserName)
    );
END;
