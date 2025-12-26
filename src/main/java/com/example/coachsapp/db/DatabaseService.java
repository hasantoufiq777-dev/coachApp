package com.example.coachsapp.db;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import java.util.List;

/**
 * Database initialization and data loading service
 * Centralizes all database operations for the application
 */
public class DatabaseService {

    private static DatabaseService instance;
    private DatabaseConnection dbConnection;
    private PlayerRepository playerRepository;
    private ManagerRepository managerRepository;
    private ClubRepository clubRepository;
    private UserRepository userRepository;
    private TransferRequestRepository transferRequestRepository;
    private RegistrationRequestRepository registrationRequestRepository;

    /**
     * Private constructor for singleton pattern
     */
    private DatabaseService() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.playerRepository = new PlayerRepository();
        this.managerRepository = new ManagerRepository();
        this.clubRepository = new ClubRepository();
        this.userRepository = new UserRepository();
        this.transferRequestRepository = new TransferRequestRepository();
        this.registrationRequestRepository = new RegistrationRequestRepository();

        // Initialize database schema
        this.dbConnection.initializeDatabase();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Get player repository
     */
    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    /**
     * Get manager repository
     */
    public ManagerRepository getManagerRepository() {
        return managerRepository;
    }

    /**
     * Get club repository
     */
    public ClubRepository getClubRepository() {
        return clubRepository;
    }

    /**
     * Get user repository
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }

    /**
     * Get transfer request repository
     */
    public TransferRequestRepository getTransferRequestRepository() {
        return transferRequestRepository;
    }

    /**
     * Get registration request repository
     */
    public RegistrationRequestRepository getRegistrationRequestRepository() {
        return registrationRequestRepository;
    }

    /**
     * Load all managers and their players from database
     */
    public List<Manager> loadAllManagers() {
        return managerRepository.findAll();
    }

    /**
     * Load all players from database
     */
    public List<Player> loadAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Load players for a specific club
     */
    public List<Player> loadPlayersByClub(int clubId) {
        return playerRepository.findByClubId(clubId);
    }

    /**
     * Print database statistics
     */
    public void printStatistics() {
        System.out.println("\n=== Database Statistics ===");
        System.out.println("Total Clubs: " + clubRepository.count());
        System.out.println("Total Managers: " + managerRepository.count());
        System.out.println("Total Players: " + playerRepository.count());
        System.out.println("========================\n");
    }

    /**
     * Close database connection
     */
    public void close() {
        dbConnection.closeConnection();
    }
}

