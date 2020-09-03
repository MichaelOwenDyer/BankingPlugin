package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.commands.account.subcommands.AccountRecover;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.*;
import com.monst.bankingplugin.gui.AccountGui;
import com.monst.bankingplugin.utils.*;
import com.monst.bankingplugin.utils.ClickType.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AccountInteractListener implements Listener, ConfirmableAccountAction {
	
	private static final Map<UUID, Set<Integer>> unconfirmed = new HashMap<>();

	private final BankingPlugin plugin;
	private final AccountUtils accountUtils;
	private final BankUtils bankUtils;

	public AccountInteractListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
		this.bankUtils = plugin.getBankUtils();
	}

	/**
	 * Checks every inventory interact event for an account action attempt, and
	 * handles the action.
	 */
	@SuppressWarnings({"deprecation","unused"})
	@EventHandler(priority = EventPriority.HIGH)
	public void onAccountInteract(PlayerInteractEvent e) {
		
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if (b == null || b.getType() == Material.AIR)
			return;
		Account account = accountUtils.getAccount(b.getLocation());
		ClickType clickType = ClickType.getPlayerClickType(p);

		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST))
			return;
		if (clickType == null && account == null)
			return;

		if (clickType != null) {

			if (account == null && !(clickType.getClickType() == ClickType.EnumClickType.CREATE
					|| clickType.getClickType() == ClickType.EnumClickType.MIGRATE
					|| clickType.getClickType() == EnumClickType.RECOVER))
				return;
			if (account == null && clickType.getClickType() == ClickType.EnumClickType.MIGRATE
					&& ((MigrateClickType) clickType).isFirstClick())
				return;
			if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK))
				return;

			switch (clickType.getClickType()) {

				case CREATE:

					if (e.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
						p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
						plugin.debug(p.getName() + " does not have permission to create an account on a protected chest.");
					} else {
						OfflinePlayer newOwner = ((CreateClickType) clickType).getNewOwner();
						create(p, newOwner, b);
					}
					ClickType.removePlayerClickType(p);
					e.setCancelled(true);
					break;

				case REMOVE:

					Objects.requireNonNull(account);
					if (confirmRemove(p, account))
						remove(p, account);
					e.setCancelled(true);
					break;

				case INFO:

					Objects.requireNonNull(account);
					info(p, account);
					ClickType.removePlayerClickType(p);
					e.setCancelled(true);
					break;

				case SET:

					SetClickType.SetField field = ((SetClickType) clickType).getField();
					String value = ((SetClickType) clickType).getValue();
					set(p, account, field, value);
					ClickType.removePlayerClickType(p);
					e.setCancelled(true);
					break;

				case TRUST:

					Objects.requireNonNull(account);
					OfflinePlayer playerToTrust = ((TrustClickType) clickType).getPlayerToTrust();
					trust(p, account, playerToTrust);
					ClickType.removePlayerClickType(p);
					e.setCancelled(true);
					break;

				case UNTRUST:

					Objects.requireNonNull(account);
					OfflinePlayer playerToUntrust = ((UntrustClickType) clickType).getPlayerToUntrust();
					untrust(p, account, playerToUntrust);
					ClickType.removePlayerClickType(p);
					e.setCancelled(true);
					break;

				case MIGRATE:

					Objects.requireNonNull(account);
					if (((MigrateClickType) clickType).isFirstClick()) {
						migratePartOne(p, account);
					} else {
						if (e.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
							p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED);
							plugin.debug(p.getName() + " does not have permission to migrate an account to a protected chest.");
						} else
							migratePartTwo(p, b, ((MigrateClickType) clickType).getAccountToMigrate());
						ClickType.removePlayerClickType(p);
					}
					e.setCancelled(true);
					break;

				case TRANSFER:

					Objects.requireNonNull(account);
					OfflinePlayer newOwner = ((TransferClickType) clickType).getNewOwner();
					if (confirmTransfer(p, newOwner, account)) {
						transfer(p, newOwner, account);
						ClickType.removePlayerClickType(p);
					}
					e.setCancelled(true);
					break;

				case RECOVER:

					Account toRecover = Objects.requireNonNull(((RecoverClickType) clickType).getAccountToRecover());
					AccountRecover.recover(p, b, toRecover);
					ClickType.removePlayerClickType(p);
					e.setCancelled(true);
					break;
			}

		} else {

			if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
				return;
			if (p.isSneaking() && Utils.hasAxeInHand(p) && e.getAction() == Action.LEFT_CLICK_BLOCK)
				return;

			// Handles account info requests using config info item
			ItemStack infoItem = Config.accountInfoItem;

			if (infoItem != null) {
				ItemStack item = Utils.getItemInMainHand(p);
				if (item != null && infoItem.getType() == item.getType()) {
					e.setCancelled(true);
					info(p, account);
					return;
				}
				item = Utils.getItemInOffHand(p);
				if (item != null && infoItem.getType() == item.getType()) {
					e.setCancelled(true);
					info(p, account);
					return;
				}
			}

			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()) {
				if (!account.isTrusted(p) && !account.getBank().isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_VIEW_OTHER)) {
					e.setCancelled(true);
					p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_VIEW);
					plugin.debug(p.getName() + " does not have permission to open " + account.getOwner().getName()
							+ "'s account chest.");
					return;
				}

				if (e.isCancelled())
					e.setCancelled(false);
				if (!account.isTrusted(p))
					p.sendMessage(String.format(Messages.ACCOUNT_OPENED, account.getOwner().getName()));

				plugin.debugf("%s is opening %s account%s (#%d)",
						p.getName(), (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"),
						(account.isCoowner(p) ? " (is co-owner)" : ""), account.getID());
			}
		}
	}

	/**
	 * Create a new account
	 *
	 * @param executor Player who executed the command will receive the message
	 *                 and become the owner of the account
	 * @param b Block where the account will be located
	 */
	private void create(final Player executor, final OfflinePlayer owner, final Block b) {

		Location location = b.getLocation();

		if (accountUtils.isAccount(location)) {
			executor.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
			plugin.debug("Chest is already an account.");
			return;
		}
		if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
			executor.sendMessage(Messages.CHEST_BLOCKED);
			plugin.debug("Chest is blocked.");
			return;
		}
		if (!bankUtils.isBank(location)) {
			executor.sendMessage(Messages.CHEST_NOT_IN_BANK);
			plugin.debug("Chest is not in a bank.");
			plugin.debug(executor.getName() + " is creating new account...");
			return;
		}

		boolean forSelf = Utils.samePlayer(executor, owner);

		Bank bank = bankUtils.getBank(location);
		if (!Config.allowSelfBanking && forSelf && bank.isOwner(executor)) {
			executor.sendMessage(Messages.NO_SELF_BANKING);
			plugin.debug(executor.getName() + " is not permitted to create an account at their own bank");
			return;
		}
		if (!forSelf) {
			int playerAccountLimit = bank.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT);
			if (playerAccountLimit > 0 && bank.getAccountsCopy(account -> account.isOwner(executor)).size() >= playerAccountLimit) {
				executor.sendMessage(Messages.PER_BANK_ACCOUNT_LIMIT_REACHED);
				plugin.debug(executor.getName() + " is not permitted to create another account at bank " + bank.getName());
				return;
			}
		}

		Account account = Account.mint(owner, location);
		
		AccountCreateEvent event = new AccountCreateEvent(executor, owner, account);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !executor.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
			plugin.debug("No permission to create account on a protected chest.");
			executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
			return;
		}

		double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
		creationPrice *= ((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1;

		if (creationPrice > 0 && creationPrice > plugin.getEconomy().getBalance(executor)
				&& forSelf && !bank.isOwner(executor)) {
			executor.sendMessage(Messages.ACCOUNT_CREATE_INSUFFICIENT_FUNDS);
			return;
		}

		OfflinePlayer accountOwner = executor.getPlayer();
		double finalCreationPrice = creationPrice;
		// Account owner pays the bank owner the creation fee
		if (!Utils.withdrawPlayer(accountOwner, location.getWorld().getName(), creationPrice,
				Callback.of(plugin,
						result -> executor.sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_PAID, Utils.format(finalCreationPrice))),
						throwable -> executor.sendMessage(Messages.ERROR_OCCURRED))))
			return;

		// Bank owner receives the payment from the customer
		if (creationPrice > 0 && bank.isPlayerBank() && !bank.isOwner(executor)) {
			OfflinePlayer bankOwner = account.getBank().getOwner();
			Utils.depositPlayer(bankOwner, location.getWorld().getName(), creationPrice, Callback.of(plugin,
					result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_CREATE_FEE_RECEIVED,
							accountOwner.getName(), Utils.format(finalCreationPrice)), bankOwner),
					throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
		}

		if (account.create(true)) {
			plugin.debug("Account created");
			accountUtils.addAccount(account, true);
			executor.sendMessage(Messages.ACCOUNT_CREATED);
		}
	}

	private boolean confirmRemove(Player p, Account account) {

		if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER) && !account.getBank().isTrusted(p)) {
			if (account.isTrusted(p))
				p.sendMessage(Messages.MUST_BE_OWNER);
			else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
			return !unconfirmed.containsKey(p.getUniqueId());
		}

		if ((account.getBalance().signum() == 1 || Config.confirmOnRemove)) {
			if (!isConfirmed(p, account.getID())) {
				plugin.debug("Needs confirmation");
				if (account.getBalance().signum() == 1) {
					p.sendMessage(Messages.ACCOUNT_BALANCE_NOT_ZERO);
				}
				p.sendMessage(Messages.CLICK_TO_CONFIRM);
				return false;
			}
		} else
			ClickType.removePlayerClickType(p);
		return true;
	}

	/**
	 * Remove a account
	 *
	 * @param executor Player, who executed the command and will receive the message
	 * @param account  Account to be removed
	 */
	private void remove(Player executor, Account account) {
		plugin.debugf("%s is removing %s account (#%d)",
				executor.getName(),
				account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s",
				account.getID());

		AccountRemoveEvent event = new AccountRemoveEvent(executor, account);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Remove event cancelled (#" + account.getID() + ")");
			return;
		}

		Bank bank = account.getBank();
		double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
		creationPrice *= account.getSize();
		creationPrice *= bank.get(BankField.REIMBURSE_ACCOUNT_CREATION) ? 1 : 0;

		if (creationPrice > 0 && account.isOwner(executor) && !account.getBank().isOwner(executor)) {

			double finalCreationPrice = creationPrice;
			Utils.depositPlayer(executor.getPlayer(), account.getLocation().getWorld().getName(), finalCreationPrice,
					Callback.of(plugin,
							result -> executor.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED,
									Utils.format(finalCreationPrice))),
							throwable -> executor.sendMessage(Messages.ERROR_OCCURRED)));

			if (account.getBank().isPlayerBank()) {
				OfflinePlayer bankOwner = account.getBank().getOwner();
				Utils.withdrawPlayer(bankOwner, account.getLocation().getWorld().getName(), finalCreationPrice,
						Callback.of(plugin,
								result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
										account.getOwner().getName(), Utils.format(finalCreationPrice)),
										Collections.singleton(bankOwner), account.getOwner()),
								throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
			}
		}

		executor.sendMessage(Messages.ACCOUNT_REMOVED);
		accountUtils.removeAccount(account, true);
		plugin.debug("Removed account (#" + account.getID() + ")");
	}

	/**
	 * @param player  Player who executed the command and will retrieve the
	 *                information
	 * @param account Account from which the information will be retrieved
	 */
	private void info(Player player, Account account) {
		plugin.debugf("%s is retrieving %s account info%s (#%d)",
				player.getName(), (account.isOwner(player) ? "their" : account.getOwner().getName() + "'s"),
				(account.isCoowner(player) ? " (is co-owner)" : ""), account.getID());

		AccountInfoEvent event = new AccountInfoEvent(player, account);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debugf("Info event cancelled (#%d)", account.getID());
			return;
		}

		new AccountGui(account).open(player);
	}

	private void set(Player executor, Account account, SetClickType.SetField field, String value) {

		switch (field) {
			case NICKNAME:
				if (!(account.isTrusted(executor) || executor.hasPermission(Permissions.ACCOUNT_SET_NICKNAME_OTHER))) {
					plugin.debugf("%s does not have permission to change another player's account nickname", executor.getName());
					executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME_OTHER);
					return;
				}

				if (value.isEmpty()) {
					plugin.debugf("%s has reset %s account nickname%s (#%d)", executor.getName(),
							(account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s"),
							(account.isCoowner(executor) ? " (is co-owner)" : ""), account.getID());
					account.setToDefaultName();
				} else {
					plugin.debugf("%s has set their account nickname to \"%s\" (#%d)",
							executor.getName(), value, account.getID());
					account.setName(value);
				}

				executor.sendMessage(Messages.NICKNAME_SET);
				break;

			case MULTIPLIER:
				if (!executor.hasPermission(Permissions.ACCOUNT_SET_MULTIPLIER)) {
					plugin.debugf("%s does not have permission to change %s's account multiplier",
							executor.getName(), account.getOwner().getName());
					executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
					return;
				}

				if (value.startsWith("+") || value.startsWith("-"))
					account.getStatus().setMultiplierStageRelative(Integer.parseInt(value));
				else
					account.getStatus().setMultiplierStage(Integer.parseInt(value));

				executor.sendMessage(String.format(Messages.MULTIPLIER_SET, account.getStatus().getRealMultiplier()));
				plugin.debugf("%s has set an account multiplier stage to %d (#%d)%s",
						executor.getName(), account.getStatus().getMultiplierStage(), account.getID(), (account.isCoowner(executor) ? " (is co-owner)" : ""));
				break;

			case DELAY:
				if (!executor.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
					executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
					return;
				}

				if (value.startsWith("+") || value.startsWith("-"))
					account.getStatus().setInterestDelayRelative(Integer.parseInt(value)); // Set relative to current if value prefixed with + or -
				else
					account.getStatus().setInterestDelay(Integer.parseInt(value));

				plugin.debugf("%s has set the interest delay of account #%d to %d.",
						executor.getName(), account.getID(), account.getStatus().getDelayUntilNextPayout());
				executor.sendMessage(String.format(Messages.INTEREST_DELAY_SET, account.getStatus().getDelayUntilNextPayout()));
		}
		plugin.getAccountUtils().addAccount(account, true);
		AccountConfigureEvent e = new AccountConfigureEvent(executor, account, field, value);
		Bukkit.getPluginManager().callEvent(e);
	}

	private void trust(Player p, Account account, OfflinePlayer playerToTrust) {
		if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
			if (account.isTrusted(p)) {
				p.sendMessage(Messages.MUST_BE_OWNER);
				return;
			}
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRUST_OTHER);
			return;
		}

		boolean isSelf = Utils.samePlayer(playerToTrust, p);
		if (account.isTrusted(playerToTrust)) {
			plugin.debugf("%s was already trusted on that account (#%d)", playerToTrust.getName(), account.getID());
			p.sendMessage(String.format(account.isOwner(playerToTrust) ? Messages.ALREADY_OWNER : Messages.ALREADY_COOWNER,
					isSelf ? "You are" : playerToTrust.getName() + " is", "account"));
			return;
		}

		plugin.debugf("%s has trusted %s to %s account (#%d)", p.getName(), playerToTrust.getName(),
				(account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"), account.getID());
		p.sendMessage(String.format(Messages.ADDED_COOWNER, isSelf ? "You were" : playerToTrust.getName() + " was"));
		account.trustPlayer(playerToTrust);
	}

	private void untrust(Player p, Account account, OfflinePlayer playerToUntrust) {
		if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
			if (account.isTrusted(p)) {
				p.sendMessage(Messages.MUST_BE_OWNER);
				return;
			}
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER);
			return;
		}

		boolean isSelf = Utils.samePlayer(playerToUntrust, p);
		if (!account.isCoowner(playerToUntrust)) {
			plugin.debugf("%s was not a co-owner of that account and could not be removed (#%d)",
					playerToUntrust.getName(), account.getID());
			p.sendMessage(String.format(Messages.NOT_A_COOWNER, isSelf ? "You are" : playerToUntrust.getName() + " is", "account"));
			return;
		}

		plugin.debugf("%s has untrusted %s from %s account (#%d)", p.getName(),	playerToUntrust.getName(),
				(account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"), account.getID());
		p.sendMessage(String.format(Messages.REMOVED_COOWNER, isSelf ? "You were" : playerToUntrust.getName() + " was"));
		account.untrustPlayer(playerToUntrust);
	}

	private void migratePartOne(Player p, Account toMigrate) {

		if (!toMigrate.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_OTHER)) {
			if (toMigrate.isTrusted(p)) {
				plugin.debugf("%s cannot migrate account #%d as a co-owner", p.getName(), toMigrate.getID());
				p.sendMessage(Messages.MUST_BE_OWNER);
				return;
			}
			plugin.debugf("%s does not have permission to migrate account #%d", p.getName(), toMigrate.getID());
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER);
			return;
		}
		plugin.debugf("%s wants to migrate account #%d", p.getName(), toMigrate.getID());
		ClickType.setPlayerClickType(p, new MigrateClickType(toMigrate));
		p.sendMessage(Messages.CLICK_CHEST_MIGRATE_SECOND);

	}

	private void migratePartTwo(Player p, Block b, Account toMigrate) {
		Location newLocation = b.getLocation();
		if (accountUtils.isAccount(newLocation)) {
			if (toMigrate.equals(accountUtils.getAccount(newLocation))) {
				plugin.debugf("%s clicked the same chest to migrate to.", p.getName());
				p.sendMessage(Messages.SAME_ACCOUNT);
				return;
			}
			plugin.debugf("%s clicked an already existing account chest to migrate to", p.getName());
			p.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
			return;
		}

		if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
			p.sendMessage(Messages.CHEST_BLOCKED);
			plugin.debug("Chest is blocked.");
			return;
		}
		Bank newBank = bankUtils.getBank(newLocation); // May or may not be the same as previous bank
		if (newBank == null) {
			p.sendMessage(Messages.CHEST_NOT_IN_BANK);
			plugin.debug("Chest is not in a bank.");
			return;
		}
		if (!toMigrate.getBank().equals(newBank) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_BANK)) {
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE_BANK);
			plugin.debugf("%s does not have permission to migrate their account to another bank.", p.getName());
			return;
		}

		Account newAccount = Account.clone(toMigrate);
		newAccount.setBank(newBank);
		newAccount.setLocation(newLocation);

		AccountMigrateEvent event = new AccountMigrateEvent(p, newAccount, newLocation);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
			plugin.debug("No permission to create account on a protected chest.");
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
			return;
		}

		Bank oldBank = toMigrate.getBank();

		double creationPrice = newBank.get(BankField.ACCOUNT_CREATION_PRICE);
		creationPrice *= (((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1);
		creationPrice *= (newBank.isOwner(p) ? 0 : 1);

		double reimbursement = oldBank.get(BankField.REIMBURSE_ACCOUNT_CREATION)
				? oldBank.get(BankField.ACCOUNT_CREATION_PRICE) : 0.0d;
		reimbursement *= toMigrate.getSize(); // Double chest is worth twice as much
		reimbursement *= (oldBank.isOwner(p) ? 0 : 1); // Free if owner

		double net = reimbursement - creationPrice;
		if (plugin.getEconomy().getBalance(p) < net * -1) {
			p.sendMessage(Messages.ACCOUNT_CREATE_INSUFFICIENT_FUNDS);
			return;
		}

		final double finalReimbursement = reimbursement;
		final double finalCreationPrice = creationPrice;

		// Customer receives reimbursement for old account
		if (finalReimbursement > 0 && !oldBank.isOwner(p)) {
			Utils.depositPlayer(p.getPlayer(), toMigrate.getLocation().getWorld().getName(), finalReimbursement,
					Callback.of(plugin,
							result -> p.sendMessage(String.format(
									Messages.ACCOUNT_REIMBURSEMENT_RECEIVED, Utils.format(finalReimbursement))),
							throwable -> p.sendMessage(Messages.ERROR_OCCURRED)));
		}

		// Bank owner of new account receives account creation fee
		if (finalCreationPrice > 0 && newBank.isPlayerBank() && !newBank.isOwner(p)) {
			OfflinePlayer bankOwner = newBank.getOwner();
			Utils.depositPlayer(bankOwner, toMigrate.getLocation().getWorld().getName(), finalCreationPrice,
					Callback.of(plugin,
							result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_CREATE_FEE_RECEIVED,
									Utils.format(finalCreationPrice)), bankOwner),
							throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
		}

		// Account owner pays creation fee for new account
		if (creationPrice > 0 && !newBank.isOwner(p)) {
			if (!Utils.withdrawPlayer(p, newLocation.getWorld().getName(), finalCreationPrice, Callback.of(plugin,
					result -> p.sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_PAID, Utils.format(finalCreationPrice))),
					throwable -> p.sendMessage(Messages.ERROR_OCCURRED))))
				return;
		}

		// Bank owner of old account pays reimbursement
		if (reimbursement > 0 && oldBank.isPlayerBank() && !oldBank.isOwner(p)) {
			OfflinePlayer bankOwner = oldBank.getOwner();
			Utils.withdrawPlayer(bankOwner, newLocation.getWorld().getName(), finalReimbursement,
					Callback.of(plugin,
							result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
									p.getName(), Utils.format(finalReimbursement)), bankOwner),
							throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
		}

		if (newAccount.create(true)) {
			plugin.debugf("Account migrated (#%d)", newAccount.getID());
			accountUtils.removeAccount(toMigrate, false,
					Callback.of(plugin,
							result -> {
								accountUtils.addAccount(newAccount, true); // Database entry is replaced
								p.sendMessage(Messages.ACCOUNT_MIGRATED);
							},
							throwable -> p.sendMessage(Messages.ERROR_OCCURRED))
			);
		}
	}

	private boolean confirmTransfer(Player p, OfflinePlayer newOwner, Account account) {
		if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER)) {
			if (account.isTrusted(p))
				p.sendMessage(Messages.MUST_BE_OWNER);
			else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRANSFER_OTHER);
			return !unconfirmed.containsKey(p.getUniqueId());
		}
		if (account.isOwner(newOwner)) {
			boolean isSelf = Utils.samePlayer(p, newOwner);
			plugin.debug(p.getName() + " is already owner of account");
			p.sendMessage(String.format(Messages.ALREADY_OWNER, isSelf ? "You are" : newOwner.getName() + " is", "account"));
			return false;
		}

		if (Config.confirmOnTransfer) {
			if (!isConfirmed(p, account.getID())) {
				plugin.debug("Needs confirmation");
				p.sendMessage(String.format(Messages.ABOUT_TO_TRANSFER,
						account.isOwner(p) ? "your account" : account.getOwner().getName() + "'s account", newOwner.getName()));
				p.sendMessage(Messages.CLICK_TO_CONFIRM);
				return false;
			}
		} else
			ClickType.removePlayerClickType(p);
		return true;
	}

	private void transfer(Player p, OfflinePlayer newOwner, Account account) {
		plugin.debug(p.getName() + " is transferring account #" + account.getID() + " to the ownership of "
				+ newOwner.getName());

		AccountTransferEvent event = new AccountTransferEvent(p, account, newOwner);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Account transfer event cancelled");
			return;
		}

		boolean hasDefaultNickname = account.isDefaultName();

		p.sendMessage(String.format(Messages.OWNERSHIP_TRANSFERRED, "You",
				Utils.samePlayer(account.getOwner(), p) ? "your account" : p.getName() + "'s account",
				newOwner.getName()));
		account.transferOwnership(newOwner);
		if (hasDefaultNickname)
			account.setToDefaultName();
		plugin.getAccountUtils().addAccount(account, true);
	}

	public static void clearUnconfirmed(OfflinePlayer p) {
		unconfirmed.remove(p.getUniqueId());
	}

}
