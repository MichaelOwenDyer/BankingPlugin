package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.ItemUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;

import net.milkbowl.vault.economy.EconomyResponse;

public class ChestProtectListener implements Listener {

	private BankingPlugin plugin;
	private AccountUtils accountUtils;

	public ChestProtectListener(BankingPlugin plugin) {
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
				if (account.getOwner().getUniqueId().equals(p.getUniqueId())
						|| p.hasPermission(Permissions.ACCOUNT_OTHER_REMOVE)) {
					removeAndCreateNew(account, b, p);
					return;
                }
            }
            e.setCancelled(true);
			e.getPlayer().sendMessage(Messages.CANNOT_BREAK_ACCOUNT);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        ArrayList<Block> bl = new ArrayList<>(e.blockList());
        for (Block b : bl) {
            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
				if (accountUtils.isAccount(b.getLocation()))
					e.blockList().remove(b);
            }
        }
    }

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		ArrayList<Block> bl = new ArrayList<>(e.blockList());
		for (Block b : bl) {
			if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
				if (plugin.getAccountUtils().isAccount(b.getLocation()))
					e.blockList().remove(b);
			}
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

        if (Utils.getMajorVersion() < 13) {
            InventoryHolder ih = c.getInventory().getHolder();
            if (!(ih instanceof DoubleChest)) {
                return;
            }

            DoubleChest dc = (DoubleChest) ih;
            Chest l = (Chest) dc.getLeftSide();
            Chest r = (Chest) dc.getRightSide();

            if (b.getLocation().equals(l.getLocation())) {
                b2 = r.getBlock();
            } else {
                b2 = l.getBlock();
            }
        } else {
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
                    neighborFacing = null;
            }

            b2 = b.getRelative(neighborFacing);
        }

		final Account account = accountUtils.getAccount(b2.getLocation());
		if (account == null)
            return;

		plugin.debug(String.format("%s tries to extend %s's account (#%d)", p.getName(), account.getOwner().getName(),
				account.getID()));

		AccountExtendEvent event = new AccountExtendEvent(p, account, b.getLocation());
        Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            e.setCancelled(true);
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED);
            return;
        }

		if (!p.getUniqueId().equals(account.getOwner().getUniqueId())
				&& !p.hasPermission(Permissions.ACCOUNT_OTHER_EXTEND)) {
            e.setCancelled(true);
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_EXTEND_OTHER);
            return;
        }

		if (!ItemUtils.isTransparent(b.getRelative(BlockFace.UP))) {
            e.setCancelled(true);
			p.sendMessage(Messages.CHEST_BLOCKED);
            return;
        }

		final Account newAccount = new Account(account.getID(), plugin, account.getOwner(), account.getCoowners(),
				account.getBank(), account.getLocation(), account.getStatus(), account.getNickname(),
				account.getBalance(), account.getPrevBalance());

		accountUtils.removeAccount(account, true, new Callback<Void>(plugin) {
            @Override
            public void onResult(Void result) {
				newAccount.create(true);
				accountUtils.addAccount(newAccount, true, new Callback<Integer>(plugin) {
					@Override
					public void onResult(Integer result) {
						newAccount.setNickname(newAccount.getNickname());
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

	private void removeAndCreateNew(final Account account, final Block b, final Player p) {
		if (account.getInventoryHolder() instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) account.getInventoryHolder();
			final Chest l = (Chest) dc.getLeftSide();
			final Chest r = (Chest) dc.getRightSide();

			Location loc = b.getLocation().equals(l.getLocation()) ? r.getLocation() : l.getLocation();
			final Account newAccount = new Account(account.getID(), plugin, account.getOwner(), account.getCoowners(),
					account.getBank(), loc, account.getStatus(), account.getNickname(), account.getBalance(),
					account.getPrevBalance());

			accountUtils.removeAccount(account, true, new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					newAccount.create(true);
					accountUtils.addAccount(newAccount, true, new Callback<Integer>(plugin) {
						@Override
						public void onResult(Integer result) {
							newAccount.setNickname(newAccount.getNickname());
						}
					});
				}
			});
		} else {
			double creationPrice = (double) account.getBank().getAccountConfig().getOrDefault(Field.ACCOUNT_CREATION_PRICE);

			if (creationPrice > 0 && (boolean) account.getBank().getAccountConfig().getOrDefault(Field.REIMBURSE_ACCOUNT_CREATION)
					&& account.isTrusted(p)) {
				EconomyResponse r = plugin.getEconomy().depositPlayer(p, account.getLocation().getWorld().getName(),
						creationPrice);
				if (!r.transactionSuccess()) {
					plugin.debug("Economy transaction failed: " + r.errorMessage);
					p.sendMessage(Messages.ERROR_OCCURRED);
				} else {
					p.sendMessage(Messages.ACCOUNT_REMOVED);
					p.sendMessage(String.format(Messages.PLAYER_REIMBURSED,
							BigDecimal.valueOf(r.amount).setScale(2, RoundingMode.HALF_EVEN)).toString());
				}
			} else {
				p.sendMessage(Messages.ACCOUNT_REMOVED);
			}

			accountUtils.removeAccount(account, true);
			plugin.debug(String.format("%s broke %s's account (#%d)", p.getName(), account.getOwner().getName(),
					account.getID()));
		}
	}
}
