package com.esports.manager.repository;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.*;
import com.esports.manager.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class MockDataSeeder {

    private MockDataSeeder() {}

    public static void seed(AppContext ctx) {
        // Nếu cơ sở dữ liệu đã có dữ liệu Tournaments hoặc Teams thì không nạp đè dữ liệu mẫu
        if (ctx.getTournamentRepo().count() > 0 || ctx.getTeamRepo().count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // ==================== 1. TEAMS ====================
        Team t1 = new Team("TEAM-000001", "GAM Esports", "GAM", "Vietnam", "Archie", "contact@gamesports.vn", 2014, "#F59E0B");
        Team t2 = new Team("TEAM-000002", "Team Flash", "FL", "Vietnam", "Stark", "esports@teamflash.gg", 2018, "#00D4FF");
        Team t3 = new Team("TEAM-000003", "Fancy United", "FCY", "Vietnam", "Kishi", "manage@fancyunited.vn", 2020, "#7C5CFC");
        Team t4 = new Team("TEAM-000004", "Vikings Esports", "VKE", "Vietnam", "SofM", "ops@vikings.gg", 2023, "#EF4444");
        Team t5 = new Team("TEAM-000005", "CERBERUS Esports", "CES", "Vietnam", "Icarus", "info@cerberus.vn", 2018, "#10B981");
        Team t6 = new Team("TEAM-000006", "Team Secret VN", "TS", "SEA", "Nixwater", "vn@teamsecret.gg", 2020, "#EC4899");
        Team t7 = new Team("TEAM-000007", "Saigon Buffalo", "SGB", "Vietnam", "Ren", "admin@sgb.vn", 2019, "#F97316");
        Team t8 = new Team("TEAM-000008", "Rapid Lofi", "RLF", "Vietnam", "Haruto", "team@rapidlofi.vn", 2022, "#38BDF8");
        t3.setGameTitle("VALORANT");
        t8.setGameTitle("VALORANT");

        ctx.getTeamRepo().save(t1);
        ctx.getTeamRepo().save(t2);
        ctx.getTeamRepo().save(t3);
        ctx.getTeamRepo().save(t4);
        ctx.getTeamRepo().save(t5);
        ctx.getTeamRepo().save(t6);
        ctx.getTeamRepo().save(t7);
        ctx.getTeamRepo().save(t8);

        // ==================== 2. PLAYERS ====================
        ctx.getPlayerRepo().save(new Player("PLR-010001", "TEAM-000001", "Levi", "Đỗ Duy Khánh", "Jungler", "Vietnam", 28, 4.85, true));
        ctx.getPlayerRepo().save(new Player("PLR-010002", "TEAM-000001", "Kiaya", "Trần Duy Sang", "Top Lane", "Vietnam", 25, 4.42, false));
        ctx.getPlayerRepo().save(new Player("PLR-010003", "TEAM-000001", "Emo", "Nguyễn Thái Vinh", "Mid Lane", "Vietnam", 22, 4.10, false));
        ctx.getPlayerRepo().save(new Player("PLR-010004", "TEAM-000002", "Slayder", "Nguyễn Linh Vương", "AD Carry", "Vietnam", 25, 4.60, true));
        ctx.getPlayerRepo().save(new Player("PLR-010005", "TEAM-000002", "Yoshino", "Lê Trung Kiên", "Top Lane", "Vietnam", 25, 3.95, false));
        ctx.getPlayerRepo().save(new Player("PLR-010006", "TEAM-000002", "Palette", "Nguyễn Hải Trung", "Support", "Vietnam", 26, 4.25, false));
        ctx.getPlayerRepo().save(new Player("PLR-010007", "TEAM-000003", "YESicaN", "Đinh Đình Đạt", "Initiator", "Vietnam", 24, 1.38, true));
        ctx.getPlayerRepo().save(new Player("PLR-010008", "TEAM-000003", "f1cio", "Hoàng Ngọc Sơn", "Controller", "Vietnam", 23, 1.24, false));
        ctx.getPlayerRepo().save(new Player("PLR-010009", "TEAM-000003", "Nizzy", "Nguyễn Tấn Thành", "Duelist", "Vietnam", 21, 1.45, false));
        ctx.getPlayerRepo().save(new Player("PLR-010010", "TEAM-000004", "Kati", "Đặng Thanh Phê", "Mid Lane", "Vietnam", 26, 4.55, true));
        ctx.getPlayerRepo().save(new Player("PLR-010011", "TEAM-000004", "Shogun", "Nguyễn Văn Huy", "AD Carry", "Vietnam", 23, 4.72, false));
        ctx.getPlayerRepo().save(new Player("PLR-010012", "TEAM-000004", "Bie", "Trần Đức Hiếu", "Support", "Vietnam", 26, 4.30, false));
        ctx.getPlayerRepo().save(new Player("PLR-010013", "TEAM-000005", "Artemis", "Trần Quốc Hưng", "AD Carry", "Vietnam", 25, 4.48, true));
        ctx.getPlayerRepo().save(new Player("PLR-010014", "TEAM-000005", "Pun", "Nguyễn Đăng Khoa", "Top Lane", "Vietnam", 24, 3.88, false));
        ctx.getPlayerRepo().save(new Player("PLR-010015", "TEAM-000006", "Hasmed", "Lâm Huỳnh Gia Huy", "Top Lane", "Vietnam", 24, 4.05, true));
        ctx.getPlayerRepo().save(new Player("PLR-010016", "TEAM-000006", "Eddie", "Hoàng Công Nghĩa", "AD Carry", "Vietnam", 23, 4.20, false));
        ctx.getPlayerRepo().save(new Player("PLR-010017", "TEAM-000007", "BeanJ", "Trần Văn Chính", "Jungler", "Vietnam", 23, 3.92, true));
        ctx.getPlayerRepo().save(new Player("PLR-010018", "TEAM-000008", "Pr0digy", "Võ Minh Quân", "Sentinel", "Vietnam", 22, 1.31, true));

        // ==================== 3. TOURNAMENTS ====================
        Tournament trn1 = new Tournament(
                "TRN-00A026", "VCT Challengers Vietnam Split 2", "VALORANT", "Riot Games VN",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.ONGOING, 8, 25000.0,
                today.minusDays(4), today.plusDays(6), "Vikings Gaming Arena, TP.HCM",
                "Giải đấu VALORANT chuyên nghiệp cấp cao nhất khu vực Việt Nam tìm kiếm đại diện tham dự VCT Ascension Pacific."
        );

        Tournament trn2 = new Tournament(
                "TRN-00A027", "VCS Summer Championship 2026", "League of Legends", "VNGGames & Riot",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.ONGOING, 4, 50000.0,
                today.minusDays(2), today.plusDays(8), "GG Stadium, TP.HCM",
                "Vòng Playoffs giải vô địch Liên Minh Huyền Thoại Việt Nam mùa Hè 2026."
        );

        Tournament trn3 = new Tournament(
                "TRN-00A028", "Hanoi CS2 Masters Open", "Counter-Strike 2", "Nexus Arena",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.UPCOMING, 8, 15000.0,
                today.plusDays(10), today.plusDays(15), "Cung Điền Kinh Mỹ Đình, Hà Nội",
                "Giải đấu Counter-Strike 2 mở rộng quy tụ các đội tuyển hàng đầu miền Bắc và khu vực Đông Nam Á."
        );

        Tournament trn4 = new Tournament(
                "TRN-00A029", "Nexus Dota 2 Invitational", "Dota 2", "Nexus Arena",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.COMPLETED, 4, 30000.0,
                today.minusDays(20), today.minusDays(14), "Online / SEA Server",
                "Giải đấu khách mời Dota 2 quy tụ 4 đội tuyển hạt giống hàng đầu Đông Nam Á."
        );

        ctx.getTournamentRepo().save(trn1);
        ctx.getTournamentRepo().save(trn2);
        ctx.getTournamentRepo().save(trn3);
        ctx.getTournamentRepo().save(trn4);

        // ==================== 4. REGISTRATIONS ====================
        String[] allTeamIds = {"TEAM-000001", "TEAM-000002", "TEAM-000003", "TEAM-000004", "TEAM-000005", "TEAM-000006", "TEAM-000007", "TEAM-000008"};
        for (int i = 0; i < allTeamIds.length; i++) {
            ctx.getRegistrationRepo().save(new Registration(
                    "REG-00B00" + (i + 1), "TRN-00A026", allTeamIds[i],
                    now.minusDays(10 - i), i + 1, RegistrationStatus.APPROVED, "Đủ điều kiện thi đấu"
            ));
        }

        // VCS 4 đội hạt giống
        for (int i = 0; i < 4; i++) {
            ctx.getRegistrationRepo().save(new Registration(
                    "REG-00B01" + (i + 1), "TRN-00A027", allTeamIds[i],
                    now.minusDays(7), i + 1, RegistrationStatus.APPROVED, "Hạt giống Playoffs"
            ));
        }

        // Hanoi CS2 Open (có cả đơn đã duyệt & chờ duyệt)
        ctx.getRegistrationRepo().save(new Registration("REG-00B021", "TRN-00A028", "TEAM-000001", now.minusDays(2), 1, RegistrationStatus.APPROVED, "Hạt giống số 1"));
        ctx.getRegistrationRepo().save(new Registration("REG-00B022", "TRN-00A028", "TEAM-000003", now.minusDays(2), 2, RegistrationStatus.APPROVED, "Hạt giống số 2"));
        ctx.getRegistrationRepo().save(new Registration("REG-00B023", "TRN-00A028", "TEAM-000005", now.minusDays(1), 0, RegistrationStatus.PENDING, "Đang kiểm tra SteamID tuyển thủ"));
        ctx.getRegistrationRepo().save(new Registration("REG-00B024", "TRN-00A028", "TEAM-000008", now.minusHours(5), 0, RegistrationStatus.PENDING, "Chờ xác nhận đội hình chính thức"));

        // Dota 2 Invitational (4 đội)
        for (int i = 0; i < 4; i++) {
            ctx.getRegistrationRepo().save(new Registration(
                    "REG-00B03" + (i + 1), "TRN-00A029", allTeamIds[i],
                    now.minusDays(25), i + 1, RegistrationStatus.APPROVED, "Khách mời trực tiếp"
            ));
        }

        // ==================== 5. MATCHES (LINKED BRACKETS) ====================
        // --- Giải TRN-00A026 (8 đội: 4 Tứ kết đã xong -> 2 Bán kết -> 1 Chung kết) ---
        ctx.getMatchRepo().save(new Match("MCH-00C001", "TRN-00A026", 3, 1, "Chung kết tổng",
                null, null, 0, 0, 5, null, null, 0, now.plusDays(3).withHour(19), MatchStatus.SCHEDULED));

        ctx.getMatchRepo().save(new Match("MCH-00C002", "TRN-00A026", 2, 1, "Bán kết 1",
                "TEAM-000001", "TEAM-000004", 1, 1, 3, null, "MCH-00C001", 1, now.withHour(18), MatchStatus.LIVE));

        ctx.getMatchRepo().save(new Match("MCH-00C003", "TRN-00A026", 2, 2, "Bán kết 2",
                "TEAM-000002", "TEAM-000003", 0, 0, 3, null, "MCH-00C001", 2, now.plusDays(1).withHour(18), MatchStatus.SCHEDULED));

        ctx.getMatchRepo().save(new Match("MCH-00C004", "TRN-00A026", 1, 1, "Tứ kết 1",
                "TEAM-000001", "TEAM-000008", 2, 0, 3, "TEAM-000001", "MCH-00C002", 1, now.minusDays(2), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-00C005", "TRN-00A026", 1, 2, "Tứ kết 2",
                "TEAM-000004", "TEAM-000005", 2, 1, 3, "TEAM-000004", "MCH-00C002", 2, now.minusDays(2), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-00C006", "TRN-00A026", 1, 3, "Tứ kết 3",
                "TEAM-000002", "TEAM-000007", 2, 0, 3, "TEAM-000002", "MCH-00C003", 1, now.minusDays(1), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-00C007", "TRN-00A026", 1, 4, "Tứ kết 4",
                "TEAM-000003", "TEAM-000006", 2, 1, 3, "TEAM-000003", "MCH-00C003", 2, now.minusDays(1), MatchStatus.COMPLETED));

        // --- Giải TRN-00A027 (4 đội: 2 Bán kết -> 1 Chung kết) ---
        ctx.getMatchRepo().save(new Match("MCH-00C008", "TRN-00A027", 2, 1, "Chung kết tổng",
                "TEAM-000001", null, 0, 0, 5, null, null, 0, now.plusDays(4).withHour(17), MatchStatus.SCHEDULED));
        ctx.getMatchRepo().save(new Match("MCH-00C009", "TRN-00A027", 1, 1, "Bán kết 1",
                "TEAM-000001", "TEAM-000004", 3, 1, 5, "TEAM-000001", "MCH-00C008", 1, now.minusDays(1), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-00C00A", "TRN-00A027", 1, 2, "Bán kết 2",
                "TEAM-000002", "TEAM-000003", 2, 1, 5, null, "MCH-00C008", 2, now.withHour(19), MatchStatus.LIVE));

        // --- Giải TRN-00A029 (Đã hoàn thành toàn bộ) ---
        ctx.getMatchRepo().save(new Match("MCH-00C00B", "TRN-00A029", 2, 1, "Chung kết tổng",
                "TEAM-000003", "TEAM-000001", 3, 2, 5, "TEAM-000003", null, 0, now.minusDays(14), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-00C00C", "TRN-00A029", 1, 1, "Bán kết 1",
                "TEAM-000001", "TEAM-000004", 2, 0, 3, "TEAM-000001", "MCH-00C00B", 2, now.minusDays(16), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-00C00D", "TRN-00A029", 1, 2, "Bán kết 2",
                "TEAM-000003", "TEAM-000002", 2, 1, 3, "TEAM-000003", "MCH-00C00B", 1, now.minusDays(16), MatchStatus.COMPLETED));

        // ==================== 6. RECALCULATE STANDINGS ====================
        ctx.getStandingsService().recalculate("TRN-00A026");
        ctx.getStandingsService().recalculate("TRN-00A027");
        ctx.getStandingsService().recalculate("TRN-00A029");

        // ==================== 7. PRIZE ALLOCATIONS ====================
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-00D001", "TRN-00A026", "Hạng 1 (Vô địch)", 50.0, 12500.0, null, false));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-00D002", "TRN-00A026", "Hạng 2 (Á quân)", 30.0, 7500.0, null, false));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-00D003", "TRN-00A026", "Hạng 3-4", 20.0, 5000.0, null, false));

        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-00D004", "TRN-00A029", "Hạng 1 (Vô địch)", 60.0, 18000.0, "TEAM-000003", true));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-00D005", "TRN-00A029", "Hạng 2 (Á quân)", 25.0, 7500.0, "TEAM-000001", true));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-00D006", "TRN-00A029", "Hạng 3 (Đồng hạng)", 15.0, 4500.0, "TEAM-000002", true));

        // ==================== 8. ACTIVITY LOGS ====================
        ctx.getActivityRepo().save(new ActivityLog(now.minusMinutes(12), "MATCH", "Trận Bán kết 1 VCT Challengers: GAM vs VKE đang diễn ra (1-1)", "Admin"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusHours(1), "REGISTRATION", "Đội Rapid Lofi gửi đơn đăng ký tham gia Hanoi CS2 Masters Open", "System"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusHours(3), "MATCH", "Fancy United đánh bại Team Secret VN 2-1 tại Tứ kết 4 VCT", "Admin"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusHours(6), "PRIZE", "Đã giải ngân $18,000 tiền thưởng Vô địch Nexus Dota 2 cho Fancy United", "Finance"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusDays(1), "TOURNAMENT", "Mở cổng đăng ký giải đấu Hanoi CS2 Masters Open ($15,000)", "Admin"));
    }
}
