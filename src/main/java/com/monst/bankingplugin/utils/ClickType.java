package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.listeners.AccountInteractListener;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Credit for this code goes to EpicEricEE

public class ClickType {
	private static final Map<UUID, ClickType> playerClickTypes = new HashMap<>();
	private static final Map<UUID, BukkitTask> playerTimers = new HashMap<>();

	private final EnumClickType enumClickType;

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
        return playerClickTypes.get(player.getUniqueId());
    }

    /**
     * Removes the click type from a player and cancels the 15 second timer.
     * 
     * @param player Player to remove the click type from
     */
	public static void removePlayerClickType(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        playerClickTypes.remove(uuid);
        
        // If a timer is still running, cancel it
		Optional.ofNullable(playerTimers.get(uuid)).ifPresent(BukkitTask::cancel);
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
        playerClickTypes.put(uuid, clickType);

        // If a timer is already running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(BukkitTask::cancel);

        // Remove ClickType after 15 seconds if player has not clicked a chest
        playerTimers.put(uuid, new BukkitRunnable() {
            @Override
            public void run() {
				playerClickTypes.remove(uuid);
				AccountInteractListener.clearUnconfirmed(player);
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
		CREATE, REMOVE, INFO, SET, TRUST, UNTRUST, MIGRATE, TRANSFER
    }

	public static class CreateClickType extends ClickType {

		private final OfflinePlayer newOwner;

		public CreateClickType(OfflinePlayer newOwner) {
			super(EnumClickType.CREATE);
			this.newOwner = newOwner;
		}

		public OfflinePlayer getNewOwner() {
			return newOwner;
		}
	}

	public static class SetClickType extends ClickType {

    	private final SetField field;
		private final String value;

		public SetClickType(SetField field, String value) {
			super(EnumClickType.SET);
			this.field = field;
			this.value = value;
		}

		public SetField getField() {
			return field;
		}

		public String getValue() {
			return value;
		}

		public enum SetField {
			NICKNAME, MULTIPLIER, DELAY
		}
	}

	public static class TrustClickType extends ClickType {

		private final OfflinePlayer toTrust;

		public TrustClickType(OfflinePlayer p) {
			super(EnumClickType.TRUST);
			toTrust = p;
		}

		public OfflinePlayer getPlayerToTrust() {
			return toTrust;
		}
	}

	public static class UntrustClickType extends ClickType {

		private final OfflinePlayer toUntrust;

		public UntrustClickType(OfflinePlayer p) {
			super(EnumClickType.UNTRUST);
			toUntrust = p;
		}

		public OfflinePlayer getPlayerToUntrust() {
			return toUntrust;
		}
	}

	public static class MigrateClickType extends ClickType {

		private final Account toMigrate;

		public MigrateClickType(Account toMigrate) {
			super(EnumClickType.MIGRATE);
			this.toMigrate = toMigrate;
		}

		public boolean isFirstClick() {
			return toMigrate == null;
		}

		public Account getAccountToMigrate() {
			return toMigrate;
		}
	}

	public static class TransferClickType extends ClickType {

		private final OfflinePlayer newOwner;

		public TransferClickType(OfflinePlayer newOwner) {
			super(EnumClickType.TRANSFER);
			this.newOwner = newOwner;
		}

		public OfflinePlayer getNewOwner() {
			return newOwner;
		}
	}
}
