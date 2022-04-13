package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.entity.geo.location.DoubleAccountLocation;
import com.monst.bankingplugin.event.account.AccountCloseEvent;
import com.monst.bankingplugin.event.account.AccountContractEvent;
import com.monst.bankingplugin.event.account.AccountExtendEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.StructureGrowEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.bukkit.block.BlockFace.*;

/**
 * This listener is intended to prevent all physical damage to {@link Account} chests,
 * as well as to ensure accounts always correspond with the size of the chest they reside in.
 */
@SuppressWarnings("unused")
public class AccountProtectListener implements Listener {

	private final BankingPlugin plugin;

	public AccountProtectListener(BankingPlugin plugin) {
		this.plugin = plugin;
    }

	/**
	 * Listen for block break events, and cancel the event if the block in question
	 * is an account chest. Allow the breaking of the account if the player has permission to do so.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onAccountChestBreak(BlockBreakEvent e) {
		Block brokenBlock = e.getBlock();
		Account account = plugin.getAccountService().findAt(brokenBlock);
		if (account == null)
			return;

		Player player = e.getPlayer();
		plugin.debugf("%s tries to break %s's account #%d", player.getName(), account.getOwner().getName(), account.getID());
		if (!(player.isSneaking() && Utils.hasAxeInHand(player) && (account.isOwner(player) || Permission.ACCOUNT_CLOSE_OTHER.ownedBy(player)))) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.CANNOT_BREAK_ACCOUNT_CHEST.translate(plugin));
			return;
		}

		Bank bank = account.getBank();
		if (plugin.config().reimburseAccountCreation.at(bank) && account.isOwner(player) && !bank.isOwner(player)) {
			final double finalCreationPrice = plugin.config().accountCreationPrice.at(bank).doubleValue();
			// Account owner is reimbursed for the part of the chest that was broken
			if (plugin.getPaymentService().deposit(player, finalCreationPrice))
				player.sendMessage(Message.ACCOUNT_REIMBURSEMENT_RECEIVED
						.with(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
						.translate(plugin));
			// Bank owner reimburses the customer
			if (bank.isPlayerBank() && plugin.getPaymentService().withdraw(bank.getOwner(), finalCreationPrice)) {
				Utils.message(bank.getOwner(), Message.ACCOUNT_REIMBURSEMENT_PAID
						.with(Placeholder.PLAYER).as(player.getName())
						.and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
						.translate(plugin));
			}
		}

		if (account.isDoubleChest()) {
			DoubleAccountLocation oldLoc = (DoubleAccountLocation) account.getLocation();
			account.setLocation(oldLoc.contract(brokenBlock));
			account.updateChestTitle();
			plugin.getAccountService().update(account);
			new AccountContractEvent(player, account).fire();
		} else {
			new AccountCloseEvent(player, account).fire();
			bank.removeAccount(account);
			plugin.getAccountService().remove(account);
			plugin.debugf("%s broke %s's account #%d", player.getName(), account.getOwner().getName(), account.getID());
			player.sendMessage(Message.ACCOUNT_CLOSED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
		}
	}

	/**
	 * Listens for block place events, and handles the expansion of a small account chest into a large account chest.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAccountExtend(BlockPlaceEvent e) {
        Block placedBlock = e.getBlockPlaced();
		if (!Utils.isChest(placedBlock))
			return;

		Chest placedChest = (Chest) placedBlock.getState();
		org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) placedChest.getBlockData();

		if (chestData.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE)
			return;

		BlockFace orientationToExisting;
		boolean leftSide = chestData.getType() == org.bukkit.block.data.type.Chest.Type.LEFT;
		switch (chestData.getFacing()) {
			case NORTH:
				orientationToExisting = leftSide ? EAST : WEST; break;
			case EAST:
				orientationToExisting = leftSide ? SOUTH : NORTH; break;
			case SOUTH:
				orientationToExisting = leftSide ? WEST : EAST; break;
			case WEST:
				orientationToExisting = leftSide ? NORTH : SOUTH; break;
			default:
				return;
		}

		Block existingBlock = placedBlock.getRelative(orientationToExisting);
		Account account = plugin.getAccountService().findAt(existingBlock);
		if (account == null)
			return;

		Player player = e.getPlayer();
		if (!Utils.isTransparent(placedBlock.getRelative(BlockFace.UP))) {
			plugin.debug("Chest is blocked");
			player.sendMessage(Message.CHEST_BLOCKED.translate(plugin));
			e.setCancelled(true);
			return;
		}

		AccountLocation newAccountLocation = new DoubleAccountLocation(placedBlock, orientationToExisting);
		Bank bank = plugin.getBankService().findContaining(newAccountLocation);
		if (bank == null) {
			plugin.debug("Chest not in bank");
			player.sendMessage(Message.CHEST_NOT_IN_BANK.translate(plugin));
			e.setCancelled(true);
			return;
		}

		plugin.debugf("%s tries to extend %s's account #%d", player.getName(), account.getOwner().getName(), account.getID());

		if (!account.isOwner(player) && Permission.ACCOUNT_EXTEND_OTHER.notOwnedBy(player)) {
			player.sendMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_OTHER.translate(plugin));
			e.setCancelled(true);
			return;
		}

		try {
			new AccountExtendEvent(player, account, newAccountLocation).fire();
		} catch (CancelledException ex) {
			if (Permission.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player)) {
				player.sendMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED.translate(plugin));
				e.setCancelled(true);
				return;
			}
		}

		BigDecimal creationPrice = plugin.config().accountCreationPrice.at(bank);
		if (creationPrice.signum() > 0 && !bank.isOwner(player)) {
			final double finalCreationPrice = creationPrice.doubleValue();
			if (!plugin.getPaymentService().withdraw(player, finalCreationPrice)) {
				double balance = plugin.getEconomy().getBalance(player);
				player.sendMessage(Message.ACCOUNT_EXTEND_INSUFFICIENT_FUNDS
						.with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
						.and(Placeholder.PLAYER_BALANCE).as(plugin.getEconomy().format(balance))
						.and(Placeholder.AMOUNT_REMAINING).as(plugin.getEconomy().format(creationPrice.subtract(BigDecimal.valueOf(balance)).doubleValue()))
						.translate(plugin));
				e.setCancelled(true);
				return;
			}
			player.sendMessage(Message.ACCOUNT_EXTEND_FEE_PAID
					.with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
					.and(Placeholder.BANK_NAME).as(bank.getColorizedName())
					.translate(plugin));
			if (bank.isPlayerBank() && plugin.getPaymentService().deposit(bank.getOwner(), finalCreationPrice)) {
				Utils.message(bank.getOwner(), Message.ACCOUNT_EXTEND_FEE_RECEIVED
						.with(Placeholder.PLAYER).as(account.getOwner().getName())
						.and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
						.and(Placeholder.BANK_NAME).as(bank.getColorizedName())
						.translate(plugin));
			}
		}

		account.setLocation(newAccountLocation);
		account.updateChestTitle();
		plugin.getAccountService().update(account);
    }

	/**
	 * Stops any and all non-player (e.g. hopper) item movement into and out of account chests.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemMove(InventoryMoveItemEvent e) {
        if (e.getInitiator().getType() == InventoryType.PLAYER)
        	return;
        if (e.getSource().getType() == InventoryType.CHEST && e.getSource().getLocation() != null
				&& plugin.getAccountService().isAccount(e.getSource().getLocation().getBlock()))
			e.setCancelled(true);
		else if (e.getDestination().getType() == InventoryType.CHEST && e.getDestination().getLocation() != null
				&& plugin.getAccountService().isAccount(e.getDestination().getLocation().getBlock()))
			e.setCancelled(true);
    }

	/**
	 * Prevents unauthorized players from editing the items in other players' account chests
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		if (e.getInventory().getType() != InventoryType.CHEST)
			return;
		if (e.getInventory().getLocation() == null)
			return;
		Account account = plugin.getAccountService().findAt(e.getInventory().getLocation().getBlock());
		if (account == null)
			return;
		Player executor = (Player) e.getWhoClicked();
		if (!account.isTrusted(executor) && Permission.ACCOUNT_EDIT_OTHER.notOwnedBy(executor)) {
			executor.sendMessage(Message.NO_PERMISSION_ACCOUNT_EDIT_OTHER.translate(plugin));
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onAccountBlocked(BlockPlaceEvent e) {
		Block block = e.getBlockPlaced();
		if (Utils.isTransparent(block))
			return;
		if (plugin.getAccountService().isAccount(block.getRelative(BlockFace.DOWN)))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
		List<Block> blocks = new LinkedList<>();
		for (BlockState blockState : e.getReplacedBlockStates())
			if (!Utils.isTransparent(blockState.getBlock()))
				// Check if the block below is an account
				blocks.add(blockState.getBlock().getRelative(BlockFace.DOWN));
		if (plugin.getAccountService().isAnyAccount(blocks))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPistonExtend(BlockPistonExtendEvent e) {
		// If the piston did not push any blocks
		Block airAfterPiston = e.getBlock().getRelative(e.getDirection());
		if (plugin.getAccountService().isAccount(airAfterPiston.getRelative(BlockFace.DOWN))) {
			e.setCancelled(true);
			return;
		}

		List<Block> blocks = new ArrayList<>();
		for (Block block : e.getBlocks())
			if (!Utils.isTransparent(block))
				// Check if the block below and in the direction of the piston movement is an account
				blocks.add(block.getRelative(BlockFace.DOWN).getRelative(e.getDirection()));
		if (plugin.getAccountService().isAnyAccount(blocks))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPistonRetract(BlockPistonRetractEvent e) {
		List<Block> blocks = new LinkedList<>();
		for (Block block : e.getBlocks())
			if (!Utils.isTransparent(block))
				// Check if the block below and in the direction of the piston movement is an account
				blocks.add(block.getRelative(BlockFace.DOWN).getRelative(e.getDirection()));
		if (plugin.getAccountService().isAnyAccount(blocks))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onStructureGrow(StructureGrowEvent e) {
		List<Block> blocks = new LinkedList<>();
		for (BlockState state : e.getBlocks()) {
			Block block = state.getBlock();
			blocks.add(block);
			if (!Utils.isTransparent(block))
				blocks.add(block.getRelative(BlockFace.DOWN));
		}
		if (plugin.getAccountService().isAnyAccount(blocks))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBurn(BlockBurnEvent e) {
		if (plugin.getAccountService().isAccount(e.getBlock()))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockGrow(BlockGrowEvent e) {
		Block newBlock = e.getNewState().getBlock();
		if (Utils.isTransparent(newBlock))
			return;
		Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
		if (plugin.getAccountService().isAnyAccount(newBlock, belowNewBlock))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockSpread(BlockSpreadEvent e) {
		Block newBlock = e.getNewState().getBlock();
		Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
		if (plugin.getAccountService().isAnyAccount(newBlock, belowNewBlock))
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		List<Account> accounts = plugin.getAccountService().findAt(e.blockList());
		for (Account account : accounts)
			for (Block block : account.getLocation())
				e.blockList().remove(block);
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		List<Account> accounts = plugin.getAccountService().findAt(e.blockList());
		for (Account account : accounts)
			for (Block block : account.getLocation())
				e.blockList().remove(block);
	}
}
