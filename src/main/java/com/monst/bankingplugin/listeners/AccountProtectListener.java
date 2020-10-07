package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
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
			e.getPlayer().sendMessage(LangUtils.getMessage(Message.CANNOT_BREAK_ACCOUNT));
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
						result -> Utils.message(bankOwner, LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
								new Replacement(Placeholder.PLAYER, p::getName),
								new Replacement(Placeholder.AMOUNT, finalCreationPrice)
						)),
						error -> Utils.message(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
								new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
						))
				));
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
			p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_REMOVED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
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
        
        Chest chest = (Chest) b.getState();
		org.bukkit.block.data.type.Chest data = (org.bukkit.block.data.type.Chest) chest.getBlockData();

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

		final Account account = accountUtils.getAccount(b.getRelative(neighborFacing).getLocation());
		if (account == null)
            return;

		Bank bank = plugin.getBankUtils().getBank(b.getLocation());
		if (bank == null || !bank.equals(account.getBank())) {
			e.setCancelled(true);
			p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
			return;
		}

		plugin.debugf("%s tries to extend %s's account (#%d)", p.getName(), account.getOwner().getName(), account.getID());

		AccountExtendEvent event = new AccountExtendEvent(p, account, b.getLocation());
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

		if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
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
						result -> Utils.message(bankOwner, LangUtils.getMessage(Message.ACCOUNT_EXTEND_FEE_RECEIVED,
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

		accountUtils.removeAccount(account, true, Callback.of(plugin, result -> {
				if (newAccount.create(true)) {
					accountUtils.addAccount(newAccount, true, newAccount.callUpdateName());
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
        if ((e.getSource().getType().equals(InventoryType.CHEST)) && (!e.getInitiator().getType().equals(InventoryType.PLAYER))) {

        	for (Inventory inv : new Inventory[] {e.getSource(), e.getDestination()}) {

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
			executor.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_EDIT_OTHER));
			e.setCancelled(true);
		}
	}
}
