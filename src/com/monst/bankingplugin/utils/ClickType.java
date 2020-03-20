package com.monst.bankingplugin.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// THIS CODE WAS TAKEN FROM de.epiceric.shopchest.utils.ClickType

import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.listeners.AccountInteractListener;

public class ClickType {
    private static Map<UUID, ClickType> playerClickType = new HashMap<>();
    private static Map<UUID, BukkitTask> playerTimers = new HashMap<>();

    private EnumClickType enumClickType;

    public ClickType(EnumClickType enumClickType) {
        this.enumClickType = enumClickType;
    }

    /**
     * Clear all click types, cancel timers
     */
    public static void clear() {
        playerTimers.forEach((uuid, timer) -> timer.cancel());
    }

    /**
	 * Gets the click type of a player
	 *
	 * @param player Player whose click type should be gotten
	 * @return The Player's click type or <b>null</b> if they don't have one
	 */
    public static ClickType getPlayerClickType(OfflinePlayer player) {
        return playerClickType.get(player.getUniqueId());
    }

    /**
     * Removes the click type from a player and cancels the 15 second timer
     * 
     * @param player Player to remove the click type from
     */
	public static void removePlayerClickType(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        playerClickType.remove(uuid);
        
        // If a timer is still running, cancel it
		Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());
		playerTimers.remove(uuid);
    }

    /**
     * Sets the click type of a player and removes it after 15 seconds
     *
     * @param player    Player whose click type should be set
     * @param clickType Click type to set
     */
    public static void setPlayerClickType(OfflinePlayer player, ClickType clickType) {

		UUID uuid = player.getUniqueId();
        playerClickType.put(uuid, clickType);

        // If a timer is already running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());

        // Remove ClickType after 15 seconds if player has not clicked a chest
        playerTimers.put(uuid, new BukkitRunnable() {
            @Override
            public void run() {
				playerClickType.remove(uuid);
				AccountInteractListener.clearUnconfirmed(player);
				BankingPlugin.getInstance().getServer().broadcastMessage("ClickType expired.");
            }
		}.runTaskLater(BankingPlugin.getInstance(), 300));
    
    }

    /**
     * @return Type of the click type
     */
    public EnumClickType getClickType() {
        return enumClickType;
    }

    public enum EnumClickType {
		CREATE, REMOVE, INFO, SET
    }

	public static class InfoClickType extends ClickType {

		private boolean verbose;

		public InfoClickType(boolean verbose) {
			super(EnumClickType.INFO);
			this.verbose = verbose;
		}

		public boolean isVerbose() {
			return verbose;
		}
	}

	public static class SetClickType extends ClickType {

		private String[] args;

		public SetClickType(String[] args) {
			super(EnumClickType.SET);
			this.args = args;
		}

		public String[] getArgs() {
			return args;
		}
	}
}
