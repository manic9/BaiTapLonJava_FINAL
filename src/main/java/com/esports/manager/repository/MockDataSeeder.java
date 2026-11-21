package com.esports.manager.repository;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.*;
import com.esports.manager.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class MockDataSeeder {

    private MockDataSeeder() {}

    public static void seed(AppContext ctx) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // ==================== 1. TEAMS ====================
        Team t1 = new Team("TEAM-01", "GAM Esports", "GAM", "Vietnam", "Archie", "contact@gamesports.vn", 2014, "#F59E0B");
        Team t2 = new Team("TEAM-02", "Team Flash", "FL", "Vietnam", "Stark", "esports@teamflash.gg", 2018, "#00D4FF");
        Team t3 = new Team("TEAM-03", "Fancy United", "FCY", "Vietnam", "Kishi", "manage@fancyunited.vn", 2020, "#7C5CFC");
        Team t4 = new Team("TEAM-04", "Vikings Esports", "VKE", "Vietnam", "SofM", "ops@vikings.gg", 2023, "#EF4444");
        Team t5 = new Team("TEAM-05", "CERBERUS Esports", "CES", "Vietnam", "Icarus", "info@cerberus.vn", 2018, "#10B981");
        Team t6 = new Team("TEAM-06", "Team Secret VN", "TS", "SEA", "Nixwater", "vn@teamsecret.gg", 2020, "#EC4899");
        Team t7 = new Team("TEAM-07", "Saigon Buffalo", "SGB", "Vietnam", "Ren", "admin@sgb.vn", 2019, "#F97316");
        Team t8 = new Team("TEAM-08", "Rapid Lofi", "RLF", "Vietnam", "Haruto", "team@rapidlofi.vn", 2022, "#38BDF8");

        ctx.getTeamRepo().save(t1);
        ctx.getTeamRepo().save(t2);
        ctx.getTeamRepo().save(t3);
        ctx.getTeamRepo().save(t4);
        ctx.getTeamRepo().save(t5);
        ctx.getTeamRepo().save(t6);
        ctx.getTeamRepo().save(t7);
        ctx.getTeamRepo().save(t8);

        // ==================== 2. PLAYERS ====================
        ctx.getPlayerRepo().save(new Player("PLR-01", "TEAM-01", "Levi", "Đỗ Duy Khánh", "Jungler", "Vietnam", 28, 4.85, true));
        ctx.getPlayerRepo().save(new Player("PLR-02", "TEAM-01", "Kiaya", "Trần Duy Sang", "Top Lane", "Vietnam", 25, 4.42, false));
        ctx.getPlayerRepo().save(new Player("PLR-03", "TEAM-01", "Emo", "Nguyễn Thái Vinh", "Mid Lane", "Vietnam", 22, 4.10, false));
        ctx.getPlayerRepo().save(new Player("PLR-04", "TEAM-02", "Slayder", "Nguyễn Linh Vương", "AD Carry", "Vietnam", 25, 4.60, true));
        ctx.getPlayerRepo().save(new Player("PLR-05", "TEAM-02", "Yoshino", "Lê Trung Kiên", "Top Lane", "Vietnam", 25, 3.95, false));
        ctx.getPlayerRepo().save(new Player("PLR-06", "TEAM-02", "Palette", "Nguyễn Hải Trung", "Support", "Vietnam", 26, 4.25, false));
        ctx.getPlayerRepo().save(new Player("PLR-07", "TEAM-03", "YESicaN", "Đinh Đình Đạt", "Initiator", "Vietnam", 24, 1.38, true));
        ctx.getPlayerRepo().save(new Player("PLR-08", "TEAM-03", "f1cio", "Hoàng Ngọc Sơn", "Controller", "Vietnam", 23, 1.24, false));
        ctx.getPlayerRepo().save(new Player("PLR-09", "TEAM-03", "Nizzy", "Nguyễn Tấn Thành", "Duelist", "Vietnam", 21, 1.45, false));
        ctx.getPlayerRepo().save(new Player("PLR-10", "TEAM-04", "Kati", "Đặng Thanh Phê", "Mid Lane", "Vietnam", 26, 4.55, true));
        ctx.getPlayerRepo().save(new Player("PLR-11", "TEAM-04", "Shogun", "Nguyễn Văn Huy", "AD Carry", "Vietnam", 23, 4.72, false));
        ctx.getPlayerRepo().save(new Player("PLR-12", "TEAM-04", "Bie", "Trần Đức Hiếu", "Support", "Vietnam", 26, 4.30, false));
        ctx.getPlayerRepo().save(new Player("PLR-13", "TEAM-05", "Artemis", "Trần Quốc Hưng", "AD Carry", "Vietnam", 25, 4.48, true));
        ctx.getPlayerRepo().save(new Player("PLR-14", "TEAM-05", "Pun", "Nguyễn Đăng Khoa", "Top Lane", "Vietnam", 24, 3.88, false));
        ctx.getPlayerRepo().save(new Player("PLR-15", "TEAM-06", "Hasmed", "Lâm Huỳnh Gia Huy", "Top Lane", "Vietnam", 24, 4.05, true));
        ctx.getPlayerRepo().save(new Player("PLR-16", "TEAM-06", "Eddie", "Hoàng Công Nghĩa", "AD Carry", "Vietnam", 23, 4.20, false));
        ctx.getPlayerRepo().save(new Player("PLR-17", "TEAM-07", "BeanJ", "Trần Văn Chính", "Jungler", "Vietnam", 23, 3.92, true));
        ctx.getPlayerRepo().save(new Player("PLR-18", "TEAM-08", "Pr0digy", "Võ Minh Quân", "Sentinel", "Vietnam", 22, 1.31, true));

        // ==================== 3. TOURNAMENTS ====================
        Tournament trn1 = new Tournament(
                "TRN-VCT26", "VCT Challengers Vietnam Split 2", "VALORANT", "Riot Games VN",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.ONGOING, 8, 25000.0,
                today.minusDays(4), today.plusDays(6), "Vikings Gaming Arena, TP.HCM",
                "Giải đấu VALORANT chuyên nghiệp cấp cao nhất khu vực Việt Nam tìm kiếm đại diện tham dự VCT Ascension Pacific."
        );

        Tournament trn2 = new Tournament(
                "TRN-VCS26", "VCS Summer Championship 2026", "League of Legends", "VNGGames & Riot",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.ONGOING, 4, 50000.0,
                today.minusDays(2), today.plusDays(8), "GG Stadium, TP.HCM",
                "Vòng Playoffs giải vô địch Liên Minh Huyền Thoại Việt Nam mùa Hè 2026."
        );

        Tournament trn3 = new Tournament(
                "TRN-CS2HN", "Hanoi CS2 Masters Open", "Counter-Strike 2", "Nexus Arena",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.UPCOMING, 8, 15000.0,
                today.plusDays(10), today.plusDays(15), "Cung Điền Kinh Mỹ Đình, Hà Nội",
                "Giải đấu Counter-Strike 2 mở rộng quy tụ các đội tuyển hàng đầu miền Bắc và khu vực Đông Nam Á."
        );

        Tournament trn4 = new Tournament(
                "TRN-DOTA2", "Nexus Dota 2 Invitational", "Dota 2", "Nexus Arena",
                BracketFormat.SINGLE_ELIMINATION, TournamentStatus.COMPLETED, 4, 30000.0,
                today.minusDays(20), today.minusDays(14), "Online / SEA Server",
                "Giải đấu khách mời Dota 2 quy tụ 4 đội tuyển hạt giống hàng đầu Đông Nam Á."
        );

        ctx.getTournamentRepo().save(trn1);
        ctx.getTournamentRepo().save(trn2);
        ctx.getTournamentRepo().save(trn3);
        ctx.getTournamentRepo().save(trn4);

        // ==================== 4. REGISTRATIONS ====================
        String[] allTeamIds = {"TEAM-01", "TEAM-02", "TEAM-03", "TEAM-04", "TEAM-05", "TEAM-06", "TEAM-07", "TEAM-08"};
        for (int i = 0; i < allTeamIds.length; i++) {
            ctx.getRegistrationRepo().save(new Registration(
                    "REG-VCT" + (i + 1), "TRN-VCT26", allTeamIds[i],
                    now.minusDays(10 - i), i + 1, RegistrationStatus.APPROVED, "Đủ điều kiện thi đấu"
            ));
        }

        // VCS 4 đội hạt giống
        for (int i = 0; i < 4; i++) {
            ctx.getRegistrationRepo().save(new Registration(
                    "REG-VCS" + (i + 1), "TRN-VCS26", allTeamIds[i],
                    now.minusDays(7), i + 1, RegistrationStatus.APPROVED, "Hạt giống Playoffs"
            ));
        }

        // Hanoi CS2 Open (có cả đơn đã duyệt & chờ duyệt)
        ctx.getRegistrationRepo().save(new Registration("REG-CS1", "TRN-CS2HN", "TEAM-01", now.minusDays(2), 1, RegistrationStatus.APPROVED, "Hạt giống số 1"));
        ctx.getRegistrationRepo().save(new Registration("REG-CS2", "TRN-CS2HN", "TEAM-03", now.minusDays(2), 2, RegistrationStatus.APPROVED, "Hạt giống số 2"));
        ctx.getRegistrationRepo().save(new Registration("REG-CS3", "TRN-CS2HN", "TEAM-05", now.minusDays(1), 0, RegistrationStatus.PENDING, "Đang kiểm tra SteamID tuyển thủ"));
        ctx.getRegistrationRepo().save(new Registration("REG-CS4", "TRN-CS2HN", "TEAM-08", now.minusHours(5), 0, RegistrationStatus.PENDING, "Chờ xác nhận đội hình chính thức"));

        // Dota 2 Invitational (4 đội)
        for (int i = 0; i < 4; i++) {
            ctx.getRegistrationRepo().save(new Registration(
                    "REG-DT" + (i + 1), "TRN-DOTA2", allTeamIds[i],
                    now.minusDays(25), i + 1, RegistrationStatus.APPROVED, "Khách mời trực tiếp"
            ));
        }

        // ==================== 5. MATCHES (LINKED BRACKETS) ====================
        // --- Giải TRN-VCT26 (8 đội: 4 Tứ kết đã xong -> 2 Bán kết -> 1 Chung kết) ---
        ctx.getMatchRepo().save(new Match("MCH-VCT-F", "TRN-VCT26", 3, 1, "Chung kết tổng",
                null, null, 0, 0, 5, null, null, 0, now.plusDays(3).withHour(19), MatchStatus.SCHEDULED));

        ctx.getMatchRepo().save(new Match("MCH-VCT-SF1", "TRN-VCT26", 2, 1, "Bán kết 1",
                "TEAM-01", "TEAM-04", 1, 1, 3, null, "MCH-VCT-F", 1, now.withHour(18), MatchStatus.LIVE));

        ctx.getMatchRepo().save(new Match("MCH-VCT-SF2", "TRN-VCT26", 2, 2, "Bán kết 2",
                "TEAM-02", "TEAM-03", 0, 0, 3, null, "MCH-VCT-F", 2, now.plusDays(1).withHour(18), MatchStatus.SCHEDULED));

        ctx.getMatchRepo().save(new Match("MCH-VCT-QF1", "TRN-VCT26", 1, 1, "Tứ kết 1",
                "TEAM-01", "TEAM-08", 2, 0, 3, "TEAM-01", "MCH-VCT-SF1", 1, now.minusDays(2), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-VCT-QF2", "TRN-VCT26", 1, 2, "Tứ kết 2",
                "TEAM-04", "TEAM-05", 2, 1, 3, "TEAM-04", "MCH-VCT-SF1", 2, now.minusDays(2), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-VCT-QF3", "TRN-VCT26", 1, 3, "Tứ kết 3",
                "TEAM-02", "TEAM-07", 2, 0, 3, "TEAM-02", "MCH-VCT-SF2", 1, now.minusDays(1), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-VCT-QF4", "TRN-VCT26", 1, 4, "Tứ kết 4",
                "TEAM-03", "TEAM-06", 2, 1, 3, "TEAM-03", "MCH-VCT-SF2", 2, now.minusDays(1), MatchStatus.COMPLETED));

        // --- Giải TRN-VCS26 (4 đội: 2 Bán kết -> 1 Chung kết) ---
        ctx.getMatchRepo().save(new Match("MCH-VCS-F", "TRN-VCS26", 2, 1, "Chung kết tổng",
                "TEAM-01", null, 0, 0, 5, null, null, 0, now.plusDays(4).withHour(17), MatchStatus.SCHEDULED));
        ctx.getMatchRepo().save(new Match("MCH-VCS-SF1", "TRN-VCS26", 1, 1, "Bán kết 1",
                "TEAM-01", "TEAM-04", 3, 1, 5, "TEAM-01", "MCH-VCS-F", 1, now.minusDays(1), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-VCS-SF2", "TRN-VCS26", 1, 2, "Bán kết 2",
                "TEAM-02", "TEAM-03", 2, 1, 5, null, "MCH-VCS-F", 2, now.withHour(19), MatchStatus.LIVE));

        // --- Giải TRN-DOTA2 (Đã hoàn thành toàn bộ) ---
        ctx.getMatchRepo().save(new Match("MCH-DT-F", "TRN-DOTA2", 2, 1, "Chung kết tổng",
                "TEAM-03", "TEAM-01", 3, 2, 5, "TEAM-03", null, 0, now.minusDays(14), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-DT-SF1", "TRN-DOTA2", 1, 1, "Bán kết 1",
                "TEAM-01", "TEAM-04", 2, 0, 3, "TEAM-01", "MCH-DT-F", 2, now.minusDays(16), MatchStatus.COMPLETED));
        ctx.getMatchRepo().save(new Match("MCH-DT-SF2", "TRN-DOTA2", 1, 2, "Bán kết 2",
                "TEAM-03", "TEAM-02", 2, 1, 3, "TEAM-03", "MCH-DT-F", 1, now.minusDays(16), MatchStatus.COMPLETED));

        // ==================== 6. RECALCULATE STANDINGS ====================
        ctx.getStandingsService().recalculate("TRN-VCT26");
        ctx.getStandingsService().recalculate("TRN-VCS26");
        ctx.getStandingsService().recalculate("TRN-DOTA2");

        // ==================== 7. PRIZE ALLOCATIONS ====================
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-01", "TRN-VCT26", "Hạng 1 (Vô địch)", 50.0, 12500.0, null, false));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-02", "TRN-VCT26", "Hạng 2 (Á quân)", 30.0, 7500.0, null, false));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-03", "TRN-VCT26", "Hạng 3-4", 20.0, 5000.0, null, false));

        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-04", "TRN-DOTA2", "Hạng 1 (Vô địch)", 60.0, 18000.0, "TEAM-03", true));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-05", "TRN-DOTA2", "Hạng 2 (Á quân)", 25.0, 7500.0, "TEAM-01", true));
        ctx.getPrizeRepo().save(new PrizeAllocation("PRZ-06", "TRN-DOTA2", "Hạng 3 (Đồng hạng)", 15.0, 4500.0, "TEAM-02", true));

        // ==================== 8. ACTIVITY LOGS ====================
        ctx.getActivityRepo().save(new ActivityLog(now.minusMinutes(12), "MATCH", "Trận Bán kết 1 VCT Challengers: GAM vs VKE đang diễn ra (1-1)", "Admin"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusHours(1), "REGISTRATION", "Đội Rapid Lofi gửi đơn đăng ký tham gia Hanoi CS2 Masters Open", "System"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusHours(3), "MATCH", "Fancy United đánh bại Team Secret VN 2-1 tại Tứ kết 4 VCT", "Admin"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusHours(6), "PRIZE", "Đã giải ngân $18,000 tiền thưởng Vô địch Nexus Dota 2 cho Fancy United", "Finance"));
        ctx.getActivityRepo().save(new ActivityLog(now.minusDays(1), "TOURNAMENT", "Mở cổng đăng ký giải đấu Hanoi CS2 Masters Open ($15,000)", "Admin"));
    }
}