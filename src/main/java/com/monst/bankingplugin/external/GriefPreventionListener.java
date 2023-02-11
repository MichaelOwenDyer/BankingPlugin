package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.account.AccountExtendEvent;
import com.monst.bankingplugin.event.account.AccountMigrateEvent;
import com.monst.bankingplugin.event.account.AccountOpenEvent;
import com.monst.bankingplugin.event.account.AccountRecoverEvent;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class GriefPreventionListener implements Listener {

    private final BankingPlugin plugin;
    private final GriefPrevention griefPrevention;

	public GriefPreventionListener(BankingPlugin plugin) {
        this.plugin = plugin;
        this.griefPrevention = plugin.getGriefPrevention();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAccountCreate(AccountOpenEvent e) {
        if (!plugin.isGriefPreventionIntegrated())
            return;

        for (Block block : e.getAccount().getLocation())
			if (isBlockedByGriefPrevention(e.getPlayer(), block)) {
				e.setCancelled(true);
				plugin.debug("Account open event cancelled by GriefPrevention.");
                return;
			}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAccountExtend(AccountExtendEvent e) {
		if (!plugin.isGriefPreventionIntegrated())
            return;

		for (Block block : e.getNewAccountLocation())
            if (isBlockedByGriefPrevention(e.getPlayer(), block)) {
                e.setCancelled(true);
                plugin.debug("Account extend event cancelled by GriefPrevention.");
                return;
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAccountMigrate(AccountMigrateEvent e) {
	    if (!plugin.isGriefPreventionIntegrated())
	        return;

	    for (Block block : e.getNewAccountLocation())
            if (isBlockedByGriefPrevention(e.getPlayer(), block)) {
                e.setCancelled(true);
                plugin.debug("Account migrate event cancelled by GriefPrevention.");
                return;
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAccountRecover(AccountRecoverEvent e) {
	    if (!plugin.isGriefPreventionIntegrated())
	        return;

	    for (Block block : e.getNewAccountLocation())
            if (isBlockedByGriefPrevention(e.getPlayer(), block)) {
                e.setCancelled(true);
                plugin.debug("Account recover event cancelled by GriefPrevention.");
                return;
            }
    }

    private boolean isBlockedByGriefPrevention(Player player, Block block) {
        Claim claim = griefPrevention.dataStore.getClaimAt(block.getLocation(), false, null);
        return claim != null && claim.allowContainers(player) != null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBankRegionInteract(PlayerInteractEvent e) {
        if (!plugin.isGriefPreventionIntegrated())
            return;
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
            return;
	    if (e.getHand() != EquipmentSlot.HAND)
	        return;

        if (e.getPlayer().getInventory().getItemInMainHand().getType() != griefPrevention.config_claims_investigationTool)
            return;

        Block clickedBlock = null;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
            clickedBlock = e.getClickedBlock();
        else if (e.getAction() == Action.RIGHT_CLICK_AIR)
            clickedBlock = e.getPlayer().getTargetBlock(null, 300);
        if (clickedBlock == null || clickedBlock.getType() == Material.AIR)
            return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK &&
                clickedBlock.getType() != Material.CHEST && clickedBlock.getType() != Material.TRAPPED_CHEST)
            return;

        Bank bank = plugin.getBankService().findContaining(clickedBlock);
        if (bank == null)
            return;

        new BankVisualization(plugin, bank).show(e.getPlayer());
    }

}
