package com.example.coachsapp.db;

import com.example.coachsapp.model.*;

import java.util.List;

public class UserSeeder {

    public static void seedDefaultUsers(DatabaseService dbService) {
        UserRepository userRepo = dbService.getUserRepository();
        
        List<User> existingUsers = userRepo.findAll();
        if (!existingUsers.isEmpty()) {
            System.out.println("✓ Users already exist, skipping seed");
            return;
        }

        User admin = new User(null, "admin", "admin123", Role.SYSTEM_ADMIN, null, null, null);
        userRepo.save(admin);

    }
}
