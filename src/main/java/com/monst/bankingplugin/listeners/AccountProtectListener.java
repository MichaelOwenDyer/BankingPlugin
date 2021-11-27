package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.account.AccountContractEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.notfound.BankNotFoundException;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.geo.locations.DoubleAccountLocation;
import com.monst.bankingplugin.geo.locations.SingleAccountLocation;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permission;
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

import java.math.BigDecimal;

/**
 * This listener is intended to prevent all physical damage to {@link Account} chests,
 * as well as to ensure accounts always correspond with the size of the chest they reside in.
 */
@SuppressWarnings("unused")
public class AccountProtectListener extends BankingPluginListener {

	private final AccountRepository accountRepo;

	public AccountProtectListener(BankingPlugin plugin) {
        super(plugin);
		this.accountRepo = plugin.getAccountRepository();
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
		plugin.debugf("%s tries to break %s's account #%d", p.getName(), account.getOwnerName(), account.getID());
		if (!(p.isSneaking() && Utils.hasAxeInHand(p)) || (!account.isOwner(p) && Permission.ACCOUNT_REMOVE_OTHER.notOwnedBy(p))) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.CANNOT_BREAK_ACCOUNT.translate());
			return;
		}

		Bank bank = account.getBank();
		if (bank.reimburseAccountCreation().get() && account.isOwner(p) && !bank.isOwner(p)) {
			BigDecimal creationPrice = bank.accountCreationPrice().get();
			// Account owner is reimbursed for the part of the chest that was broken
			if (PayrollOffice.deposit(p, creationPrice))
				p.sendMessage(Message.REIMBURSEMENT_RECEIVED.with(Placeholder.AMOUNT).as(creationPrice).translate());
			// Bank owner reimburses the customer
			if (bank.isPlayerBank() && PayrollOffice.withdraw(bank.getOwner(), creationPrice)) {
				Utils.notify(bank.getOwner(), Message.REIMBURSEMENT_PAID
						.with(Placeholder.PLAYER).as(p.getName())
						.and(Placeholder.AMOUNT).as(creationPrice)
						.translate());
			}
		}

		if (account.isDoubleChest()) {
			DoubleAccountLocation oldLoc = (DoubleAccountLocation) account.getLocation();
			SingleAccountLocation newLoc = oldLoc.contract(b);
			account.setLocation(newLoc);
			accountRepo.update(account, account.callUpdateChestName(), AccountField.LOCATION);
			new AccountContractEvent(p, account).fire();
		} else {
			accountRepo.remove(account, true);
			plugin.debugf("%s broke %s's account #%d", p.getName(), account.getOwnerName(), account.getID());
			p.sendMessage(Message.ACCOUNT_REMOVED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
			new AccountRemoveEvent(p, account).fire();
		}
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
		boolean leftSide = data.getType() == org.bukkit.block.data.type.Chest.Type.LEFT;
		switch (data.getFacing()) {
			case NORTH:
				neighborFacing = leftSide ? BlockFace.EAST : BlockFace.WEST;
				break;
			case EAST:
				neighborFacing = leftSide ? BlockFace.SOUTH : BlockFace.NORTH;
				break;
			case SOUTH:
				neighborFacing = leftSide ? BlockFace.WEST : BlockFace.EAST;
				break;
			case WEST:
				neighborFacing = leftSide ? BlockFace.NORTH : BlockFace.SOUTH;
				break;
			default:
				return;
		}

        Block existingChest = b.getRelative(neighborFacing);
		Account account = accountRepo.getAt(existingChest);
		if (account == null)
			return;

		AccountLocation newLoc = new DoubleAccountLocation(existingChest, neighborFacing.getOppositeFace());

		Bank bank;
		try {
			newLoc.checkSpaceAbove();
			bank = newLoc.findBank(plugin.getBankRepository());
		} catch (ChestBlockedException | BankNotFoundException ex) {
			plugin.debug(ex);
			p.sendMessage(ex.getMessage());
			e.setCancelled(true);
			return;
		}

		plugin.debugf("%s tries to extend %s's account #%d", p.getName(), account.getOwnerName(), account.getID());

		if (!account.isOwner(p) && Permission.ACCOUNT_EXTEND_OTHER.notOwnedBy(p)) {
			e.setCancelled(true);
			p.sendMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_OTHER.translate());
			return;
		}

		AccountExtendEvent event = new AccountExtendEvent(p, account, newLoc);
        event.fire();
		if (event.isCancelled() && Permission.ACCOUNT_CREATE_PROTECTED.notOwnedBy(p)) {
            e.setCancelled(true);
			p.sendMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED.translate());
            return;
        }

		BigDecimal creationPrice = bank.accountCreationPrice().get();
		if (!PayrollOffice.allowPayment(p, creationPrice.negate())) {
			BigDecimal balance = BigDecimal.valueOf(plugin.getEconomy().getBalance(p));
			p.sendMessage(Message.ACCOUNT_EXTEND_INSUFFICIENT_FUNDS
					.with(Placeholder.PRICE).as(creationPrice)
					.and(Placeholder.PLAYER_BALANCE).as(balance)
					.and(Placeholder.AMOUNT_REMAINING).as(creationPrice.subtract(balance))
					.translate());
			e.setCancelled(true);
			return;
		}
		if (creationPrice.signum() > 0 && !bank.isOwner(p)) {
			if (PayrollOffice.withdraw(p, creationPrice)) {
				p.sendMessage(Message.ACCOUNT_EXTEND_FEE_PAID
						.with(Placeholder.PRICE).as(creationPrice)
						.and(Placeholder.BANK_NAME).as(bank.getColorizedName())
						.translate());
			} else {
				e.setCancelled(true);
				return;
			}
			if (bank.isPlayerBank() && PayrollOffice.deposit(bank.getOwner(), creationPrice)) {
				Utils.notify(bank.getOwner(), Message.ACCOUNT_EXTEND_FEE_RECEIVED
						.with(Placeholder.PLAYER).as(account.getOwnerDisplayName())
						.and(Placeholder.AMOUNT).as(creationPrice)
						.and(Placeholder.BANK_NAME).as(bank.getColorizedName())
						.translate());
			}
		}

		account.setLocation(newLoc);
		accountRepo.update(account, account.callUpdateChestName(), AccountField.LOCATION);
    }

	/**
	 * Stops any and all non-player (e.g. hopper) item movement into and out of account chests.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemMove(InventoryMoveItemEvent e) {
        if (e.getInitiator().getType() == InventoryType.PLAYER)
        	return;
        if (e.getSource().getType() == InventoryType.CHEST && accountRepo.isAccount(e.getSource().getLocation().getBlock()))
			e.setCancelled(true);
		else if (e.getDestination().getType() == InventoryType.CHEST && accountRepo.isAccount(e.getDestination().getLocation().getBlock()))
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
		if (!account.isTrusted(executor) && Permission.ACCOUNT_EDIT_OTHER.notOwnedBy(executor)) {
			executor.sendMessage(Message.NO_PERMISSION_ACCOUNT_EDIT_OTHER.translate());
			e.setCancelled(true);
		}
	}
}
