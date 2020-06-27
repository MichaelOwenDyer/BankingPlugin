package com.monst.bankingplugin.external;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.utils.Utils;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionListener implements Listener {
	private final BankingPlugin plugin;
    private final GriefPrevention griefPrevention;

	public GriefPreventionListener(BankingPlugin plugin) {
        this.plugin = plugin;
        this.griefPrevention = plugin.getGriefPrevention();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreateAccount(AccountCreateEvent e) {
        if (!Config.enableGriefPreventionIntegration)
            return;

		Set<Location> chestLocations = Utils.getChestLocations(e.getAccount());
        for (Location loc : chestLocations) {
			if (handleForLocation(e.getPlayer(), loc, e)) {
				e.setCancelled(true);
				plugin.debug("Account create event cancelled by GriefPrevention");
                return;
			}
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onExtendAccount(AccountExtendEvent e) {
		if (!Config.enableGriefPreventionIntegration)
            return;

		if (handleForLocation(e.getPlayer(), e.getNewChestLocation(), e)) {
			e.setCancelled(true);
			plugin.debug("Account extend event cancelled by GriefPrevention");
		}
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
        if (claim == null) 
            return false;
		return claim.allowContainers(player) != null;
    }
}