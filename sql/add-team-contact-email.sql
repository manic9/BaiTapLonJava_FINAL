/* Adds the optional team contact email column to existing databases. */
IF OBJECT_ID(N'dbo.Teams', N'U') IS NULL
BEGIN
    ;THROW 51001, 'dbo.Teams does not exist. Select the application database first.', 1;
END;
GO

IF COL_LENGTH(N'dbo.Teams', N'ContactEmail') IS NULL
BEGIN
    ALTER TABLE dbo.Teams ADD ContactEmail NVARCHAR(254) NULL;
END;
GO
