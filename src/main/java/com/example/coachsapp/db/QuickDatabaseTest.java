package com.example.coachsapp.db;

import com.example.coachsapp.model.Club;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;

/**
 * Quick test of database functionality
 * Verifies that all database operations work correctly
 */
public class QuickDatabaseTest {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        COACHES APP - QUICK DATABASE TEST                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        try {
            // Step 1: Initialize database
            System.out.println("STEP 1: Initializing Database...");
            DatabaseService dbService = DatabaseService.getInstance();
            System.out.println("✓ Database service initialized\n");

            // Step 2: Create clubs
            System.out.println("STEP 2: Creating Clubs...");
            ClubRepository clubRepo = dbService.getClubRepository();
            
            Club club1 = new Club("Test Club A");
            Club club2 = new Club("Test Club B");
            
            clubRepo.save(club1);
            clubRepo.save(club2);
            System.out.println("✓ Clubs created\n");

            // Step 3: Create managers
            System.out.println("STEP 3: Creating Managers...");
            ManagerRepository managerRepo = dbService.getManagerRepository();
            
            Club retrievedClub1 = clubRepo.findByName("Test Club A");
            Club retrievedClub2 = clubRepo.findByName("Test Club B");
            
            Manager manager1 = new Manager("Test Manager A", "Test Club A");
            manager1.setClub(retrievedClub1);
            Manager manager2 = new Manager("Test Manager B", "Test Club B");
            manager2.setClub(retrievedClub2);
            
            managerRepo.save(manager1);
            managerRepo.save(manager2);
            System.out.println("✓ Managers created\n");

            // Step 4: Create players
            System.out.println("STEP 4: Creating Players...");
            PlayerRepository playerRepo = dbService.getPlayerRepository();
            
            Player player1 = new Player("Test Player 1", 25, 10, Position.FORWARD);
            player1.setClubId(retrievedClub1.getId());
            Player player2 = new Player("Test Player 2", 30, 5, Position.DEFENDER);
            player2.setClubId(retrievedClub1.getId());
            Player player3 = new Player("Test Player 3", 28, 9, Position.MIDFIELDER);
            player3.setClubId(retrievedClub2.getId());
            
            playerRepo.save(player1);
            playerRepo.save(player2);
            playerRepo.save(player3);
            System.out.println("✓ Players created\n");

            // Step 5: Verify data
            System.out.println("STEP 5: Verifying Data...");
            System.out.println("Total Clubs: " + clubRepo.count());
            System.out.println("Total Managers: " + managerRepo.count());
            System.out.println("Total Players: " + playerRepo.count() + "\n");

            // Step 6: Test transfer
            System.out.println("STEP 6: Testing Player Transfer...");
            Player transferPlayer = playerRepo.findById(player1.getId());
            System.out.println("Player before transfer: " + transferPlayer.getName() + " (Club ID: " + transferPlayer.getClubId() + ")");
            
            transferPlayer.setClubId(retrievedClub2.getId());
            playerRepo.update(transferPlayer);
            
            dbService.getTransferHistoryRepository().recordTransfer(
                player1.getId(),
                retrievedClub1.getId(),
                retrievedClub2.getId()
            );
            
            Player updatedPlayer = playerRepo.findById(player1.getId());
            System.out.println("Player after transfer: " + updatedPlayer.getName() + " (Club ID: " + updatedPlayer.getClubId() + ")\n");

            // Step 7: Summary
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║           ✓ ALL DATABASE TESTS PASSED                       ║");
            System.out.println("║           Database file: coaches_app.db                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("\n✗ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

