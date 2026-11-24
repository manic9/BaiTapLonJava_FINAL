package com.esports.manager.config;

import com.esports.manager.model.*;
import com.esports.manager.repository.InMemoryRepository;
import com.esports.manager.repository.MockDataSeeder;
import com.esports.manager.repository.Repository;
import com.esports.manager.repository.sqlserver.SqlServerTournamentRepository;
import com.esports.manager.service.*;
import com.esports.manager.service.AuthenticationService;

public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final AppSettings settings = new AppSettings();

    private final DatabaseConfig databaseConfig;
    private final DatabaseConnection databaseConnection;
    private final TransactionManager transactionManager;
    private final AuthenticationService authenticationService;
    private DatabaseConnection.ConnectionStatus initialDbStatus;

    private final Repository<Tournament> tournamentRepo;
    private final Repository<Team> teamRepo = new InMemoryRepository<>();
    private final Repository<Player> playerRepo = new InMemoryRepository<>();
    private final Repository<Registration> registrationRepo = new InMemoryRepository<>();
    private final Repository<Match> matchRepo = new InMemoryRepository<>();
    private final Repository<StandingEntry> standingRepo = new InMemoryRepository<>();
    private final Repository<PrizeAllocation> prizeRepo = new InMemoryRepository<>();
    private final Repository<ActivityLog> activityRepo = new InMemoryRepository<>();

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
        DatabaseConnection.ConnectionStatus status = this.databaseConnection.testConnection();
        Repository<Tournament> repository;
        if (status.connected()) {
            try {
                repository = new SqlServerTournamentRepository(databaseConnection, transactionManager);
            } catch (RuntimeException e) {
                status = new DatabaseConnection.ConnectionStatus(false, e.getMessage(), 0);
                repository = new InMemoryRepository<>();
            }
        } else {
            repository = new InMemoryRepository<>();
        }
        this.initialDbStatus = status;
        this.tournamentRepo = repository;
    }

    public static AppContext getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) return;

        activityService = new ActivityService(activityRepo);
        tournamentService = new TournamentService(tournamentRepo, prizeRepo, activityService);
        teamService = new TeamService(teamRepo, playerRepo, activityService);
        playerService = new PlayerService(playerRepo, activityService);
        registrationService = new RegistrationService(registrationRepo, tournamentRepo, teamService, activityService);
        bracketService = new BracketService(matchRepo, registrationService, settings, activityService);
        standingsService = new StandingsService(standingRepo, matchRepo, registrationRepo);
        matchService = new MatchService(matchRepo, bracketService, standingsService, teamService, activityService);
        prizeService = new PrizeService(prizeRepo, activityService);

        MockDataSeeder.seed(this);
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
