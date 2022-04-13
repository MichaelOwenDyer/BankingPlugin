package com.monst.bankingplugin.entity.geo.location;

import com.monst.bankingplugin.entity.geo.Vector2;
import com.monst.bankingplugin.entity.geo.Vector3;
import jakarta.persistence.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

@Entity
@DiscriminatorValue(value = "Double")
public class DoubleAccountLocation extends AccountLocation {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "x2")),
            @AttributeOverride(name = "z", column = @Column(name = "z2"))
    })
    private Vector2 v2;

    public DoubleAccountLocation() {}

    public DoubleAccountLocation(DoubleChest dc) {
        super(dc.getWorld(), new Vector3(((Chest) dc.getLeftSide()).getBlock()));
        this.v2 = new Vector2(((Chest) dc.getRightSide()).getBlock());
    }

    public DoubleAccountLocation(Block block, BlockFace direction) {
        super(block.getWorld(), new Vector3(block));
        this.v2 = new Vector2(block.getRelative(direction));
    }

    @Override
    public Vector3 getMinimumBlock() {
        BlockFace direction = v1.getFace(v2);
        if (direction == BlockFace.SOUTH || direction == BlockFace.EAST)
            return v1;
        return v2.toVector3(v1.getY());
    }

    @Override
    public Vector3 getMaximumBlock() {
        BlockFace direction = v1.getFace(v2);
        if (direction == BlockFace.NORTH || direction == BlockFace.WEST)
            return v1;
        return v2.toVector3(v1.getY());
    }

    @Override
    public Optional<InventoryHolder> findChest() {
        InventoryHolder ih1 = getChestAt(v1);
        InventoryHolder ih2 = getChestAt(v2.toVector3(v1.getY()));
        if (ih1 == null || ih2 == null || !isSameChest(ih1, ih2))
            return Optional.empty();
        return Optional.of(ih1);
    }

    private boolean isSameChest(InventoryHolder ih1, InventoryHolder ih2) {
        Location loc1 = ih1.getInventory().getLocation();
        Location loc2 = ih2.getInventory().getLocation();
        return Objects.equals(loc1.getBlock(), loc2.getBlock());
    }

    @Override
    public Iterator<Block> iterator() {
        return Arrays.asList(getMinimumBlock().toBlock(world), getMaximumBlock().toBlock(world)).iterator();
    }

    @Override
    public Location getTeleportLocation() {
        switch (v1.getFace(v2)) {
            case NORTH:
            case SOUTH:
                return new Location(getWorld(), v1.getX() + 0.5, v1.getY() + 1, getMaximumBlock().getZ());
            case EAST:
            case WEST:
                return new Location(getWorld(), getMaximumBlock().getX(), v1.getY() + 1, v1.getZ() + 0.5);
        }
        return null;
    }

    public SingleAccountLocation contract(Block block) {
        Vector3 leaveBehind = new Vector3(block);
        if (Objects.equals(v1, leaveBehind))
            return new SingleAccountLocation(world, v2.toVector3(v1.getY()));
        if (Objects.equals(v2.toVector3(v1.getY()), leaveBehind))
            return new SingleAccountLocation(world, v1);
        throw new IllegalArgumentException("Block not contained in AccountLocation cannot be removed!");
    }

    @Override
    public byte getSize() {
        return 2;
    }

    public String toString() {
        return "(" +
                v1.getX() + ", " +
                v1.getY() + ", " +
                v1.getZ() +
                "), (" +
                v2.getX() + ", " +
                v1.getY() + ", " +
                v2.getZ() +
                ")";
    }

}
