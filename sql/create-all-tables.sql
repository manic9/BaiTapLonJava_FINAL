-- ============================================================================
-- 1. BẢNG USERS (Tài khoản quản trị & điều hành)
-- ============================================================================
IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        Id              NVARCHAR(36)    NOT NULL,
        Username        NVARCHAR(50)    NOT NULL,
        PasswordHash    NVARCHAR(256)   NOT NULL,
        PasswordSalt    NVARCHAR(64)    NOT NULL,
        FullName        NVARCHAR(100)   NOT NULL,
        Role            NVARCHAR(30)    NOT NULL CONSTRAINT DF_Users_Role DEFAULT N'ADMIN',
        IsActive        BIT             NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT 1,
        CreatedAt       DATETIME2       NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSDATETIME(),
        UpdatedAt       DATETIME2       NULL,

        CONSTRAINT PK_Users PRIMARY KEY (Id),
        CONSTRAINT UQ_Users_Username UNIQUE (Username),
        CONSTRAINT CK_Users_Role CHECK (Role IN (N'ADMIN', N'ORGANIZER', N'REFEREE'))
    );
END
GO

-- ============================================================================
-- 2. BẢNG TOURNAMENTS (Quản lý giải đấu)
-- ============================================================================
IF OBJECT_ID(N'dbo.Tournaments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Tournaments (
        Id              NVARCHAR(36)    NOT NULL,
        Name            NVARCHAR(150)   NOT NULL,
        Game            NVARCHAR(80)    NOT NULL,
        Organizer       NVARCHAR(120)   NOT NULL CONSTRAINT DF_Tournaments_Organizer DEFAULT N'Nexus Arena',
        Format          NVARCHAR(40)    NOT NULL CONSTRAINT DF_Tournaments_Format DEFAULT N'SINGLE_ELIMINATION',
        MaxTeams        INT             NOT NULL,
        StartDate       DATE            NOT NULL,
        EndDate         DATE            NOT NULL,
        Location        NVARCHAR(150)   NULL,
        PrizePool       DECIMAL(18, 2)  NOT NULL CONSTRAINT DF_Tournaments_PrizePool DEFAULT 0,
        Status          NVARCHAR(25)    NOT NULL CONSTRAINT DF_Tournaments_Status DEFAULT N'UPCOMING',
        BannerUrl       NVARCHAR(300)   NULL,
        Description     NVARCHAR(MAX)   NULL,
        CreatedAt       DATETIME2       NOT NULL CONSTRAINT DF_Tournaments_CreatedAt DEFAULT SYSDATETIME(),
        UpdatedAt       DATETIME2       NULL,

        CONSTRAINT PK_Tournaments PRIMARY KEY (Id),
        CONSTRAINT CK_Tournaments_MaxTeams CHECK (MaxTeams >= 2),
        CONSTRAINT CK_Tournaments_Dates CHECK (EndDate >= StartDate),
        CONSTRAINT CK_Tournaments_PrizePool CHECK (PrizePool >= 0),
        CONSTRAINT CK_Tournaments_Status CHECK (Status IN (N'DRAFT', N'UPCOMING', N'ONGOING', N'COMPLETED', N'CANCELLED'))
    );

    CREATE INDEX IX_Tournaments_Game_Status ON dbo.Tournaments (Game, Status);
    CREATE INDEX IX_Tournaments_StartDate ON dbo.Tournaments (StartDate DESC);
END
GO

IF COL_LENGTH(N'dbo.Tournaments', N'Organizer') IS NULL
BEGIN
    ALTER TABLE dbo.Tournaments ADD Organizer NVARCHAR(120) NOT NULL
        CONSTRAINT DF_Tournaments_Organizer DEFAULT N'Nexus Arena' WITH VALUES;
END
GO

