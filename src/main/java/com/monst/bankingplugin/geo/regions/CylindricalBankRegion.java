package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.Vector2D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CylindricalBankRegion extends BankRegion {

    public static CylindricalBankRegion of(World world, Vector2D center, int radiusX, int radiusZ, int y1, int y2) {
        y1 = Math.min(Math.max(0, y1), world.getMaxHeight()); // Ensure y1 is between 0 and world.getMaxHeight()
        y2 = Math.min(Math.max(0, y2), world.getMaxHeight()); // Ensure y2 is between 0 and world.getMaxHeight()
        return new CylindricalBankRegion(
                world,
                center,
                new Vector2D(Math.abs(radiusX), Math.abs(radiusZ)),
                Math.min(y1, y2),
                Math.max(y1, y2)
        );
    }

    private final Vector2D center;
    private final Vector2D radius;
    private final int minY;
    private final int maxY;

    private CylindricalBankRegion(World world, Vector2D center, Vector2D radius, int minY, int maxY) {
        super(world);
        this.center = center;
        this.radius = radius;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Block getCenterPoint() {
        return world.getBlockAt(getCenterX(), (minY + maxY) / 2, getCenterZ());
    }

    @Override
    public int getMinX() {
        return getCenterX() - getRadiusX();
    }

    @Override
    public int getMaxX() {
        return getCenterX() + getRadiusX();
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
        return getCenterZ() - getRadiusZ();
    }

    @Override
    public int getMaxZ() {
        return getCenterZ() + getRadiusZ();
    }

    public int getCenterX() {
        return center.getX();
    }

    public int getCenterZ() {
        return center.getZ();
    }

    public int getRadiusX() {
        return radius.getX();
    }

    public int getRadiusZ() {
        return radius.getZ();
    }

    @Override
    public String getCoordinates() {
        return ""; // TODO
    }

    @Override
    public long getVolume() {
        return (long) (Math.PI * getRadiusX() * getRadiusZ() * getHeight());
    }

    @Override
    public boolean overlaps(BankRegion region) {
        if (isDisjunct(region))
            return false;
        Set<Vector2D> footprint = region.getFootprint();
        return getFootprint().stream().anyMatch(footprint::contains);
    }

    /**
     * Calculates (x−h)^2/rx^2+(y−k)^2/ry^2≤1 with center (h, k) and radii (rx, ry)
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @return whether the specified point lies within the region
     */
    @Override
    public boolean contains(int x, int z) {
        return overlapsX(x, x) && overlapsZ(z, z) &&
                  (x - getCenterX()) * (x - getCenterX()) + (z - getCenterZ()) * (z - getCenterZ())
                          <= getRadiusX() * getRadiusX() * getRadiusZ() * getRadiusZ();
    }

    @Override
    public List<Location> getCorners() {
        return Arrays.asList(
                new Location(world, getCenterX() + getRadiusX(), minY, getCenterZ()),
                new Location(world, getCenterX() + getRadiusX(), maxY, getCenterZ()),
                new Location(world, getCenterX(), minY, getCenterZ() + getRadiusZ()),
                new Location(world, getCenterX(), maxY, getCenterZ() + getRadiusZ()),
                new Location(world, getCenterX() - getRadiusX(), minY, getCenterZ()),
                new Location(world, getCenterX() - getRadiusX(), maxY, getCenterZ()),
                new Location(world, getCenterX(), minY, getCenterZ() - getRadiusZ()),
                new Location(world, getCenterX(), maxY, getCenterZ() - getRadiusZ())
        );
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
                && getRadiusX() == other.getRadiusX()
                && getRadiusZ() == other.getRadiusZ()
                && Objects.equals(center, other.center)
                && Objects.equals(world, other.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCenterX(), getCenterZ(), getRadiusX(), getRadiusZ(), minY, maxY, world);
    }

}
