-- Run this once against EsportsDB to add the player profile fields.
-- Existing rows remain NULL until you enter their actual values.
IF OBJECT_ID(N'dbo.Players', N'U') IS NULL
    THROW 50001, 'dbo.Players does not exist. Select the application database first.', 1;
GO

IF COL_LENGTH(N'dbo.Players', N'Nationality') IS NULL
    ALTER TABLE dbo.Players ADD Nationality NVARCHAR(60) NULL;
GO

IF COL_LENGTH(N'dbo.Players', N'Age') IS NULL
    ALTER TABLE dbo.Players ADD Age INT NULL;
GO

SELECT TOP (20) Id, Nickname, Nationality, Age
FROM dbo.Players
ORDER BY Nickname;
GO
