package com.example.coachsapp.test;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;
import com.example.coachsapp.util.AppState;

public class SanityAppStateTest {
    public static void main(String[] args) {
        System.out.println("Initial players: " + AppState.players.size());
        Player p = new Player("Test Player X", 22, 99, Position.FORWARD);
        AppState.players.add(p);
        System.out.println("After add players: " + AppState.players.size());
        for (Player pl : AppState.players) {
            System.out.println(pl.getName() + " - " + pl.getAge() + " - " + pl.getJersey());
        }
    }
}

