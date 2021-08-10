package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class SingleAccountLocation extends AccountLocation {

    public SingleAccountLocation(@Nonnull Block block) {
        super(block);
    }

    @Override
    public Block getMinimumBlock() {
        return b1;
    }

    @Override
    public Block getMaximumBlock() {
        return b1;
    }

    @Override
    public InventoryHolder findChest() throws ChestNotFoundException {
        return Optional.ofNullable(getChestAt(b1)).orElseThrow(() -> new ChestNotFoundException(this));
    }

    @Override
    public Iterator<Block> iterator() {
        return Collections.singleton(b1).iterator();
    }

    @Override
    public Location getTeleportLocation() {
        return b1.getLocation().add(0.5, 1, 0.5);
    }

    @Override
    public byte getSize() {
        return 1;
    }

    public DoubleAccountLocation extend(BlockFace direction) {
        return new DoubleAccountLocation(b1, direction);
    }

    public String toString() {
        return "(" +
                b1.getX() + ", " +
                b1.getY() + ", " +
                b1.getZ() +
                ")";
    }

}
