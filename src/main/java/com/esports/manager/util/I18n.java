package com.esports.manager.util;

import com.esports.manager.config.AppContext;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class I18n {

    public enum Language {
        VIETNAMESE("vi", "Tiếng Việt", "🇻🇳 Tiếng Việt"),
        ENGLISH("en", "English", "🇬🇧 English");

        private final String code;
        private final String name;
        private final String displayName;

        Language(String code, String name, String displayName) {
            this.code = code;
            this.name = name;
            this.displayName = displayName;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }

        public static Language fromCode(String code) {
            if (code == null) return VIETNAMESE;
            for (Language l : values()) {
                if (l.code.equalsIgnoreCase(code)) return l;
            }
            return VIETNAMESE;
        }
    }

    private static Language currentLanguage = Language.VIETNAMESE;
    private static final List<Consumer<Language>> listeners = new CopyOnWriteArrayList<>();

    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();

    static {
        Map<String, String> vi = new HashMap<>();
        Map<String, String> en = new HashMap<>();

        // ==================== SIDEBAR & TOPBAR ====================
        vi.put("sidebar.overview", "TỔNG QUAN");
        en.put("sidebar.overview", "OVERVIEW");

        vi.put("sidebar.tournaments", "QUẢN LÝ GIẢI ĐẤU");
        en.put("sidebar.tournaments", "TOURNAMENTS");

        vi.put("sidebar.teams", "ĐỘI TUYỂN & TUYỂN THỦ");
        en.put("sidebar.teams", "TEAMS & PLAYERS");

        vi.put("sidebar.system", "TÀI CHÍNH & HỆ THỐNG");
        en.put("sidebar.system", "FINANCE & SYSTEM");

        vi.put("nav.dashboard", "Tổng quan");
        en.put("nav.dashboard", "Dashboard");

        vi.put("nav.tournaments", "Quản lý giải đấu");
        en.put("nav.tournaments", "Tournaments");

        vi.put("nav.registrations", "Hồ sơ đăng ký");
        en.put("nav.registrations", "Registrations");

        vi.put("nav.matches", "Quản lý trận đấu");
        en.put("nav.matches", "Match Management");

        vi.put("nav.bracket", "Sơ đồ nhánh đấu");
        en.put("nav.bracket", "Tournament Bracket");

        vi.put("nav.standings", "Bảng xếp hạng");
        en.put("nav.standings", "Standings");

        vi.put("nav.teams", "Quản lý đội tuyển");
        en.put("nav.teams", "Team Management");

        vi.put("nav.players", "Quản lý tuyển thủ");
        en.put("nav.players", "Player Management");

        vi.put("nav.prizes", "Cơ cấu giải thưởng");
        en.put("nav.prizes", "Prize Management");

        vi.put("nav.settings", "Cài đặt");
        en.put("nav.settings", "Settings");

        vi.put("topbar.search_prompt", "Tìm nhanh giải đấu, đội tuyển...");
        en.put("topbar.search_prompt", "Search tournaments, teams...");

        vi.put("topbar.create_tournament", "Tạo Giải Đấu");
        en.put("topbar.create_tournament", "Create Tournament");

        vi.put("admin.role", "Xin Chào! Admin");
        en.put("admin.role", "Hi! Admin");
        vi.put("admin.greeting", "Xin Chào! %s");
        en.put("admin.greeting", "Hi! %s");

        vi.put("lang.label", "Ngôn ngữ:");
        en.put("lang.label", "Language:");

        vi.put("msg.lang_switched", "Đã chuyển đổi sang Tiếng Việt");
        en.put("msg.lang_switched", "Language switched to English");

        vi.put("lang.tooltip.vi", "Chuyển sang tiếng Việt");
        en.put("lang.tooltip.vi", "Switch to Vietnamese");

        vi.put("lang.tooltip.en", "Chuyển sang tiếng Anh");
        en.put("lang.tooltip.en", "Switch to English");

        // ==================== ENUM STATUS LABELS ====================
        vi.put("status.match.scheduled", "Chưa đấu");
        en.put("status.match.scheduled", "Scheduled");

        vi.put("status.match.live", "Đang diễn ra");
        en.put("status.match.live", "Live");

        vi.put("status.match.completed", "Hoàn thành");
        en.put("status.match.completed", "Completed");

        vi.put("status.tournament.upcoming", "Sắp diễn ra");
        en.put("status.tournament.upcoming", "Upcoming");

        vi.put("status.tournament.ongoing", "Đang diễn ra");
        en.put("status.tournament.ongoing", "Ongoing");

        vi.put("status.tournament.completed", "Đã kết thúc");
        en.put("status.tournament.completed", "Completed");

        vi.put("status.tournament.cancelled", "Đã hủy");
        en.put("status.tournament.cancelled", "Cancelled");

        vi.put("status.registration.pending", "Chờ duyệt");
        en.put("status.registration.pending", "Pending");

        vi.put("status.registration.approved", "Đã duyệt");
        en.put("status.registration.approved", "Approved");

        vi.put("status.registration.rejected", "Từ chối");
        en.put("status.registration.rejected", "Rejected");

        vi.put("status.live_dot", "● LIVE");
        en.put("status.live_dot", "● LIVE");

        // ==================== MATCH DIALOG ====================
        vi.put("match.dialog.title", "Cập nhật tỉ số");
        en.put("match.dialog.title", "Update Score");

        vi.put("match.dialog.team_a", "Đội A");
        en.put("match.dialog.team_a", "Team A");

        vi.put("match.dialog.team_b", "Đội B");
        en.put("match.dialog.team_b", "Team B");

        vi.put("match.dialog.status", "Trạng thái");
        en.put("match.dialog.status", "Status");

        vi.put("match.dialog.hint", "Lưu ý: Khi chọn trạng thái \"Hoàn thành\", đội có tỉ số cao hơn sẽ tự động tiến vào vòng tiếp theo.");
        en.put("match.dialog.hint", "Note: When status is \"Completed\", the team with the higher score will advance to the next round.");

        vi.put("match.dialog.draw_error", "Trận đấu hoàn thành không thể có tỉ số hòa (%d - %d)!");
        en.put("match.dialog.draw_error", "A completed match cannot have a draw score (%d - %d)!");

        vi.put("match.dialog.updated", "Đã cập nhật tỉ số trận %s");
        en.put("match.dialog.updated", "Score updated for match %s");

        vi.put("match.dialog.incomplete_teams", "Chưa đủ 2 đội thi đấu (TBD) để kết thúc trận!");
        en.put("match.dialog.incomplete_teams", "Both teams must be assigned to complete a match!");

        vi.put("match.dialog.edit_title", "Chỉnh sửa lịch thi đấu");
        en.put("match.dialog.edit_title", "Edit Match");

        vi.put("match.dialog.create_title", "Tạo trận đấu mới");
        en.put("match.dialog.create_title", "Create New Match");

        vi.put("match.dialog.edit_header", "Cập nhật thông số trận");
        en.put("match.dialog.edit_header", "Update match details");

        vi.put("match.dialog.create_header", "Lên lịch trận đấu mới cho giải");
        en.put("match.dialog.create_header", "Schedule a new match");

        vi.put("match.dialog.save_btn", "Lưu trận đấu");
        en.put("match.dialog.save_btn", "Save Match");

        vi.put("match.dialog.same_team_error", "Hai đội thi đấu không được trùng nhau!");
        en.put("match.dialog.same_team_error", "The two teams must be different!");

        vi.put("match.dialog.updated_msg", "Đã cập nhật lịch trận đấu!");
        en.put("match.dialog.updated_msg", "Match schedule updated!");

        vi.put("match.dialog.created_msg", "Đã tạo trận đấu mới!");
        en.put("match.dialog.created_msg", "New match created!");

        // ==================== TOURNAMENT FORMAT ====================
        vi.put("format.single_elimination", "Loại trực tiếp");
        en.put("format.single_elimination", "Single Elimination");

        vi.put("format.double_elimination", "Loại kép");
        en.put("format.double_elimination", "Double Elimination");

        vi.put("format.round_robin", "Vòng tròn");
        en.put("format.round_robin", "Round Robin");

        vi.put("format.group_stage", "Vòng bảng");
        en.put("format.group_stage", "Group Stage");

        // ==================== ROUND NAMES ====================
        vi.put("round.final", "Chung kết");
        en.put("round.final", "Final");
        vi.put("round.unknown", "Vòng đấu");
        en.put("round.unknown", "Round");

        vi.put("round.semi_final", "Bán kết");
        en.put("round.semi_final", "Semi-Final");

        vi.put("round.quarter_final", "Tứ kết");
        en.put("round.quarter_final", "Quarter-Final");

        vi.put("round.round_of_16", "Vòng 16");
        en.put("round.round_of_16", "Round of 16");

        vi.put("round.round_%d", "Vòng %d");
        en.put("round.round_%d", "Round %d");

        vi.put("team.tbd", "TBD");
        en.put("team.tbd", "TBD");

        // ==================== COMMON BUTTONS & FILTERS ====================
        vi.put("btn.reset", "Đặt lại");
        en.put("btn.reset", "Reset");

        vi.put("btn.view_all", "Xem tất cả");
        en.put("btn.view_all", "View All");

        vi.put("btn.details", "Chi tiết");
        en.put("btn.details", "Details");

        vi.put("btn.bracket", "Nhánh đấu");
        en.put("btn.bracket", "Bracket");

        vi.put("btn.edit", "Sửa");
        en.put("btn.edit", "Edit");

        vi.put("btn.delete", "Xóa");
        en.put("btn.delete", "Delete");

        vi.put("btn.save", "Lưu thay đổi");
        en.put("btn.save", "Save Changes");

        vi.put("btn.cancel", "Hủy bỏ");
        en.put("btn.cancel", "Cancel");

        vi.put("btn.back", "Quay lại");
        en.put("btn.back", "Back");

        vi.put("btn.back_to_list", "Quay lại Quản lý giải đấu");
        en.put("btn.back_to_list", "Back to Tournaments");

        vi.put("filter.all_tournaments", "Tất cả giải đấu");
        en.put("filter.all_tournaments", "All Tournaments");

        vi.put("filter.all_statuses", "Tất cả trạng thái");
        en.put("filter.all_statuses", "All Statuses");

        vi.put("filter.all_games", "Tất cả game");
        en.put("filter.all_games", "All Games");

        vi.put("filter.all_regions", "Tất cả khu vực");
        en.put("filter.all_regions", "All Regions");

        vi.put("filter.all_teams", "Tất cả đội tuyển");
        en.put("filter.all_teams", "All Teams");

        vi.put("filter.all_roles", "Tất cả vị trí");
        en.put("filter.all_roles", "All Roles");

        // ==================== TABLE COLUMNS ====================
        vi.put("col.tournament_id", "MÃ GIẢI");
        en.put("col.tournament_id", "ID");

        vi.put("col.tournament_name", "TÊN GIẢI ĐẤU");
        en.put("col.tournament_name", "TOURNAMENT NAME");

        vi.put("col.game", "TỰA GAME");
        en.put("col.game", "GAME");

        vi.put("col.format", "THỂ THỨC");
        en.put("col.format", "FORMAT");

        vi.put("col.teams", "SỐ ĐỘI");
        en.put("col.teams", "TEAMS");

        vi.put("col.prize_pool", "QUỸ THƯỞNG");
        en.put("col.prize_pool", "PRIZE POOL");

        vi.put("col.dates", "THỜI GIAN");
        en.put("col.dates", "DATES");

        vi.put("col.status", "TRẠNG THÁI");
        en.put("col.status", "STATUS");

        vi.put("col.actions", "THAO TÁC");
        en.put("col.actions", "ACTIONS");

        vi.put("col.reg_id", "MÃ ĐƠN");
        en.put("col.reg_id", "REG ID");

        vi.put("col.tournament", "GIẢI ĐẤU");
        en.put("col.tournament", "TOURNAMENT");

        vi.put("col.team", "ĐỘI TUYỂN");
        en.put("col.team", "TEAM");

        vi.put("col.seed", "SEED");
        en.put("col.seed", "SEED");

        vi.put("col.reg_date", "NGÀY ĐĂNG KÝ");
        en.put("col.reg_date", "DATE REGISTERED");

        vi.put("col.notes", "GHI CHÚ HỒ SƠ");
        en.put("col.notes", "NOTES");

        vi.put("col.review_actions", "XÉT DUYỆT & THAO TÁC");
        en.put("col.review_actions", "REVIEW & ACTIONS");

        vi.put("col.match_id", "MÃ TRẬN");
        en.put("col.match_id", "MATCH ID");

        vi.put("col.round", "VÒNG ĐẤU");
        en.put("col.round", "ROUND");

        vi.put("col.team_a", "ĐỘI A");
        en.put("col.team_a", "TEAM A");

        vi.put("col.score", "TỈ SỐ");
        en.put("col.score", "SCORE");

        vi.put("col.team_b", "ĐỘI B");
        en.put("col.team_b", "TEAM B");

        vi.put("col.best_of", "BO");
        en.put("col.best_of", "BO");

        vi.put("col.schedule", "LỊCH THI ĐẤU");
        en.put("col.schedule", "SCHEDULE");

        vi.put("col.rank", "HẠNG");
        en.put("col.rank", "RANK");

        vi.put("col.tag", "TAG");
        en.put("col.tag", "TAG");

        vi.put("col.played", "SỐ TRẬN");
        en.put("col.played", "PLAYED");

        vi.put("col.wins", "THẮNG (W)");
        en.put("col.wins", "WINS (W)");

        vi.put("col.losses", "THUA (L)");
        en.put("col.losses", "LOSSES (L)");

        vi.put("col.diff", "HIỆU SỐ (+/-)");
        en.put("col.diff", "DIFF (+/-)");

        vi.put("col.win_rate", "TỶ LỆ THẮNG");
        en.put("col.win_rate", "WIN RATE");

        vi.put("col.points", "ĐIỂM (PTS)");
        en.put("col.points", "POINTS (PTS)");

        vi.put("col.team_id", "MÃ ĐỘI");
        en.put("col.team_id", "TEAM ID");

        vi.put("col.team_name", "TÊN ĐỘI TUYỂN");
        en.put("col.team_name", "TEAM NAME");

        vi.put("col.region", "KHU VỰC");
        en.put("col.region", "REGION");

        vi.put("col.coach", "HUẤN LUYỆN VIÊN");
        en.put("col.coach", "HEAD COACH");

        vi.put("col.roster", "ĐỘI HÌNH");
        en.put("col.roster", "ROSTER");

        vi.put("col.founded", "THÀNH LẬP");
        en.put("col.founded", "FOUNDED");

        vi.put("col.email", "EMAIL LIÊN HỆ");
        en.put("col.email", "CONTACT EMAIL");

        vi.put("col.player_id", "MÃ");
        en.put("col.player_id", "ID");

        vi.put("col.ign", "IN-GAME NAME (IGN)");
        en.put("col.ign", "IN-GAME NAME (IGN)");

        vi.put("col.full_name", "HỌ VÀ TÊN");
        en.put("col.full_name", "FULL NAME");

        vi.put("col.role", "VỊ TRÍ");
        en.put("col.role", "ROLE");

        vi.put("col.nationality", "QUỐC TỊCH");
        en.put("col.nationality", "NATIONALITY");

        vi.put("col.age", "TUỔI");
        en.put("col.age", "AGE");
        vi.put("bracket.zoom_reset", "Đặt lại");
        en.put("bracket.zoom_reset", "Reset");

        vi.put("col.kda", "KDA RATIO");
        en.put("col.kda", "KDA RATIO");

        vi.put("col.prize_id", "MÃ");
        en.put("col.prize_id", "ID");

        vi.put("col.placement", "THỨ HẠNG");
        en.put("col.placement", "PLACEMENT");

        vi.put("col.percent", "TỶ LỆ (%)");
        en.put("col.percent", "PERCENTAGE (%)");

        vi.put("col.amount", "GIÁ TRỊ THƯỞNG");
        en.put("col.amount", "AMOUNT");

        vi.put("col.recipient", "ĐỘI NHẬN GIẢI");
        en.put("col.recipient", "RECIPIENT TEAM");

        vi.put("col.disbursement", "GIẢI NGÂN");
        en.put("col.disbursement", "DISBURSEMENT");

        // ==================== DASHBOARD VIEW ====================
        vi.put("dashboard.kpi.total_tournaments", "TỔNG GIẢI ĐẤU");
        en.put("dashboard.kpi.total_tournaments", "TOTAL TOURNAMENTS");

        vi.put("dashboard.kpi.system_teams", "ĐỘI TUYỂN HỆ THỐNG");
        en.put("dashboard.kpi.system_teams", "REGISTERED TEAMS");

        vi.put("dashboard.kpi.matches", "TRẬN ĐẤU");
        en.put("dashboard.kpi.matches", "MATCHES");

        vi.put("dashboard.kpi.total_prize_pool", "TỔNG QUỸ GIẢI THƯỞNG");
        en.put("dashboard.kpi.total_prize_pool", "TOTAL PRIZE POOL");

        vi.put("dashboard.section.featured_tournaments", "Giải đấu đang hoạt động & Sắp khởi tranh");
        en.put("dashboard.section.featured_tournaments", "Active & Upcoming Tournaments");

        vi.put("dashboard.section.spotlight_matches", "Lịch thi đấu & Tỉ số trực tiếp");
        en.put("dashboard.section.spotlight_matches", "Schedules & Live Scores");

        vi.put("dashboard.btn.manage_matches", "Quản lý trận đấu");
        en.put("dashboard.btn.manage_matches", "Manage Matches");

        vi.put("dashboard.section.recent_activity", "Hoạt động gần đây");
        en.put("dashboard.section.recent_activity", "Recent Activities");

        vi.put("dashboard.kpi.ongoing_format", "%d giải đang diễn ra");
        en.put("dashboard.kpi.ongoing_format", "%d ongoing");

        vi.put("dashboard.kpi.players_format", "%d tuyển thủ đăng ký");
        en.put("dashboard.kpi.players_format", "%d registered players");

        vi.put("dashboard.kpi.live_format", "%d trận đang LIVE");
        en.put("dashboard.kpi.live_format", "%d live matches");

        vi.put("dashboard.kpi.pending_format", "%d đơn đăng ký chờ duyệt");
        en.put("dashboard.kpi.pending_format", "%d pending applications");

        // ==================== TOURNAMENT VIEW ====================
        vi.put("tournament.search_prompt", "Tìm tên giải, game, đơn vị tổ chức...");
        en.put("tournament.search_prompt", "Search by tournament, game, organizer...");

        vi.put("tournament.btn_create", "Tạo Giải Đấu");
        en.put("tournament.btn_create", "Create Tournament");

        vi.put("tournament.count_format", "%d giải đấu");
        en.put("tournament.count_format", "%d tournaments");

        // ==================== TOURNAMENT DETAIL VIEW ====================
        vi.put("detail.btn_auto_bracket", "Tạo nhánh đấu tự động");
        en.put("detail.btn_auto_bracket", "Generate Auto Bracket");

        vi.put("detail.btn_view_bracket", "Xem Tournament Bracket");
        en.put("detail.btn_view_bracket", "View Tournament Bracket");

        vi.put("detail.lbl_prize_pool", "QUỸ GIẢI THƯỞNG");
        en.put("detail.lbl_prize_pool", "PRIZE POOL");

        vi.put("detail.lbl_teams", "ĐỘI THAM DỰ");
        en.put("detail.lbl_teams", "TEAM SLOTS");

        vi.put("detail.lbl_format_org", "THỂ THỨC & TỔ CHỨC");
        en.put("detail.lbl_format_org", "FORMAT & ORGANIZER");

        vi.put("detail.lbl_time_loc", "THỜI GIAN & ĐỊA ĐIỂM");
        en.put("detail.lbl_time_loc", "DATES & LOCATION");

        vi.put("detail.tab_teams", "Đội tuyển tham dự");
        en.put("detail.tab_teams", "Participating Teams");

        vi.put("detail.tab_matches", "Lịch thi đấu & Kết quả");
        en.put("detail.tab_matches", "Matches & Results");

        vi.put("detail.tab_prizes", "Cơ cấu giải thưởng");
        en.put("detail.tab_prizes", "Prize Allocations");

        // ==================== REGISTRATION VIEW ====================
        vi.put("reg.search_prompt", "Tìm tên đội, giải đấu, ghi chú...");
        en.put("reg.search_prompt", "Search team, tournament, notes...");

        vi.put("reg.btn_create", "Đăng Ký Đội Vào Giải");
        en.put("reg.btn_create", "Register Team");

        vi.put("reg.count_format", "%d đơn đăng ký");
        en.put("reg.count_format", "%d registrations");

        // ==================== MATCH VIEW ====================
        vi.put("match.search_prompt", "Tìm tên đội, mã trận, vòng đấu...");
        en.put("match.search_prompt", "Search team, match ID, round...");

        vi.put("match.btn_create", "Tạo Trận Đấu");
        en.put("match.btn_create", "Create Match");

        vi.put("match.count_format", "%d trận đấu");
        en.put("match.count_format", "%d matches");

        // ==================== BRACKET VIEW ====================
        vi.put("bracket.lbl_tournament", "Giải đấu:");
        en.put("bracket.lbl_tournament", "Tournament:");

        vi.put("bracket.prompt_select", "Chọn giải đấu để xem nhánh đấu");
        en.put("bracket.prompt_select", "Select tournament to view bracket");

        vi.put("bracket.btn_generate", "Tạo / Làm mới nhánh đấu");
        en.put("bracket.btn_generate", "Generate / Refresh Bracket");

        vi.put("bracket.lbl_champion", "NHÀ VÔ ĐỊCH:");
        en.put("bracket.lbl_champion", "CHAMPION:");

        vi.put("bracket.hint_click", "Nhấp vào thẻ trận đấu để cập nhật tỉ số");
        en.put("bracket.hint_click", "Click on match card to update score");

        vi.put("bracket.empty_state", "Giải đấu này chưa có sơ đồ nhánh đấu.");
        en.put("bracket.empty_state", "This tournament has no bracket yet.");

        vi.put("bracket.empty_hint", "Hãy đảm bảo giải có tối thiểu 4 đội được duyệt và nhấn \"Tạo / Làm mới nhánh đấu\".");
        en.put("bracket.empty_hint", "Ensure at least 4 approved teams, then click \"Generate / Refresh Bracket\".");

        // ==================== STANDINGS VIEW ====================
        vi.put("standings.lbl_tournament", "Giải đấu:");
        en.put("standings.lbl_tournament", "Tournament:");

        vi.put("standings.prompt_select", "Chọn giải đấu");
        en.put("standings.prompt_select", "Select tournament");

        vi.put("standings.search_prompt", "Lọc tên hoặc tag đội...");
        en.put("standings.search_prompt", "Filter team name or tag...");

        vi.put("standings.pts_rule", "Thắng: +3đ  •  Thua: 0đ");
        en.put("standings.pts_rule", "Win: +3pts  •  Loss: 0pts");

        vi.put("standings.btn_recalculate", "Tính lại BXH");
        en.put("standings.btn_recalculate", "Recalculate Table");

        vi.put("standings.toast.recalculated", "Đã cập nhật lại bảng xếp hạng cho %s");
        en.put("standings.toast.recalculated", "Standings recalculated for %s");

        // ==================== TEAM VIEW ====================
        vi.put("team.search_prompt", "Tìm tên đội, tag, HLV, mã đội...");
        en.put("team.search_prompt", "Search team name, tag, coach...");

        vi.put("team.btn_create", "Thêm Đội Tuyển");
        en.put("team.btn_create", "Add Team");

        vi.put("team.count_format", "%d đội tuyển");
        en.put("team.count_format", "%d teams");

        // ==================== PLAYER VIEW ====================
        vi.put("player.search_prompt", "Tìm IGN, họ tên, quốc tịch...");
        en.put("player.search_prompt", "Search IGN, full name, nationality...");

        vi.put("player.btn_create", "Thêm Tuyển Thủ");
        en.put("player.btn_create", "Add Player");

        vi.put("player.count_format", "%d tuyển thủ");
        en.put("player.count_format", "%d players");

        // ==================== PRIZE VIEW ====================
        vi.put("prize.kpi.total_allocated", "TỔNG CƠ CẤU GIẢI THƯỞNG");
        en.put("prize.kpi.total_allocated", "TOTAL PRIZE ALLOCATION");

        vi.put("prize.kpi.total_paid", "ĐÃ GIẢI NGÂN");
        en.put("prize.kpi.total_paid", "DISBURSED");

        vi.put("prize.kpi.total_pending", "CHỜ TRAO GIẢI");
        en.put("prize.kpi.total_pending", "PENDING AWARD");

        vi.put("prize.search_prompt", "Tìm hạng mục, tên đội nhận...");
        en.put("prize.search_prompt", "Search placement, recipient team...");

        vi.put("prize.btn_create", "Thêm Hạng Mục Thưởng");
        en.put("prize.btn_create", "Add Prize Allocation");

        vi.put("prize.paid", "Đã giải ngân");
        en.put("prize.paid", "Disbursed");

        vi.put("prize.pending", "Chờ giải ngân");
        en.put("prize.pending", "Pending");

        vi.put("prize.disburse", "Giải ngân");
        en.put("prize.disburse", "Disburse");

        vi.put("prize.revoke", "Hoàn tác");
        en.put("prize.revoke", "Revoke");

        vi.put("prize.unassigned", "Chưa xác định");
        en.put("prize.unassigned", "Unassigned");

        vi.put("prize.filter.all_statuses", "Tất cả trạng thái");
        en.put("prize.filter.all_statuses", "All Statuses");

        vi.put("prize.filter.paid", "Đã giải ngân");
        en.put("prize.filter.paid", "Disbursed");

        vi.put("prize.filter.pending", "Chờ giải ngân");
        en.put("prize.filter.pending", "Pending");

        vi.put("prize.dialog.edit_title", "Chỉnh sửa giải thưởng");
        en.put("prize.dialog.edit_title", "Edit Prize");

        vi.put("prize.dialog.create_title", "Thêm hạng mục giải thưởng");
        en.put("prize.dialog.create_title", "Add Prize Allocation");

        vi.put("prize.dialog.edit_header", "Cập nhật");
        en.put("prize.dialog.edit_header", "Update");

        vi.put("prize.dialog.create_header", "Thiết lập mức thưởng mới cho giải đấu");
        en.put("prize.dialog.create_header", "Set up new prize for tournament");

        vi.put("prize.dialog.save_btn", "Lưu giải thưởng");
        en.put("prize.dialog.save_btn", "Save Prize");

        vi.put("prize.dialog.tournament_label", "Giải đấu *");
        en.put("prize.dialog.tournament_label", "Tournament *");

        vi.put("prize.dialog.placement_label", "Thứ hạng *");
        en.put("prize.dialog.placement_label", "Placement *");

        vi.put("prize.dialog.percent_label", "Tỷ lệ (%)");
        en.put("prize.dialog.percent_label", "Percentage (%)");

        vi.put("prize.dialog.amount_label", "Số tiền thưởng");
        en.put("prize.dialog.amount_label", "Prize Amount");

        vi.put("prize.dialog.team_label", "Đội nhận giải");
        en.put("prize.dialog.team_label", "Recipient Team");

        vi.put("prize.dialog.paid_checkbox", "Đã hoàn tất giải ngân tiền thưởng");
        en.put("prize.dialog.paid_checkbox", "Prize has been disbursed");

        vi.put("prize.dialog.validation_error", "Vui lòng chọn Giải đấu và nhập tên Thứ hạng!");
        en.put("prize.dialog.validation_error", "Please select a Tournament and enter a Placement!");

        vi.put("prize.dialog.number_error", "Tỷ lệ phần trăm và giá trị thưởng phải là số hợp lệ (>= 0)!");
        en.put("prize.dialog.number_error", "Percentage and prize amount must be valid numbers (>= 0)!");

        vi.put("prize.dialog.updated_msg", "Đã cập nhật hạng mục thưởng!");
        en.put("prize.dialog.updated_msg", "Prize allocation updated!");

        vi.put("prize.dialog.created_msg", "Đã thêm hạng mục thưởng mới!");
        en.put("prize.dialog.created_msg", "New prize allocation added!");

        vi.put("prize.toast.disbursed", "Đã xác nhận giải ngân: %s");
        en.put("prize.toast.disbursed", "Confirmed disbursement: %s");

        vi.put("prize.toast.reverted", "Đã chuyển về trạng thái chờ giải ngân.");
        en.put("prize.toast.reverted", "Reverted to pending status.");

        vi.put("prize.delete.title", "Xóa hạng mục giải thưởng");
        en.put("prize.delete.title", "Delete Prize Allocation");

        vi.put("prize.delete.message", "Bạn có chắc chắn muốn xóa hạng mục \"%s\" (%s)?");
        en.put("prize.delete.message", "Are you sure you want to delete \"%s\" (%s)?");

        vi.put("prize.toast.deleted", "Đã xóa hạng mục giải thưởng!");
        en.put("prize.toast.deleted", "Prize allocation deleted!");

        vi.put("prize.edit_tooltip", "Chỉnh sửa giải thưởng");
        en.put("prize.edit_tooltip", "Edit prize");

        vi.put("prize.delete_tooltip", "Xóa hạng mục thưởng");
        en.put("prize.delete_tooltip", "Delete prize allocation");

        // ==================== SETTINGS VIEW ====================
        vi.put("settings.card1_title", "Cấu hình tổ chức & Thể thức mặc định");
        en.put("settings.card1_title", "Organization & Default Format Configuration");

        vi.put("settings.org_name", "Tên đơn vị tổ chức (Organization Name)");
        en.put("settings.org_name", "Organization Name");

        vi.put("settings.org_name_sub", "Hiển thị mặc định khi khởi tạo giải đấu mới");
        en.put("settings.org_name_sub", "Default organizer when creating new tournaments");

        vi.put("settings.lang", "Ngôn ngữ giao diện (Application Language)");
        en.put("settings.lang", "Application Language");

        vi.put("settings.lang_sub", "Chọn ngôn ngữ hiển thị trên toàn bộ thanh điều hướng và giao diện");
        en.put("settings.lang_sub", "Choose display language across navigation and views");

        vi.put("settings.currency", "Đơn vị tiền tệ hiển thị quỹ thưởng");
        en.put("settings.currency", "Prize Currency Symbol");

        vi.put("settings.currency_sub", "Áp dụng cho Dashboard, Giải đấu và Phân bổ giải thưởng");
        en.put("settings.currency_sub", "Applied to Dashboard, Tournaments, and Prize Pools");

        vi.put("settings.default_bo", "Thể thức trận đấu mặc định (Default Best-Of)");
        en.put("settings.default_bo", "Default Match Format (Best-Of)");

        vi.put("settings.default_bo_sub", "Số ván tối đa khi tạo trận mới hoặc sinh nhánh đấu tự động");
        en.put("settings.default_bo_sub", "Maximum games when creating matches or generating brackets");

        vi.put("settings.card2_title", "Tự động hóa & An toàn thao tác");
        en.put("settings.card2_title", "Automation & Operation Safety");

        vi.put("settings.chk_auto_advance", "Tự động đưa đội thắng vào vòng kế tiếp trên Tournament Bracket khi cập nhật kết quả");
        en.put("settings.chk_auto_advance", "Automatically advance winners to the next round in Tournament Bracket when updating scores");

        vi.put("settings.chk_confirm_delete", "Hiển thị hộp thoại xác nhận trước khi xóa giải đấu, đội tuyển, tuyển thủ hoặc trận đấu");
        en.put("settings.chk_confirm_delete", "Show confirmation dialog before deleting tournaments, teams, players, or matches");

        vi.put("settings.btn_save", "Lưu cấu hình");
        en.put("settings.btn_save", "Save Settings");

        vi.put("settings.card3_title", "Quản lý dữ liệu mẫu (In-Memory Collections)");
        en.put("settings.card3_title", "In-Memory Mock Data Management");

        vi.put("settings.mock_desc", "Ứng dụng hiện đang lưu trữ toàn bộ dữ liệu trên bộ nhớ RAM thông qua JavaFX ObservableList. Bạn có thể khôi phục toàn bộ dữ liệu mẫu ban đầu (4 giải đấu, 8 đội tuyển, 18 tuyển thủ, nhánh đấu và bảng xếp hạng) bất cứ lúc nào.");
        en.put("settings.mock_desc", "The application currently stores all data in RAM memory via JavaFX ObservableList. You can restore the initial mock dataset (4 tournaments, 8 teams, 18 players, brackets and standings) at any time.");

        vi.put("settings.btn_reset_mock", "Khôi phục dữ liệu mẫu mặc định");
        en.put("settings.btn_reset_mock", "Reset Default Mock Data");

        // ==================== PAGE TITLES & SUBTITLES ====================
        vi.put("page.dashboard.title", "Tổng quan");
        en.put("page.dashboard.title", "Dashboard");
        vi.put("page.dashboard.sub", "Tổng quan giải đấu, đội tuyển, trận đấu và hoạt động gần đây");
        en.put("page.dashboard.sub", "Overview of tournaments, teams, matches and recent activities");

        vi.put("page.tournaments.title", "Quản lý giải đấu");
        en.put("page.tournaments.title", "Tournament Management");
        vi.put("page.tournaments.sub", "Tạo mới, chỉnh sửa, lọc và vận hành các giải đấu Esports");
        en.put("page.tournaments.sub", "Create, edit, filter and operate Esports tournaments");

        vi.put("page.registrations.title", "Hồ sơ đăng ký");
        en.put("page.registrations.title", "Registrations");
        vi.put("page.registrations.sub", "Xét duyệt hồ sơ đăng ký tham gia giải đấu của các đội tuyển");
        en.put("page.registrations.sub", "Review and approve tournament registration applications");

        vi.put("page.matches.title", "Quản lý trận đấu");
        en.put("page.matches.title", "Match Management");
        vi.put("page.matches.sub", "Quản lý lịch thi đấu, cập nhật tỉ số Bo1/Bo3/Bo5 trực tiếp");
        en.put("page.matches.sub", "Manage match schedules, live Bo1/Bo3/Bo5 score tracking");

        vi.put("page.bracket.title", "Sơ đồ nhánh đấu");
        en.put("page.bracket.title", "Tournament Bracket");
        vi.put("page.bracket.sub", "Sơ đồ nhánh đấu loại trực tiếp (Single Elimination)");
        en.put("page.bracket.sub", "Single Elimination knockout tournament bracket tree");

        vi.put("page.standings.title", "Bảng xếp hạng");
        en.put("page.standings.title", "Standings");
        vi.put("page.standings.sub", "Bảng xếp hạng điểm số, hiệu số thắng/thua theo từng giải");
        en.put("page.standings.sub", "Leaderboard standings and win/loss records by tournament");

        vi.put("page.teams.title", "Quản lý đội tuyển");
        en.put("page.teams.title", "Team Management");
        vi.put("page.teams.sub", "Quản lý thông tin đội tuyển, khu vực và huấn luyện viên");
        en.put("page.teams.sub", "Manage esports teams, regions, and coaches");

        vi.put("page.players.title", "Quản lý tuyển thủ");
        en.put("page.players.title", "Player Management");
        vi.put("page.players.sub", "Hồ sơ tuyển thủ, IGN, vị trí thi đấu và chỉ số KDA");
        en.put("page.players.sub", "Player rosters, IGNs, roles and KDA statistics");

        vi.put("page.prizes.title", "Cơ cấu giải thưởng");
        en.put("page.prizes.title", "Prize Management");
        vi.put("page.prizes.sub", "Cơ cấu giải thưởng và trạng thái giải ngân cho các đội đạt giải");
        en.put("page.prizes.sub", "Prize distribution and disbursement statuses");

        vi.put("page.settings.title", "Cài đặt hệ thống");
        en.put("page.settings.title", "Settings");
        vi.put("page.settings.sub", "Cấu hình ứng dụng và quản lý dữ liệu mẫu trong bộ nhớ");
        en.put("page.settings.sub", "Application settings and in-memory mock data management");

        vi.put("search.results", "Kết quả tìm kiếm cho: \"%s\"");
        en.put("search.results", "Search results for: \"%s\"");

        // ==================== TOURNAMENT SERVICES ====================
        vi.put("tour.placeone.title", "Hạng 1");
        en.put("tour.placeone.title", "1st Place");

        vi.put("tour.placetwo.title", "Hạng 2");
        en.put("tour.placetwo.title", "2nd Place");

        vi.put("tour.placeother.title", "Hạng 3");
        en.put("tour.placeother.title", "3rd Place");

        // ==================== TOURNAMENT CONTROLLER ====================
        vi.put("tournament.tooltip.details", "Xem thông tin chi tiết giải đấu");
        en.put("tournament.tooltip.details", "View tournament details");

        vi.put("tournament.tooltip.bracket", "Xem sơ đồ nhánh đấu");
        en.put("tournament.tooltip.bracket", "View tournament bracket");

        vi.put("tournament.tooltip.edit", "Chỉnh sửa giải đấu");
        en.put("tournament.tooltip.edit", "Edit tournament");

        vi.put("tournament.tooltip.delete", "Xóa giải đấu");
        en.put("tournament.tooltip.delete", "Delete tournament");

        vi.put("tournament.delete.title", "Xóa giải đấu");
        en.put("tournament.delete.title", "Delete Tournament");

        vi.put("tournament.delete.message", "Bạn có chắc chắn muốn xóa giải đấu \"%s\" (#%s)?");
        en.put("tournament.delete.message", "Are you sure you want to delete \"%s\" (#%s)?");

        vi.put("tournament.toast.deleted", "Đã xóa giải đấu: %s");
        en.put("tournament.toast.deleted", "Tournament deleted: %s");

        vi.put("tournament.toast.delete_error", "Không thể xóa giải đấu này.");
        en.put("tournament.toast.delete_error", "Cannot delete this tournament.");

        vi.put("tournament.dialog.edit_title", "Chỉnh sửa giải đấu");
        en.put("tournament.dialog.edit_title", "Edit Tournament");

        vi.put("tournament.dialog.create_title", "Tạo giải đấu mới");
        en.put("tournament.dialog.create_title", "Create New Tournament");

        vi.put("tournament.dialog.edit_header", "Cập nhật giải đấu: ");
        en.put("tournament.dialog.edit_header", "Update tournament: ");

        vi.put("tournament.dialog.create_header", "Khởi tạo giải đấu Esports mới");
        en.put("tournament.dialog.create_header", "Create a new Esports tournament");

        vi.put("tournament.dialog.save_edit", "Lưu thay đổi");
        en.put("tournament.dialog.save_edit", "Save Changes");

        vi.put("tournament.dialog.save_create", "Tạo giải đấu");
        en.put("tournament.dialog.save_create", "Create Tournament");

        vi.put("tournament.dialog.name_prompt", "VD: Vietnam Championship Series 2026");
        en.put("tournament.dialog.name_prompt", "e.g.: Vietnam Championship Series 2026");

        vi.put("tournament.dialog.organizer_prompt", "VD: Riot Games / VNG Games");
        en.put("tournament.dialog.organizer_prompt", "e.g.: Riot Games / VNG Games");

        vi.put("tournament.dialog.location_default", "TP. Hồ Chí Minh");
        en.put("tournament.dialog.location_default", "Ho Chi Minh City");

        vi.put("tournament.dialog.location_prompt", "Địa điểm tổ chức");
        en.put("tournament.dialog.location_prompt", "Venue location");

        vi.put("tournament.dialog.desc_prompt", "Mô tả giải đấu, quy định và thể thức chi tiết...");
        en.put("tournament.dialog.desc_prompt", "Tournament description, rules and format details...");

        vi.put("tournament.dialog.lbl_name", "Tên giải đấu *");
        en.put("tournament.dialog.lbl_name", "Tournament Name *");

        vi.put("tournament.dialog.lbl_game", "Tựa game *");
        en.put("tournament.dialog.lbl_game", "Game Title *");

        vi.put("tournament.dialog.lbl_organizer", "Đơn vị tổ chức *");
        en.put("tournament.dialog.lbl_organizer", "Organizer *");

        vi.put("tournament.dialog.lbl_format", "Thể thức thi đấu");
        en.put("tournament.dialog.lbl_format", "Bracket Format");

        vi.put("tournament.dialog.lbl_status", "Trạng thái");
        en.put("tournament.dialog.lbl_status", "Status");

        vi.put("tournament.dialog.lbl_teams", "Số đội tham gia");
        en.put("tournament.dialog.lbl_teams", "Number of Teams");

        vi.put("tournament.dialog.lbl_prize", "Quỹ thưởng ($)");
        en.put("tournament.dialog.lbl_prize", "Prize Pool ($)");

        vi.put("tournament.dialog.lbl_start", "Ngày bắt đầu");
        en.put("tournament.dialog.lbl_start", "Start Date");

        vi.put("tournament.dialog.lbl_end", "Ngày kết thúc");
        en.put("tournament.dialog.lbl_end", "End Date");

        vi.put("tournament.dialog.lbl_location", "Địa điểm");
        en.put("tournament.dialog.lbl_location", "Location");

        vi.put("tournament.dialog.lbl_desc", "Mô tả");
        en.put("tournament.dialog.lbl_desc", "Description");

        vi.put("tournament.dialog.validation_error", "Vui lòng điền đầy đủ Tên giải đấu và Đơn vị tổ chức!");
        en.put("tournament.dialog.validation_error", "Please fill in Tournament Name and Organizer!");

        vi.put("tournament.dialog.prize_error", "Quỹ giải thưởng không hợp lệ!");
        en.put("tournament.dialog.prize_error", "Invalid prize pool amount!");

        vi.put("tournament.toast.updated", "Đã cập nhật thông tin giải đấu: %s");
        en.put("tournament.toast.updated", "Tournament updated: %s");

        vi.put("tournament.toast.created", "Đã tạo giải đấu mới: %s");
        en.put("tournament.toast.created", "New tournament created: %s");

        // ==================== PLAYER CONTROLLER ====================
        vi.put("player.tooltip.edit", "Chỉnh sửa tuyển thủ");
        en.put("player.tooltip.edit", "Edit player");

        vi.put("player.tooltip.delete", "Xóa tuyển thủ");
        en.put("player.tooltip.delete", "Delete player");

        vi.put("player.captain", "CAPTAIN");
        en.put("player.captain", "CAPTAIN");

        vi.put("player.free_agent", "Tự do (Free Agent)");
        en.put("player.free_agent", "Free Agent");

        vi.put("player.delete.title", "Xóa tuyển thủ");
        en.put("player.delete.title", "Delete Player");

        vi.put("player.delete.message", "Bạn có chắc chắn muốn xóa tuyển thủ \"%s\" (%s)?");
        en.put("player.delete.message", "Are you sure you want to delete player \"%s\" (%s)?");

        vi.put("player.toast.deleted", "Đã xóa tuyển thủ: %s");
        en.put("player.toast.deleted", "Player deleted: %s");

        vi.put("player.toast.delete_error", "Không thể xóa tuyển thủ này.");
        en.put("player.toast.delete_error", "Cannot delete this player.");

        vi.put("player.dialog.edit_title", "Chỉnh sửa tuyển thủ");
        en.put("player.dialog.edit_title", "Edit Player");

        vi.put("player.dialog.create_title", "Thêm tuyển thủ mới");
        en.put("player.dialog.create_title", "Add New Player");

        vi.put("player.dialog.edit_header", "Cập nhật hồ sơ: ");
        en.put("player.dialog.edit_header", "Update profile: ");

        vi.put("player.dialog.create_header", "Đăng ký tuyển thủ mới vào hệ thống");
        en.put("player.dialog.create_header", "Register a new player to the system");

        vi.put("player.dialog.save_edit", "Lưu thay đổi");
        en.put("player.dialog.save_edit", "Save Changes");

        vi.put("player.dialog.save_create", "Thêm tuyển thủ");
        en.put("player.dialog.save_create", "Add Player");

        vi.put("player.dialog.ign_prompt", "VD: Faker, Levi, TenZ...");
        en.put("player.dialog.ign_prompt", "e.g.: Faker, Levi, TenZ...");

        vi.put("player.dialog.fullname_prompt", "Họ và tên đầy đủ");
        en.put("player.dialog.fullname_prompt", "Full name");

        vi.put("player.captain_checkbox", "Đội trưởng (Team Captain)");
        en.put("player.captain_checkbox", "Team Captain");

        vi.put("player.dialog.lbl_team", "Đội tuyển *");
        en.put("player.dialog.lbl_team", "Team *");

        vi.put("player.dialog.lbl_ign", "In-Game Name *");
        en.put("player.dialog.lbl_ign", "In-Game Name *");

        vi.put("player.dialog.lbl_fullname", "Họ và tên *");
        en.put("player.dialog.lbl_fullname", "Full Name *");

        vi.put("player.dialog.lbl_role", "Vị trí thi đấu");
        en.put("player.dialog.lbl_role", "Role");

        vi.put("player.dialog.lbl_nationality", "Quốc tịch");
        en.put("player.dialog.lbl_nationality", "Nationality");

        vi.put("player.dialog.lbl_age", "Tuổi");
        en.put("player.dialog.lbl_age", "Age");

        vi.put("player.dialog.lbl_kda", "Chỉ số KDA");
        en.put("player.dialog.lbl_kda", "KDA Ratio");

        vi.put("player.dialog.validation_error", "Vui lòng nhập đầy đủ Đội tuyển, IGN và Họ tên!");
        en.put("player.dialog.validation_error", "Please fill in Team, IGN and Full Name!");

        vi.put("player.dialog.kda_error", "Chỉ số KDA phải là số không âm (VD: 4.25)!");
        en.put("player.dialog.kda_error", "KDA must be a non-negative number (e.g.: 4.25)!");

        vi.put("player.toast.updated", "Đã cập nhật tuyển thủ: %s");
        en.put("player.toast.updated", "Player updated: %s");

        vi.put("player.toast.created", "Đã thêm tuyển thủ: %s");
        en.put("player.toast.created", "Player added: %s");

        // ==================== TEAM CONTROLLER ====================
        vi.put("team.tooltip.roster", "Xem danh sách tuyển thủ của đội");
        en.put("team.tooltip.roster", "View team roster");

        vi.put("team.tooltip.edit", "Chỉnh sửa đội tuyển");
        en.put("team.tooltip.edit", "Edit team");

        vi.put("team.tooltip.delete", "Xóa đội tuyển");
        en.put("team.tooltip.delete", "Delete team");

        vi.put("team.roster_count", "%d tuyển thủ");
        en.put("team.roster_count", "%d players");

        vi.put("team.roster_header", "Đội hình thi đấu (Roster) — %s");
        en.put("team.roster_header", "Team Roster — %s");

        vi.put("team.delete.title", "Xóa đội tuyển");
        en.put("team.delete.title", "Delete Team");

        vi.put("team.delete.message", "Bạn có chắc chắn muốn xóa đội tuyển \"%s\" ([%s])?");
        en.put("team.delete.message", "Are you sure you want to delete team \"%s\" ([%s])?");

        vi.put("team.toast.deleted", "Đã xóa đội tuyển: %s");
        en.put("team.toast.deleted", "Team deleted: %s");

        vi.put("team.toast.delete_error", "Không thể xóa đội tuyển này.");
        en.put("team.toast.delete_error", "Cannot delete this team.");

        vi.put("team.dialog.edit_title", "Chỉnh sửa đội tuyển");
        en.put("team.dialog.edit_title", "Edit Team");

        vi.put("team.dialog.create_title", "Thêm đội tuyển mới");
        en.put("team.dialog.create_title", "Add New Team");

        vi.put("team.dialog.edit_header", "Cập nhật hồ sơ: ");
        en.put("team.dialog.edit_header", "Update profile: ");

        vi.put("team.dialog.create_header", "Đăng ký hồ sơ đội tuyển Esports mới");
        en.put("team.dialog.create_header", "Register a new Esports team profile");

        vi.put("team.dialog.save_edit", "Lưu thay đổi");
        en.put("team.dialog.save_edit", "Save Changes");

        vi.put("team.dialog.save_create", "Thêm đội tuyển");
        en.put("team.dialog.save_create", "Add Team");

        vi.put("team.dialog.name_prompt", "VD: T1 Esports");
        en.put("team.dialog.name_prompt", "e.g.: T1 Esports");

        vi.put("team.dialog.tag_prompt", "VD: T1 (tối đa 6 ký tự)");
        en.put("team.dialog.tag_prompt", "e.g.: T1 (max 6 chars)");

        vi.put("team.dialog.coach_prompt", "Tên HLV trưởng");
        en.put("team.dialog.coach_prompt", "Head coach name");

        vi.put("team.dialog.lbl_name", "Tên đội tuyển *");
        en.put("team.dialog.lbl_name", "Team Name *");

        vi.put("team.dialog.lbl_tag", "Tag viết tắt *");
        en.put("team.dialog.lbl_tag", "Short Tag *");

        vi.put("team.dialog.lbl_region", "Khu vực");
        en.put("team.dialog.lbl_region", "Region");

        vi.put("team.dialog.lbl_coach", "Huấn luyện viên");
        en.put("team.dialog.lbl_coach", "Head Coach");

        vi.put("team.dialog.lbl_email", "Email liên hệ");
        en.put("team.dialog.lbl_email", "Contact Email");

        vi.put("team.dialog.lbl_founded", "Năm thành lập");
        en.put("team.dialog.lbl_founded", "Founded Year");

        vi.put("team.dialog.lbl_color", "Màu nhận diện");
        en.put("team.dialog.lbl_color", "Brand Color");

        vi.put("team.dialog.validation_error", "Vui lòng nhập đầy đủ Tên đội tuyển và Tag!");
        en.put("team.dialog.validation_error", "Please fill in Team Name and Tag!");

        vi.put("team.dialog.tag_error", "Tag đội tuyển không được vượt quá 6 ký tự!");
        en.put("team.dialog.tag_error", "Team tag must not exceed 6 characters!");

        vi.put("team.toast.updated", "Đã cập nhật đội tuyển: %s");
        en.put("team.toast.updated", "Team updated: %s");

        vi.put("team.toast.created", "Đã thêm đội tuyển: %s");
        en.put("team.toast.created", "Team added: %s");

        // ==================== PLAYER CONTROLLER ====================
        vi.put("login.password.title", "Đăng nhập");
        en.put("login.password.title", "Login");

        TRANSLATIONS.put("vi", vi);
        TRANSLATIONS.put("en", en);
    }

    private I18n() {}

    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(Language lang) {
        if (lang == null || lang == currentLanguage) return;
        currentLanguage = lang;
        AppContext context = AppContext.getInstance();
        context.getSettings().setLanguage(lang.getCode());
        try {
            context.saveSettings();
        } catch (RuntimeException e) {
            System.err.println("[SQL settings] Không thể lưu ngôn ngữ: " + e.getMessage());
        }
        notifyListeners();
    }

    public static void addListener(Consumer<Language> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(Consumer<Language> listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Consumer<Language> listener : listeners) {
            try {
                listener.accept(currentLanguage);
            } catch (Exception e) {
                // Silently ignore listener errors to avoid crashing UI
            }
        }
    }

    public static String get(String key) {
        Map<String, String> langMap = TRANSLATIONS.get(currentLanguage.getCode());
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        Map<String, String> fallback = TRANSLATIONS.get("vi");
        if (fallback != null && fallback.containsKey(key)) {
            return fallback.get(key);
        }
        return key;
    }

    public static String get(String key, Object... args) {
        String msg = get(key);
        try {
            return String.format(msg, args);
        } catch (Exception e) {
            return msg;
        }
    }

    public static ResourceBundle getResourceBundle() {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return get(key);
            }

            @Override
            public Enumeration<String> getKeys() {
                Set<String> keys = new HashSet<>();
                Map<String, String> current = TRANSLATIONS.get(currentLanguage.getCode());
                if (current != null) keys.addAll(current.keySet());
                Map<String, String> vi = TRANSLATIONS.get("vi");
                if (vi != null) keys.addAll(vi.keySet());
                return Collections.enumeration(keys);
            }
        };
    }
}
