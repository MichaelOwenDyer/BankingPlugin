package com.monst.bankingplugin.entity.geo.location;

import com.monst.bankingplugin.converter.WorldConverter;
import com.monst.bankingplugin.entity.AbstractEntity;
import com.monst.bankingplugin.entity.geo.Vector3;
import com.monst.bankingplugin.util.Utils;
import jakarta.persistence.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import java.util.Optional;

@Entity
@Table(name = "account_location")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "size")
public abstract class AccountLocation extends AbstractEntity implements Iterable<Block> {

    public static AccountLocation toAccountLocation(InventoryHolder ih) {
        if (ih instanceof DoubleChest)
            return new DoubleAccountLocation((DoubleChest) ih);
        else if (ih instanceof Chest)
            return new SingleAccountLocation((Chest) ih);
        throw new IllegalArgumentException("InventoryHolder must be a chest!");
    }

    @Convert(converter = WorldConverter.class)
    World world;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "x1")),
            @AttributeOverride(name = "z", column = @Column(name = "z1"))
    })
    Vector3 v1;

    public AccountLocation() {}

    AccountLocation(World world, Vector3 v1) {
        this.generateID();
        this.world = world;
        this.v1 = v1;
    }

    public abstract Vector3 getMinimumBlock();

    public abstract Vector3 getMaximumBlock();

    public abstract Optional<InventoryHolder> findChest();

    InventoryHolder getChestAt(Vector3 vector) {
        if (world == null)
            return null;
        Block block = vector.toBlock(world);
        if (!Utils.isChest(block))
            return null;
        return ((Chest) block.getState()).getInventory().getHolder();
    }

    /**
     * Ensures that the account chest is openable.
     *
     * @see Utils#isTransparent(Block)
     */
    public boolean isBlocked() {
        for (Block chestSide : this)
            if (!Utils.isTransparent(chestSide.getRelative(BlockFace.UP)))
                return true;
        return false;
    }

    public World getWorld() {
        return world;
    }

    public int getY() {
        return v1.getY();
    }

    public boolean contains(Vector3 v) {
        if (v.getY() != getY())
            return false;
        for (Block chest : this)
            if (chest.getX() == v.getX() && chest.getZ() == v.getZ())
                return true;
        return false;
    }

    public abstract Location getTeleportLocation();

    public abstract byte getSize();

}
