package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
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
import org.bukkit.inventory.EquipmentSlot;
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
	    Chest chest = (Chest) b.getState();
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
	    Chest chest = (Chest) b.getState();
	    for (Location loc : Utils.getChestLocations(chest.getInventory().getHolder()))
            if (handleForLocation(e.getPlayer(), loc, e)) {
                e.setCancelled(true);
                plugin.debug("Account recover event cancelled by GriefPrevention");
                return;
            }
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
        if (claim == null)
            return false;
        return claim.allowContainers(player) != null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSelectionInteract(PlayerInteractEvent e) {
        if (!Config.enableGriefPreventionIntegration)
            return;
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
            return;
	    if (e.getHand() != EquipmentSlot.HAND)
	        return;

        ItemStack itemInHand = Utils.getItemInMainHand(e.getPlayer());
        if (itemInHand == null)
            return;
        if (itemInHand.getType() != griefPrevention.config_claims_investigationTool)
            return;

        Block clickedBlock;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
            clickedBlock = e.getClickedBlock();
        else if (e.getAction() == Action.RIGHT_CLICK_AIR)
            clickedBlock = e.getPlayer().getTargetBlock(null, 100);
        else
            return;
        if (clickedBlock == null || clickedBlock.getType() == Material.AIR)
            return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (clickedBlock.getType() == Material.CHEST || clickedBlock.getType() == Material.TRAPPED_CHEST))
            return;

        Bank bank = plugin.getBankRepository().getAt(clickedBlock.getLocation());
        if (bank == null)
            return;

        VisualizationManager.visualizeSelection(e.getPlayer(), bank);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankCreate(BankCreateEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
            VisualizationManager.visualizeSelection(((Player) executor), e.getBank());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankRemove(BankRemoveEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
            VisualizationManager.revertVisualization((Player) executor);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankResize(BankResizeEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
	        VisualizationManager.visualizeSelection(((Player) executor), e.getNewSelection(), e.getBank().isAdminBank());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankSelect(BankSelectEvent e) {
	    if (!Config.enableGriefPreventionIntegration)
	        return;
        VisualizationManager.visualizeSelection(((Player) e.getExecutor()), e.getBank());
    }
}
