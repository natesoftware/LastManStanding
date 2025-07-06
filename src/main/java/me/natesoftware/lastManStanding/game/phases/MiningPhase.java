package me.natesoftware.lastManStanding.game.phases;

import me.natesoftware.lastManStanding.game.GameManager;
import me.natesoftware.lastManStanding.player.LMSPlayer;
import me.natesoftware.lastManStanding.player.PlayerState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Mining phase - players gather resources before PvP
 */
public class MiningPhase extends BaseGamePhase {

    private static final String PHASE_NAME = "Mining";
    private static final String PHASE_DESCRIPTION = "Gather resources and prepare for battle!";
    private static final int PHASE_DURATION = 300; // 5 minutes

    public MiningPhase(GameManager gameManager) {
        super(gameManager, PHASE_NAME, PHASE_DESCRIPTION, PHASE_DURATION);
    }

    @Override
    protected void onStart() {
        // Setup all players for mining phase
        setupMiningPlayers();

        // Announce phase start
        MessageUtil.broadcast("&a&lMINING PHASE STARTED!");
        MessageUtil.broadcast("&7You have &f" + PHASE_DURATION + " &7seconds to gather resources!");
        MessageUtil.broadcast("&7PvP is &cDISABLED &7during this phase.");
    }

    @Override
    protected void onStop() {
        // Announce phase end
        MessageUtil.broadcast("&c&lMINING PHASE ENDED!");
        MessageUtil.broadcast("&7PvP will be enabled in the next phase!");
    }

    @Override
    protected void onUpdate() {
        // Announce time remaining at intervals
        announceTimeRemaining();

        // Check if all players are ready (future feature)
        // Could add vote-to-skip functionality here
    }

    @Override
    protected void onTimeout() {
        // Transition to PvP phase
        MessageUtil.broadcast("&c&lMINING TIME IS UP!");
        MessageUtil.broadcast("&7Transitioning to PvP phase...");

        // Transition handled by PhaseManager
    }

    @Override
    protected void onPlayerJoin(Player player) {
        // Setup joining player for mining
        setupMiningPlayer(player);

        // Announce to others
        MessageUtil.broadcast("&a" + player.getName() + " &7joined during mining phase!");

        // Welcome message
        MessageUtil.sendInfoMessage(player, "Mining phase is active! Gather resources quickly!");
        MessageUtil.sendInfoMessage(player, "Time remaining: " + getRemainingTime() + " seconds");
    }

    @Override
    protected void onPlayerLeave(Player player) {
        // Announce departure
        MessageUtil.broadcast("&c" + player.getName() + " &7left during mining phase!");

        // Check if game should continue
        if (gameManager.getAlivePlayerCount() < 2) {
            MessageUtil.broadcast("&cNot enough players remaining! Game will end...");
            gameManager.forceEndGame();
        }
    }

    @Override
    protected void onPlayerDeath(Player player) {
        // Players can die from environment during mining
        MessageUtil.broadcast("&c" + player.getName() + " &7died during mining phase!");

        // Handle elimination
        gameManager.handlePlayerDeath(player);
    }

    @Override
    public boolean canTransition(String toPhase) {
        switch (toPhase.toLowerCase()) {
            case "pvp":
                return true; // Can always transition to PvP
            case "endgame":
                return gameManager.getAlivePlayerCount() <= 1; // Only if game should end
            default:
                return false;
        }
    }

    /**
     * Setup all players for mining phase
     */
    private void setupMiningPlayers() {
        for (Player player : gameManager.getActivePlayers()) {
            setupMiningPlayer(player);
        }
    }

    /**
     * Setup a single player for mining phase
     */
    private void setupMiningPlayer(Player player) {
        LMSPlayer lmsPlayer = gameManager.getPlugin().getPlayerManager().getPlayer(player);

        // Set player state
        lmsPlayer.setPlayerState(PlayerState.PLAYING);

        // Setup player
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);

        // Give starting items
        giveMiningItems(player);

        // Teleport to mining area
        Location miningLocation = getMiningLocation();
        player.teleport(miningLocation);
    }

    /**
     * Give mining starter items to player
     */
    private void giveMiningItems(Player player) {
        // Clear inventory first
        player.getInventory().clear();

        // Give basic mining tools
        player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));
        player.getInventory().addItem(new ItemStack(Material.STONE_AXE));
        player.getInventory().addItem(new ItemStack(Material.STONE_SHOVEL));

        // Give some food
        player.getInventory().addItem(new ItemStack(Material.BREAD, 16));

        // Give basic materials
        player.getInventory().addItem(new ItemStack(Material.OAK_PLANKS, 32));
        player.getInventory().addItem(new ItemStack(Material.COBBLESTONE, 32));
    }

    /**
     * Get mining location from config
     */
    private Location getMiningLocation() {
        try {
            String worldName = gameManager.getPlugin().getConfigManager().getConfig()
                    .getString("world.mining-world", "world");
            double x = gameManager.getPlugin().getConfigManager().getConfig()
                    .getDouble("world.mining.x", 0);
            double y = gameManager.getPlugin().getConfigManager().getConfig()
                    .getDouble("world.mining.y", 64);
            double z = gameManager.getPlugin().getConfigManager().getConfig()
                    .getDouble("world.mining.z", 0);

            return new Location(gameManager.getPlugin().getServer().getWorld(worldName), x, y, z);
        } catch (Exception e) {
            MessageUtil.logWarning("Failed to get mining location from config, using spawn");
            return gameManager.getPlugin().getServer().getWorlds().get(0).getSpawnLocation();
        }
    }
}