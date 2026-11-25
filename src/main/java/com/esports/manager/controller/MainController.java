package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.util.I18n;
import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.esports.manager.EsportsManagerApp;

import java.io.InputStream;
import java.util.List;

public class MainController {

    @FXML private StackPane rootStackPane;
    @FXML private StackPane contentViewport;
    @FXML private VBox toastContainer;
    @FXML private Label lblPageTitle;
    @FXML private TextField txtGlobalSearch;

    // Sidebar Section Labels
    @FXML private Label lblSectionOverview;
    @FXML private Label lblSectionTournaments;
    @FXML private Label lblSectionTeams;
    @FXML private Label lblSectionSystem;

    // Sidebar Navigation Buttons
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

    // Topbar & Admin Footer
    @FXML private Button btnQuickCreate;
    @FXML private Label lblAdminRole;

    // Language Toggle
    @FXML private Label lblLanguageLabel;
    @FXML private StackPane langTogglePane;
    @FXML private ImageView ivLangFlag;

    private List<Button> navButtons;

    private enum PageType {
        DASHBOARD, TOURNAMENTS, REGISTRATIONS, MATCHES,
        BRACKET, STANDINGS, TEAMS, PLAYERS, PRIZES, SETTINGS
    }
    private PageType currentPage = PageType.DASHBOARD;
    private boolean isInitialLoad = true;

    @FXML
    public void initialize() {
        ToastManager.init(toastContainer);
        ViewNavigator.init(contentViewport, lblPageTitle);

        navButtons = List.of(
                btnDashboard, btnTournaments, btnRegistrations, btnMatches,
                btnBracket, btnStandings, btnTeams, btnPlayers, btnPrizes, btnSettings
        );

        setupLanguageSelector();
        updateLocalizedTexts();

        handleNavDashboard();
        isInitialLoad = false;
    }

    private void setupLanguageSelector() {
        String savedCode = AppContext.getInstance().getSettings().getLanguage();
        I18n.Language initialLang = I18n.Language.fromCode(savedCode);
        I18n.setLanguage(initialLang);
        updateLangButton();

        I18n.addListener(lang -> {
            updateLangButton();
            updateLocalizedTexts();
            reloadCurrentPage();
            if (!isInitialLoad) {
                ToastManager.showSuccess(I18n.get("msg.lang_switched"));
            }
        });
    }

    private void updateLangButton() {
        I18n.Language current = I18n.getCurrentLanguage();
        String flagPath;
        if (current == I18n.Language.ENGLISH) {
            flagPath = "/com/esports/manager/images/vietnam.png";
        } else {
            flagPath = "/com/esports/manager/images/united-kingdom.png";
        }
        InputStream is = getClass().getResourceAsStream(flagPath);
        if (is != null) {
            ivLangFlag.setImage(new Image(is));
        }
        String tooltipKey = (current == I18n.Language.ENGLISH) ? "lang.tooltip.vi" : "lang.tooltip.en";
        Tooltip.install(langTogglePane, new Tooltip(I18n.get(tooltipKey)));
    }

    @FXML
    public void handleToggleLanguage(MouseEvent event) {
        I18n.Language current = I18n.getCurrentLanguage();
        I18n.Language next = (current == I18n.Language.ENGLISH) ? I18n.Language.VIETNAMESE : I18n.Language.ENGLISH;
        I18n.setLanguage(next);
    }

    @FXML
    public void handleToggleLanguage() {
        I18n.Language current = I18n.getCurrentLanguage();
        I18n.Language next = (current == I18n.Language.ENGLISH) ? I18n.Language.VIETNAMESE : I18n.Language.ENGLISH;
        I18n.setLanguage(next);
    }

    private void updateLocalizedTexts() {
        if (lblSectionOverview != null) lblSectionOverview.setText(I18n.get("sidebar.overview"));
        if (lblSectionTournaments != null) lblSectionTournaments.setText(I18n.get("sidebar.tournaments"));
        if (lblSectionTeams != null) lblSectionTeams.setText(I18n.get("sidebar.teams"));
        if (lblSectionSystem != null) lblSectionSystem.setText(I18n.get("sidebar.system"));

        if (btnDashboard != null) btnDashboard.setText(I18n.get("nav.dashboard"));
        if (btnTournaments != null) btnTournaments.setText(I18n.get("nav.tournaments"));
        if (btnRegistrations != null) btnRegistrations.setText(I18n.get("nav.registrations"));
        if (btnMatches != null) btnMatches.setText(I18n.get("nav.matches"));
        if (btnBracket != null) btnBracket.setText(I18n.get("nav.bracket"));
        if (btnStandings != null) btnStandings.setText(I18n.get("nav.standings"));
        if (btnTeams != null) btnTeams.setText(I18n.get("nav.teams"));
        if (btnPlayers != null) btnPlayers.setText(I18n.get("nav.players"));
        if (btnPrizes != null) btnPrizes.setText(I18n.get("nav.prizes"));
        if (btnSettings != null) btnSettings.setText(I18n.get("nav.settings"));

        if (txtGlobalSearch != null) txtGlobalSearch.setPromptText(I18n.get("topbar.search_prompt"));
        if (btnQuickCreate != null) btnQuickCreate.setText(I18n.get("topbar.create_tournament"));

        if (lblAdminRole != null) {
            lblAdminRole.setText(I18n.get("admin.greeting", AppContext.getInstance().getCurrentUserFullName()));
        }

        if (lblLanguageLabel != null) lblLanguageLabel.setText(I18n.get("lang.label"));
    }

