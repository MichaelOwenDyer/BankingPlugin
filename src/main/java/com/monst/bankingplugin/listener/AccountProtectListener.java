package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.entity.geo.location.DoubleAccountLocation;
import com.monst.bankingplugin.event.account.AccountCloseEvent;
import com.monst.bankingplugin.event.account.AccountContractEvent;
import com.monst.bankingplugin.event.account.AccountExtendEvent;
import com.monst.bankingplugin.event.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		Account account = plugin.getAccountService().findAtChest(brokenBlock);
		if (account == null)
			return;

		Player player = e.getPlayer();
		plugin.debug("%s tries to break account %s", player.getName(), account);
  
		boolean canBreak = player.isSneaking();
        if (canBreak)
            canBreak = account.isOwner(player) || Permissions.ACCOUNT_CLOSE_OTHER.ownedBy(player);
		if (canBreak) {
            Set<Material> axes = EnumSet.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
                    Material.GOLDEN_AXE, Material.DIAMOND_AXE);
            canBreak = Stream.of(player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand())
                    .filter(item -> item != null)
                    .anyMatch(item -> axes.contains(item.getType()));
		}
  
		if (!canBreak) {
            plugin.debug("%s cannot break account %s", player.getName(), account);
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
				if (bank.getOwner().isOnline()) {
					bank.getOwner().getPlayer().sendMessage(Message.ACCOUNT_REIMBURSEMENT_PAID
							.with(Placeholder.PLAYER).as(player.getName())
							.and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
							.translate(plugin));
				}
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
			plugin.debug("%s broke account %s", player.getName(), account);
			player.sendMessage(Message.ACCOUNT_CLOSED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
		}
	}

	/**
	 * Listens for block place events, and handles the expansion of a small account chest into a large account chest.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAccountExtend(BlockPlaceEvent e) {
        Block placedBlock = e.getBlockPlaced();
		if (placedBlock.getType() != Material.CHEST && placedBlock.getType() != Material.TRAPPED_CHEST)
			return;

		Chest placedChest = (Chest) placedBlock.getState();
		org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) placedChest.getBlockData();

		if (chestData.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE)
			return;

		BlockFace orientationToExisting;
		boolean leftSide = chestData.getType() == org.bukkit.block.data.type.Chest.Type.LEFT;
		switch (chestData.getFacing()) {
			case NORTH:
				orientationToExisting = leftSide ? EAST : WEST;
				break;
			case EAST:
				orientationToExisting = leftSide ? SOUTH : NORTH;
				break;
			case SOUTH:
				orientationToExisting = leftSide ? WEST : EAST;
				break;
			case WEST:
				orientationToExisting = leftSide ? NORTH : SOUTH;
				break;
			default:
				return;
		}

		Block existingBlock = placedBlock.getRelative(orientationToExisting);
		Account account = plugin.getAccountService().findAtChest(existingBlock);
		if (account == null)
			return;

		Player player = e.getPlayer();
		Bank bank = plugin.getBankService().findContaining(placedBlock);
		if (bank == null || !bank.equals(account.getBank())) {
			plugin.debug("Chest not in bank.");
			player.sendMessage(Message.CHEST_NOT_IN_BANK.translate(plugin));
			e.setCancelled(true);
			return;
		}

		plugin.debug("%s tries to extend account %s", player.getName(), account);

		if (!account.isOwner(player) && Permissions.ACCOUNT_EXTEND_OTHER.notOwnedBy(player)) {
			player.sendMessage(Message.NO_PERMISSION_ACCOUNT_EXTEND_OTHER.translate(plugin));
			e.setCancelled(true);
			return;
		}
		
		AccountLocation newAccountLocation = new DoubleAccountLocation(placedBlock, orientationToExisting);

		try {
			new AccountExtendEvent(player, account, newAccountLocation).fire();
		} catch (EventCancelledException ex) {
			if (Permissions.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player)) {
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
				if (bank.getOwner().isOnline()) {
					bank.getOwner().getPlayer().sendMessage(Message.ACCOUNT_EXTEND_FEE_RECEIVED
							.with(Placeholder.PLAYER).as(account.getOwner().getName())
							.and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
							.and(Placeholder.BANK_NAME).as(bank.getColorizedName())
							.translate(plugin));
				}
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
		List<Block> chests = Stream.of(e.getSource(), e.getDestination())
				.filter(inv -> inv.getType() == InventoryType.CHEST)
				.filter(inv -> inv.getLocation() != null)
				.map(inv -> inv.getLocation().getBlock())
				.filter(block -> block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)
				.collect(Collectors.toList());
		if (plugin.getAccountService().isAnyAccount(chests))
			e.setCancelled(true);
    }

	/**
	 * Prevents unauthorized players from editing the items in other players' account chests
	 * TODO: Try to find another way to do this that does not require querying the database
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		if (e.getInventory().getType() != InventoryType.CHEST)
			return;
		if (e.getInventory().getLocation() == null)
			return;
		Block block = e.getInventory().getLocation().getBlock();
		if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
			return;
		Account account = plugin.getAccountService().findAtChest(block);
		if (account == null)
			return;
		Player executor = (Player) e.getWhoClicked();
		if (!account.isTrusted(executor) && Permissions.ACCOUNT_EDIT_OTHER.notOwnedBy(executor)) {
			executor.sendMessage(Message.NO_PERMISSION_ACCOUNT_EDIT_OTHER.translate(plugin));
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBurn(BlockBurnEvent e) {
		if (plugin.getAccountService().isAccount(e.getBlock()))
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		removeIfAccount(e.blockList());
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		removeIfAccount(e.blockList());
	}
    
    private void removeIfAccount(List<Block> blocks) {
        List<Block> chests = blocks.stream()
                .filter(b -> b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST)
                .collect(Collectors.toList());
        if (chests.isEmpty())
            return;
        Set<Account> accounts = plugin.getAccountService().findAtBlocks(chests);
        for (Account account : accounts)
            for (Block block : account.getLocation())
                blocks.remove(block);
    }
    
}
