package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.utils.*;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import net.milkbowl.vault.economy.EconomyResponse;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AccountProtectListener implements Listener {

	private final BankingPlugin plugin;
	private final AccountUtils accountUtils;

	public AccountProtectListener(BankingPlugin plugin) {
        this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
    }

    @EventHandler(ignoreCancelled = true)
	public void onAccountChestBreak(BlockBreakEvent e) {
		final Block b = e.getBlock();

		if (accountUtils.isAccount(b.getLocation())) {
			final Account account = accountUtils.getAccount(e.getBlock().getLocation());
			Player p = e.getPlayer();

			if (p.isSneaking() && Utils.hasAxeInHand(p)) {
				plugin.debug(String.format("%s tries to break %s's account (#%d)", p.getName(),
						account.getOwner().getName(), account.getID()));
				if (account.isOwner(p) || p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) {
					removeAndCreateSmaller(account, b, p);
					return;
				}
			}
			e.setCancelled(true);
			e.getPlayer().sendMessage(Messages.CANNOT_BREAK_ACCOUNT);
		}
	}

	private void removeAndCreateSmaller(final Account account, final Block b, final Player p) {
		AccountConfig accountConfig = account.getBank().getAccountConfig();
		double creationPrice = (double) accountConfig.getOrDefault(Field.ACCOUNT_CREATION_PRICE);

		if (creationPrice > 0 && (boolean) accountConfig.getOrDefault(Field.REIMBURSE_ACCOUNT_CREATION)
				&& account.isOwner(p)) {
			EconomyResponse r = plugin.getEconomy().depositPlayer(p, account.getLocation().getWorld().getName(),
					creationPrice);
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				p.sendMessage(Messages.ERROR_OCCURRED);
			} else {
				p.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED, Utils.formatNumber(r.amount)));
			}

			if (!account.getBank().isAdminBank()) {
				OfflinePlayer bankOwner = account.getBank().getOwner();
				EconomyResponse r2 = plugin.getEconomy().withdrawPlayer(bankOwner,
						account.getLocation().getWorld().getName(), creationPrice);
				if (!r2.transactionSuccess()) {
					plugin.debug("Economy transaction failed: " + r2.errorMessage);
					if (bankOwner.isOnline())
						bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					return;
				} else if (!account.isOwner(bankOwner) && bankOwner.isOnline())
					bankOwner.getPlayer().sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
							account.getOwner().getName(), Utils.formatNumber(r2.amount)));
			}
		}

		if (account.getInventoryHolder() instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) account.getInventoryHolder();
			final Chest l = (Chest) dc.getLeftSide();
			final Chest r = (Chest) dc.getRightSide();

			Location loc = b.getLocation().equals(l.getLocation()) ? r.getLocation() : l.getLocation();
			final Account newAccount = new Account(account.getID(), plugin, account.getOwner(), account.getCoowners(),
					account.getBank(), loc, account.getStatus(), account.getRawNickname(), account.getBalance(),
					account.getPrevBalance());

			accountUtils.removeAccount(account, true, new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					newAccount.create(true);
					accountUtils.addAccount(newAccount, true, new Callback<Integer>(plugin) {
						@Override
						public void onResult(Integer result) {
							newAccount.setNickname(newAccount.getRawNickname());
						}
					});
				}
			});

		} else {
			accountUtils.removeAccount(account, true);
			plugin.debug(String.format("%s broke %s's account (#%d)", p.getName(), account.getOwner().getName(),
					account.getID()));
			p.sendMessage(Messages.ACCOUNT_REMOVED);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onAccountExtend(BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();

		if (!(b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST))) {
            return;
        }
        
        Chest c = (Chest) b.getState();
        Block b2;

        // Can't use Utils::getChestLocations since inventory holder
        // has not been updated yet in this event (for 1.13+)

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

		plugin.debug(String.format("%s tries to extend %s's account (#%d)", p.getName(), account.getOwner().getName(),
				account.getID()));

		AccountExtendEvent event = new AccountExtendEvent(p, account, b.getLocation());
        Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_EXTEND_OTHER)
				&& !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
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
		
		AccountConfig accountConfig = account.getBank().getAccountConfig();
		double creationPrice = (double) accountConfig.getOrDefault(Field.ACCOUNT_CREATION_PRICE);
		
		if (creationPrice > 0 && account.isOwner(p) && !account.getBank().isOwner(p)) {
			OfflinePlayer owner = p.getPlayer();
			EconomyResponse r = plugin.getEconomy().withdrawPlayer(owner, 
					account.getLocation().getWorld().getName(), creationPrice);
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				p.sendMessage(Messages.ERROR_OCCURRED);
				return;
			} else {
				p.sendMessage(String.format(Messages.ACCOUNT_EXTEND_FEE_PAID,
						BigDecimal.valueOf(r.amount).setScale(2, RoundingMode.HALF_EVEN)));
			}
			
			if (!account.getBank().isAdminBank()) {
				OfflinePlayer bankOwner = account.getBank().getOwner();
				EconomyResponse r2 = plugin.getEconomy().depositPlayer(bankOwner, 
						account.getLocation().getWorld().getName(), creationPrice);
				if (!r2.transactionSuccess()) {
					plugin.debug("Economy transaction failed: " + r2.errorMessage);
					p.sendMessage(Messages.ERROR_OCCURRED);
					return;
				} else if (bankOwner.isOnline())
					bankOwner.getPlayer().sendMessage(String.format(Messages.ACCOUNT_EXTEND_FEE_RECEIVED,
							account.getOwner().getName(),
							BigDecimal.valueOf(r2.amount).setScale(2, RoundingMode.HALF_EVEN)));
			}
		}

		final Account newAccount = new Account(account.getID(), plugin, account.getOwner(), account.getCoowners(),
				account.getBank(), account.getLocation(), account.getStatus(), account.getRawNickname(),
				account.getBalance(), account.getPrevBalance());

		accountUtils.removeAccount(account, true, new Callback<Void>(plugin) {
            @Override
            public void onResult(Void result) {
				newAccount.create(true);
				accountUtils.addAccount(newAccount, true, new Callback<Integer>(plugin) {
					@Override
					public void onResult(Integer result) {
						newAccount.setNickname(newAccount.getRawNickname());
					}
				});
				plugin.debug(String.format("%s extended %s's account (#%d)", p.getName(), account.getOwner().getName(),
						account.getID()));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAccountItemMove(InventoryMoveItemEvent e) {
        if ((e.getSource().getType().equals(InventoryType.CHEST)) && (!e.getInitiator().getType().equals(InventoryType.PLAYER))) {

            if (e.getSource().getHolder() instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) e.getSource().getHolder();
                Chest r = (Chest) dc.getRightSide();
                Chest l = (Chest) dc.getLeftSide();

				if (accountUtils.isAccount(r.getLocation()) || accountUtils.isAccount(l.getLocation()))
					e.setCancelled(true);

            } else if (e.getSource().getHolder() instanceof Chest) {
                Chest c = (Chest) e.getSource().getHolder();

				if (accountUtils.isAccount(c.getLocation()))
					e.setCancelled(true);
            }
        }
    }

	/**
	 * Prevents unauthorized players from editing other players' accounts
	 * 
	 * @param e InventoryClickEvent
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
