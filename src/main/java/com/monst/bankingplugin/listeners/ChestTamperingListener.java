package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;

@SuppressWarnings("unused")
public class ChestTamperingListener extends BankingPluginListener {

    private final AccountRepository accountRepo;

    public ChestTamperingListener(BankingPlugin plugin) {
        super(plugin);
        this.accountRepo = plugin.getAccountRepository();
    }

    @EventHandler(priority = EventPriority.HIGH)
	public void onAccountBlocked(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced();
        if (Utils.isTransparent(b))
            return;
        if (accountRepo.isAccount(b.getRelative(BlockFace.DOWN))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        for (BlockState blockState : e.getReplacedBlockStates())
            if (accountRepo.isAccount(blockState.getBlock().getRelative(BlockFace.DOWN))) {
                e.setCancelled(true);
                return;
            }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
		// If the piston did not push any blocks
        Block airAfterPiston = e.getBlock().getRelative(e.getDirection());
        if (accountRepo.isAccount(airAfterPiston.getRelative(BlockFace.DOWN))) {
            e.setCancelled(true);
            return;
        }

        for (Block b : e.getBlocks()) {
            if (Utils.isTransparent(b))
                continue;
            Block blockToBeCovered = b.getRelative(e.getDirection()).getRelative(BlockFace.DOWN);
            if (accountRepo.isAccount(blockToBeCovered)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            Block blockToBeCovered = b.getRelative(e.getDirection()).getRelative(BlockFace.DOWN);
            if (accountRepo.isAccount(blockToBeCovered)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLiquidFlow(BlockFromToEvent e) {
        if (accountRepo.isAccount(e.getToBlock().getRelative(BlockFace.DOWN)))
        	e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Block clicked = e.getBlockClicked();
        Block underWater = clicked.getRelative(BlockFace.DOWN).getRelative(e.getBlockFace());
        if (accountRepo.isAccount(clicked) || accountRepo.isAccount(underWater))
        	e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent e) {
        for (BlockState state : e.getBlocks()) {
            Block newBlock = state.getBlock();
            Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
            if (accountRepo.isAccount(newBlock) || accountRepo.isAccount(belowNewBlock)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent e) {
        Block newBlock = e.getNewState().getBlock();
        Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
        if (accountRepo.isAccount(newBlock) || accountRepo.isAccount(belowNewBlock))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent e) {
        Block newBlock = e.getNewState().getBlock();
        Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
        if (accountRepo.isAccount(newBlock) || accountRepo.isAccount(belowNewBlock))
            e.setCancelled(true);
    }

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(accountRepo::isAccount);
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(accountRepo::isAccount);
	}

}
