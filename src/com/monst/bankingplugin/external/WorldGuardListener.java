package com.monst.bankingplugin.external;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.utils.Utils;

public class WorldGuardListener implements Listener {

	private final BankingPlugin plugin;
    private final WorldGuardWrapper wgWrapper;

	public WorldGuardListener(BankingPlugin plugin) {
        this.plugin = plugin;
        this.wgWrapper = WorldGuardWrapper.getInstance();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreateAccount(AccountCreateEvent e) {
        if (!Config.enableWorldGuardIntegration)
            return;

		Set<Location> chestLocations = Utils.getChestLocations(e.getAccount());
		IWrappedFlag<WrappedState> flag = getStateFlag("create-account");
        for (Location loc : chestLocations) {
            if (handleForLocation(e.getPlayer(), loc, e, flag))
                return;
        }
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreateBank(BankCreateEvent e) {
		if (!Config.enableWorldGuardIntegration)
			return;

		List<Location> vertices = e.getBank().getVertices();
		IWrappedFlag<WrappedState> flag = getStateFlag("create-bank");
		for (Location loc : vertices) {
			if (handleForLocation(e.getPlayer(), loc, e, flag))
				return;
		}
	}

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onExtendShop(AccountExtendEvent e) {
        if (!Config.enableWorldGuardIntegration)
            return;

		handleForLocation(e.getPlayer(), e.getNewChestLocation(), e, getStateFlag("create-account"));
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