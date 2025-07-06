package me.natesoftware.lastManStanding.game.phases;

import me.natesoftware.lastManStanding.game.GameManager;

import java.util.HashMap;
import java.util.Map;

public class PhaseManager {

    private final GameManager gameManager;
    private GamePhase currentPhase;
    private final Map<String, GamePhase> registeredPhases = new HashMap<>();

    public PhaseManager(GameManager gameManager) {
        this.gameManager = gameManager;

        // Register all phases here
        registeredPhases.put("waiting", new WaitingPhase(gameManager));
//        registeredPhases.put("mining", new MiningPhase(gameManager));
    }

    public void transitionTo(String id) {
        if (currentPhase != null) currentPhase.stop();
        currentPhase = registeredPhases.get(id);
        if (currentPhase != null) currentPhase.start();
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void shutdown() {
        if (currentPhase != null) currentPhase.stop();
    }
}
