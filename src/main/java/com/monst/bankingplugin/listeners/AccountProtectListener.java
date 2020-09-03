package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * This listener is intended to prevent all physical damage to {@link Account} chests,
 * as well as to ensure accounts always correspond with the size of the chest they reside in.
 */
@SuppressWarnings("unused")
public class AccountProtectListener implements Listener {

	private final BankingPlugin plugin;
	private final AccountUtils accountUtils;

	public AccountProtectListener(BankingPlugin plugin) {
        this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
    }

	/**
	 * Listen for block break events, and cancel the event if the block in question
	 * is an account chest. Allow the breaking of the account if the player has permission to do so.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onAccountChestBreak(BlockBreakEvent e) {
		final Block b = e.getBlock();

		if (accountUtils.isAccount(b.getLocation())) {
			final Account account = accountUtils.getAccount(e.getBlock().getLocation());
			Player p = e.getPlayer();

			if (p.isSneaking() && Utils.hasAxeInHand(p)) {
				plugin.debugf("%s tries to break %s's account (#%d)",
						p.getName(), account.getOwner().getName(), account.getID());
				if (account.isOwner(p) || p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) {
					removeAndCreateSmaller(account, b, p);
					return;
				}
			}
			e.setCancelled(true);
			e.getPlayer().sendMessage(Messages.CANNOT_BREAK_ACCOUNT);
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
	@SuppressWarnings("ConstantConditions")
	private void removeAndCreateSmaller(final Account account, final Block b, final Player p) {
		Bank bank = account.getBank();
		double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
		creationPrice *= bank.get(BankField.REIMBURSE_ACCOUNT_CREATION) ? 1 : 0;

		if (creationPrice > 0 && account.isOwner(p) && !account.getBank().isOwner(p)) {
			double finalCreationPrice = creationPrice;
			String worldName = account.getLocation().getWorld() != null ? account.getLocation().getWorld().getName() : "world";
			// Account owner is reimbursed for the part of the chest that was broken
			Utils.depositPlayer(p, worldName, finalCreationPrice, Callback.of(plugin,
					result -> p.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED, Utils.format(finalCreationPrice))),
					throwable -> p.sendMessage(Messages.ERROR_OCCURRED)));

			// Bank owner reimburses the customer
			if (creationPrice > 0 && bank.isPlayerBank() && !bank.isOwner(p)) {
				OfflinePlayer bankOwner = bank.getOwner();
				Utils.withdrawPlayer(bankOwner, account.getLocation().getWorld().getName(), finalCreationPrice, Callback.of(plugin,
						result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
								account.getOwner().getName(), Utils.format(finalCreationPrice)), bankOwner),
						throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
			}
		}

		if (account.getInventory(true).getHolder() instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) account.getInventory(false).getHolder();
			final Chest l = (Chest) dc.getLeftSide();
			final Chest r = (Chest) dc.getRightSide();

			Location newLocation = b.getLocation().equals(l.getLocation()) ? r.getLocation() : l.getLocation();
			Account newAccount = Account.clone(account);
			newAccount.setLocation(newLocation);

			accountUtils.removeAccount(account, false, Callback.of(plugin, result -> {
				newAccount.create(true);
				accountUtils.addAccount(newAccount, true, newAccount.callUpdateName());
			}));
		} else {
			accountUtils.removeAccount(account, true);
			plugin.debugf("%s broke %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());
			p.sendMessage(Messages.ACCOUNT_REMOVED);
		}
	}

	/**
	 * Listens for block place events, and handles the expansion of a small account chest into a large
	 * account chest.
	 * {@link Utils#getChestLocations(InventoryHolder)} performs largely the same task as a good portion of this {@link EventHandler},
	 * but cannot be used since {@link BlockPlaceEvent}s are fired before the {@link org.bukkit.inventory.InventoryHolder}
	 * of the new chest has been updated.
	 * This means that when an account chest is extended and this handler is executed,
	 * for all intents and purposes the account chest is actually still a single chest.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onAccountExtend(BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();

		if (!(b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST))) {
            return;
        }
        
        Chest c = (Chest) b.getState();
        Block b2;
		org.bukkit.block.data.type.Chest data = (org.bukkit.block.data.type.Chest) c.getBlockData();

		if (data.getType() == Type.SINGLE) {
			return;
		}

		BlockFace neighborFacing;

		switch (data.getFacing()) {
		case NORTH:
			neighborFacing = data.getType() == Type.LEFT ? BlockFace.EAST : BlockFace.WEST;
			break;
		case EAST:
			neighborFacing = data.getType() == Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
			break;
		case SOUTH:
			neighborFacing = data.getType() == Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
			break;
		case WEST:
			neighborFacing = data.getType() == Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
			break;
		default:
			throw new IllegalStateException("Unknown chest orientation! " + data.toString());
		}

		b2 = b.getRelative(neighborFacing);

		final Account account = accountUtils.getAccount(b2.getLocation());
		if (account == null)
            return;

		plugin.debugf("%s tries to extend %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());

		AccountExtendEvent event = new AccountExtendEvent(p, account, b.getLocation());
        Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            e.setCancelled(true);
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED);
            return;
        }

		if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_EXTEND_OTHER)) {
            e.setCancelled(true);
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_EXTEND_OTHER);
            return;
        }

		if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            e.setCancelled(true);
			p.sendMessage(Messages.CHEST_BLOCKED);
            return;
		}

		double creationPrice = account.getBank().get(BankField.ACCOUNT_CREATION_PRICE);
		if (creationPrice > 0 && account.isOwner(p) && !account.getBank().isOwner(p)) {
			OfflinePlayer owner = p.getPlayer();
			String worldName = account.getLocation().getWorld() != null ? account.getLocation().getWorld().getName() : "world";
			if (!Utils.withdrawPlayer(owner, worldName, creationPrice, Callback.of(plugin,
					result -> p.sendMessage(String.format(Messages.ACCOUNT_EXTEND_FEE_PAID, Utils.format(creationPrice))),
					throwable -> p.sendMessage(Messages.ERROR_OCCURRED)))) {
				e.setCancelled(true);
				return;
			}

			if (creationPrice > 0 && account.isOwner(p) && !account.getBank().isOwner(p)) {
				OfflinePlayer bankOwner = account.getBank().getOwner();
				Utils.depositPlayer(bankOwner, account.getLocation().getWorld().getName(), creationPrice, Callback.of(plugin,
						result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_EXTEND_FEE_RECEIVED,
								account.getOwner().getName(), Utils.format(creationPrice)), bankOwner),
						throwable -> p.sendMessage(Messages.ERROR_OCCURRED)));
			}
		}

		final Account newAccount = Account.clone(account);

		accountUtils.removeAccount(account, true, Callback.of(plugin, result -> {
				if (newAccount.create(true)) {
					accountUtils.addAccount(newAccount, true, newAccount.callUpdateName());
					plugin.debugf("%s extended %s's account (#%d)",
							p.getName(), account.getOwner().getName(), account.getID());
				} else
					p.sendMessage(Messages.ERROR_OCCURRED);
			})
		);
    }

	/**
	 * Stops any and all non-player (e.g. hopper) item movement into and out of account chests.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemMove(InventoryMoveItemEvent e) {
        if ((e.getSource().getType().equals(InventoryType.CHEST)) && (!e.getInitiator().getType().equals(InventoryType.PLAYER))) {

        	for (Inventory inv : new Inventory[]{e.getSource(), e.getDestination()}) {

				if (inv.getHolder() instanceof DoubleChest) {
					DoubleChest dc = (DoubleChest) inv.getHolder();
					Chest r = (Chest) dc.getRightSide();
					Chest l = (Chest) dc.getLeftSide();

					if ((r != null && accountUtils.isAccount(r.getLocation()))
							|| (l != null && accountUtils.isAccount(l.getLocation())))
						e.setCancelled(true);

				} else if (inv.getHolder() instanceof Chest) {
					Chest c = (Chest) inv.getHolder();

					if (accountUtils.isAccount(c.getLocation()))
						e.setCancelled(true);
				}
			}
        }
    }

	/**
	 * Prevents unauthorized players from editing the items in other players' account chests
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemClick(InventoryClickEvent e) {
		if (!(e.getInventory().getHolder() instanceof Chest || e.getInventory().getHolder() instanceof DoubleChest))
			return;
		if (!accountUtils.isAccount(e.getInventory().getLocation()))
			return;
		if (!(e.getWhoClicked() instanceof Player))
			return;
		Account account = accountUtils.getAccount(e.getInventory().getLocation());
		Player executor = (Player) e.getWhoClicked();
		if (!account.isTrusted(executor) && !executor.hasPermission(Permissions.ACCOUNT_EDIT_OTHER)) {
			executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_EDIT);
			e.setCancelled(true);
		}
	}
}
