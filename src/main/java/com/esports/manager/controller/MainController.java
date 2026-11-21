package com.esports.manager.controller;

import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MainController {

    @FXML private StackPane rootStackPane;
    @FXML private StackPane contentViewport;
    @FXML private VBox toastContainer;
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;
    @FXML private TextField txtGlobalSearch;

    @FXML private Button btnDashboard;
    @FXML private Button btnTournaments;
    @FXML private Button btnRegistrations;
    @FXML private Button btnMatches;
    @FXML private Button btnBracket;
    @FXML private Button btnStandings;
    @FXML private Button btnTeams;
    @FXML private Button btnPlayers;
    @FXML private Button btnPrizes;
    @FXML private Button btnSettings;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        ToastManager.init(toastContainer);
        ViewNavigator.init(contentViewport, lblPageTitle, lblPageSubtitle);

        navButtons = List.of(
                btnDashboard, btnTournaments, btnRegistrations, btnMatches,
                btnBracket, btnStandings, btnTeams, btnPlayers, btnPrizes, btnSettings
        );

        handleNavDashboard();
    }

    private void setActiveNav(Button activeBtn) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
        }
        if (!activeBtn.getStyleClass().contains("nav-button-active")) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    public void handleNavDashboard() {
        setActiveNav(btnDashboard);
        ViewNavigator.navigate("DashboardView.fxml", "Dashboard",
                "Tổng quan giải đấu, đội tuyển, trận đấu và hoạt động gần đây");
    }

    @FXML
    public void handleNavTournaments() {
        setActiveNav(btnTournaments);
        ViewNavigator.navigate("TournamentView.fxml", "Tournament Management",
                "Tạo mới, chỉnh sửa, lọc và vận hành các giải đấu Esports");
    }

    @FXML
    public void handleNavRegistrations() {
        setActiveNav(btnRegistrations);
        ViewNavigator.navigate("RegistrationView.fxml", "Registrations",
                "Xét duyệt hồ sơ đăng ký tham gia giải đấu của các đội tuyển");
    }

    @FXML
    public void handleNavMatches() {
        setActiveNav(btnMatches);
        ViewNavigator.navigate("MatchView.fxml", "Match Management",
                "Quản lý lịch thi đấu, cập nhật tỉ số Bo1/Bo3/Bo5 trực tiếp");
    }

    @FXML
    public void handleNavBracket() {
        setActiveNav(btnBracket);
        ViewNavigator.navigate("BracketView.fxml", "Tournament Bracket",
                "Sơ đồ nhánh đấu loại trực tiếp (Single Elimination)");
    }

    @FXML
    public void handleNavStandings() {
        setActiveNav(btnStandings);
        ViewNavigator.navigate("StandingsView.fxml", "Standings",
                "Bảng xếp hạng điểm số, hiệu số thắng/thua theo từng giải");
    }

    @FXML
    public void handleNavTeams() {
        setActiveNav(btnTeams);
        ViewNavigator.navigate("TeamView.fxml", "Team Management",
                "Quản lý thông tin đội tuyển, khu vực và huấn luyện viên");
    }

    @FXML
    public void handleNavPlayers() {
        setActiveNav(btnPlayers);
        ViewNavigator.navigate("PlayerView.fxml", "Player Management",
                "Hồ sơ tuyển thủ, IGN, vị trí thi đấu và chỉ số KDA");
    }

    @FXML
    public void handleNavPrizes() {
        setActiveNav(btnPrizes);
        ViewNavigator.navigate("PrizeView.fxml", "Prize Management",
                "Cơ cấu giải thưởng và trạng thái giải ngân cho các đội đạt giải");
    }

    @FXML
    public void handleNavSettings() {
        setActiveNav(btnSettings);
        ViewNavigator.navigate("SettingsView.fxml", "Settings",
                "Cấu hình ứng dụng và quản lý dữ liệu mẫu trong bộ nhớ");
    }

    @FXML
    public void handleQuickCreateTournament() {
        setActiveNav(btnTournaments);
        ViewNavigator.<TournamentController>navigate("TournamentView.fxml", "Tournament Management",
                "Tạo mới, chỉnh sửa, lọc và vận hành các giải đấu Esports",
                TournamentController::openCreateModal);
    }

    @FXML
    public void handleGlobalSearch() {
        String keyword = txtGlobalSearch.getText().trim();
        if (keyword.isEmpty()) return;
        setActiveNav(btnTournaments);
        ViewNavigator.<TournamentController>navigate("TournamentView.fxml", "Tournament Management",
                "Kết quả tìm kiếm cho: \"" + keyword + "\"",
                ctrl -> ctrl.applyExternalSearch(keyword));
    }
}