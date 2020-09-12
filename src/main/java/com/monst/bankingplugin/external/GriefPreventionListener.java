package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public class GriefPreventionListener implements Listener {

	private final BankingPlugin plugin;
    private final GriefPrevention griefPrevention;

	public GriefPreventionListener(BankingPlugin plugin) {
        this.plugin = plugin;
        this.griefPrevention = plugin.getGriefPrevention();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAccountCreate(AccountCreateEvent e) {
        if (!Config.enableGriefPreventionIntegration)
            return;

        for (Location loc : Utils.getChestLocations(e.getAccount().getInventory(true).getHolder()))
			if (handleForLocation(e.getPlayer(), loc, e)) {
				e.setCancelled(true);
				plugin.debug("Account create event cancelled by GriefPrevention");
                return;
			}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAccountExtend(AccountExtendEvent e) {
		if (!Config.enableGriefPreventionIntegration)
            return;

		if (handleForLocation(e.getPlayer(), e.getNewChestLocation(), e)) {
			e.setCancelled(true);
			plugin.debug("Account extend event cancelled by GriefPrevention");
		}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAccountMigrate(AccountMigrateEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;

	    Block b = e.getNewAccountLocation().getBlock();
	    Chest chest = (Chest) b.getBlockData();
	    for (Location loc : Utils.getChestLocations(chest.getInventory().getHolder()))
            if (handleForLocation(e.getPlayer(), loc, e)) {
                e.setCancelled(true);
                plugin.debug("Account migrate event cancelled by GriefPrevention");
                return;
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAccountRecover(AccountRecoverEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;

	    Block b = e.getNewAccountLocation().getBlock();
	    Chest chest = (Chest) b.getBlockData();
	    for (Location loc : Utils.getChestLocations(chest.getInventory().getHolder()))
            if (handleForLocation(e.getPlayer(), loc, e)) {
                e.setCancelled(true);
                plugin.debug("Account recover event cancelled by GriefPrevention");
                return;
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBankCreate(BankCreateEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        Selection sel = e.getBank().getSelection();
        if (executor instanceof Player && sel instanceof CuboidSelection)
	    VisualizationManager.visualize(((Player) executor), ((CuboidSelection) sel));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBankResize(BankResizeEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        Selection sel = e.getBank().getSelection();
        if (executor instanceof Player && sel instanceof CuboidSelection)
	    VisualizationManager.visualize(((Player) executor), ((CuboidSelection) sel));
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
        if (claim == null) 
            return false;
		return claim.allowContainers(player) != null;
    }
}