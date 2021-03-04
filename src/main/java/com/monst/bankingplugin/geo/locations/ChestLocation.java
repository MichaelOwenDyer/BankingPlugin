package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.exceptions.AccountNotFoundException;
import com.monst.bankingplugin.exceptions.BankNotFoundException;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.utils.AccountRepository;
import com.monst.bankingplugin.utils.BankRepository;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Iterator;

public abstract class ChestLocation implements Iterable<Location> {

    private static final BankRepository BANK_REPO = BankingPlugin.getInstance().getBankRepository();
    private static final AccountRepository ACCOUNT_REPO = BankingPlugin.getInstance().getAccountRepository();

    public static ChestLocation from(Chest c) {
        World world = c.getWorld();
        if (world == null)
            throw new IllegalArgumentException("World must not be null!");
        BlockVector3D[] locations = Utils.getChestCoordinates(c);
        if (locations.length == 1)
            return new SingleChestLocation(world, locations[0]);
        Arrays.sort(locations);
        return new DoubleChestLocation(world, locations[0], locations[1]);
    }

    public static SingleChestLocation single(Chest c) {
        World world = c.getWorld();
        if (world == null)
            throw new IllegalArgumentException("World must not be null!");
        return new SingleChestLocation(world, BlockVector3D.fromLocation(c.getLocation()));
    }

    final World world;
    final BlockVector3D v1;

    ChestLocation(World world, BlockVector3D v1) {
        this.world = world;
        this.v1 = v1;
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

    public Bank getBank() throws BankNotFoundException {
        return BANK_REPO.getAt(this);
    }

    public Account getAccount() throws AccountNotFoundException {
        return ACCOUNT_REPO.getAt(this);
    }

    /**
     * Checks to see if the chest is blocked
     *
     * @return true if the chest is blocked and cannot be opened.
     * @see Utils#isTransparent(Block)
     */
    public boolean isBlocked() {
        try {
            checkSpaceAbove();
        } catch (ChestBlockedException e) {
            return true;
        }
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

    public boolean contains(Location loc) {
        if (!world.equals(loc.getWorld()))
            return false;
        BlockVector3D bv = BlockVector3D.fromLocation(loc);
        for (Location chest : this)
            if (BlockVector3D.fromLocation(chest).equals(bv))
                return true;
        return false;
    }

    public Iterator<Location> iterator() {
        return Arrays.asList(getLocations()).iterator();
    }

    public boolean isSingle() {
        return getSize() == 1;
    }

    public boolean isDouble() {
        return getSize() == 2;
    }

    public abstract Location getTeleportLocation();

    public abstract Inventory findInventory() throws ChestNotFoundException;

    public abstract Location[] getLocations();

    public abstract byte getSize();

}
