package com.monst.bankingplugin.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.ItemUtils;

public class AccountTamperingListener implements Listener {

    private AccountUtils accountUtils;

    public AccountTamperingListener(BankingPlugin plugin) {
        this.accountUtils = plugin.getAccountUtils();
    }

    @EventHandler(priority = EventPriority.HIGH)
	public void onAccountBlocked(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced();
        Block below = b.getRelative(BlockFace.DOWN);

		if (ItemUtils.isTransparent(b))
			return;

        if (accountUtils.isAccount(below.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        for (BlockState blockState : e.getReplacedBlockStates()) {
            Block below = blockState.getBlock().getRelative(BlockFace.DOWN);
            if (accountUtils.isAccount(below.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
		// If the piston did not push any blocks
        Block airAfterPiston = e.getBlock().getRelative(e.getDirection());
        Block belowAir = airAfterPiston.getRelative(BlockFace.DOWN);
        if (accountUtils.isAccount(belowAir.getLocation())) {
            e.setCancelled(true);
            return;
        }

        for (Block b : e.getBlocks()) {
            Block newBlock = b.getRelative(e.getDirection());
            Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
            if (accountUtils.isAccount(belowNewBlock.getLocation())) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            Block newBlock = b.getRelative(e.getDirection());
            Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
            if (accountUtils.isAccount(belowNewBlock.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLiquidFlow(BlockFromToEvent e) {
        Block b = e.getToBlock();
        Block below = b.getRelative(BlockFace.DOWN);

        if (accountUtils.isAccount(below.getLocation())) 
        	e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Block clicked = e.getBlockClicked();
        Block underWater = clicked.getRelative(BlockFace.DOWN).getRelative(e.getBlockFace());

        if (accountUtils.isAccount(clicked.getLocation()) || accountUtils.isAccount(underWater.getLocation()))
        	e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent e) {
        for (BlockState state : e.getBlocks()) {
            Block newBlock = state.getBlock();
            if (accountUtils.isAccount(newBlock.getLocation()) || accountUtils.isAccount(newBlock.getRelative(BlockFace.DOWN).getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent e) {
        Block newBlock = e.getNewState().getBlock();
        if (accountUtils.isAccount(newBlock.getLocation()) || accountUtils.isAccount(newBlock.getRelative(BlockFace.DOWN).getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent e) {
        Block newBlock = e.getNewState().getBlock();
        if (accountUtils.isAccount(newBlock.getLocation()) || accountUtils.isAccount(newBlock.getRelative(BlockFace.DOWN).getLocation())) {
            e.setCancelled(true);
        }
    }

}
