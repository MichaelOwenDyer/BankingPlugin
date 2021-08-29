package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountExtendEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.listeners.BankingPluginListener;
import com.monst.bankingplugin.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GriefPreventionListener extends BankingPluginListener {

    private final GriefPrevention griefPrevention;

	public GriefPreventionListener(BankingPlugin plugin) {
        super(plugin);
        this.griefPrevention = plugin.getGriefPrevention();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAccountCreate(AccountCreateEvent e) {
        if (!plugin.isGriefPreventionIntegrated())
            return;

        for (Block block : e.getAccount().getLocation())
			if (isBlockedByGriefPrevention(e.getPlayer(), block)) {
				e.setCancelled(true);
				plugin.debug("Account create event cancelled by GriefPrevention");
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
                plugin.debug("Account extend event cancelled by GriefPrevention");
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
                plugin.debug("Account migrate event cancelled by GriefPrevention");
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
                plugin.debug("Account recover event cancelled by GriefPrevention");
                return;
            }
    }

    private boolean isBlockedByGriefPrevention(Player player, Block block) {
        Claim claim = griefPrevention.dataStore.getClaimAt(block.getLocation(), false, null);
        if (claim == null)
            return false;
        return claim.allowContainers(player) != null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBankRegionInteract(PlayerInteractEvent e) {
        if (!plugin.isGriefPreventionIntegrated())
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
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && Utils.isChest(clickedBlock))
            return;

        Bank bank = plugin.getBankRepository().getAt(clickedBlock);
        if (bank == null)
            return;

        VisualizationManager.visualizeRegion(e.getPlayer(), bank);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankCreate(BankCreateEvent e) {
	    if (!plugin.isGriefPreventionIntegrated())
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
            VisualizationManager.visualizeRegion(((Player) executor), e.getBank());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankRemove(BankRemoveEvent e) {
	    if (!plugin.isGriefPreventionIntegrated())
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
            VisualizationManager.revertVisualization((Player) executor);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBankResize(BankResizeEvent e) {
	    if (!plugin.isGriefPreventionIntegrated())
	        return;
        CommandSender executor = e.getExecutor();
        if (executor instanceof Player)
	        VisualizationManager.visualizeRegion(((Player) executor), e.getNewRegion(), e.getBank().isAdminBank());
    }

}
