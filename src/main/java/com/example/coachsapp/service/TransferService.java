package com.example.coachsapp.service;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.db.DatabaseService;

public class TransferService {

    /**
     * Transfer a player from one manager to another
     * Updates both in-memory and database
     */
    public static boolean transferPlayer(Manager fromManager, Manager toManager, Player player) {
        if (fromManager == null || toManager == null || player == null) {
            System.err.println("✗ Transfer failed: Invalid parameters");
            return false;
        }

        if (fromManager.getClub().getId() != null &&
            fromManager.getClub().getId().equals(toManager.getClub().getId())) {
            System.err.println("✗ Transfer failed: Cannot transfer to the same club");
            return false;
        }

        try {
            // Get database service
            DatabaseService dbService = DatabaseService.getInstance();
            Integer fromClubId = fromManager.getClub().getId();
            Integer toClubId = toManager.getClub().getId();

            // Update player's club in database
            player.setClubId(toClubId);
            dbService.getPlayerRepository().update(player);

            // Record transfer in history
            dbService.getTransferHistoryRepository().recordTransfer(
                player.getId(),
                fromClubId,
                toClubId
            );

            // Update in-memory lists
            boolean removed = fromManager.getClub().removePlayer(player);
            if (removed) {
                toManager.getClub().addPlayer(player);
                System.out.println("✓ Transfer successful: " + player.getName() +
                                 " from " + fromManager.getClub().getClubName() +
                                 " to " + toManager.getClub().getClubName());
                return true;
            }
        } catch (Exception e) {
            System.err.println("✗ Transfer error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

