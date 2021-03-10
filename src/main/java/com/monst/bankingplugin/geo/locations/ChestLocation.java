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
import java.util.Optional;

public abstract class ChestLocation implements Iterable<Location> {

    private static final BankRepository BANK_REPO = BankingPlugin.getInstance().getBankRepository();
    private static final AccountRepository ACCOUNT_REPO = BankingPlugin.getInstance().getAccountRepository();

    public static ChestLocation from(Chest c) {
        BlockVector3D[] locations = Utils.getChestCoordinates(c);
        if (locations.length == 1)
            return new SingleChestLocation(c.getWorld(), locations[0]);
        return new DoubleChestLocation(c.getWorld(), locations[0], locations[1]);
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

    public Bank getBank() throws BankNotFoundException {
        return Optional.ofNullable(BANK_REPO.getAt(this)).orElseThrow(() -> new BankNotFoundException(this));
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

    public Inventory findInventory() throws ChestNotFoundException {
        Chest chest = Utils.getChestAt(getMinimumLocation().getBlock());
        if (chest == null)
            throw new ChestNotFoundException(this);
        return chest.getInventory();
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

    public abstract Location[] getLocations();

    public abstract byte getSize();

    public abstract String toString();

}