    private void updateCurrentPageHeader() {
        switch (currentPage) {
            case DASHBOARD -> ViewNavigator.updateHeader(I18n.get("page.dashboard.title"));
            case TOURNAMENTS -> ViewNavigator.updateHeader(I18n.get("page.tournaments.title"));
            case REGISTRATIONS -> ViewNavigator.updateHeader(I18n.get("page.registrations.title"));
            case MATCHES -> ViewNavigator.updateHeader(I18n.get("page.matches.title"));
            case BRACKET -> ViewNavigator.updateHeader(I18n.get("page.bracket.title"));
            case STANDINGS -> ViewNavigator.updateHeader(I18n.get("page.standings.title"));
            case TEAMS -> ViewNavigator.updateHeader(I18n.get("page.teams.title"));
            case PLAYERS -> ViewNavigator.updateHeader(I18n.get("page.players.title"));
            case PRIZES -> ViewNavigator.updateHeader(I18n.get("page.prizes.title"));
            case SETTINGS -> ViewNavigator.updateHeader(I18n.get("page.settings.title"));
        }
    }

    private void reloadCurrentPage() {
        switch (currentPage) {
            case DASHBOARD -> handleNavDashboard();
            case TOURNAMENTS -> handleNavTournaments();
            case REGISTRATIONS -> handleNavRegistrations();
            case MATCHES -> handleNavMatches();
            case BRACKET -> handleNavBracket();
            case STANDINGS -> handleNavStandings();
            case TEAMS -> handleNavTeams();
            case PLAYERS -> handleNavPlayers();
            case PRIZES -> handleNavPrizes();
            case SETTINGS -> handleNavSettings();
        }
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
        currentPage = PageType.DASHBOARD;
        setActiveNav(btnDashboard);
        ViewNavigator.navigate("DashboardView.fxml", I18n.get("page.dashboard.title"));
    }

    @FXML
    public void handleNavTournaments() {
        currentPage = PageType.TOURNAMENTS;
        setActiveNav(btnTournaments);
        ViewNavigator.navigate("TournamentView.fxml", I18n.get("page.tournaments.title"));
    }

    @FXML
    public void handleNavRegistrations() {
        currentPage = PageType.REGISTRATIONS;
        setActiveNav(btnRegistrations);
        ViewNavigator.navigate("RegistrationView.fxml", I18n.get("page.registrations.title"));
    }

    @FXML
    public void handleNavMatches() {
        currentPage = PageType.MATCHES;
        setActiveNav(btnMatches);
        ViewNavigator.navigate("MatchView.fxml", I18n.get("page.matches.title"));
    }

    @FXML
    public void handleNavBracket() {
        currentPage = PageType.BRACKET;
        setActiveNav(btnBracket);
        ViewNavigator.navigate("BracketView.fxml", I18n.get("page.bracket.title"));
    }

    @FXML
    public void handleNavStandings() {
        currentPage = PageType.STANDINGS;
        setActiveNav(btnStandings);
        ViewNavigator.navigate("StandingsView.fxml", I18n.get("page.standings.title"));
    }

    @FXML
    public void handleNavTeams() {
        currentPage = PageType.TEAMS;
        setActiveNav(btnTeams);
        ViewNavigator.navigate("TeamView.fxml", I18n.get("page.teams.title"));
    }

    @FXML
    public void handleNavPlayers() {
        currentPage = PageType.PLAYERS;
        setActiveNav(btnPlayers);
        ViewNavigator.navigate("PlayerView.fxml", I18n.get("page.players.title"));
    }

    @FXML
    public void handleNavPrizes() {
        currentPage = PageType.PRIZES;
        setActiveNav(btnPrizes);
        ViewNavigator.navigate("PrizeView.fxml", I18n.get("page.prizes.title"));
    }

    @FXML
    public void handleNavSettings() {
        currentPage = PageType.SETTINGS;
        setActiveNav(btnSettings);
        ViewNavigator.navigate("SettingsView.fxml", I18n.get("page.settings.title"));
    }

    @FXML
    public void handleQuickCreateTournament() {
        currentPage = PageType.TOURNAMENTS;
        setActiveNav(btnTournaments);
        ViewNavigator.<TournamentController>navigate("TournamentView.fxml",
                I18n.get("page.tournaments.title"),
                TournamentController::openCreateModal);
    }

    @FXML
    public void handleGlobalSearch() {
        String keyword = txtGlobalSearch.getText().trim();
        if (keyword.isEmpty()) return;
        currentPage = PageType.TOURNAMENTS;
        setActiveNav(btnTournaments);
        ViewNavigator.<TournamentController>navigate("TournamentView.fxml",
                I18n.get("page.tournaments.title"),
                ctrl -> ctrl.applyExternalSearch(keyword));
    }

    @FXML
    public void handleLogout() {
        EsportsManagerApp.showLogin((Stage) rootStackPane.getScene().getWindow());
    }
}
