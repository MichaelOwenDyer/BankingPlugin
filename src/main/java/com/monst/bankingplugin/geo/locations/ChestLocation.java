package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.repository.BankRepository;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public abstract class ChestLocation implements Iterable<Location> {

    private static final BankRepository BANK_REPO = BankingPlugin.getInstance().getBankRepository();
    private static final AccountRepository ACCOUNT_REPO = BankingPlugin.getInstance().getAccountRepository();

    public static ChestLocation from(InventoryHolder ih) {
        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Location leftLoc = ((Chest) dc.getLeftSide()).getLocation();
            Location rightLoc = ((Chest) dc.getRightSide()).getLocation();
            BlockVector3D leftBV = BlockVector3D.fromLocation(leftLoc);
            BlockVector3D rightBV = BlockVector3D.fromLocation(rightLoc);
            return DoubleChestLocation.of(dc.getWorld(), leftBV, rightBV);
        } else {
            Chest chest = (Chest) ih;
            Location loc = chest.getLocation();
            BlockVector3D bv = BlockVector3D.fromLocation(loc);
            return SingleChestLocation.of(chest.getWorld(), bv);
        }
    }

    public static ChestLocation of(World world, BlockVector3D v1, BlockVector3D v2) {
        if (Objects.equals(v1, v2))
            return SingleChestLocation.of(world, v1);
        return DoubleChestLocation.of(world, v1, v2);
    }

    final World world;
    final BlockVector3D v1;

    ChestLocation(World world, BlockVector3D v1) {
        if (world == null)
            throw new IllegalArgumentException("World must not be null!");
        this.world = world;
        this.v1 = v1;
    }

    public BlockVector3D getMinimumBlock() {
        return v1;
    }

    public BlockVector3D getMaximumBlock() {
        return getMinimumBlock();
    }

    public Location getMinimumLocation() {
        return v1.toLocation(world);
    }

    public Location getMaximumLocation() {
        return getMinimumLocation();
    }

    public World getWorld() {
        return world;
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
        for (Location chest : this)
            if (!Utils.isTransparent(chest.getBlock().getRelative(BlockFace.UP)))
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
        for (Location chest : this)
            if (!Utils.isTransparent(chest.getBlock().getRelative(BlockFace.UP)))
                throw new ChestBlockedException(chest.getBlock());
    }

    public InventoryHolder findInventoryHolder() throws ChestNotFoundException {
        Block b = getMinimumLocation().getBlock();
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)
            throw new ChestNotFoundException(this);
        return ((Chest) b.getState()).getInventory().getHolder();
    }

    public boolean contains(Location loc) {
        if (!Objects.equals(getWorld(), loc.getWorld()))
            return false;
        BlockVector3D bv = BlockVector3D.fromLocation(loc);
        for (Location chest : this)
            if (Objects.equals(BlockVector3D.fromLocation(chest), bv))
                return true;
        return false;
    }

    public Iterator<Location> iterator() {
        return Arrays.stream(getLocations()).iterator();
    }

    public boolean isSingle() {
        return getSize() == 1;
    }

    public boolean isDouble() {
        return getSize() == 2;
    }

    public abstract Location getTeleportLocation();

    public abstract Location[] getLocations();

    public abstract byte getSize();

    public abstract String toString();

}
