package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.exceptions.AccountNotFoundException;
import com.monst.bankingplugin.exceptions.BankNotFoundException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.geo.locations.SingleChestLocation;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
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
		if (bank.getReimburseAccountCreation().get() && account.isOwner(p) && !bank.isOwner(p)) {
			double creationPrice = bank.getAccountCreationPrice().get();
			// Account owner is reimbursed for the part of the chest that was broken
			if (PayrollOffice.deposit(p, creationPrice))
				p.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
						new Replacement(Placeholder.AMOUNT, creationPrice)
				));
			// Bank owner reimburses the customer
			if (bank.isPlayerBank() && PayrollOffice.withdraw(bank.getOwner(), creationPrice)) {
				Mailman.notify(bank.getOwner(), LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
						new Replacement(Placeholder.PLAYER, p.getName()),
						new Replacement(Placeholder.AMOUNT, creationPrice)
				));
			}
		}

		if (account.isDoubleChest()) {
			Chest chest = Utils.getChestAt(Utils.getAttachedChestBlock(b));
			if (chest == null)
				return;
            Account newAccount = Account.clone(account);
			newAccount.setChestLocation(SingleChestLocation.from(chest));

			accountRepo.remove(account, false, Callback.of(result -> {
				newAccount.create(true);
				accountRepo.update(newAccount, newAccount.callUpdateChestName(), AccountField.LOCATION);
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
        otherChest = Utils.getAttachedChestBlock(b);
        if (otherChest == null)
            return;

        SingleChestLocation chest = SingleChestLocation.from(b.getWorld(), BlockVector3D.fromLocation(otherChest.getLocation()));
		final Account account;
		try {
			account = accountRepo.getAt(chest);
		} catch (AccountNotFoundException ex) {
			return;
		}

		ChestLocation newChestLocation = chest.extend(BlockVector3D.fromLocation(b.getLocation()));
		Bank bank;
		try {
			bank = newChestLocation.getBank();
		} catch (BankNotFoundException ex) {
			plugin.debugf("%s tried to extend %s's account (#%d), but new chest was not in a bank.",
					p.getName(), account.getOwner().getName(), account.getID());
			p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
			e.setCancelled(true);
			return;
		}

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

		double creationPrice = bank.getAccountCreationPrice().get();
		if (!PayrollOffice.allowPayment(p, creationPrice * -1)) {
			double balance = plugin.getEconomy().getBalance(p);
			p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_EXTEND_INSUFFICIENT_FUNDS,
					new Replacement(Placeholder.PRICE, creationPrice),
					new Replacement(Placeholder.AMOUNT_REMAINING, creationPrice - balance),
					new Replacement(Placeholder.PLAYER_BALANCE, balance)
			));
			e.setCancelled(true);
			return;
		}
		if (creationPrice > 0 && !bank.isOwner(p)) {
			if (PayrollOffice.withdraw(p, creationPrice))
				p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_EXTEND_FEE_PAID,
						new Replacement(Placeholder.PRICE, creationPrice),
						new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
				));
			else {
				e.setCancelled(true);
				return;
			}
			if (bank.isPlayerBank() && PayrollOffice.deposit(bank.getOwner(), creationPrice)) {
				Mailman.notify(bank.getOwner(), LangUtils.getMessage(Message.ACCOUNT_EXTEND_FEE_RECEIVED,
						new Replacement(Placeholder.PLAYER, account::getOwnerName),
						new Replacement(Placeholder.AMOUNT, creationPrice),
						new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
				));
			}
		}

		Account newAccount = Account.clone(account);
		newAccount.setChestLocation(newChestLocation);

		accountRepo.remove(account, true, Callback.of(result -> {
				if (newAccount.create(true)) {
					accountRepo.update(newAccount, newAccount.callUpdateChestName(), AccountField.LOCATION);
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
