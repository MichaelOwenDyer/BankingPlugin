package com.monst.bankingplugin.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountInfoEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.ItemUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class AccountInteractListener implements Listener {
	
	private static Map<UUID, Set<Integer>> unconfirmed = new HashMap<>();

	private BankingPlugin plugin;
	private AccountUtils accountUtils;
	private BankUtils bankUtils;

	public AccountInteractListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
		this.bankUtils = plugin.getBankUtils();
	}

	/**
	 * Prevents unauthorized players from editing other players' accounts
	 * 
	 * @param InventoryClickEvent e
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		
		Inventory chestInv = e.getInventory();

		if (!(chestInv.getHolder() instanceof Chest || chestInv.getHolder() instanceof DoubleChest)) {
			return;
		}

		Location loc = null;
		if (chestInv.getHolder() instanceof Chest) {
			loc = ((Chest) chestInv.getHolder()).getLocation();
		} else if (chestInv.getHolder() instanceof DoubleChest) {
			loc = ((DoubleChest) chestInv.getHolder()).getLocation();
		}

		final Account account = plugin.getAccountUtils().getAccount(loc);
		if (account == null)
			return;
		OfflinePlayer owner = account.getOwner();
		if (owner.getUniqueId().equals(e.getWhoClicked().getUniqueId()))
			return;
		if (e.getWhoClicked() instanceof Player) {
			Player executor = (Player) e.getWhoClicked();
			if (!executor.hasPermission(Permissions.ACCOUNT_EDIT_OTHER)) {
				plugin.debug(executor.getName() + " does not have permission to edit " + owner.getName() + "'s account"); 
				executor.sendMessage(Messages.getWithValue(Messages.NO_PERMISSION_ACCOUNT_EDIT_OTHER, owner.getName()));
				e.setCancelled(true);
			}
				
		}
	}

	/**
	 * Checks every inventory interact event for an account create attempt, and
	 * handles the creation.
	 * 
	 * @param PlayerInteractEvent
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onInteractCreate(PlayerInteractEvent e) {
		
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		Account account = accountUtils.getAccount(b.getLocation());
		ClickType clickType = ClickType.getPlayerClickType(p);

		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST))
			return;

		if (clickType != null) {
			if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK))
				return;

			switch (clickType.getClickType()) {
			case CREATE:
				if (e.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
					p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
					plugin.debug(p.getName() + " does not have permission to create an account on the selected chest.");
				} else
					tryCreate(p, b);
				ClickType.removePlayerClickType(p);
				break;
			case REMOVE:
				if (confirmRemove(p, account))
					ClickType.removePlayerClickType(p);
				break;
			case INFO:
				info(p, account);
				ClickType.removePlayerClickType(p);
				break;
			}
			e.setCancelled(true);
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
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				e.setCancelled(true); // peek method handles the chest opening instead
				tryPeek(p, account, true);
			}
		}
		ClickType.removePlayerClickType(p);
	}

	/**
	 * Create a new account
	 *
	 * @param executor Player, who executed the command, will receive the message
	 *                 and become the owner of the account
	 * @param location Where the account will be located
	 */
	private void tryCreate(final Player p, final Block b) {

		if (accountUtils.isAccount(b.getLocation())) {
			p.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
			plugin.debug("Chest is already an account.");
			return;
		}
		if (!ItemUtils.isTransparent(b.getRelative(BlockFace.UP))) {
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

		double creationPrice = Config.creationPriceAccount;
		Bank bank = bankUtils.getBank(location);
		Account account = new Account(plugin, p, bank, location);

		AccountCreateEvent event = new AccountCreateEvent(p, account, creationPrice);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
			plugin.debug("No permission to create account on a protected chest.");
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
			return;
		}

		if (creationPrice > 0) {
			OfflinePlayer player = p.getPlayer();
			EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, location.getWorld().getName(),
					creationPrice);

			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				p.sendMessage(Messages.ERROR_OCCURRED);
				return;
			}
		}

		if (account.create(true)) {
			plugin.debug("Account created");
			accountUtils.addAccount(account, true);
			p.sendMessage(Messages.ACCOUNT_CREATED);
		}

	}
	
	private boolean confirmRemove(Player executor, Account account) {
		if (!executor.getUniqueId().equals(account.getOwner().getUniqueId())
				&& !executor.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) {
			executor.sendMessage(
					Messages.getWithValue(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER, account.getOwner().getName()));
			return !unconfirmed.containsKey(executor.getUniqueId());
		}
		
		boolean confirmed = unconfirmed.containsKey(executor.getUniqueId()) 
				&& unconfirmed.get(executor.getUniqueId()).contains(account.getID());
		
		if (!confirmed && Config.confirmOnRemove) {
			plugin.debug("Needs confirmation");
	        executor.sendMessage(Messages.CLICK_TO_CONFIRM);
			Set<Integer> ids = unconfirmed.containsKey(executor.getUniqueId())
					? unconfirmed.get(executor.getUniqueId())
					: new HashSet<>();
	        ids.add(account.getID());
	        unconfirmed.put(executor.getUniqueId(), ids);
			return false;
		} else {
	        tryRemove(executor, account);
			if (Config.confirmOnRemove) {
				Set<Integer> ids = unconfirmed.containsKey(executor.getUniqueId())
						? unconfirmed.get(executor.getUniqueId())
						: new HashSet<>();
                ids.remove(account.getID());
				if (ids.isEmpty())
					unconfirmed.remove(executor.getUniqueId());
				else
					unconfirmed.put(executor.getUniqueId(), ids);
			}
			return true;
		}
	}

	/**
	 * Remove a account
	 * 
	 * @param executor Player, who executed the command and will receive the message
	 * @param account  Account to be removed
	 */
	private void tryRemove(Player executor, Account account) {

		if (account.getBalance().signum() == 1) {
			executor.sendMessage(Messages.ACCOUNT_BALANCE_NOT_ZERO);
			return;
		}

		plugin.debug(executor.getName() + " is removing " + account.getOwner().getName() + "'s account (#"
				+ account.getID() + ")");
		AccountRemoveEvent event = new AccountRemoveEvent(executor, account);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			plugin.debug("Remove event cancelled (#" + account.getID() + ")");
			return;
		}

		double creationPrice = Config.creationPriceAccount;
		if (creationPrice > 0 && Config.reimburseAccountCreation
				&& executor.getUniqueId().equals(account.getOwner().getUniqueId())) {
			OfflinePlayer owner = executor.getPlayer();
			EconomyResponse r = plugin.getEconomy().depositPlayer(owner, account.getLocation().getWorld().getName(),
					creationPrice);

			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				executor.sendMessage(Messages.ERROR_OCCURRED);
			} else {
				executor.sendMessage(Messages.ACCOUNT_REMOVED_REFUND);
			}
		} else {
			executor.sendMessage(Messages.ACCOUNT_REMOVED);
		}

		accountUtils.removeAccount(account, true);
		plugin.debug("Removed account (#" + account.getID() + ")");
	}

	/**
	 * Look into an account
	 * 
	 * @param executor Player, who executed the command and will receive the message
	 * @param account  Account to be opened
	 * @param message  Whether the player should receive the
	 *                 {@link Message#ACCOUNT_OPENED} message
	 */
	private void tryPeek(Player executor, Account account, boolean message) {
		boolean executorIsOwner = executor.getUniqueId().equals(account.getOwner().getUniqueId());
		if (!executorIsOwner && !executor.hasPermission(Permissions.ACCOUNT_VIEW_OTHER)) {
			executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_VIEW_OTHER);
			plugin.debug(executor.getName() + " does not have permission to open " + account.getOwner().getName()
					+ "'s account chest.");
			return;
		}

		if (executorIsOwner)
			plugin.debug(executor.getName() + " is opening their account (#" + account.getID() + ")");
		else
			plugin.debug(executor.getName() + " is opening " + account.getOwner().getName() + "'s account (#"
					+ account.getID() + ")");

		executor.openInventory(account.getInventoryHolder().getInventory());

		if (message && !executorIsOwner)
			executor.sendMessage(Messages.getWithValue(Messages.ACCOUNT_OPENED, account.getOwner().getName()));
	}

	/**
	 *
	 * @param executor Player, who executed the command and will retrieve the
	 *                 information
	 * @param account  Account from which the information will be retrieved
	 */
	private void info(Player executor, Account account) {
		boolean executorIsOwner = executor.getUniqueId().equals(account.getOwner().getUniqueId());
		if (!executorIsOwner && !executor.hasPermission(Permissions.ACCOUNT_INFO_OTHER)) {
			executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_INFO_OTHER);
			return;
		}

		if (executorIsOwner)
			plugin.debug(executor.getName() + " is retrieving their account info (#" + account.getID() + ")");
		else
			plugin.debug(executor.getName() + " is retrieving " + account.getOwner().getName() + "'s account info (#"
					+ account.getID() + ")");

		AccountInfoEvent event = new AccountInfoEvent(executor, account);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			plugin.debug("Info event cancelled (#" + account.getID() + ")");
			return;
		}

		String ownerName = account.getOwner().getName();
		if (ownerName == null)
			ownerName = account.getOwner().getUniqueId().toString();

		String bank = Messages.ACCOUNT_INFO_BANK + account.getBank().getName();
		String owner = Messages.ACCOUNT_INFO_OWNER + ChatColor.GOLD + ownerName;
		// TODO: Format balance nicely?
		String balance = Messages.ACCOUNT_INFO_BALANCE + ChatColor.GREEN + account.getBalance().toString();
		String multiplier = Messages.ACCOUNT_INFO_MULTIPLIER + ChatColor.GREEN
				+ account.getStatus().getRealMultiplier() + ChatColor.GRAY + " (Stage "
				+ account.getStatus().getMultiplierStage() + " of " + Config.interestMultipliers.size() + ")";
		String id = Messages.ACCOUNT_INFO_ID + ChatColor.DARK_RED + account.getID();

		executor.sendMessage(" ");
		executor.sendMessage(bank);
		if (!executorIsOwner)
			executor.sendMessage(owner);
		executor.sendMessage(balance);
		if (Config.enableInterestMultipliers)
			executor.sendMessage(multiplier);
		executor.sendMessage(id);
		executor.sendMessage(" ");
	}

}
