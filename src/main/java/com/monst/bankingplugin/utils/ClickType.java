package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.commands.account.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class ClickType {

	private static final Map<UUID, ClickType> playerClickTypes = new HashMap<>();
	private static final Map<UUID, BukkitTask> playerTimers = new HashMap<>();

	private final EClickType eClickType;

    private ClickType(EClickType eClickType) {
        this.eClickType = eClickType;
    }

	public boolean canClickedBlockNotBeAccount() {
		return false;
	}

	public void execute(Player p, Account account, Block block) {
		throw new UnsupportedOperationException("This operation is not supported for this ClickType!");
	}

	public void execute(Player p, Account account) {
		throw new UnsupportedOperationException("This operation is not supported for this ClickType!");
	}

	public void execute(Player p, Block block) {
		throw new UnsupportedOperationException("This operation is not supported for this ClickType!");
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
    public static ClickType getPlayerClickType(OfflinePlayer player) {
        return playerClickTypes.get(player.getUniqueId());
    }

    /**
     * Removes the click type from a player and cancels the 15 second timer.
     *
     * @param player Player to remove the click type from
     */
	public static void removeClickType(OfflinePlayer player) {
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
    private static void setClickType(OfflinePlayer player, ClickType clickType) {

		UUID uuid = player.getUniqueId();
        playerClickTypes.put(uuid, clickType);

        // If a timer is already running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(BukkitTask::cancel);

        // Remove ClickType after 15 seconds if player has not clicked a chest
        playerTimers.put(uuid, Utils.runTaskLater(() -> playerClickTypes.remove(uuid), 300));

    }

    /**
     * @return Type of the click type
     */
    public EClickType getType() {
        return eClickType;
    }

    public enum EClickType {
		CREATE, INFO, MIGRATE_SELECT_ACCOUNT, MIGRATE_SELECT_NEW_CHEST, RECOVER, REMOVE, RENAME, CONFIGURE, TRANSFER, TRUST, UNTRUST
    }

    public static void setCreateClickType(Player p) {
    	ClickType.setClickType(p, new CreateClickType());
	}

	public static void setInfoClickType(Player p) {
    	ClickType.setClickType(p, new InfoClickType());
	}

	public static void setMigrateClickType(Player p) {
		ClickType.setClickType(p, new SelectAccountMigrateClickType());
	}

	public static void setMigrateClickType(Player p, Account accountToMove) {
		ClickType.setClickType(p, new SelectNewChestMigrateClickType(accountToMove));
	}

	public static void setRecoverClickType(Player p, Account toRecover) {
    	ClickType.setClickType(p, new RecoverClickType(toRecover));
	}

	public static void setRemoveClickType(Player p) {
    	ClickType.setClickType(p, new RemoveClickType());
	}

	public static void setRenameClickType(Player p, String newName) {
		ClickType.setClickType(p, new RenameClickType(newName));
	}

	public static void setConfigureClickType(Player p, AccountField field, String value) {
    	ClickType.setClickType(p, new ConfigureClickType(field, value));
	}

	public static void setTransferClickType(Player p, OfflinePlayer newOwner) {
    	ClickType.setClickType(p, new TransferClickType(newOwner));
	}

	public static void setTrustClickType(Player p, OfflinePlayer playerToTrust) {
    	ClickType.setClickType(p, new TrustClickType(playerToTrust));
	}

	public static void setUntrustClickType(Player p, OfflinePlayer playerToUntrust) {
    	ClickType.setClickType(p, new UntrustClickType(playerToUntrust));
	}

	private static class CreateClickType extends ClickType {
		private CreateClickType() {
			super(EClickType.CREATE);
		}

		@Override
		public void execute(Player p, Block block) {
			AccountCreate.create(p, block);
		}

		@Override
		public boolean canClickedBlockNotBeAccount() {
			return true;
		}
	}

	private static class InfoClickType extends ClickType {
		private InfoClickType() {
    		super(EClickType.INFO);
		}

		@Override
		public void execute(Player p, Account account) {
			AccountInfo.info(p, account);
		}
	}

	private static class SelectAccountMigrateClickType extends ClickType {

		private SelectAccountMigrateClickType() {
			super(EClickType.MIGRATE_SELECT_ACCOUNT);
		}

		@Override
		public void execute(Player p, Account accountToMove) {
			AccountMigrate.selectAccount(p, accountToMove);
		}
	}

	private static class SelectNewChestMigrateClickType extends ClickType {

    	private final Account selectedAccount;

		private SelectNewChestMigrateClickType(Account accountToMove) {
			super(EClickType.MIGRATE_SELECT_NEW_CHEST);
			this.selectedAccount = accountToMove;
		}

		@Override
		public void execute(Player p, Account accountToMove, Block targetBlock) {
			AccountMigrate.selectNewChest(p, this.selectedAccount, targetBlock);
		}

		@Override
		public boolean canClickedBlockNotBeAccount() {
			return true;
		}
	}

	private static class RecoverClickType extends ClickType {

    	private final Account accountToRecover;

		private RecoverClickType(Account toRecover) {
			super(EClickType.RECOVER);
			this.accountToRecover = toRecover;
		}

		@Override
		public void execute(Player p, Block block) {
			AccountRecover.recover(p, accountToRecover, block);
		}

		@Override
		public boolean canClickedBlockNotBeAccount() {
			return true;
		}
	}

	private static class RemoveClickType extends ClickType {
		private RemoveClickType() {
			super(EClickType.REMOVE);
		}

		@Override
		public void execute(Player p, Account account) {
			AccountRemove.getInstance().remove(p, account);
		}
	}

	private static class RenameClickType extends ClickType {

    	private final String newName;

		private RenameClickType(String newName) {
			super(EClickType.RENAME);
			this.newName = newName;
		}

		@Override
		public void execute(Player p, Account account) {
			AccountRename.rename(p, account, newName);
		}
	}

	private static class ConfigureClickType extends ClickType {

    	private final AccountField field;
    	private final String value;

		private ConfigureClickType(AccountField field, String value) {
			super(EClickType.CONFIGURE);
			this.field = field;
			this.value = value;
		}

		@Override
		public void execute(Player p, Account account) {
			AccountConfigure.configure(p, account, field, value);
		}
	}

	private static class TransferClickType extends ClickType {

    	private final OfflinePlayer newOwner;

		private TransferClickType(OfflinePlayer newOwner) {
			super(EClickType.TRANSFER);
			this.newOwner = newOwner;
		}

		@Override
		public void execute(Player p, Account account) {
			AccountTransfer.getInstance().transfer(p, account, newOwner);
		}
	}

	private static class TrustClickType extends ClickType {

		private final OfflinePlayer playerToTrust;

		private TrustClickType(OfflinePlayer playerToTrust) {
			super(EClickType.TRUST);
			this.playerToTrust = playerToTrust;
		}

		@Override
		public void execute(Player p, Account account) {
			AccountTrust.trust(p, account, playerToTrust);
		}
	}

	private static class UntrustClickType extends ClickType {

    	private final OfflinePlayer playerToUntrust;

		private UntrustClickType(OfflinePlayer playerToUntrust) {
			super(EClickType.UNTRUST);
			this.playerToUntrust = playerToUntrust;
		}

		@Override
		public void execute(Player p, Account account) {
			AccountUntrust.untrust(p, account, playerToUntrust);
		}
	}

}
