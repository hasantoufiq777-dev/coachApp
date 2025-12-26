package com.example.coachsapp.db;

import com.example.coachsapp.model.*;
import java.util.List;

/**
 * Database test and initialization utility
 * Creates sample data and tests all database operations
 */
public class DatabaseInitializer {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   COACHES APP - DATABASE INITIALIZATION        ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Get database service
        DatabaseService dbService = DatabaseService.getInstance();

        // Initialize new tables
        dbService.getUserRepository().createTable();
        dbService.getTransferRequestRepository().createTable();

        // Create sample data
        createSampleData(dbService);

        // Load and display data
        loadAndDisplayData(dbService);

        // Print statistics
        dbService.printStatistics();

        // Keep connection open or close
        System.out.println("✓ Database initialization complete!");
        System.out.println("✓ Database file: coaches_app.db\n");
    }

    /**
     * Create sample data in the database
     */
    private static void createSampleData(DatabaseService dbService) {
        System.out.println("=== Creating Sample Data ===\n");

        ClubRepository clubRepo = dbService.getClubRepository();
        ManagerRepository managerRepo = dbService.getManagerRepository();
        PlayerRepository playerRepo = dbService.getPlayerRepository();

        // Create clubs
        Club club1 = new Club("Manchester United");
        Club club2 = new Club("Liverpool");
        Club club3 = new Club("Manchester City");

        clubRepo.save(club1);
        clubRepo.save(club2);
        clubRepo.save(club3);

        // Create managers
        Manager manager1 = new Manager("Erik ten Hag", "Manchester United");
        Manager manager2 = new Manager("Arne Slot", "Liverpool");
        Manager manager3 = new Manager("Pep Guardiola", "Manchester City");

        managerRepo.save(manager1);
        managerRepo.save(manager2);
        managerRepo.save(manager3);

        // Reload clubs with IDs
        club1 = clubRepo.findByName("Manchester United");
        club2 = clubRepo.findByName("Liverpool");
        club3 = clubRepo.findByName("Manchester City");

        // Create players for Manchester United
        Player p1 = new Player("Cristiano Ronaldo", 39, 7, Position.FORWARD);
        p1.setClubId(club1.getId());
        playerRepo.save(p1);

        Player p2 = new Player("Bruno Fernandes", 29, 8, Position.MIDFIELDER);
        p2.setClubId(club1.getId());
        playerRepo.save(p2);

        Player p3 = new Player("Harry Maguire", 31, 6, Position.DEFENDER);
        p3.setClubId(club1.getId());
        playerRepo.save(p3);

        // Create players for Liverpool
        Player p4 = new Player("Mohamed Salah", 32, 11, Position.FORWARD);
        p4.setClubId(club2.getId());
        playerRepo.save(p4);

        Player p5 = new Player("Virgil van Dijk", 33, 4, Position.DEFENDER);
        p5.setClubId(club2.getId());
        playerRepo.save(p5);

        // Create players for Manchester City
        Player p6 = new Player("Erling Haaland", 24, 9, Position.FORWARD);
        p6.setClubId(club3.getId());
        playerRepo.save(p6);

        Player p7 = new Player("Rodri", 27, 16, Position.MIDFIELDER);
        p7.setClubId(club3.getId());
        playerRepo.save(p7);

        // Create sample users
        UserRepository userRepo = dbService.getUserRepository();
        
        // Admin user
        User admin = new User(null, "admin", "admin123", Role.SYSTEM_ADMIN, null, null, null);
        userRepo.save(admin);
        
        // Club owners
        User owner1 = new User(null, "mufc_owner", "password", Role.CLUB_OWNER, club1.getId(), null, null);
        userRepo.save(owner1);
        
        User owner2 = new User(null, "lfc_owner", "password", Role.CLUB_OWNER, club2.getId(), null, null);
        userRepo.save(owner2);
        
        // Club managers (linked to manager entities)
        User mgr1 = new User(null, "ten_hag", "password", Role.CLUB_MANAGER, club1.getId(), null, manager1.getId());
        userRepo.save(mgr1);
        
        User mgr2 = new User(null, "arne_slot", "password", Role.CLUB_MANAGER, club2.getId(), null, manager2.getId());
        userRepo.save(mgr2);
        
        // Players (linked to player entities)
        User player1 = new User(null, "ronaldo", "password", Role.PLAYER, club1.getId(), p1.getId(), null);
        userRepo.save(player1);
        
        User player2 = new User(null, "salah", "password", Role.PLAYER, club2.getId(), p4.getId(), null);
        userRepo.save(player2);

        System.out.println("\n✓ Sample data created successfully\n");
    }

    /**
     * Load and display data from database
     */
    private static void loadAndDisplayData(DatabaseService dbService) {
        System.out.println("=== Loading Data from Database ===\n");

        // Load all managers
        List<Manager> managers = dbService.loadAllManagers();
        System.out.println("Managers (" + managers.size() + "):");
        for (Manager manager : managers) {
            System.out.println("  ✓ " + manager.getName() + " @ " + manager.getClub().getClubName() + " (ID: " + manager.getId() + ")");
        }

        // Load all players
        System.out.println("\nPlayers (" + dbService.loadAllPlayers().size() + "):");
        for (Manager manager : managers) {
            List<Player> players = dbService.loadPlayersByClub(manager.getClub().getId());
            System.out.println("  " + manager.getClub().getClubName() + ":");
            for (Player player : players) {
                System.out.println("    ✓ " + player.getName() + " - #" + player.getJersey() +
                                 " (" + player.getPosition() + ") [ID: " + player.getId() + "]");
            }
        }

        System.out.println();
    }
}

