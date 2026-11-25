/*
   Chuẩn hóa ID cũ về đúng quy ước của ứng dụng: PREFIX- + 6 ký tự hex hoa.
   Sau đó thêm dữ liệu minh họa có khóa ngoại hợp lệ.
   Chạy file này trong SSMS sau khi chọn đúng database EsportsDB.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Tournaments', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Teams', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Players', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Registrations', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Matches', N'U') IS NULL
       OR OBJECT_ID(N'dbo.PrizeAllocations', N'U') IS NULL
       OR OBJECT_ID(N'dbo.ActivityLogs', N'U') IS NULL
    BEGIN
        ;THROW 51000, 'Chua co day du bang. Hay chay sql/create-all-tables.sql truoc.', 1;
    END;

    CREATE TABLE #IdMap (
        EntityName sysname NOT NULL,
        OldId NVARCHAR(36) NOT NULL,
        NewId NVARCHAR(36) NOT NULL,
        CONSTRAINT PK_IdMap PRIMARY KEY (EntityName, OldId),
        CONSTRAINT UQ_IdMap_New UNIQUE (EntityName, NewId)
    );
    CREATE TABLE #IdsToChange (Id NVARCHAR(36) NOT NULL PRIMARY KEY);

    DECLARE @Entities TABLE (EntityName sysname NOT NULL, Prefix NVARCHAR(8) NOT NULL);
    INSERT INTO @Entities (EntityName, Prefix) VALUES
        (N'Tournaments', N'TRN'),
        (N'Teams', N'TEAM'),
        (N'Players', N'PLR'),
        (N'Registrations', N'REG'),
        (N'Matches', N'MCH'),
        (N'PrizeAllocations', N'PRZ'),
        (N'ActivityLogs', N'LOG');

    DECLARE @TableName sysname;
    DECLARE @Prefix NVARCHAR(8);
    DECLARE @Pattern NVARCHAR(80);
    DECLARE @Sql NVARCHAR(MAX);
    DECLARE @OldId NVARCHAR(36);
    DECLARE @NewId NVARCHAR(36);
    DECLARE @CandidateExists BIT;

    DECLARE entity_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT EntityName, Prefix FROM @Entities;
    OPEN entity_cursor;
    FETCH NEXT FROM entity_cursor INTO @TableName, @Prefix;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DELETE FROM #IdsToChange;
        SET @Pattern = @Prefix + N'-[0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F][0-9A-F]';
        SET @Sql = N'INSERT INTO #IdsToChange (Id)
                     SELECT Id FROM dbo.' + QUOTENAME(@TableName) + N'
                     WHERE Id COLLATE Latin1_General_100_BIN2 NOT LIKE @P;';
        EXEC sys.sp_executesql @Sql, N'@P NVARCHAR(80)', @P = @Pattern;

        DECLARE ids_cursor CURSOR LOCAL FAST_FORWARD FOR SELECT Id FROM #IdsToChange;
        OPEN ids_cursor;
        FETCH NEXT FROM ids_cursor INTO @OldId;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            SET @CandidateExists = 1;
            WHILE @CandidateExists = 1
            BEGIN
                SET @NewId = @Prefix + N'-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6));
                SET @CandidateExists = 0;
                SET @Sql = N'IF EXISTS (SELECT 1 FROM dbo.' + QUOTENAME(@TableName) + N' WHERE Id = @Candidate)
                                SET @Found = 1;';
                EXEC sys.sp_executesql @Sql,
                    N'@Candidate NVARCHAR(36), @Found BIT OUTPUT',
                    @Candidate = @NewId, @Found = @CandidateExists OUTPUT;
                IF EXISTS (SELECT 1 FROM #IdMap WHERE EntityName = @TableName AND NewId = @NewId)
                    SET @CandidateExists = 1;
            END;

            INSERT INTO #IdMap (EntityName, OldId, NewId) VALUES (@TableName, @OldId, @NewId);
            FETCH NEXT FROM ids_cursor INTO @OldId;
        END;
        CLOSE ids_cursor;
        DEALLOCATE ids_cursor;

        FETCH NEXT FROM entity_cursor INTO @TableName, @Prefix;
    END;
    CLOSE entity_cursor;
    DEALLOCATE entity_cursor;

    -- Tạm tắt kiểm tra khóa ngoại để cập nhật đồng bộ cả ID cha và khóa ngoại con.
    ALTER TABLE dbo.Players NOCHECK CONSTRAINT ALL;
    ALTER TABLE dbo.Registrations NOCHECK CONSTRAINT ALL;
    ALTER TABLE dbo.Matches NOCHECK CONSTRAINT ALL;
    ALTER TABLE dbo.PrizeAllocations NOCHECK CONSTRAINT ALL;

    UPDATE p SET TeamId = m.NewId
    FROM dbo.Players p JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = p.TeamId;

    UPDATE r SET TournamentId = m.NewId
    FROM dbo.Registrations r JOIN #IdMap m ON m.EntityName = N'Tournaments' AND m.OldId = r.TournamentId;
    UPDATE r SET TeamId = m.NewId
    FROM dbo.Registrations r JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = r.TeamId;

    UPDATE mrow SET TournamentId = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Tournaments' AND m.OldId = mrow.TournamentId;
    UPDATE mrow SET Team1Id = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = mrow.Team1Id;
    UPDATE mrow SET Team2Id = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = mrow.Team2Id;
    UPDATE mrow SET WinnerTeamId = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = mrow.WinnerTeamId;
    UPDATE mrow SET NextMatchId = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Matches' AND m.OldId = mrow.NextMatchId;
    UPDATE mrow SET LoserNextMatchId = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Matches' AND m.OldId = mrow.LoserNextMatchId;

    UPDATE p SET TournamentId = m.NewId
    FROM dbo.PrizeAllocations p JOIN #IdMap m ON m.EntityName = N'Tournaments' AND m.OldId = p.TournamentId;
    UPDATE p SET TeamId = m.NewId
    FROM dbo.PrizeAllocations p JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = p.TeamId;

    UPDATE t SET Id = m.NewId
    FROM dbo.Tournaments t JOIN #IdMap m ON m.EntityName = N'Tournaments' AND m.OldId = t.Id;
    UPDATE t SET Id = m.NewId
    FROM dbo.Teams t JOIN #IdMap m ON m.EntityName = N'Teams' AND m.OldId = t.Id;
    UPDATE p SET Id = m.NewId
    FROM dbo.Players p JOIN #IdMap m ON m.EntityName = N'Players' AND m.OldId = p.Id;
    UPDATE r SET Id = m.NewId
    FROM dbo.Registrations r JOIN #IdMap m ON m.EntityName = N'Registrations' AND m.OldId = r.Id;
    UPDATE mrow SET Id = m.NewId
    FROM dbo.Matches mrow JOIN #IdMap m ON m.EntityName = N'Matches' AND m.OldId = mrow.Id;
    UPDATE p SET Id = m.NewId
    FROM dbo.PrizeAllocations p JOIN #IdMap m ON m.EntityName = N'PrizeAllocations' AND m.OldId = p.Id;
    UPDATE a SET Id = m.NewId
    FROM dbo.ActivityLogs a JOIN #IdMap m ON m.EntityName = N'ActivityLogs' AND m.OldId = a.Id;

    ALTER TABLE dbo.Players WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE dbo.Registrations WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE dbo.Matches WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE dbo.PrizeAllocations WITH CHECK CHECK CONSTRAINT ALL;

    /* Thêm 12 đội mẫu, bốn đội cho mỗi game. */
    DECLARE @TeamSeed TABLE (
        Tag NVARCHAR(15) PRIMARY KEY,
        Name NVARCHAR(120), Game NVARCHAR(80), Captain NVARCHAR(100), Region NVARCHAR(60)
    );
    INSERT INTO @TeamSeed VALUES
        (N'NXV', N'Nexus Vortex', N'VALORANT', N'Khoa Nguyễn', N'Hà Nội'),
        (N'BLZ', N'Blue Phoenix', N'VALORANT', N'Minh Trần', N'TP. Hồ Chí Minh'),
        (N'ARC', N'Arcane Wolves', N'VALORANT', N'Quân Lê', N'Đà Nẵng'),
        (N'RFT', N'Red Falcon Team', N'League of Legends', N'Hoàng Phạm', N'Hà Nội'),
        (N'ORB', N'Orbit Gaming', N'League of Legends', N'Long Võ', N'TP. Hồ Chí Minh'),
        (N'KNG', N'Kingdom Esports', N'League of Legends', N'Thành Đặng', N'Cần Thơ'),
        (N'WLF', N'Wolfpack Legends', N'League of Legends', N'Phúc Lâm', N'Đà Nẵng'),
        (N'CRW', N'Crossfire Reborn', N'Counter-Strike 2', N'Nam Bùi', N'Hải Phòng'),
        (N'VPR', N'Vesper Unit', N'Counter-Strike 2', N'Đức Đỗ', N'Hà Nội'),
        (N'NTH', N'Night Hawks', N'Counter-Strike 2', N'Khánh Võ', N'Đà Nẵng'),
        (N'BLK', N'Blackout Gaming', N'Counter-Strike 2', N'Bảo Trần', N'TP. Hồ Chí Minh'),
        (N'TMT', N'Tempest Esports', N'VALORANT', N'Vinh Nguyễn', N'Cần Thơ');

    INSERT INTO dbo.Teams (Id, Name, Tag, Game, CaptainName, Region, Status)
    SELECT N'TEAM-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6)),
           s.Name, s.Tag, s.Game, s.Captain, s.Region, N'ACTIVE'
    FROM @TeamSeed s
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Teams t WHERE t.Tag = s.Tag);

    CREATE TABLE #SeedTeams (Tag NVARCHAR(15) PRIMARY KEY, TeamId NVARCHAR(36) NOT NULL, Game NVARCHAR(80) NOT NULL);
    INSERT INTO #SeedTeams (Tag, TeamId, Game)
    SELECT t.Tag, t.Id, t.Game FROM dbo.Teams t JOIN @TeamSeed s ON s.Tag = t.Tag;

    /* 36 tuyển thủ, GameId giữ unique để script có thể chạy lại. */
    DECLARE @PlayerSeed TABLE (
        Tag NVARCHAR(15), GameId NVARCHAR(80) PRIMARY KEY, Nickname NVARCHAR(60),
        FullName NVARCHAR(100), Role NVARCHAR(40), IsCaptain BIT
    );
    INSERT INTO @PlayerSeed VALUES
      (N'NXV',N'nxv-kuro',N'Kuro',N'Nguyễn Minh Khoa',N'Duelist',1),(N'NXV',N'nxv-ryu',N'Ryu',N'Trần Quốc Huy',N'Initiator',0),(N'NXV',N'nxv-sora',N'Sora',N'Lê Nhật Minh',N'Controller',0),
      (N'BLZ',N'blz-ace',N'Ace',N'Phạm Gia Bảo',N'Duelist',1),(N'BLZ',N'blz-moon',N'Moon',N'Võ Ngọc Anh',N'Sentinel',0),(N'BLZ',N'blz-zero',N'Zero',N'Đặng Hoàng Long',N'Initiator',0),
      (N'ARC',N'arc-zen',N'Zen',N'Bùi Thanh Tùng',N'Controller',1),(N'ARC',N'arc-loki',N'Loki',N'Nguyễn Đức Anh',N'Duelist',0),(N'ARC',N'arc-fox',N'Fox',N'Trần Minh Đức',N'Sentinel',0),
      (N'RFT',N'rft-king',N'King',N'Đỗ Anh Tú',N'Top Lane',1),(N'RFT',N'rft-jay',N'Jay',N'Nguyễn Hải Nam',N'Jungler',0),(N'RFT',N'rft-luna',N'Luna',N'Phan Ngọc Hà',N'Mid Lane',0),
      (N'ORB',N'orb-flame',N'Flame',N'Lê Quang Huy',N'AD Carry',1),(N'ORB',N'orb-mint',N'Mint',N'Vũ Hoàng Sơn',N'Support',0),(N'ORB',N'orb-rain',N'Rain',N'Bùi Đức Mạnh',N'Jungler',0),
      (N'KNG',N'kng-drake',N'Drake',N'Hoàng Minh Nhật',N'Mid Lane',1),(N'KNG',N'kng-panda',N'Panda',N'Đặng Quốc Việt',N'Top Lane',0),(N'KNG',N'kng-milo',N'Milo',N'Phạm Tuấn Kiệt',N'Support',0),
      (N'WLF',N'wlf-sun',N'Sun',N'Nguyễn Thanh Tùng',N'Top Lane',1),(N'WLF',N'wlf-breeze',N'Breeze',N'Lê Hồng Phúc',N'Jungler',0),(N'WLF',N'wlf-ivy',N'Ivy',N'Vũ Khánh Linh',N'Support',0),
      (N'CRW',N'crw-bolt',N'Bolt',N'Trần Gia Hưng',N'Rifler',1),(N'CRW',N'crw-ash',N'Ash',N'Nguyễn Hoàng Phúc',N'AWPer',0),(N'CRW',N'crw-raven',N'Raven',N'Võ Thành Đạt',N'IGL',0),
      (N'VPR',N'vpr-cold',N'Cold',N'Phạm Duy Khánh',N'Rifler',1),(N'VPR',N'vpr-nova',N'Nova',N'Lê Trọng Nghĩa',N'AWPer',0),(N'VPR',N'vpr-echo',N'Echo',N'Bùi Quốc Bảo',N'Support',0),
      (N'NTH',N'nth-frost',N'Frost',N'Đỗ Minh Hiếu',N'Rifler',1),(N'NTH',N'nth-scope',N'Scope',N'Phạm Quốc Anh',N'AWPer',0),(N'NTH',N'nth-vex',N'Vex',N'Nguyễn Hữu Tài',N'IGL',0),
      (N'BLK',N'blk-ghost',N'Ghost',N'Võ Tuấn Kiệt',N'Rifler',1),(N'BLK',N'blk-aim',N'Aim',N'Bùi Minh Quân',N'AWPer',0),(N'BLK',N'blk-shade',N'Shade',N'Trần Đức Thịnh',N'Support',0),
      (N'TMT',N'tmt-storm',N'Storm',N'Phan Anh Duy',N'Duelist',1),(N'TMT',N'tmt-cloud',N'Cloud',N'Nguyễn Trọng Tín',N'Initiator',0),(N'TMT',N'tmt-spark',N'Spark',N'Lê Hoàng Phúc',N'Sentinel',0);

    INSERT INTO dbo.Players (Id, TeamId, FullName, Nickname, GameId, Role, IsCaptain, Status)
    SELECT N'PLR-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6)),
           st.TeamId, ps.FullName, ps.Nickname, ps.GameId, ps.Role, ps.IsCaptain, N'ACTIVE'
    FROM @PlayerSeed ps
    JOIN #SeedTeams st ON st.Tag = ps.Tag
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Players p WHERE p.GameId = ps.GameId);

    /* Thêm 3 giải mẫu, mỗi giải gắn với đúng game và đội phù hợp. */
    DECLARE @TournamentSeed TABLE (
        SeedKey NVARCHAR(20) PRIMARY KEY, Name NVARCHAR(150), Game NVARCHAR(80),
        StartDate DATE, EndDate DATE, Location NVARCHAR(150), PrizePool DECIMAL(18,2)
    );
    INSERT INTO @TournamentSeed VALUES
      (N'VAL',N'Nexus Valorant Cup 2026',N'VALORANT',DATEADD(day,7,CONVERT(date,GETDATE())),DATEADD(day,9,CONVERT(date,GETDATE())),N'Hà Nội',50000000),
      (N'LOL',N'Nexus League Championship 2026',N'League of Legends',DATEADD(day,14,CONVERT(date,GETDATE())),DATEADD(day,17,CONVERT(date,GETDATE())),N'TP. Hồ Chí Minh',75000000),
      (N'CS2',N'Nexus CS2 Open 2026',N'Counter-Strike 2',DATEADD(day,21,CONVERT(date,GETDATE())),DATEADD(day,23,CONVERT(date,GETDATE())),N'Hải Phòng',40000000);

    INSERT INTO dbo.Tournaments (Id, Name, Game, Format, MaxTeams, StartDate, EndDate, Location, PrizePool, Status, Description)
    SELECT N'TRN-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6)),
           s.Name, s.Game, N'SINGLE_ELIMINATION', 8, s.StartDate, s.EndDate, s.Location, s.PrizePool,
           N'UPCOMING', N'Dữ liệu mẫu được thêm để kiểm tra quản lý giải, đăng ký và lịch thi đấu.'
    FROM @TournamentSeed s
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Tournaments t WHERE t.Name = s.Name);

    CREATE TABLE #SeedTournaments (
        SeedKey NVARCHAR(20) PRIMARY KEY,
        TournamentId NVARCHAR(36) NOT NULL,
        Game NVARCHAR(80) NOT NULL,
        StartDate DATE NOT NULL
    );
    INSERT INTO #SeedTournaments (SeedKey, TournamentId, Game, StartDate)
    SELECT s.SeedKey, t.Id, s.Game, s.StartDate
    FROM @TournamentSeed s JOIN dbo.Tournaments t ON t.Name = s.Name;

    DECLARE @RegistrationSeed TABLE (SeedKey NVARCHAR(20), Tag NVARCHAR(15), Seed INT, PRIMARY KEY (SeedKey, Tag));
    INSERT INTO @RegistrationSeed VALUES
      (N'VAL',N'NXV',1),(N'VAL',N'BLZ',2),(N'VAL',N'ARC',3),(N'VAL',N'TMT',4),
      (N'LOL',N'RFT',1),(N'LOL',N'ORB',2),(N'LOL',N'KNG',3),(N'LOL',N'WLF',4),
      (N'CS2',N'CRW',1),(N'CS2',N'VPR',2),(N'CS2',N'NTH',3),(N'CS2',N'BLK',4);

    INSERT INTO dbo.Registrations (Id, TournamentId, TeamId, Seed, Status, Notes)
    SELECT N'REG-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6)),
           stn.TournamentId, st.TeamId, rs.Seed, N'APPROVED', N'Đội mẫu đã được duyệt'
    FROM @RegistrationSeed rs
    JOIN #SeedTournaments stn ON stn.SeedKey = rs.SeedKey
    JOIN #SeedTeams st ON st.Tag = rs.Tag AND st.Game = stn.Game
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.Registrations r
        WHERE r.TournamentId = stn.TournamentId AND r.TeamId = st.TeamId AND r.Status <> N'REJECTED'
    );

    /* Trận bán kết mẫu cho từng giải; liên kết FK tới đúng đội và giải. */
    DECLARE @MatchSeed TABLE (SeedKey NVARCHAR(20), RoundName NVARCHAR(60), MatchOrder INT, Tag1 NVARCHAR(15), Tag2 NVARCHAR(15), PRIMARY KEY (SeedKey, MatchOrder));
    INSERT INTO @MatchSeed VALUES
      (N'VAL',N'Bán kết',1,N'NXV',N'BLZ'),(N'VAL',N'Bán kết',2,N'ARC',N'TMT'),
      (N'LOL',N'Bán kết',1,N'RFT',N'ORB'),(N'LOL',N'Bán kết',2,N'KNG',N'WLF'),
      (N'CS2',N'Bán kết',1,N'CRW',N'VPR'),(N'CS2',N'Bán kết',2,N'NTH',N'BLK');

    INSERT INTO dbo.Matches (Id, TournamentId, BracketSide, RoundNumber, RoundName, MatchOrder,
                             Team1Id, Team2Id, Score1, Score2, ScheduledAt, Status, Notes)
    SELECT N'MCH-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6)),
           stn.TournamentId, N'UPPER', 1, ms.RoundName, ms.MatchOrder,
           t1.TeamId, t2.TeamId, 0, 0, DATEADD(hour, 18, CAST(stn.StartDate AS DATETIME2)), N'SCHEDULED',
           N'Trận mẫu được thêm từ script SQL.'
    FROM @MatchSeed ms
    JOIN #SeedTournaments stn ON stn.SeedKey = ms.SeedKey
    JOIN #SeedTeams t1 ON t1.Tag = ms.Tag1 AND t1.Game = stn.Game
    JOIN #SeedTeams t2 ON t2.Tag = ms.Tag2 AND t2.Game = stn.Game
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.Matches m
        WHERE m.TournamentId = stn.TournamentId AND m.RoundNumber = 1 AND m.MatchOrder = ms.MatchOrder
    );

    /* Hai dòng chia thưởng cho mỗi giải. */
    INSERT INTO dbo.PrizeAllocations (Id, TournamentId, RankPosition, RankTitle, Percentage, Amount, DisbursementStatus)
    SELECT N'PRZ-' + UPPER(LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6)),
           stn.TournamentId, r.RankPosition, r.RankTitle, r.Percentage,
           stnPrize.PrizePool * r.Percentage / 100.0, N'UNPAID'
    FROM #SeedTournaments stn
    JOIN @TournamentSeed stnPrize ON stnPrize.SeedKey = stn.SeedKey
    CROSS JOIN (VALUES (1,N'Vô địch',60.0),(2,N'Á quân',40.0)) r(RankPosition,RankTitle,Percentage)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.PrizeAllocations p WHERE p.TournamentId = stn.TournamentId AND p.RankPosition = r.RankPosition);

    COMMIT TRANSACTION;
    SELECT EntityName, COUNT(*) AS ChangedIds FROM #IdMap GROUP BY EntityName ORDER BY EntityName;
    SELECT N'Teams' AS Entity, COUNT(*) AS AddedSampleCount FROM #SeedTeams;
    SELECT N'Tournaments' AS Entity, COUNT(*) AS AddedSampleCount FROM #SeedTournaments;
END TRY
BEGIN CATCH
    IF CURSOR_STATUS('local', 'ids_cursor') >= 0 CLOSE ids_cursor;
    IF CURSOR_STATUS('local', 'ids_cursor') > -3 DEALLOCATE ids_cursor;
    IF CURSOR_STATUS('local', 'entity_cursor') >= 0 CLOSE entity_cursor;
    IF CURSOR_STATUS('local', 'entity_cursor') > -3 DEALLOCATE entity_cursor;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
