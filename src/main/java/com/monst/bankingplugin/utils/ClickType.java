package com.monst.bankingplugin.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.commands.account.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class ClickType {

	private static final Cache<UUID, ClickType> PLAYER_CLICK_TYPES = CacheBuilder.newBuilder()
			.expireAfterWrite(15, TimeUnit.SECONDS)
			.build();

	private final Type type;

	private ClickType(Type type) {
		this.type = type;
	}

	public boolean mustClickedBlockBeAccount() {
		return true;
	}

	boolean isConfirmed() {
		return true;
	}

	void setConfirmed() {

	}

	public void execute(BankingPlugin plugin, Player p, Account account, Block block) {
		throw new UnsupportedOperationException("This operation is not supported for this ClickType!");
	}

	public void execute(BankingPlugin plugin, Player p, Account account) {
		throw new UnsupportedOperationException("This operation is not supported for this ClickType!");
	}

	public void execute(BankingPlugin plugin, Player p, Block block) {
		throw new UnsupportedOperationException("This operation is not supported for this ClickType!");
	}

    /**
     * Clear all click types, cancel timers
     */
    public static void clear() {
		PLAYER_CLICK_TYPES.invalidateAll();
    }

	/**
	 * Gets the click type of a player
	 *
	 * @param player Player whose click type to get
	 * @return The player's click type or <b>null</b> if none
	 */
	public static ClickType getPlayerClickType(OfflinePlayer player) {
		return PLAYER_CLICK_TYPES.asMap().get(player.getUniqueId());
	}

	/**
	 * Removes the click type from a player and cancels the 15 second timer.
	 *
	 * @param player Player to remove the click type from
	 */
	public static void removeClickType(OfflinePlayer player) {
		PLAYER_CLICK_TYPES.asMap().remove(player.getUniqueId());
	}

	public static boolean needsConfirmation(OfflinePlayer player) {
		ClickType clickType = getPlayerClickType(player);
		if (clickType == null)
			return false;
		return !clickType.isConfirmed();
	}

	public static void confirmClickType(OfflinePlayer player) {
		ClickType clickType = PLAYER_CLICK_TYPES.asMap().remove(player.getUniqueId());
		if (clickType == null)
			return;
		clickType.setConfirmed();
		PLAYER_CLICK_TYPES.put(player.getUniqueId(), clickType);
	}

	/**
	 * Sets the click type of a player and removes it after 15 seconds
	 *
	 * @param player    Player whose click type should be set
	 * @param clickType Click type to set
	 */
	private static void setClickType(OfflinePlayer player, ClickType clickType) {
		PLAYER_CLICK_TYPES.asMap().put(player.getUniqueId(), clickType);
	}

	/**
	 * @return Type of the click type
	 */
	public Type getType() {
		return type;
	}

	public enum Type {
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

	public static void setConfigureClickType(Player p, AccountField field, int value) {
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
			super(Type.CREATE);
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Block block) {
			removeClickType(p);
			AccountCreate.create(plugin, p, block);
		}

		@Override
		public boolean mustClickedBlockBeAccount() {
			return false;
		}
	}

	private static class InfoClickType extends ClickType {

		private InfoClickType() {
			super(Type.INFO);
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			removeClickType(p);
			AccountInfo.info(plugin, p, account);
		}
	}

	private static class SelectAccountMigrateClickType extends ClickType {

		private SelectAccountMigrateClickType() {
			super(Type.MIGRATE_SELECT_ACCOUNT);
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account accountToMove) {
			removeClickType(p);
			AccountMigrate.selectAccount(plugin, p, accountToMove);
		}
	}

	private static class SelectNewChestMigrateClickType extends ClickType {

    	private final Account selectedAccount;

		private SelectNewChestMigrateClickType(Account accountToMove) {
			super(Type.MIGRATE_SELECT_NEW_CHEST);
			this.selectedAccount = accountToMove;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account accountToMove, Block targetBlock) {
			removeClickType(p);
			AccountMigrate.selectNewChest(plugin, p, selectedAccount, targetBlock);
		}

		@Override
		public boolean mustClickedBlockBeAccount() {
			return false;
		}
	}

	private static class RecoverClickType extends ClickType {

    	private final Account accountToRecover;

		private RecoverClickType(Account toRecover) {
			super(Type.RECOVER);
			this.accountToRecover = toRecover;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Block block) {
			removeClickType(p);
			AccountRecover.recover(plugin, p, accountToRecover, block);
		}

		@Override
		public boolean mustClickedBlockBeAccount() {
			return false;
		}
	}

	private static class RemoveClickType extends ClickType {

		private boolean confirmed;

		private RemoveClickType() {
			super(Type.REMOVE);
			this.confirmed = false;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			AccountRemove.remove(plugin, p, account);
		}

		@Override
		boolean isConfirmed() {
			return confirmed;
		}

		void setConfirmed() {
			confirmed = true;
		}
	}

	private static class RenameClickType extends ClickType {

    	private final String newName;

		private RenameClickType(String newName) {
			super(Type.RENAME);
			this.newName = newName;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			AccountRename.rename(plugin, p, account, newName);
		}
	}

	private static class ConfigureClickType extends ClickType {

    	private final AccountField field;
    	private final int value;

		private ConfigureClickType(AccountField field, int value) {
			super(Type.CONFIGURE);
			this.field = field;
			this.value = value;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			AccountConfigure.configure(plugin, p, account, field, value);
		}
	}

	private static class TransferClickType extends ClickType {

		private final OfflinePlayer newOwner;
		private boolean confirmed;

		private TransferClickType(OfflinePlayer newOwner) {
			super(Type.TRANSFER);
			this.newOwner = newOwner;
			this.confirmed = false;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			AccountTransfer.transfer(plugin, p, account, newOwner);
		}

		@Override
		boolean isConfirmed() {
			return confirmed;
		}

		@Override
		void setConfirmed() {
			confirmed = true;
		}
	}

	private static class TrustClickType extends ClickType {

		private final OfflinePlayer playerToTrust;

		private TrustClickType(OfflinePlayer playerToTrust) {
			super(Type.TRUST);
			this.playerToTrust = playerToTrust;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			AccountTrust.trust(plugin, p, account, playerToTrust);
		}
	}

	private static class UntrustClickType extends ClickType {

    	private final OfflinePlayer playerToUntrust;

		private UntrustClickType(OfflinePlayer playerToUntrust) {
			super(Type.UNTRUST);
			this.playerToUntrust = playerToUntrust;
		}

		@Override
		public void execute(BankingPlugin plugin, Player p, Account account) {
			AccountUntrust.untrust(plugin, p, account, playerToUntrust);
		}
	}

}
