package com.monst.bankingplugin.entity.geo.location;

import com.monst.bankingplugin.entity.geo.Vector3;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

@Entity
@DiscriminatorValue(value = "Single")
public class SingleAccountLocation extends AccountLocation {

    public SingleAccountLocation() {}

    public SingleAccountLocation(Chest chest) {
        super(chest.getWorld(), new Vector3(chest.getBlock()));
    }

    public SingleAccountLocation(World world, Vector3 b1) {
        super(world, b1);
    }

    @Override
    public Vector3 getMinimumBlock() {
        return v1;
    }

    @Override
    public Vector3 getMaximumBlock() {
        return v1;
    }

    @Override
    public Optional<InventoryHolder> findChest() {
        return Optional.ofNullable(getChestAt(v1));
    }

    @Override
    public Iterator<Block> iterator() {
        return Collections.singleton(v1.toBlock(world)).iterator();
    }

    @Override
    public Location getTeleportLocation() {
        return v1.toLocation(world).add(0.5, 1, 0.5);
    }

    @Override
    public byte getSize() {
        return 1;
    }

    public String toString() {
        return "(" +
                v1.getX() + ", " +
                v1.getY() + ", " +
                v1.getZ() +
                ")";
    }

}
