package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.exceptions.AccountNotFoundException;
import com.monst.bankingplugin.exceptions.BankNotFoundException;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.geo.locations.SingleChestLocation;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * This listener is intended to prevent all physical damage to {@link Account} chests,
 * as well as to ensure accounts always correspond with the size of the chest they reside in.
 */
@SuppressWarnings("unused")
public class AccountProtectListener extends BankingPluginListener {

	public AccountProtectListener(BankingPlugin plugin) {
        super(plugin);
    }

	/**
	 * Listen for block break events, and cancel the event if the block in question
	 * is an account chest. Allow the breaking of the account if the player has permission to do so.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onAccountChestBreak(BlockBreakEvent e) {
		Chest c = Utils.getChestAt(e.getBlock());
		if (c == null)
			return;
		ChestLocation chestLocation = ChestLocation.from(c);
		Account account = null;
		try {
			account = accountRepo.getAt(chestLocation);
		} catch (AccountNotFoundException ex) {
			return;
		}

		Player p = e.getPlayer();
		if (p.isSneaking() && Utils.hasAxeInHand(p)) {
			plugin.debugf("%s tries to break %s's account (#%d)",
					p.getName(), account.getOwner().getName(), account.getID());
			if (account.isOwner(p) || p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) {
				removeAndCreateSmaller(account, e.getBlock(), p);
				return;
			}
		}
		e.setCancelled(true);
		e.getPlayer().sendMessage(LangUtils.getMessage(Message.CANNOT_BREAK_ACCOUNT));
	}

	/**
	 * Converts a large account chest to a small account chest by removing the account and creating
	 * an identical small account in the remaining chest. Also removes accounts entirely when the
	 * broken account was already a small chest.
	 * @param account the account whose chest was broken
	 * @param b the block where the account is located
	 * @param p the player who broke the account chest
	 */
	@SuppressWarnings("ConstantConditions")
	private void removeAndCreateSmaller(Account account, Block b, Player p) {
		Bank bank = account.getBank();
		double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
		creationPrice *= bank.get(BankField.REIMBURSE_ACCOUNT_CREATION) ? 1 : 0;

		if (creationPrice > 0 && account.isOwner(p) && !account.getBank().isOwner(p)) {
			double finalCreationPrice = creationPrice;
			String worldName = account.getChestLocation().getWorld() != null ? account.getChestLocation().getWorld().getName() : "world";
			// Account owner is reimbursed for the part of the chest that was broken
			Utils.depositPlayer(p, finalCreationPrice, Callback.of(plugin,
					result -> p.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
							new Replacement(Placeholder.AMOUNT, finalCreationPrice)
					)),
					error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, error::getLocalizedMessage)))
			));

			// Bank owner reimburses the customer
			if (creationPrice > 0 && bank.isPlayerBank() && !bank.isOwner(p)) {
				OfflinePlayer bankOwner = bank.getOwner();
				Utils.withdrawPlayer(bankOwner, finalCreationPrice, Callback.of(plugin,
						result -> Mailman.notify(bankOwner, LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
								new Replacement(Placeholder.PLAYER, p::getName),
								new Replacement(Placeholder.AMOUNT, finalCreationPrice)
						)),
						error -> Mailman.notify(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
								new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
						))
				));
			}
		}

		if (account.isDoubleChest()) {

			Chest chest;
			try {
				chest = Utils.getChestAt(Utils.getAttachedChestBlock(b));
			} catch (ChestNotFoundException e) {
				throw new IllegalStateException("Chest could not be found!");
			}

			Account newAccount = Account.clone(account);
			newAccount.setChestLocation(SingleChestLocation.from(chest));

			accountRepo.remove(account, false, Callback.of(plugin, result -> {
				newAccount.create(true);
				accountRepo.add(newAccount, true, newAccount.callUpdateChestName());
			}));
		} else {
			accountRepo.remove(account, true);
			plugin.debugf("%s broke %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());
			p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_REMOVED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
		}
	}

	/**
	 * Listens for block place events, and handles the expansion of a small account chest into a large
	 * account chest.
	 * {@link Utils#getChestCoordinates(Chest)} performs largely the same task as a good portion of this {@link EventHandler},
	 * but cannot be used since {@link BlockPlaceEvent}s are fired before the {@link org.bukkit.inventory.InventoryHolder}
	 * of the new chest has been updated.
	 * This means that when an account chest is extended and this handler is executed,
	 * for all intents and purposes the account chest is actually still a single chest.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAccountExtend(BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();

		Block otherChest;
		try {
			otherChest = Utils.getAttachedChestBlock(b);
		} catch (ChestNotFoundException ex) {
			return;
		}

		SingleChestLocation chestLocation = SingleChestLocation.from(b.getWorld(), BlockVector3D.fromLocation(otherChest.getLocation()));
		final Account account;
		try {
			account = accountRepo.getAt(chestLocation);
		} catch (AccountNotFoundException ex) {
			return;
		}

		Bank bank;
		try {
			bank = chestLocation.getBank();
		} catch (BankNotFoundException ex) {
			e.setCancelled(true);
			p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
			return;
		}

		ChestLocation newChestLocation = chestLocation.extend(BlockVector3D.fromLocation(b.getLocation()));

		plugin.debugf("%s tries to extend %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());

		AccountExtendEvent event = new AccountExtendEvent(p, account, newChestLocation);
        Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            e.setCancelled(true);
			p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED));
            return;
        }

		if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_EXTEND_OTHER)) {
            e.setCancelled(true);
			p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_OTHER));
            return;
        }

		if (newChestLocation.isBlocked()) {
            e.setCancelled(true);
			p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            return;
		}

		double creationPrice = account.getBank().get(BankField.ACCOUNT_CREATION_PRICE);
		double balance = plugin.getEconomy().getBalance(p);
		if (creationPrice > 0 && creationPrice > balance) {
			p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_EXTEND_INSUFFICIENT_FUNDS,
					new Replacement(Placeholder.PRICE, creationPrice),
					new Replacement(Placeholder.AMOUNT_REMAINING, creationPrice - balance),
					new Replacement(Placeholder.PLAYER_BALANCE, balance)
			));
			e.setCancelled(true);
			return;
		}
		if (creationPrice > 0 && !account.getBank().isOwner(p)) {
			OfflinePlayer owner = p.getPlayer();
			if (!Utils.withdrawPlayer(owner, creationPrice, Callback.of(plugin,
					result -> p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_EXTEND_FEE_PAID,
							new Replacement(Placeholder.PRICE, creationPrice),
							new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
					)),
					error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, error::getLocalizedMessage)))
			))) {
				e.setCancelled(true);
				return;
			}

			if (creationPrice > 0 && !account.getBank().isOwner(p)) {
				OfflinePlayer bankOwner = account.getBank().getOwner();
				Utils.depositPlayer(bankOwner, creationPrice, Callback.of(plugin,
						result -> Mailman.notify(bankOwner, LangUtils.getMessage(Message.ACCOUNT_EXTEND_FEE_RECEIVED,
								new Replacement(Placeholder.PLAYER, () -> account.getOwner().getName()),
								new Replacement(Placeholder.AMOUNT, creationPrice),
								new Replacement(Placeholder.BANK_NAME, () -> account.getBank().getColorizedName())
						)),
						error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
								new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
						))
				));
			}
		}

		final Account newAccount = Account.clone(account);
		newAccount.setChestLocation(newChestLocation);

		accountRepo.remove(account, true, Callback.of(plugin, result -> {
				if (newAccount.create(true)) {
					accountRepo.add(newAccount, true, newAccount.callUpdateChestName());
					plugin.debugf("%s extended %s's account (#%d)",
							p.getName(), account.getOwner().getName(), account.getID());
				} else
					p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
							new Replacement(Placeholder.ERROR, "Failed to create account.")
					));
			})
		);
    }

	/**
	 * Stops any and all non-player (e.g. hopper) item movement into and out of account chests.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemMove(InventoryMoveItemEvent e) {
        if (!e.getInitiator().getType().equals(InventoryType.PLAYER)) {

        	for (Inventory inv : new Inventory[] { e.getSource(), e.getDestination() }) {
        		Chest chest = Utils.getChestHolding(inv);
				if (chest != null && accountRepo.isAccount(ChestLocation.from(chest)))
					e.setCancelled(true);
			}
        }
    }

	/**
	 * Prevents unauthorized players from editing the items in other players' account chests
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		Chest chest = Utils.getChestHolding(e.getInventory());
		if (chest == null)
			return;
		ChestLocation chestLocation = ChestLocation.from(chest);
		Account account;
		try {
			account = accountRepo.getAt(chestLocation);
		} catch (AccountNotFoundException ex) {
			return;
		}
		Player executor = (Player) e.getWhoClicked();
		if (!account.isTrusted(executor) && !executor.hasPermission(Permissions.ACCOUNT_EDIT_OTHER)) {
			executor.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_EDIT_OTHER));
			e.setCancelled(true);
		}
	}
}
