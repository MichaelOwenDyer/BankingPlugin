package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
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


public class AccountInteractListener implements Listener {
	
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
	@SuppressWarnings("deprecation")
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
					|| clickType.getClickType() == ClickType.EnumClickType.MIGRATE))
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
				} else
					create(p, b);
				ClickType.removePlayerClickType(p);
				e.setCancelled(true);
				break;

			case REMOVE:

				if (confirmRemove(p, account))
					remove(p, account);
				e.setCancelled(true);
				break;

			case INFO:

				info(p, account);
				ClickType.removePlayerClickType(p);
				e.setCancelled(true);
				break;

			case SET:

				SetClickType.SetClickTypeField field = ((SetClickType) clickType).getField();
				String value = ((SetClickType) clickType).getValue();
				set(p, account, field, value);
				ClickType.removePlayerClickType(p);
				e.setCancelled(true);
				break;

			case TRUST:

				OfflinePlayer playerToTrust = ((TrustClickType) clickType).getPlayerToTrust();
				trust(p, account, playerToTrust);
				ClickType.removePlayerClickType(p);
				e.setCancelled(true);
				break;

			case UNTRUST:

				OfflinePlayer playerToUntrust = ((UntrustClickType) clickType).getPlayerToUntrust();
				untrust(p, account, playerToUntrust);
				ClickType.removePlayerClickType(p);
				e.setCancelled(true);
				break;

			case MIGRATE:
				if (((MigrateClickType) clickType).isFirstClick()) {
					migratePartOne(p, account);
				} else {
					if (e.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
						p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED);
						plugin.debug(p.getName() + " does not have permission to migrate an account to a protected chest.");
					} else
						migratePartTwo(p, b, ((MigrateClickType) clickType).getAccount());
					ClickType.removePlayerClickType(p);
				}
				e.setCancelled(true);
				break;

			case TRANSFER:
				OfflinePlayer newOwner = ((TransferClickType) clickType).getNewOwner();
				if (confirmTransfer(p, newOwner, account))
					transfer(p, newOwner, account);
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

				plugin.debug(String.format(p.getName() + " is opening %s account%s (#" + account.getID() + ")",
						(account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"),
						(account.isCoowner(p) ? " as a co-owner" : "")));
			}
		}
	}

	/**
	 * Create a new account
	 *
	 * @param p Player who executed the command will receive the message
	 *                 and become the owner of the account
	 * @param b Block where the account will be located
	 */
	private void create(final Player p, final Block b) {

		if (accountUtils.isAccount(b.getLocation())) {
			p.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
			plugin.debug("Chest is already an account.");
			return;
		}
		if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
			p.sendMessage(Messages.CHEST_BLOCKED);
			plugin.debug("Chest is blocked.");
			return;
		}
		if (!bankUtils.isBank(b.getLocation())) {
			p.sendMessage(Messages.CHEST_NOT_IN_BANK);
			plugin.debug("Chest is not in a bank.");
			plugin.debug(p.getName() + " is creating new account...");
			return;
		}
		if (!p.hasPermission(Permissions.ACCOUNT_CREATE)) {
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE);
			plugin.debug(p.getName() + " is not permitted to create the account");
			return;
		}

		Location location = b.getLocation();
		Bank bank = bankUtils.getBank(location);

		if (!Config.allowSelfBanking && bank.isOwner(p)) {
			p.sendMessage(Messages.NO_SELF_BANKING);
			plugin.debug(p.getName() + " is not permitted to create an account at their own bank");
			return;
		}

		int playerAccountLimit = bank.getAccountConfig().get(AccountConfig.Field.PLAYER_BANK_ACCOUNT_LIMIT);
		if (playerAccountLimit > 0 && bank.getAccounts().stream().filter(account -> account.isOwner(p)).count() >= playerAccountLimit) {
			p.sendMessage(Messages.PER_BANK_ACCOUNT_LIMIT_REACHED);
			plugin.debug(p.getName() + " is not permitted to create another account at bank " + bank.getName());
			return;
		}

		Account account = new Account(plugin, p, bank, location);
		
		AccountCreateEvent event = new AccountCreateEvent(p, account);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
			plugin.debug("No permission to create account on a protected chest.");
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
			return;
		}

		double creationPrice = bank.getAccountConfig().get(AccountConfig.Field.ACCOUNT_CREATION_PRICE);
		creationPrice *= ((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1;

		if (creationPrice > 0 && creationPrice > plugin.getEconomy().getBalance(p) && !bank.isOwner(p)) {
			p.sendMessage(Messages.ACCOUNT_CREATE_INSUFFICIENT_FUNDS);
			return;
		}

		OfflinePlayer accountOwner = p.getPlayer();
		double finalCreationPrice = creationPrice;
		// Account owner pays the bank owner the creation fee
		if (!Utils.withdrawPlayer(accountOwner, location.getWorld().getName(), creationPrice, new Callback<Void>(plugin) {
			@Override
			public void onResult(Void result) {
				p.sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_PAID,
						Utils.format(finalCreationPrice)));
			}
			@Override
			public void onError(Throwable throwable) {
				super.onError(throwable);
				p.sendMessage(Messages.ERROR_OCCURRED);
			}
		}))
			return;

		// Bank owner receives the payment from the customer
		if (creationPrice > 0 && bank.isPlayerBank() && !bank.isOwner(p)) {
			OfflinePlayer bankOwner = account.getBank().getOwner();
			Utils.depositPlayer(bankOwner, location.getWorld().getName(), creationPrice, new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					if (bankOwner.isOnline())
						bankOwner.getPlayer().sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_RECEIVED,
								accountOwner.getName(), Utils.format(finalCreationPrice)));
				}
				@Override
				public void onError(Throwable throwable) {
					super.onError(throwable);
					p.sendMessage(Messages.ERROR_OCCURRED);
				}
			});
		}

		if (account.create(true)) {
			plugin.debug("Account created");
			accountUtils.addAccount(account, true, new Callback<Integer>(plugin) {
				@Override
				public void onResult(Integer result) {
					account.setToDefaultName();
				}
			});
			p.sendMessage(Messages.ACCOUNT_CREATED);
		}
	}
	
	private boolean confirmRemove(Player executor, Account account) {
		if (!account.isOwner(executor) && !executor.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)
				&& !account.getBank().isTrusted(executor)) {
			if (account.isTrusted(executor))
				executor.sendMessage(Messages.MUST_BE_OWNER);
			else
				executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
			return !unconfirmed.containsKey(executor.getUniqueId());
		}
		
		boolean confirmed = unconfirmed.containsKey(executor.getUniqueId()) 
				&& unconfirmed.get(executor.getUniqueId()).contains(account.getID());
		
		if (!confirmed && (Config.confirmOnRemove || account.getBalance().signum() == 1)) {
			plugin.debug("Needs confirmation");

			if (account.getBalance().signum() == 1) {
				executor.sendMessage(Messages.ACCOUNT_BALANCE_NOT_ZERO);
			}
	        executor.sendMessage(Messages.CLICK_TO_CONFIRM);
			Set<Integer> ids = unconfirmed.containsKey(executor.getUniqueId())
					? unconfirmed.get(executor.getUniqueId())
					: new HashSet<>();
	        ids.add(account.getID());
	        unconfirmed.put(executor.getUniqueId(), ids);
			return false;
		} else {
			Set<Integer> ids = unconfirmed.containsKey(executor.getUniqueId()) ? unconfirmed.get(executor.getUniqueId())
					: new HashSet<>();
			ids.remove(account.getID());
			if (ids.isEmpty()) {
				unconfirmed.remove(executor.getUniqueId());
				ClickType.removePlayerClickType(executor);
			} else
				unconfirmed.put(executor.getUniqueId(), ids);
			return true;
		}
	}

	/**
	 * Remove a account
	 * 
	 * @param executor Player, who executed the command and will receive the message
	 * @param account  Account to be removed
	 */
	private void remove(Player executor, Account account) {
		plugin.debug(executor.getName() + String.format(" is removing %s account (#", 
				account.isOwner(executor) ? "their" 
				: account.getOwner().getName() + "'s") + account.getID() + ")");
		AccountRemoveEvent event = new AccountRemoveEvent(executor, account);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			plugin.debug("Remove event cancelled (#" + account.getID() + ")");
			return;
		}
		
		AccountConfig accountConfig = account.getBank().getAccountConfig();
		double creationPrice = accountConfig.get(AccountConfig.Field.ACCOUNT_CREATION_PRICE);
		creationPrice *= account.getSize();
		creationPrice *= accountConfig.get(AccountConfig.Field.REIMBURSE_ACCOUNT_CREATION) ? 1 : 0;

		if (creationPrice > 0 && account.isOwner(executor) && !account.getBank().isOwner(executor)) {

			double finalCreationPrice = creationPrice;
			Utils.depositPlayer(executor.getPlayer(), account.getLocation().getWorld().getName(), finalCreationPrice, new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					executor.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED, Utils.format(finalCreationPrice)));
				}
				@Override
				public void onError(Throwable throwable) {
					super.onError(throwable);
					executor.sendMessage(Messages.ERROR_OCCURRED);
				}
			});

			if (account.getBank().isPlayerBank()) {
				OfflinePlayer bankOwner = account.getBank().getOwner();
				Utils.withdrawPlayer(bankOwner, account.getLocation().getWorld().getName(), finalCreationPrice, new Callback<Void>(plugin) {
					@Override
					public void onResult(Void result) {
						if (!account.isOwner(bankOwner) && bankOwner.isOnline())
							bankOwner.getPlayer().sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
									account.getOwner().getName(), Utils.format(finalCreationPrice)));
					}
					@Override
					public void onError(Throwable throwable) {
						super.onError(throwable);
						if (bankOwner.isOnline())
							bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					}
				});
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
		plugin.debug(String.format(player.getName() + " is retrieving %s account info%s (#" + account.getID() + ")",
				(account.isOwner(player) ? "their" : account.getOwner().getName() + "'s"),
				(account.isCoowner(player) ? " as a co-owner" : "")));

		AccountInfoEvent event = new AccountInfoEvent(player, account);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Info event cancelled (#" + account.getID() + ")");
			return;
		}

		new AccountGui(account).open(player);
	}

	private void set(Player executor, Account account, SetClickType.SetClickTypeField field, String value) {

		switch (field) {
			case NICKNAME:
				if (!(account.isTrusted(executor) || executor.hasPermission(Permissions.ACCOUNT_SET_NICKNAME_OTHER))) {
					plugin.debug(executor.getName() + " does not have permission to change another player's account nickname");
					executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME_OTHER);
					return;
				}

				if (value.isEmpty()) {
					plugin.debug(String.format(executor.getName() + " has reset %s account nickname%s (#" + account.getID() + ")",
							(account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s"),
							(account.isCoowner(executor) ? " as a co-owner" : "")));
					account.setToDefaultName();
				} else {
					plugin.debug(String.format(executor.getName() + " has set their account nickname to \"%s\" (#%d)", value, account.getID()));
					account.setName(value);
				}

				plugin.getAccountUtils().addAccount(account, true);
				executor.sendMessage(Messages.NICKNAME_SET);
				break;

			case MULTIPLIER:
				if (!executor.hasPermission(Permissions.ACCOUNT_SET_MULTIPLIER)) {
					plugin.debug(executor.getName() + " does not have permission to change " + account.getOwner().getName()
							+ "'s account multiplier");
					executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
					return;
				}

				int stage = 0;
				if (value.startsWith("+"))
					stage = account.getStatus().setMultiplierStageRelative(Integer.parseInt(value.substring(1)));
				else if (value.startsWith("-"))
					stage = account.getStatus().setMultiplierStageRelative(Integer.parseInt(value.substring(1)) * -1);
				else
					stage = account.getStatus().setMultiplierStage(Integer.parseInt(value));

				plugin.getAccountUtils().addAccount(account, true);
				executor.sendMessage(String.format(Messages.MULTIPLIER_SET, account.getStatus().getRealMultiplier()));
				plugin.debug(String.format(
						executor.getName() + " has set an account multiplier stage to %d (#" + account.getID() + ")",
						stage, (account.isCoowner(executor) ? " as a co-owner" : "")));
				break;

			case DELAY:
				if (!executor.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
					executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
					return;
				}

				int delay;
				if (value.startsWith("+"))
					delay = account.getStatus().setInterestDelayRelative(Integer.parseInt(value.substring(1)));
				else if (value.startsWith("-"))
					delay = account.getStatus().setInterestDelayRelative(Integer.parseInt(value.substring(1)) * -1);
				else
					delay = account.getStatus().setInterestDelay(Integer.parseInt(value));
				plugin.getAccountUtils().addAccount(account, true);
				plugin.debug(String.format(executor.getName()
						+ " has set the interest delay of account #%d to %d.", account.getID(), delay));
				executor.sendMessage(Messages.INTEREST_DELAY_SET);
		}
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
		plugin.debug(String.format(p.getName() + " has trusted %s to %s account (#%d)",
				playerToTrust.getName(), account.isOwner(p) ? "their" : account.getOwner().getName() + "'s",
				account.getID()));

		if (account.isTrusted(playerToTrust)) {
			plugin.debug(playerToTrust.getName() + " was already trusted on that account (#" + account.getID() + ")");
			p.sendMessage(String.format(Messages.ALREADY_A_COOWNER, playerToTrust.getName()));
			return;
		}

		p.sendMessage(String.format(Messages.ADDED_COOWNER, playerToTrust.getName()));
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
		plugin.debug(String.format(p.getName() + " has untrusted %s from %s account (#%d)",
				playerToUntrust.getName(), account.isOwner(p) ? "their" : account.getOwner().getName() + "'s",
				account.getID()));

		if (!account.isTrusted(playerToUntrust)) {
			plugin.debug(playerToUntrust.getName() + " was not trusted on that account and could not be removed (#"
					+ account.getID() + ")");
			p.sendMessage(String.format(Messages.NOT_A_COOWNER, playerToUntrust.getName()));
			return;
		}

		p.sendMessage(String.format(Messages.REMOVED_COOWNER, playerToUntrust.getName()));
		account.untrustPlayer(playerToUntrust);
	}

	private void migratePartOne(Player p, Account toMigrate) {

		if (!toMigrate.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_OTHER)) {
			if (toMigrate.isTrusted(p)) {
				plugin.debug(p.getName() + " cannot migrate account #" + toMigrate.getID() + " as a coowner");
				p.sendMessage(Messages.MUST_BE_OWNER);
				return;
			}
			plugin.debug(p.getName() + " does not have permission to migrate account #" + toMigrate.getID());
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER);
			return;
		}
		plugin.debug(p.getName() + " wants to migrate account #" + toMigrate.getID());
		ClickType.setPlayerClickType(p, new MigrateClickType(toMigrate));
		p.sendMessage(Messages.CLICK_CHEST_MIGRATE_SECOND);

	}
	
	private void migratePartTwo(Player p, Block b, Account toMigrate) {
		Location location = b.getLocation();
		if (accountUtils.isAccount(location)) {
			if (toMigrate.equals(accountUtils.getAccount(location))) {
				plugin.debug(p.getName() + " clicked the same chest to migrate to");
				p.sendMessage(Messages.SAME_ACCOUNT);
				return;
			}
			plugin.debug(p.getName() + " clicked an already existing account chest to migrate to");
			p.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
			return;
		}

		if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
			p.sendMessage(Messages.CHEST_BLOCKED);
			plugin.debug("Chest is blocked.");
			return;
		}
		if (!bankUtils.isBank(location)) {
			p.sendMessage(Messages.CHEST_NOT_IN_BANK);
			plugin.debug("Chest is not in a bank.");
			return;
		}
		if (!toMigrate.getBank().equals(bankUtils.getBank(location))
				&& !p.hasPermission(Permissions.ACCOUNT_MIGRATE_BANK)) {
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE_BANK);
			plugin.debug(p.getName() + " does not have permission to migrate their account to another bank.");
			return;
		}

		Bank oldBank = toMigrate.getBank();
		Bank newBank = bankUtils.getBank(location); // May or may not be the same as oldBank

		Account newAccount = new Account(toMigrate.getID(), plugin, toMigrate.getOwner(),
				toMigrate.getCoowners(), newBank, location, toMigrate.getStatus(),
				toMigrate.getRawName(), toMigrate.getBalance(), toMigrate.getPrevBalance());

		AccountMigrateEvent event = new AccountMigrateEvent(p, newAccount, location);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
			plugin.debug("No permission to create account on a protected chest.");
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
			return;
		}

		double creationPrice = newBank.getAccountConfig().get(AccountConfig.Field.ACCOUNT_CREATION_PRICE);
		creationPrice *= (((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1);
		creationPrice *= (newBank.isOwner(p) ? 0 : 1);

		AccountConfig oldConfig = oldBank.getAccountConfig();
		double reimbursement = oldConfig.get(AccountConfig.Field.REIMBURSE_ACCOUNT_CREATION)
				? oldConfig.get(AccountConfig.Field.ACCOUNT_CREATION_PRICE) : 0.0d;
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
			Utils.depositPlayer(p.getPlayer(), toMigrate.getLocation().getWorld().getName(),
					finalReimbursement, new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							p.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED,
									Utils.format(finalReimbursement)));
						}
						@Override
						public void onError(Throwable throwable) {
							super.onError(throwable);
							p.sendMessage(Messages.ERROR_OCCURRED);
						}
					});
		}

		// Bank owner of new account receives account creation fee
		if (finalCreationPrice > 0 && newBank.isPlayerBank() && !newBank.isOwner(p)) {
			OfflinePlayer bankOwner = newBank.getOwner();
			Utils.depositPlayer(bankOwner, toMigrate.getLocation().getWorld().getName(),
					finalCreationPrice, new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (bankOwner.isOnline())
								bankOwner.getPlayer().sendMessage(String.format(
										Messages.ACCOUNT_CREATE_FEE_RECEIVED,
										Utils.format(finalCreationPrice)
								));
						}
						@Override
						public void onError(Throwable throwable) {
							super.onError(throwable);
							if (bankOwner.isOnline())
								bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
						}
					});
		}

		// Account owner pays creation fee for new account
		if (creationPrice > 0 && !newBank.isOwner(p)) {
			if (!Utils.withdrawPlayer(p, location.getWorld().getName(), finalCreationPrice, new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							p.sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_PAID,
									Utils.format(finalCreationPrice)));
						}
						@Override
						public void onError(Throwable throwable) {
							plugin.debug(throwable);
							p.sendMessage(Messages.ERROR_OCCURRED);
						}
					}))
				return;
		}

		// Bank owner of old account pays reimbursement
		if (reimbursement > 0 && oldBank.isPlayerBank() && !oldBank.isOwner(p)) {
			OfflinePlayer bankOwner = oldBank.getOwner();
			Utils.withdrawPlayer(bankOwner, location.getWorld().getName(),
					finalReimbursement, new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (bankOwner.isOnline())
								bankOwner.getPlayer().sendMessage(String.format(
										Messages.ACCOUNT_REIMBURSEMENT_PAID,
										p.getName(), Utils.format(finalReimbursement)
								));
						}
						@Override
						public void onError(Throwable throwable) {
							super.onError(throwable);
							if (bankOwner.isOnline())
								bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
						}
					});
		}

		if (newAccount.create(true)) {
			plugin.debug("Account migrated (#" + newAccount.getID() + ")");
			accountUtils.removeAccount(toMigrate, false); // Database entry is preserved
			accountUtils.addAccount(newAccount, true, new Callback<Integer>(plugin) { // Database entry is overwritten with new location
				@Override
				public void onResult(Integer result) {
					newAccount.updateName();
				}
			});
			p.sendMessage(Messages.ACCOUNT_MIGRATED);
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
			boolean isSelf = p.getUniqueId().equals(newOwner.getUniqueId());
			plugin.debug(p.getName() + " is already owner of account");
			p.sendMessage(
					String.format(Messages.ALREADY_OWNER_ACCOUNT, isSelf ? "You" : newOwner.getName(), isSelf ? "are" : "is"));
			return false;
		}

		boolean confirmed = unconfirmed.containsKey(p.getUniqueId())
				&& unconfirmed.get(p.getUniqueId()).contains(account.getID());

		if (!confirmed && Config.confirmOnTransfer) {
			plugin.debug("Needs confirmation");

			p.sendMessage(String.format(Messages.ABOUT_TO_TRANSFER,
					account.isOwner(p) ? "your account" : account.getOwner().getName() + "'s account", newOwner.getName()));
			p.sendMessage(Messages.CLICK_TO_CONFIRM);
			Set<Integer> ids = unconfirmed.containsKey(p.getUniqueId()) ? unconfirmed.get(p.getUniqueId())
					: new HashSet<>();
			ids.add(account.getID());
			unconfirmed.put(p.getUniqueId(), ids);
			return false;
		} else {
			Set<Integer> ids = unconfirmed.containsKey(p.getUniqueId()) ? unconfirmed.get(p.getUniqueId())
					: new HashSet<>();
			ids.remove(account.getID());
			if (ids.isEmpty()) {
				unconfirmed.remove(p.getUniqueId());
				ClickType.removePlayerClickType(p);
			} else
				unconfirmed.put(p.getUniqueId(), ids);
			return true;
		}
	}

	private void transfer(Player p, OfflinePlayer newOwner, Account account) {
		plugin.debug(p.getName() + " is transferring account #" + account.getID() + " to the ownership of "
				+ newOwner.getName());

		TransferOwnershipEvent event = new TransferOwnershipEvent(p, account, newOwner);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Account transfer ownership event cancelled");
			return;
		}

		boolean hasDefaultNickname = account.isDefaultName();

		p.sendMessage(String.format(Messages.OWNERSHIP_TRANSFERRED, newOwner.getName()));
		account.transferOwnership(newOwner);
		if (hasDefaultNickname)
			account.setToDefaultName();
		plugin.getAccountUtils().addAccount(account, true);
	}

	public static void clearUnconfirmed(OfflinePlayer p) {
		unconfirmed.remove(p.getUniqueId());
	}
}
