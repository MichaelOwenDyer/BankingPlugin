package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.Vector2D;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.Set;

public class CylindricalBankRegion extends BankRegion {

    public static CylindricalBankRegion of(World world, Vector2D center, int y1, int y2, int radius) {
        y1 = Math.min(Math.max(0, y1), world.getMaxHeight()); // Ensure y1 is between 0 and world.getMaxHeight()
        y2 = Math.min(Math.max(0, y2), world.getMaxHeight()); // Ensure y2 is between 0 and world.getMaxHeight()
        return new CylindricalBankRegion(
                world,
                center,
                Math.min(y1, y2),
                Math.max(y1, y2),
                Math.abs(radius)
        );
    }

    private final Vector2D center;
    private final int minY;
    private final int maxY;
    private final int radius;

    private CylindricalBankRegion(World world, Vector2D center, int minY, int maxY, int radius) {
        super(world);
        this.center = center;
        this.minY = minY;
        this.maxY = maxY;
        this.radius = radius;
    }

    @Override
    public Block getCenterPoint() {
        return world.getBlockAt(center.getX(), (minY + maxY) / 2, center.getZ());
    }

    @Override
    public int getMinX() {
        return center.getX() - radius;
    }

    @Override
    public int getMaxX() {
        return center.getX() + radius;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public int getMinZ() {
        return center.getZ() - radius;
    }

    @Override
    public int getMaxZ() {
        return center.getZ() + radius;
    }

    @Override
    public String getCoordinates() {
        return null;
    }

    @Override
    public long getVolume() {
        return (long) Math.PI * radius * radius * (maxY - minY + 1);
    }

    @Override
    public boolean overlaps(BankRegion region) {
        if (isDisjunct(region))
            return false;
        Set<Vector2D> footprint = region.getFootprint();
        return getFootprint().stream().anyMatch(footprint::contains);
    }

    @Override
    public boolean contains(int x, int z) {
        return !isDisjunctX(x, x) && !isDisjunctZ(z, z) && Math.hypot(center.getX() - x, center.getZ() - z) < radius;
    }

    @Override
    public Vector2D[] getVertices() {
        return new Vector2D[0];
    }

    @Override
    public boolean isCylindrical() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CylindricalBankRegion other = (CylindricalBankRegion) o;
        return     minY == other.minY
                && maxY == other.maxY
                && radius == other.radius
                && Objects.equals(center, other.center)
                && Objects.equals(world, other.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radius, minY, maxY, world);
    }

}
