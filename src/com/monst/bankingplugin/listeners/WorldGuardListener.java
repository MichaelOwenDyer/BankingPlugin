package com.monst.bankingplugin.listeners;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.event.WrappedUseBlockEvent;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.ClickType.EnumClickType;

public class WorldGuardListener implements Listener {

    private BankingPlugin plugin;

    public WorldGuardListener(BankingPlugin plugin) {
        this.plugin = plugin;
    }

	@EventHandler(priority = EventPriority.LOW)
	public void onUseBlock(WrappedUseBlockEvent event) {
		if (!Config.enableWorldGuardIntegration)
			return;

		Player player = event.getPlayer();

		if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
			Block block = event.getBlocks().get(0);
			Material type = block.getType();

			if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
				if (isAllowed(player, block.getLocation())) {
					event.setResult(Result.ALLOW);
				}
			}
		} else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
			InventoryOpenEvent orig = (InventoryOpenEvent) event.getOriginalEvent();

			if (orig.getInventory().getHolder() instanceof Chest) {
				if (isAllowed(player, ((Chest) orig.getInventory().getHolder()).getLocation())) {
					event.setResult(Result.ALLOW);
				}
			}
		}

	}

    private boolean isAllowed(Player player, Location location) {
        ClickType clickType = ClickType.getPlayerClickType(player);

        if (clickType != null && clickType.getClickType() == EnumClickType.CREATE) {
            // If the player is about to create an account, but does not have
            // access to the chest, show the 'permission denied' message
            // (if not previously set to allowed by another plugin).
            // If the player can open the chest, that message should be hidden.
            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
            if (!flag.isPresent()) plugin.debug("WorldGuard flag 'chest-access' is not present!");
            WrappedState state = flag.map(f -> wgWrapper.queryFlag(player, location, f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
            return state == WrappedState.ALLOW;
        }

        Account account = plugin.getAccountUtils().getAccount(location);

        if (account != null) {
            // Don't show 'permission denied' messages for any kind of
            // account interaction even if block interaction is not
            // allowed in the region.
            return true;
        }

        return false;
    }
}
