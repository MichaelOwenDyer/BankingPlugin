package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.repository.BankRepository;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public abstract class ChestLocation implements Iterable<Block> {

    private static final BankRepository BANK_REPO = BankingPlugin.getInstance().getBankRepository();
    private static final AccountRepository ACCOUNT_REPO = BankingPlugin.getInstance().getAccountRepository();

    public static ChestLocation from(InventoryHolder ih) {
        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Block leftLoc = ((Chest) dc.getLeftSide()).getBlock();
            Block rightLoc = ((Chest) dc.getRightSide()).getBlock();
            return new DoubleChestLocation(leftLoc, leftLoc.getFace(rightLoc));
        } else if (ih instanceof Chest)
            return new SingleChestLocation(((Chest) ih).getBlock());
        throw new IllegalArgumentException("InventoryHolder must be a chest!");
    }

    public static ChestLocation at(World world, int y, int x1, int z1, int x2, int z2) {
        Block b1 = world.getBlockAt(x1, y, z1);
        if (x1 == x2 && z1 == z2)
            return new SingleChestLocation(b1);
        Block b2 = world.getBlockAt(x2, y, z2);
        return new DoubleChestLocation(b1, b1.getFace(b2));
    }

    Block b1;

    ChestLocation(Block b1) {
        this.b1 = b1;
    }

    public abstract Block getMinimumBlock();

    public abstract Block getMaximumBlock();

    public World getWorld() {
        return b1.getWorld();
    }

    public Bank getBank() {
        return Optional.ofNullable(BANK_REPO.getAt(this)).orElse(null);
    }

    public Account getAccount() {
        return ACCOUNT_REPO.getAt(this);
    }

    /**
     * Checks to see if the chest is blocked
     *
     * @return true if the chest is blocked and cannot be opened.
     * @see Utils#isTransparent(Block)
     */
    public boolean isBlocked() {
        for (Block chestSide : this)
            if (!Utils.isTransparent(chestSide.getRelative(BlockFace.UP)))
                return true;
        return false;
    }

    /**
     * Ensures that the account chest is able to be opened.
     *
     * @throws ChestBlockedException if the chest cannot be opened.
     * @see Utils#isTransparent(Block)
     */
    public void checkSpaceAbove() throws ChestBlockedException {
        for (Block chestSide : this)
            if (!Utils.isTransparent(chestSide.getRelative(BlockFace.UP)))
                throw new ChestBlockedException(chestSide);
    }

    public abstract InventoryHolder findInventoryHolder() throws ChestNotFoundException;

    public static InventoryHolder getInventoryHolderAt(Block b) {
        if (!Utils.isChest(b))
            return null;
        return ((Chest) b.getState()).getInventory().getHolder();
    }

    public boolean contains(Block b) {
        for (Block chestSide : this)
            if (Objects.equals(chestSide, b))
                return true;
        return false;
    }

    public abstract Iterator<Block> iterator();

    public abstract byte getSize();

    public abstract Location getTeleportLocation();

}
