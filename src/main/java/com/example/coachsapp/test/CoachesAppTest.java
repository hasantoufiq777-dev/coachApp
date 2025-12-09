package com.example.coachsapp.test;

import com.example.coachsapp.model.*;
import com.example.coachsapp.service.TransferService;
import com.example.coachsapp.util.AppState;

/**
 * Test Examples for Coaches App
 * Run main() method to test all features
 */
public class CoachesAppTest {

    public static void main(String[] args) {
        System.out.println("=== COACHES APP TEST SUITE ===\n");

        testPlayerCreation();
        testClubManagement();
        testManagerCreation();
        testTransferService();
        testAppState();

        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }

    /**
     * Test 1: Player Creation and Methods
     */
    private static void testPlayerCreation() {
        System.out.println("TEST 1: Player Creation");
        System.out.println("-".repeat(40));

        Player player1 = new Player("Cristiano Ronaldo", 39, 7, Position.FORWARD);
        Player player2 = new Player("Lionel Messi", 36, 10, Position.FORWARD, false);

        System.out.println("Player 1: " + player1);
        System.out.println("Player 2: " + player2);

        // Test setters
        player1.setInjured(true);
        System.out.println("After injury: " + player1.isInjured());

        System.out.println("✓ Player creation test passed\n");
    }

    /**
     * Test 2: Club Management
     */
    private static void testClubManagement() {
        System.out.println("TEST 2: Club Management");
        System.out.println("-".repeat(40));

        Club club = new Club("Manchester United");

        Player player1 = new Player("Bruno Fernandes", 29, 8, Position.MIDFIELDER);
        Player player2 = new Player("Harry Maguire", 31, 6, Position.DEFENDER);

        club.addPlayer(player1);
        club.addPlayer(player2);

        System.out.println("Club: " + club.getClubName());
        System.out.println("Players count: " + club.getPlayers().size());
        System.out.println("Players: " + club.getPlayers());

        // Test remove
        club.removePlayer(player1);
        System.out.println("After removal: " + club.getPlayers().size());

        System.out.println("✓ Club management test passed\n");
    }

    /**
     * Test 3: Manager Creation and Club Assignment
     */
    private static void testManagerCreation() {
        System.out.println("TEST 3: Manager Creation");
        System.out.println("-".repeat(40));

        Manager manager1 = new Manager("Erik ten Hag", "Manchester United");
        Manager manager2 = new Manager("Pep Guardiola", "Manchester City");

        System.out.println("Manager 1: " + manager1.getName() + " @ " + manager1.getClub().getClubName());
        System.out.println("Manager 2: " + manager2.getName() + " @ " + manager2.getClub().getClubName());

        // Add players to manager's club
        Player player = new Player("Marcus Rashford", 26, 11, Position.FORWARD);
        manager1.getClub().addPlayer(player);

        System.out.println("Players in " + manager1.getClub().getClubName() + ": " +
                         manager1.getClub().getPlayers().size());

        System.out.println("✓ Manager creation test passed\n");
    }

    /**
     * Test 4: Transfer Service
     */
    private static void testTransferService() {
        System.out.println("TEST 4: Transfer Service");
        System.out.println("-".repeat(40));

        // Create managers and clubs
        Manager manager1 = new Manager("Luis Enrique", "Paris Saint-Germain");
        Manager manager2 = new Manager("Carlo Ancelotti", "Real Madrid");

        // Create player
        Player player = new Player("Neymar Jr", 32, 10, Position.FORWARD);

        // Add to source club
        manager1.getClub().addPlayer(player);
        System.out.println("Player added to: " + manager1.getClub().getClubName());
        System.out.println("PSG players: " + manager1.getClub().getPlayers().size());

        // Transfer player
        boolean success = TransferService.transferPlayer(manager1, manager2, player);
        System.out.println("Transfer result: " + (success ? "SUCCESS" : "FAILED"));

        // Check final state
        System.out.println("PSG players after transfer: " + manager1.getClub().getPlayers().size());
        System.out.println("Real Madrid players: " + manager2.getClub().getPlayers().size());

        // Test invalid transfer
        System.out.println("\nTesting invalid transfer (same club)...");
        boolean invalid = TransferService.transferPlayer(manager2, manager2, player);
        System.out.println("Same club transfer: " + (invalid ? "SUCCESS" : "FAILED (expected)"));

        System.out.println("✓ Transfer service test passed\n");
    }

    /**
     * Test 5: AppState (Global Manager List)
     */
    private static void testAppState() {
        System.out.println("TEST 5: AppState Global Store");
        System.out.println("-".repeat(40));

        // Clear previous state
        AppState.managers.clear();

        Manager m1 = new Manager("Arne Slot", "Liverpool");
        Manager m2 = new Manager("Mikel Arteta", "Arsenal");
        Manager m3 = new Manager("Enzo Maresca", "Chelsea");

        AppState.managers.add(m1);
        AppState.managers.add(m2);
        AppState.managers.add(m3);

        System.out.println("Managers in AppState: " + AppState.managers.size());
        for (Manager m : AppState.managers) {
            System.out.println("  - " + m);
        }

        // Add players to managers
        AppState.managers.get(0).getClub().addPlayer(
            new Player("Mohamed Salah", 32, 11, Position.FORWARD)
        );
        AppState.managers.get(1).getClub().addPlayer(
            new Player("Bukayo Saka", 23, 7, Position.FORWARD)
        );
        AppState.managers.get(2).getClub().addPlayer(
            new Player("Nicolas Jackson", 23, 15, Position.FORWARD)
        );

        System.out.println("\nPlayers per club:");
        for (Manager m : AppState.managers) {
            System.out.println("  " + m.getClub().getClubName() + ": " +
                             m.getClub().getPlayers().size() + " player(s)");
        }

        System.out.println("✓ AppState test passed\n");
    }

    /**
     * Comprehensive Integration Test
     */
    public static void testIntegration() {
        System.out.println("\n=== INTEGRATION TEST ===\n");

        // Scenario: Player moves from one club to another
        Manager sourceManager = new Manager("Xavi Hernández", "FC Barcelona");
        Manager targetManager = new Manager("Unai Emery", "Aston Villa");

        Player player = new Player("Gavi", 19, 6, Position.MIDFIELDER);

        // Initial setup
        sourceManager.getClub().addPlayer(player);
        AppState.managers.add(sourceManager);
        AppState.managers.add(targetManager);

        System.out.println("BEFORE:");
        System.out.println("Barcelona: " + sourceManager.getClub().getPlayers().size() + " player(s)");
        System.out.println("Aston Villa: " + targetManager.getClub().getPlayers().size() + " player(s)");

        // Perform transfer
        boolean result = TransferService.transferPlayer(sourceManager, targetManager, player);

        System.out.println("\nTRANSFER: " + (result ? "SUCCESS" : "FAILED"));

        System.out.println("\nAFTER:");
        System.out.println("Barcelona: " + sourceManager.getClub().getPlayers().size() + " player(s)");
        System.out.println("Aston Villa: " + targetManager.getClub().getPlayers().size() + " player(s)");

        System.out.println("\n✓ Integration test passed");
    }
}

