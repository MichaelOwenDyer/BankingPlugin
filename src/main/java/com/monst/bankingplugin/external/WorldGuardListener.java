package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.event.bank.BankCreateEvent;
import com.monst.bankingplugin.event.bank.BankResizeEvent;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.event.WrappedUseBlockEvent;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

public class WorldGuardListener implements Listener {

	private final BankingPlugin plugin;
    private final WorldGuardWrapper wgWrapper;
    private final IWrappedFlag<WrappedState> bankCreateFlag;
	private final IWrappedFlag<WrappedState> chestAccessFlag;

	public WorldGuardListener(BankingPlugin plugin) {
		this.plugin = plugin;
        this.wgWrapper = WorldGuardWrapper.getInstance();
        this.bankCreateFlag = wgWrapper.getFlag("create-bank", WrappedState.class).orElse(null);
		this.chestAccessFlag = wgWrapper.getFlag("chest-access", WrappedState.class).orElse(null);
        if (bankCreateFlag == null) {
			plugin.getLogger().severe("Failed to find WorldGuard state flag 'create-bank'");
			plugin.debug("WorldGuard state flag 'create-bank' is not present!");
		}
		if (chestAccessFlag == null) {
			plugin.debug("WorldGuard state flag 'chest-access' is not present!");
		}
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreateBank(BankCreateEvent e) {
		if (bankCreateFlag == null || !plugin.isWorldGuardIntegrated() || Permissions.BYPASS_EXTERNAL_PLUGINS.ownedBy(e.getPlayer()))
			return;

		for (Block block : e.getBank().getRegion().getCorners())
			if (isBankCreationBlockedByWorldGuard(e.getPlayer(), block)) {
				e.setCancelled(true);
				plugin.debug("Bank create event cancelled by WorldGuard");
				return;
			}
	}

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onResizeBank(BankResizeEvent e) {
		if (bankCreateFlag == null || !plugin.isWorldGuardIntegrated() || Permissions.BYPASS_EXTERNAL_PLUGINS.ownedBy(e.getPlayer()))
			return;

		for (Block block : e.getNewRegion().getCorners())
			if (isBankCreationBlockedByWorldGuard(e.getPlayer(), block)) {
				e.setCancelled(true);
				plugin.debug("Bank resize event cancelled by WorldGuard");
				return;
			}
    }

	private boolean isBankCreationBlockedByWorldGuard(Player player, Block block) {
		if (bankCreateFlag == null)
			return false;
		return wgWrapper.queryFlag(player, block.getLocation(), bankCreateFlag).orElse(WrappedState.DENY) == WrappedState.DENY;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onUseBlock(WrappedUseBlockEvent event) {
		if (!plugin.isWorldGuardIntegrated())
			return;

		Block block;
		if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
			block = ((PlayerInteractEvent) event.getOriginalEvent()).getClickedBlock();
		} else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
			Inventory inv = ((InventoryOpenEvent) event.getOriginalEvent()).getInventory();
			if (inv.getLocation() == null)
				return;
			block = inv.getLocation().getBlock();
		} else
			return;
		if (block == null)
			return;
		if (isChestInteractAllowed(event.getPlayer(), block))
			event.setResult(Result.ALLOW);
	}

	private boolean isChestInteractAllowed(Player player, Block block) {
		ClickAction clickAction = ClickAction.get(player);
		if (clickAction != null && clickAction.isBlockAction()) {
			if (chestAccessFlag == null)
				return true;
			// If the player is about to open an account, but does not have
			// access to the chest, show the 'permission denied' message
			// (if not previously set to allowed by another plugin).
			// If the player can open the chest, that message should be hidden.
			return wgWrapper.queryFlag(player, block.getLocation(), chestAccessFlag).orElse(WrappedState.DENY) == WrappedState.ALLOW;
		}
		// Don't show 'permission denied' messages for any kind of
		// account interaction even if block interaction is not
		// allowed in the region.
		return plugin.getAccountService().isAccount(block);
    }

}
