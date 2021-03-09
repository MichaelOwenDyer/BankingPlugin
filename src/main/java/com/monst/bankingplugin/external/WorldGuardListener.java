package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.listeners.BankingPluginListener;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.ClickType.EClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.event.WrappedUseBlockEvent;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.util.Optional;

public class WorldGuardListener extends BankingPluginListener {

    private final WorldGuardWrapper wgWrapper;
    private IWrappedFlag<WrappedState> bankCreateFlag;

	public WorldGuardListener(BankingPlugin plugin) {
        super(plugin);
        this.wgWrapper = WorldGuardWrapper.getInstance();
        wgWrapper.getFlag("create-bank", WrappedState.class).ifPresent(flag -> this.bankCreateFlag = flag);
        if (bankCreateFlag == null) {
			plugin.getLogger().severe("Failed to find WorldGuard state flag 'create-bank'");
			plugin.debug("WorldGuard state flag 'create-bank' is not present!");
		}
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreateBank(BankCreateEvent e) {
		if (!Config.enableWorldGuardIntegration || e.getExecutor().hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS))
			return;

		for (BlockVector3D bv : e.getBank().getSelection().getCorners())
			if (isBankCreationBlockedByWorldGuard((Player) e.getExecutor(), bv.toLocation(e.getBank().getSelection().getWorld()))) {
				e.setCancelled(true);
				plugin.debug("Bank create event cancelled by WorldGuard");
				return;
			}
	}

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onResizeBank(BankResizeEvent e) {
		if (!Config.enableWorldGuardIntegration || e.getExecutor().hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS))
			return;

		for (BlockVector3D bv : e.getNewSelection().getCorners())
			if (isBankCreationBlockedByWorldGuard((Player) e.getExecutor(), bv.toLocation(e.getNewSelection().getWorld()))) {
				e.setCancelled(true);
				plugin.debug("Bank resize event cancelled by WorldGuard");
				return;
			}
    }

	@EventHandler(priority = EventPriority.LOW)
	public void onUseBlock(WrappedUseBlockEvent event) {
		if (!Config.enableWorldGuardIntegration)
			return;

		Block block;
		if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
			block = ((PlayerInteractEvent) event.getOriginalEvent()).getClickedBlock();
		} else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
			block = ((InventoryOpenEvent) event.getOriginalEvent()).getInventory().getLocation().getBlock();
		} else
			return;
		if (block == null)
			return;
		if (isChestInteractAllowed(event.getPlayer(), block))
			event.setResult(Result.ALLOW);
	}

	private boolean isChestInteractAllowed(Player player, Block block) {
		ClickType<?> clickType = ClickType.getPlayerClickType(player);
		if (clickType != null && clickType.getType() == EClickType.CREATE) {
			// If the player is about to create an account, but does not have
			// access to the chest, show the 'permission denied' message
			// (if not previously set to allowed by another plugin).
			// If the player can open the chest, that message should be hidden.
			Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
			if (!flag.isPresent())
				plugin.debug("WorldGuard flag 'chest-access' is not present!");
			return flag.map(f -> wgWrapper.queryFlag(player, block.getLocation(), f).orElse(WrappedState.DENY))
					.orElse(WrappedState.DENY) == WrappedState.ALLOW;
		}
		// Don't show 'permission denied' messages for any kind of
		// account interaction even if block interaction is not
		// allowed in the region.
		return plugin.getAccountRepository().isAccount(ChestLocation.from(Utils.getChestAt(block)));
    }

    private boolean isBankCreationBlockedByWorldGuard(Player player, Location loc) {
        if (bankCreateFlag == null)
            return false;
        return wgWrapper.queryFlag(player, loc, bankCreateFlag).orElse(WrappedState.DENY) == WrappedState.DENY;
    }

}
