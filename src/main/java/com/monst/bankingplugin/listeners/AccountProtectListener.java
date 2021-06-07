package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountContractEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.geo.locations.DoubleChestLocation;
import com.monst.bankingplugin.geo.locations.SingleChestLocation;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;

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
		Block b = e.getBlock();
		Account account = accountRepo.getAt(b);
		if (account == null)
			return;

		Player p = e.getPlayer();
		plugin.debugf("%s tries to break %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());
		if (!(p.isSneaking() && Utils.hasAxeInHand(p)) || (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER))) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(LangUtils.getMessage(Message.CANNOT_BREAK_ACCOUNT));
			return;
		}

		if (account.isDoubleChest()) {
			AccountContractEvent event = new AccountContractEvent(p, account);
			event.fire();
			if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_REMOVE_PROTECTED)) {
				e.setCancelled(true);
				p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CONTRACT_PROTECTED));
				return;
			}
		} else {
			AccountRemoveEvent event = new AccountRemoveEvent(p, account);
			event.fire();
			if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_REMOVE_PROTECTED)) {
				e.setCancelled(true);
				p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_REMOVE_PROTECTED));
				return;
			}
		}

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
			DoubleChestLocation oldLoc = (DoubleChestLocation) account.getChestLocation();
			SingleChestLocation newLoc = oldLoc.contract(b);
			account.setChestLocation(newLoc);
			accountRepo.update(account, account.callUpdateChestName(), AccountField.LOCATION);
		} else {
			accountRepo.remove(account, true);
			plugin.debugf("%s broke %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());
			p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_REMOVED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
		}
	}

	/**
	 * Converts a large account chest to a small account chest by removing the account and creating
	 * an identical small account in the remaining chest. Also removes accounts entirely when the
	 * broken account was already a small chest.
	 * @param account the account whose chest was broken
	 * @param b the block where the account is located
	 * @param p the player who broke the account chest
	 */
	private void removeAndCreateSmaller(Account account, Block b, Player p) {

	}

	/**
	 * Listens for block place events, and handles the expansion of a small account chest into a large
	 * account chest.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAccountExtend(BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();

		if (!Utils.isChest(b))
			return;

		org.bukkit.block.data.type.Chest data = (org.bukkit.block.data.type.Chest) b.getState().getBlockData();

		if (data.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE)
			return;

		BlockFace neighborFacing;
		switch (data.getFacing()) {
			case NORTH:
				neighborFacing = data.getType() == org.bukkit.block.data.type.Chest.Type.LEFT ? BlockFace.EAST : BlockFace.WEST;
				break;
			case EAST:
				neighborFacing = data.getType() == org.bukkit.block.data.type.Chest.Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
				break;
			case SOUTH:
				neighborFacing = data.getType() == org.bukkit.block.data.type.Chest.Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
				break;
			case WEST:
				neighborFacing = data.getType() == org.bukkit.block.data.type.Chest.Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
				break;
			default:
				return;
		}

        Block firstChest = b.getRelative(neighborFacing);
		Account account = accountRepo.getAt(firstChest);
		if (account == null)
			return;

		ChestLocation newLoc = new DoubleChestLocation(firstChest, neighborFacing.getOppositeFace());
		Bank bank = newLoc.getBank();
		if (bank == null) {
			plugin.debugf("%s tried to extend %s's account (#%d), but new chest was not in a bank.",
					p.getName(), account.getOwner().getName(), account.getID());
			p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
			e.setCancelled(true);
			return;
		}

		plugin.debugf("%s tries to extend %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());

		AccountExtendEvent event = new AccountExtendEvent(p, account, newLoc);
        event.fire();
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

		if (newLoc.isBlocked()) {
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

		account.setChestLocation(newLoc);
		accountRepo.update(account, account.callUpdateChestName(), AccountField.LOCATION);
    }

	/**
	 * Stops any and all non-player (e.g. hopper) item movement into and out of account chests.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemMove(InventoryMoveItemEvent e) {
        if (e.getInitiator().getType().equals(InventoryType.PLAYER))
        	return;
		if (accountRepo.isAccount(ChestLocation.from(e.getSource().getHolder())))
			e.setCancelled(true);
		else if (accountRepo.isAccount(ChestLocation.from(e.getDestination().getHolder())))
			e.setCancelled(true);
    }

	/**
	 * Prevents unauthorized players from editing the items in other players' account chests
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		Location invLoc = e.getInventory().getLocation();
		if (invLoc == null)
			return;
		Account account = accountRepo.getAt(invLoc.getBlock());
		if (account == null)
			return;
		Player executor = (Player) e.getWhoClicked();
		if (!account.isTrusted(executor) && !executor.hasPermission(Permissions.ACCOUNT_EDIT_OTHER)) {
			executor.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_EDIT_OTHER));
			e.setCancelled(true);
		}
	}
}
