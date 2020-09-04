package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.listeners.AccountInteractListener;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Credit for this code goes to EpicEricEE

public abstract class ClickType<T> {

	private static final Map<UUID, ClickType<?>> playerClickTypes = new HashMap<>();
	private static final Map<UUID, BukkitTask> playerTimers = new HashMap<>();

	private final EClickType eClickType;
	private final T t;

    private ClickType(EClickType eClickType, T t) {
        this.eClickType = eClickType;
        this.t = t;
    }

	/**
	 * Retrieves the object that is being carried by this ClickType.
	 * @param <K> the dynamic return type
	 */
	@SuppressWarnings("unchecked")
    public <K> K get() {
    	return (K) eClickType.getDataType().cast(t);
	}

    /**
     * Clear all click types, cancel timers
     */
    public static void clear() {
    	playerClickTypes.clear();
        playerTimers.values().forEach(BukkitTask::cancel);
        playerTimers.clear();
    }

    /**
	 * Gets the click type of a player
	 *
	 * @param player Player whose click type should be gotten
	 * @return The Player's click type or <b>null</b> if they don't have one
	 */
    public static ClickType<?> getPlayerClickType(OfflinePlayer player) {
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
    public static void setPlayerClickType(OfflinePlayer player, ClickType<?> clickType) {

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
    public EClickType getType() {
        return eClickType;
    }

    public enum EClickType {

		CREATE (OfflinePlayer.class),
		REMOVE (Void.class),
		INFO (Void.class),
		SET (SetPair.class),
		TRUST (OfflinePlayer.class),
		UNTRUST (OfflinePlayer.class),
		MIGRATE (Account.class),
		RECOVER (Account.class),
		TRANSFER (OfflinePlayer.class);

		private final Class<?> dataType;

		EClickType(Class<?> dataType) {
			this.dataType = dataType;
		}
		public Class<?> getDataType() {
			return dataType;
		}
    }

    public static CreateClickType create(OfflinePlayer newOwner) {
    	return new CreateClickType(newOwner);
	}

	public static InfoClickType remove() {
    	return new InfoClickType();
	}

	public static RemoveClickType info() {
    	return new RemoveClickType();
	}

	public static SetClickType set(AccountField field, String value) {
    	return new SetClickType(new SetPair(field, value));
	}

	public static TrustClickType trust(OfflinePlayer toTrust) {
    	return new TrustClickType(toTrust);
	}

	public static UntrustClickType untrust(OfflinePlayer toUntrust) {
    	return new UntrustClickType(toUntrust);
	}

	public static MigrateClickType migrate(Account toMigrate) {
    	return new MigrateClickType(toMigrate);
	}

	public static RecoverClickType recover(Account toRecover) {
    	return new RecoverClickType(toRecover);
	}

	public static TransferClickType transfer(OfflinePlayer newOwner) {
    	return new TransferClickType(newOwner);
	}

	public static class CreateClickType extends ClickType<OfflinePlayer> {
		private CreateClickType(OfflinePlayer owner) {
			super(EClickType.CREATE, owner);
		}
	}

	public static class RemoveClickType extends ClickType<Void> {
		private RemoveClickType() {
    		super(EClickType.REMOVE, null);
		}
	}

	public static class InfoClickType extends ClickType<Void> {
		private InfoClickType() {
    		super(EClickType.INFO, null);
		}
	}

	public static class SetClickType extends ClickType<SetPair> {
		private SetClickType(SetPair pair) {
			super(EClickType.SET, pair);
		}
	}

	public static class TrustClickType extends ClickType<OfflinePlayer> {
		private TrustClickType(OfflinePlayer toTrust) {
			super(EClickType.TRUST, toTrust);
		}
	}

	public static class UntrustClickType extends ClickType<OfflinePlayer> {
		private UntrustClickType(OfflinePlayer toUntrust) {
			super(EClickType.UNTRUST, toUntrust);
		}
	}

	public static class MigrateClickType extends ClickType<Account> {
		private MigrateClickType(Account toMigrate) {
			super(EClickType.MIGRATE, toMigrate);
		}
	}

	public static class RecoverClickType extends ClickType<Account> {
		private RecoverClickType(Account toRecover) {
			super(EClickType.RECOVER, toRecover);
		}
	}

	public static class TransferClickType extends ClickType<OfflinePlayer> {
		private TransferClickType(OfflinePlayer newOwner) {
			super(EClickType.TRANSFER, newOwner);
		}
	}

	public static class SetPair extends Pair<AccountField, String> {
		private SetPair(AccountField field, String value) {
			super(field, value);
		}
		public AccountField getField() { return super.getFirst(); }
		public String getValue() { return super.getSecond(); }
	}
}