-- Đồng bộ các cột ứng dụng trên database đã được tạo từ phiên bản cũ.
IF COL_LENGTH(N'dbo.Tournaments', N'Game') IS NULL
BEGIN
    ALTER TABLE dbo.Tournaments ADD Game NVARCHAR(80) NULL;
    IF COL_LENGTH(N'dbo.Tournaments', N'GameTitle') IS NOT NULL
        EXEC(N'UPDATE dbo.Tournaments SET Game = GameTitle WHERE Game IS NULL');
    UPDATE dbo.Tournaments SET Game = N'League of Legends' WHERE Game IS NULL OR LTRIM(RTRIM(Game)) = N'';
    ALTER TABLE dbo.Tournaments ALTER COLUMN Game NVARCHAR(80) NOT NULL;
END
GO

-- ============================================================================
-- 3. BẢNG TEAMS (Quản lý đội tuyển)
-- ============================================================================
IF OBJECT_ID(N'dbo.Teams', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Teams (
        Id              NVARCHAR(36)    NOT NULL,
        Name            NVARCHAR(120)   NOT NULL,
        Tag             NVARCHAR(15)    NOT NULL,
        Game            NVARCHAR(80)    NOT NULL,
        CaptainName     NVARCHAR(100)   NULL,
        ContactEmail    NVARCHAR(254)  NULL,
        Region          NVARCHAR(60)    NULL,
        LogoUrl         NVARCHAR(300)   NULL,
        Status          NVARCHAR(25)    NOT NULL CONSTRAINT DF_Teams_Status DEFAULT N'ACTIVE',
        CreatedAt       DATETIME2       NOT NULL CONSTRAINT DF_Teams_CreatedAt DEFAULT SYSDATETIME(),
        UpdatedAt       DATETIME2       NULL,

        CONSTRAINT PK_Teams PRIMARY KEY (Id),
        CONSTRAINT UQ_Teams_Name UNIQUE (Name),
        CONSTRAINT UQ_Teams_Tag UNIQUE (Tag),
        CONSTRAINT CK_Teams_Status CHECK (Status IN (N'ACTIVE', N'INACTIVE', N'DISBANDED'))
    );

    CREATE INDEX IX_Teams_Game_Status ON dbo.Teams (Game, Status);
END
GO

IF COL_LENGTH(N'dbo.Teams', N'ContactEmail') IS NULL
BEGIN
    ALTER TABLE dbo.Teams ADD ContactEmail NVARCHAR(254) NULL;
END
GO

GO

-- ============================================================================
-- 4. BẢNG PLAYERS (Quản lý tuyển thủ thuộc đội)
-- ============================================================================
IF OBJECT_ID(N'dbo.Players', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Players (
        Id              NVARCHAR(36)    NOT NULL,
        TeamId          NVARCHAR(36)    NULL,
        FullName        NVARCHAR(100)   NOT NULL,
        Nickname        NVARCHAR(60)    NOT NULL,
        GameId          NVARCHAR(80)    NOT NULL,
        Role            NVARCHAR(40)    NULL,
        Nationality     NVARCHAR(60)    NULL,
        Age             INT             NULL,
        IsCaptain       BIT             NOT NULL CONSTRAINT DF_Players_IsCaptain DEFAULT 0,
        AvatarUrl       NVARCHAR(300)   NULL,
        Status          NVARCHAR(25)    NOT NULL CONSTRAINT DF_Players_Status DEFAULT N'ACTIVE',
        CreatedAt       DATETIME2       NOT NULL CONSTRAINT DF_Players_CreatedAt DEFAULT SYSDATETIME(),
        UpdatedAt       DATETIME2       NULL,

        CONSTRAINT PK_Players PRIMARY KEY (Id),
        CONSTRAINT UQ_Players_GameId UNIQUE (GameId),
        CONSTRAINT FK_Players_Team FOREIGN KEY (TeamId) REFERENCES dbo.Teams(Id) ON DELETE SET NULL,
        CONSTRAINT CK_Players_Status CHECK (Status IN (N'ACTIVE', N'BENCHED', N'SUSPENDED', N'FREE_AGENT'))
    );

    CREATE INDEX IX_Players_TeamId ON dbo.Players (TeamId);
    CREATE INDEX IX_Players_Nickname ON dbo.Players (Nickname);
END
GO

-- Keep existing installations compatible with the current Players model.
IF OBJECT_ID(N'dbo.Players', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Players', N'Nationality') IS NULL
    ALTER TABLE dbo.Players ADD Nationality NVARCHAR(60) NULL;
GO
IF OBJECT_ID(N'dbo.Players', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Players', N'Age') IS NULL
    ALTER TABLE dbo.Players ADD Age INT NULL;
GO

GO

-- ============================================================================
-- 5. BẢNG REGISTRATIONS (Quản lý đăng ký tham gia giải đấu)
-- ============================================================================
IF OBJECT_ID(N'dbo.Registrations', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Registrations (
        Id              NVARCHAR(36)    NOT NULL,
        TournamentId    NVARCHAR(36)    NOT NULL,
        TeamId          NVARCHAR(36)    NOT NULL,
        Seed            INT             NULL,
        Status          NVARCHAR(20)    NOT NULL CONSTRAINT DF_Registrations_Status DEFAULT N'PENDING',
        Notes           NVARCHAR(300)   NULL,
        RegisteredAt    DATETIME2       NOT NULL CONSTRAINT DF_Registrations_RegisteredAt DEFAULT SYSDATETIME(),
        UpdatedAt       DATETIME2       NULL,

        CONSTRAINT PK_Registrations PRIMARY KEY (Id),
        CONSTRAINT FK_Registrations_Tournament FOREIGN KEY (TournamentId) REFERENCES dbo.Tournaments(Id) ON DELETE CASCADE,
        CONSTRAINT FK_Registrations_Team FOREIGN KEY (TeamId) REFERENCES dbo.Teams(Id) ON DELETE NO ACTION,
        CONSTRAINT CK_Registrations_Seed CHECK (Seed IS NULL OR Seed >= 1),
        CONSTRAINT CK_Registrations_Status CHECK (Status IN (N'PENDING', N'APPROVED', N'REJECTED'))
    );

    -- Ngăn đăng ký trùng khi đang PENDING hoặc APPROVED, nhưng cho phép nộp lại nếu đơn trước đã REJECTED
    CREATE UNIQUE INDEX UX_Registrations_Active_Tournament_Team
        ON dbo.Registrations (TournamentId, TeamId)
        WHERE Status <> N'REJECTED';

    CREATE INDEX IX_Registrations_Tournament_Status ON dbo.Registrations (TournamentId, Status);
END
GO

-- ============================================================================
-- 6. BẢNG MATCHES (Trận đấu, lịch, tỷ số và liên kết nhánh đấu)
-- ============================================================================
IF OBJECT_ID(N'dbo.Matches', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Matches (
        Id                  NVARCHAR(36)    NOT NULL,
        TournamentId        NVARCHAR(36)    NOT NULL,
        BracketSide         NVARCHAR(20)    NOT NULL CONSTRAINT DF_Matches_BracketSide DEFAULT N'UPPER',
        RoundNumber         INT             NOT NULL,
        RoundName           NVARCHAR(60)    NOT NULL,
        MatchOrder          INT             NOT NULL CONSTRAINT DF_Matches_MatchOrder DEFAULT 1,
        Team1Id             NVARCHAR(36)    NULL,
        Team2Id             NVARCHAR(36)    NULL,
        Score1              INT             NOT NULL CONSTRAINT DF_Matches_Score1 DEFAULT 0,
        Score2              INT             NOT NULL CONSTRAINT DF_Matches_Score2 DEFAULT 0,
        WinnerTeamId        NVARCHAR(36)    NULL,
        ScheduledAt         DATETIME2       NULL,
        Status              NVARCHAR(20)    NOT NULL CONSTRAINT DF_Matches_Status DEFAULT N'SCHEDULED',
        NextMatchId         NVARCHAR(36)    NULL,
        NextMatchSlot       INT             NULL,
        LoserNextMatchId    NVARCHAR(36)    NULL,
        Notes               NVARCHAR(300)   NULL,
        CreatedAt           DATETIME2       NOT NULL CONSTRAINT DF_Matches_CreatedAt DEFAULT SYSDATETIME(),
        UpdatedAt           DATETIME2       NULL,

        CONSTRAINT PK_Matches PRIMARY KEY (Id),
        CONSTRAINT FK_Matches_Tournament FOREIGN KEY (TournamentId) REFERENCES dbo.Tournaments(Id) ON DELETE CASCADE,
        CONSTRAINT FK_Matches_Team1 FOREIGN KEY (Team1Id) REFERENCES dbo.Teams(Id) ON DELETE NO ACTION,
        CONSTRAINT FK_Matches_Team2 FOREIGN KEY (Team2Id) REFERENCES dbo.Teams(Id) ON DELETE NO ACTION,
        CONSTRAINT FK_Matches_Winner FOREIGN KEY (WinnerTeamId) REFERENCES dbo.Teams(Id) ON DELETE NO ACTION,
        CONSTRAINT FK_Matches_NextMatch FOREIGN KEY (NextMatchId) REFERENCES dbo.Matches(Id) ON DELETE NO ACTION,
        CONSTRAINT FK_Matches_LoserNextMatch FOREIGN KEY (LoserNextMatchId) REFERENCES dbo.Matches(Id) ON DELETE NO ACTION,
        CONSTRAINT CK_Matches_BracketSide CHECK (BracketSide IN (N'UPPER', N'LOWER', N'GRAND_FINAL', N'GROUP')),
        CONSTRAINT CK_Matches_RoundNumber CHECK (RoundNumber >= 1),
        CONSTRAINT CK_Matches_Scores CHECK (Score1 >= 0 AND Score2 >= 0),
        CONSTRAINT CK_Matches_Status CHECK (Status IN (N'SCHEDULED', N'LIVE', N'COMPLETED', N'CANCELLED')),
        CONSTRAINT CK_Matches_NextSlot CHECK (NextMatchSlot IS NULL OR NextMatchSlot IN (1, 2)),
        CONSTRAINT CK_Matches_NoSelfNext CHECK (NextMatchId IS NULL OR NextMatchId <> Id)
    );

    CREATE INDEX IX_Matches_Tournament_Round ON dbo.Matches (TournamentId, BracketSide, RoundNumber, MatchOrder);
    CREATE INDEX IX_Matches_ScheduledAt ON dbo.Matches (ScheduledAt);
    CREATE INDEX IX_Matches_Status ON dbo.Matches (TournamentId, Status);
END
GO

GO

-- ============================================================================
-- 7. BẢNG PRIZEALLOCATIONS (Phân bổ & giải ngân giải thưởng)
-- ============================================================================
IF OBJECT_ID(N'dbo.PrizeAllocations', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PrizeAllocations (
        Id                  NVARCHAR(36)    NOT NULL,
        TournamentId        NVARCHAR(36)    NOT NULL,
        RankPosition        INT             NOT NULL,
        RankTitle           NVARCHAR(60)    NOT NULL,
        Percentage          DECIMAL(5, 2)   NOT NULL CONSTRAINT DF_Prize_Percentage DEFAULT 0,
        Amount              DECIMAL(18, 2)  NOT NULL CONSTRAINT DF_Prize_Amount DEFAULT 0,
        TeamId              NVARCHAR(36)    NULL,
        DisbursementStatus  NVARCHAR(20)    NOT NULL CONSTRAINT DF_Prize_Status DEFAULT N'UNPAID',
        PaidAt              DATETIME2       NULL,
        CreatedAt           DATETIME2       NOT NULL CONSTRAINT DF_Prize_CreatedAt DEFAULT SYSDATETIME(),
        UpdatedAt           DATETIME2       NULL,

        CONSTRAINT PK_PrizeAllocations PRIMARY KEY (Id),
        CONSTRAINT UQ_Prize_Tournament_Rank UNIQUE (TournamentId, RankPosition),
        CONSTRAINT FK_Prize_Tournament FOREIGN KEY (TournamentId) REFERENCES dbo.Tournaments(Id) ON DELETE CASCADE,
        CONSTRAINT FK_Prize_Team FOREIGN KEY (TeamId) REFERENCES dbo.Teams(Id) ON DELETE SET NULL,
        CONSTRAINT CK_Prize_Rank CHECK (RankPosition >= 1),
        CONSTRAINT CK_Prize_Percentage CHECK (Percentage >= 0 AND Percentage <= 100),
        CONSTRAINT CK_Prize_Amount CHECK (Amount >= 0),
        CONSTRAINT CK_Prize_Status CHECK (DisbursementStatus IN (N'UNPAID', N'PROCESSING', N'PAID'))
    );
END
GO

-- ============================================================================
-- 8. BẢNG ACTIVITYLOGS (Nhật ký hoạt động hệ thống)
-- ============================================================================
IF OBJECT_ID(N'dbo.ActivityLogs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ActivityLogs (
        Id              NVARCHAR(36)    NOT NULL,
        ActorUsername   NVARCHAR(50)    NULL,
        ActionType      NVARCHAR(40)    NOT NULL,
        Description     NVARCHAR(400)   NOT NULL,
        CreatedAt       DATETIME2       NOT NULL CONSTRAINT DF_ActivityLogs_CreatedAt DEFAULT SYSDATETIME(),

        CONSTRAINT PK_ActivityLogs PRIMARY KEY (Id)
    );

    CREATE INDEX IX_ActivityLogs_CreatedAt ON dbo.ActivityLogs (CreatedAt DESC);
END
GO

-- ============================================================================
-- 9. BẢNG APPSETTINGS (Cấu hình ứng dụng bền vững)
-- ============================================================================
IF OBJECT_ID(N'dbo.AppSettings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.AppSettings (
        SettingKey      NVARCHAR(80)    NOT NULL,
        SettingValue    NVARCHAR(500)   NOT NULL,
        Description     NVARCHAR(200)   NULL,
        UpdatedAt       DATETIME2       NOT NULL CONSTRAINT DF_AppSettings_UpdatedAt DEFAULT SYSDATETIME(),

        CONSTRAINT PK_AppSettings PRIMARY KEY (SettingKey)
    );
END
GO

-- ============================================================================
-- 10. VIEW BẢNG XẾP HẠNG TÍNH ĐỘNG TỪ KẾT QUẢ TRẬN ĐẤU (vw_TournamentStandings)
-- ============================================================================
CREATE OR ALTER VIEW dbo.vw_TournamentStandings AS
SELECT
    r.TournamentId,
    t.Id AS TeamId,
    t.Name AS TeamName,
    t.Tag AS TeamTag,
    t.LogoUrl,
    COUNT(m.Id) AS MatchesPlayed,
    ISNULL(SUM(CASE WHEN m.WinnerTeamId = t.Id THEN 1 ELSE 0 END), 0) AS Wins,
    ISNULL(SUM(CASE WHEN m.WinnerTeamId IS NOT NULL AND m.WinnerTeamId <> t.Id THEN 1 ELSE 0 END), 0) AS Losses,
    ISNULL(SUM(CASE WHEN m.Team1Id = t.Id THEN m.Score1 WHEN m.Team2Id = t.Id THEN m.Score2 ELSE 0 END), 0) AS ScoreFor,
    ISNULL(SUM(CASE WHEN m.Team1Id = t.Id THEN m.Score2 WHEN m.Team2Id = t.Id THEN m.Score1 ELSE 0 END), 0) AS ScoreAgainst,
    ISNULL(SUM(CASE WHEN m.Team1Id = t.Id THEN m.Score1 - m.Score2 WHEN m.Team2Id = t.Id THEN m.Score2 - m.Score1 ELSE 0 END), 0) AS ScoreDiff,
    ISNULL(SUM(CASE WHEN m.WinnerTeamId = t.Id THEN 3 ELSE 0 END), 0) AS Points
FROM dbo.Registrations r
INNER JOIN dbo.Teams t
    ON t.Id = r.TeamId
LEFT JOIN dbo.Matches m
    ON m.TournamentId = r.TournamentId
    AND m.Status = N'COMPLETED'
    AND (m.Team1Id = t.Id OR m.Team2Id = t.Id)
WHERE r.Status = N'APPROVED'
GROUP BY
    r.TournamentId,
    t.Id,
    t.Name,
    t.Tag,
    t.LogoUrl;
GO
