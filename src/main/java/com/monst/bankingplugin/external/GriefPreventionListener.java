package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.events.bank.BankSelectEvent;
import com.monst.bankingplugin.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
    public void onSelectionInteract(PlayerInteractEvent e) {

        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;
        Block b = e.getClickedBlock();
        if (b == null || b.getType() == Material.AIR || b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST)
            return;
        Player p = e.getPlayer();
        Material investigationTool = griefPrevention.config_claims_investigationTool;
        if (investigationTool != null) {
            ItemStack item = Utils.getItemInMainHand(p);
            if (item != null && investigationTool == item.getType()) {
                Bank bank = plugin.getBankUtils().getBank(b.getLocation());
                if (bank != null)
                    VisualizationManager.visualizeSelection(p, bank.getSelection());
                return;
            }
            item = Utils.getItemInOffHand(p);
            if (item != null && investigationTool == item.getType()) {
                Bank bank = plugin.getBankUtils().getBank(b.getLocation());
                if (bank != null)
                    VisualizationManager.visualizeSelection(p, bank.getSelection());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBankCreate(BankCreateEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
	        VisualizationManager.visualizeSelection(((Player) executor), e.getBank().getSelection());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBankResize(BankResizeEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
	        VisualizationManager.visualizeSelection(((Player) executor), e.getBank().getSelection());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBankSelect(BankSelectEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        VisualizationManager.visualizeSelection(((Player) e.getExecutor()), e.getBank().getSelection());
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
        if (claim == null) 
            return false;
		return claim.allowContainers(player) != null;
    }
}