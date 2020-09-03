package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.ClickType.EClickType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
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

import java.util.Optional;

@SuppressWarnings("unused")
public class WorldGuardListener implements Listener {

	private final BankingPlugin plugin;
    private final WorldGuardWrapper wgWrapper;

	public WorldGuardListener(BankingPlugin plugin) {
        this.plugin = plugin;
        this.wgWrapper = WorldGuardWrapper.getInstance();
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreateBank(BankCreateEvent e) {
		if (!Config.enableWorldGuardIntegration)
			return;

		IWrappedFlag<WrappedState> flag = getStateFlag("create-bank");
		for (Location loc : e.getBank().getSelection().getVertices())
			if (handleForLocation((Player) e.getExecutor(), loc, e, flag))
				return;
	}

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onResizeBank(BankResizeEvent e) {
		if (!Config.enableWorldGuardIntegration)
			return;

		IWrappedFlag<WrappedState> flag = getStateFlag("create-bank");
		for (Location loc : e.getNewSelection().getVertices())
			if (handleForLocation((Player) e.getExecutor(), loc, e, flag))
				return;
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

		if (clickType != null && clickType.getType() == EClickType.CREATE) {
			// If the player is about to create an account, but does not have
			// access to the chest, show the 'permission denied' message
			// (if not previously set to allowed by another plugin).
			// If the player can open the chest, that message should be hidden.
			WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
			Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
			if (!flag.isPresent())
				plugin.debug("WorldGuard flag 'chest-access' is not present!");
			WrappedState state = flag.map(f -> wgWrapper.queryFlag(player, location, f).orElse(WrappedState.DENY))
					.orElse(WrappedState.DENY);
			return state == WrappedState.ALLOW;
		}

		Account account = plugin.getAccountUtils().getAccount(location);
        // Don't show 'permission denied' messages for any kind of
        // account interaction even if block interaction is not
        // allowed in the region.
        return account != null;
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e, IWrappedFlag<WrappedState> flag) {
        if (flag == null) {
            // Flag may have not been registered successfully, so ignore them.
            return false;
        }

        WrappedState state = wgWrapper.queryFlag(player, loc, flag).orElse(WrappedState.DENY);
        if (state == WrappedState.DENY) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: WorldGuard");
            return true;
        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private IWrappedFlag<WrappedState> getStateFlag(String flagName) {
        Optional<IWrappedFlag<WrappedState>> flagOptional = wgWrapper.getFlag(flagName, WrappedState.class);
        if (!flagOptional.isPresent()) {
            plugin.getLogger().severe("Failed to get WorldGuard state flag '" + flagName + "'.");
            plugin.debug("WorldGuard state flag '" + flagName + "' is not present!");
            return null;
        }
        return flagOptional.get();
    }
}