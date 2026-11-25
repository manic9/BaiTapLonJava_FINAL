package com.esports.manager.config;

import com.esports.manager.model.*;
import com.esports.manager.repository.InMemoryRepository;
import com.esports.manager.repository.MockDataSeeder;
import com.esports.manager.repository.Repository;
import com.esports.manager.repository.sqlserver.SqlServerActivityLogRepository;
import com.esports.manager.repository.sqlserver.SqlServerMatchRepository;
import com.esports.manager.repository.sqlserver.SqlServerPlayerRepository;
import com.esports.manager.repository.sqlserver.SqlServerPrizeAllocationRepository;
import com.esports.manager.repository.sqlserver.SqlServerRegistrationRepository;
import com.esports.manager.repository.sqlserver.SqlServerTeamRepository;
import com.esports.manager.repository.sqlserver.SqlServerTournamentRepository;
import com.esports.manager.service.*;
import com.esports.manager.service.AuthenticationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final AppSettings settings = new AppSettings();

    private final DatabaseConfig databaseConfig;
    private final DatabaseConnection databaseConnection;
    private final TransactionManager transactionManager;
    private final AuthenticationService authenticationService;
    private final SqlServerAppSettingsStore appSettingsStore;
    private DatabaseConnection.ConnectionStatus initialDbStatus;
    private final List<String> repositoryWarnings = new ArrayList<>();

    private final Repository<Tournament> tournamentRepo;
    private final Repository<Team> teamRepo;
    private final Repository<Player> playerRepo;
    private final Repository<Registration> registrationRepo;
    private final Repository<Match> matchRepo;
    private final Repository<StandingEntry> standingRepo = new InMemoryRepository<>();
    private final Repository<PrizeAllocation> prizeRepo;
    private final Repository<ActivityLog> activityRepo;

    private ActivityService activityService;
    private TournamentService tournamentService;
    private TeamService teamService;
    private PlayerService playerService;
    private RegistrationService registrationService;
    private BracketService bracketService;
    private StandingsService standingsService;
    private MatchService matchService;
    private PrizeService prizeService;

    private boolean initialized = false;

    private AppContext() {
        this.databaseConfig = DatabaseConfig.load();
        this.databaseConnection = new DatabaseConnection(this.databaseConfig);
        this.transactionManager = new TransactionManager(this.databaseConnection);
        this.authenticationService = new AuthenticationService(this.databaseConnection);
        this.appSettingsStore = new SqlServerAppSettingsStore(this.databaseConnection, this.transactionManager);
        DatabaseConnection.ConnectionStatus status = this.databaseConnection.testConnection();

        Repository<Tournament> tourRepo = new InMemoryRepository<>();
        Repository<Team> tRepo = new InMemoryRepository<>();
        Repository<Player> pRepo = new InMemoryRepository<>();
        Repository<Registration> regRepo = new InMemoryRepository<>();
        Repository<Match> mRepo = new InMemoryRepository<>();
        Repository<PrizeAllocation> przRepo = new InMemoryRepository<>();
        Repository<ActivityLog> actRepo = new InMemoryRepository<>();

        if (status.connected()) {
            try {
                // Khởi tạo đồng bộ: không để các bảng khác nguồn (SQL + memory) vì sẽ
                // làm mất liên kết tournament/team/match trong các controller.
                Repository<Tournament> sqlTournaments = new SqlServerTournamentRepository(databaseConnection, transactionManager);
                Repository<Team> sqlTeams = new SqlServerTeamRepository(databaseConnection, transactionManager);
                Repository<Player> sqlPlayers = new SqlServerPlayerRepository(databaseConnection, transactionManager);
                Repository<Registration> sqlRegistrations = new SqlServerRegistrationRepository(databaseConnection, transactionManager);
                Repository<Match> sqlMatches = new SqlServerMatchRepository(databaseConnection, transactionManager);
                Repository<PrizeAllocation> sqlPrizes = new SqlServerPrizeAllocationRepository(databaseConnection, transactionManager);
                Repository<ActivityLog> sqlActivity = new SqlServerActivityLogRepository(databaseConnection, transactionManager);

                tourRepo = sqlTournaments;
                tRepo = sqlTeams;
                pRepo = sqlPlayers;
                regRepo = sqlRegistrations;
                mRepo = sqlMatches;
                przRepo = sqlPrizes;
                actRepo = sqlActivity;
            } catch (RuntimeException e) {
                recordRepositoryFallback("Toàn bộ dữ liệu", e);
                tourRepo = new InMemoryRepository<>();
                tRepo = new InMemoryRepository<>();
                pRepo = new InMemoryRepository<>();
                regRepo = new InMemoryRepository<>();
                mRepo = new InMemoryRepository<>();
                przRepo = new InMemoryRepository<>();
                actRepo = new InMemoryRepository<>();
            }
        }

        this.initialDbStatus = status;
        this.tournamentRepo = tourRepo;
        this.teamRepo = tRepo;
        this.playerRepo = pRepo;
        this.registrationRepo = regRepo;
        this.matchRepo = mRepo;
        this.prizeRepo = przRepo;
        this.activityRepo = actRepo;
    }

    public static AppContext getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) return;

        if (!initialDbStatus.connected()) {
            throw new IllegalStateException("Không thể khởi động chế độ lưu dữ liệu SQL: "
                    + initialDbStatus.message());
        }

        if (initialDbStatus.connected()) {
            try {
                appSettingsStore.loadInto(settings);
            } catch (RuntimeException e) {
                recordRepositoryFallback("Cài đặt", e);
            }
        }
        if (!repositoryWarnings.isEmpty()) {
            throw new IllegalStateException("Không thể khởi động vì dữ liệu chưa đọc được đồng bộ từ SQL Server:\n"
                    + String.join("\n", repositoryWarnings));
        }

        activityService = new ActivityService(activityRepo);
        tournamentService = new TournamentService(tournamentRepo, prizeRepo, activityService);
        teamService = new TeamService(teamRepo, playerRepo, activityService);
        playerService = new PlayerService(playerRepo, activityService);
        registrationService = new RegistrationService(registrationRepo, tournamentRepo, teamService, activityService);
        bracketService = new BracketService(matchRepo, registrationService, settings, activityService);
        standingsService = new StandingsService(standingRepo, matchRepo, registrationRepo);
        matchService = new MatchService(matchRepo, bracketService, standingsService, teamService, activityService);
        prizeService = new PrizeService(prizeRepo, activityService);

        // Seed demo data only for a completely empty store. Re-seeding a populated
        // SQL database on every launch causes unnecessary writes and startup lag.
        boolean storeIsEmpty = tournamentRepo.findAll().isEmpty()
                && teamRepo.findAll().isEmpty()
                && playerRepo.findAll().isEmpty()
                && registrationRepo.findAll().isEmpty()
                && matchRepo.findAll().isEmpty()
                && prizeRepo.findAll().isEmpty();
        if (storeIsEmpty) {
            MockDataSeeder.seed(this);
        }
        initialized = true;
    }

    public void resetMockData() {
        tournamentRepo.clear();
        teamRepo.clear();
        playerRepo.clear();
        registrationRepo.clear();
        matchRepo.clear();
        standingRepo.clear();
        prizeRepo.clear();
        activityRepo.clear();
        MockDataSeeder.seed(this);
        activityService.log("SYSTEM", "Đã khôi phục toàn bộ dữ liệu mẫu về trạng thái mặc định.");
    }

    public DatabaseConfig getDatabaseConfig() { return databaseConfig; }
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public TransactionManager getTransactionManager() { return transactionManager; }
    public AuthenticationService getAuthenticationService() { return authenticationService; }
    public DatabaseConnection.ConnectionStatus getInitialDbStatus() { return initialDbStatus; }
    public List<String> getRepositoryWarnings() { return Collections.unmodifiableList(repositoryWarnings); }
    public void saveSettings() {
        if (!initialDbStatus.connected()) {
            throw new IllegalStateException("SQL Server chưa kết nối; cài đặt chưa được lưu lâu dài.");
        }
        appSettingsStore.save(settings);
    }

    private void recordRepositoryFallback(String name, RuntimeException error) {
        String reason = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        String warning = name + ": " + reason;
        repositoryWarnings.add(warning);
        System.err.println("[SQL repository fallback] " + warning);
    }

    public AppSettings getSettings() { return settings; }
    public Repository<Tournament> getTournamentRepo() { return tournamentRepo; }
    public Repository<Team> getTeamRepo() { return teamRepo; }
    public Repository<Player> getPlayerRepo() { return playerRepo; }
    public Repository<Registration> getRegistrationRepo() { return registrationRepo; }
    public Repository<Match> getMatchRepo() { return matchRepo; }
    public Repository<StandingEntry> getStandingRepo() { return standingRepo; }
    public Repository<PrizeAllocation> getPrizeRepo() { return prizeRepo; }
    public Repository<ActivityLog> getActivityRepo() { return activityRepo; }

    public ActivityService getActivityService() { return activityService; }
    public TournamentService getTournamentService() { return tournamentService; }
    public TeamService getTeamService() { return teamService; }
    public PlayerService getPlayerService() { return playerService; }
    public RegistrationService getRegistrationService() { return registrationService; }
    public BracketService getBracketService() { return bracketService; }
    public StandingsService getStandingsService() { return standingsService; }
    public MatchService getMatchService() { return matchService; }
    public PrizeService getPrizeService() { return prizeService; }
}
