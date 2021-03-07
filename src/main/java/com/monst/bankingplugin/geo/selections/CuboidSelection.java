package com.monst.bankingplugin.geo.selections;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents a region of space in the shape of a rectangular prism. It is defined by a {@link World} and
 * a minimum and a maximum {@link BlockVector3D} point in space.
 */
public class CuboidSelection extends Selection {

	private final BlockVector3D min;
	private final BlockVector3D max;

	/**
	 * Creates a new {@link CuboidSelection} with the specified attributes
	 *
	 * @param world the world the selection is in
	 * @param loc1 the first corner bound (any combination of upper/lower x, y, z values)
	 * @param loc2 the other corner bound
	 * @return a new CuboidSelection
	 */
	public static CuboidSelection of(World world, BlockVector3D loc1, BlockVector3D loc2) {
		BlockVector3D min = new BlockVector3D(
				Math.min(loc1.getX(), loc2.getX()),
				Math.min(loc1.getY(), loc2.getY()),
				Math.min(loc1.getZ(), loc2.getZ())
		);
		BlockVector3D max = new BlockVector3D(
				Math.max(loc1.getX(), loc2.getX()),
				Math.max(loc1.getY(), loc2.getY()),
				Math.max(loc1.getZ(), loc2.getZ())
		);
		return new CuboidSelection(world, min, max);
	}

	private CuboidSelection(World world, BlockVector3D min, BlockVector3D max) {
		super(world);
		this.min = min;
		this.max = max;
	}

	@Override
	public BlockVector3D getMinimumPoint() {
		return min;
	}

	@Override
	public BlockVector3D getMaximumPoint() {
		return max;
	}

	@Override
	public BlockVector3D getCenterPoint() {
		int centerX = (getMaxX() + getMinX()) / 2;
		int centerY = (getMaxY() + getMinZ()) / 2;
		int centerZ = (getMaxZ() + getMinZ()) / 2;
		return new BlockVector3D(centerX, centerY, centerZ);
	}

	@Override
	public int getMinX() { return min.getX(); }
	@Override
	public int getMaxX() { return max.getX(); }
	@Override
	public int getMinY() { return min.getY(); }
	@Override
	public int getMaxY() { return max.getY(); }
	@Override
	public int getMinZ() { return min.getZ(); }
	@Override
	public int getMaxZ() { return max.getZ(); }

	@Override
	public String getCoordinates() {
		return "(" + getMinX() + ", " + getMinY() + ", " + getMinZ()
				+ ") -> ("
				+ getMaxX() + ", " + getMaxY() + ", " + getMaxZ() + ")";
	}

	@Override
	public long getVolume() {
		return (long) (getMaxX() - getMinX() + 1)
			 * (getMaxY() - getMinY() + 1)
			 * (getMaxZ() - getMinZ() + 1);
	}

	@Override
	public boolean overlaps(Selection sel) {
		if (isDisjunct(sel))
			return false;
		Set<BlockVector2D> blocks = getFootprint();
		return sel.getFootprint().stream().anyMatch(blocks::contains);
	}

	@Override
	public Set<BlockVector2D> getFootprint() {
		Set<BlockVector2D> blocks = new HashSet<>();
		for (int x = getMinX(); x <= getMaxX(); x++)
			for (int z = getMinZ(); z <= getMaxZ(); z++)
				blocks.add(new BlockVector2D(x, z));
		return blocks;
	}

	@Override
	public Collection<BlockVector3D> getCorners() {
		Set<BlockVector3D> vertices = new HashSet<>();
		for (int x : new int[] {getMinX(), getMaxX()})
			for (int y : new int[] {getMinY(), getMaxY()})
				for (int z : new int[] {getMinZ(), getMaxZ()})
					vertices.add(new BlockVector3D(x, y, z));
		return vertices;
	}

	@Override
	public boolean contains(Location loc) {
		if (!getWorld().equals(loc.getWorld()))
			return false;
		return contains(BlockVector3D.fromLocation(loc));
	}

	@Override
	public boolean contains(BlockVector3D bv) {
		int x = bv.getX();
		int y = bv.getY();
		int z = bv.getZ();
		return x <= getMaxX() && x >= getMinX()
				&& y <= getMaxY() && y >= getMinY()
				&& z <= getMaxZ() && z >= getMinZ();
	}

	@Override
	public boolean contains(BlockVector2D bv) {
		return bv.getX() <= getMaxX()
			&& bv.getX() >= getMinX()
			&& bv.getZ() <= getMaxZ()
			&& bv.getZ() >= getMinZ();
	}

	@Override
	public boolean isCuboid() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CuboidSelection otherSel = (CuboidSelection) o;
		return getWorld().equals(otherSel.getWorld())
				&& getMaximumPoint().equals(otherSel.getMaximumPoint())
				&& getMinimumPoint().equals(otherSel.getMinimumPoint());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getWorld(), getMinX(), getMaxX(), getMinY(), getMaxY(), getMinZ(), getMaxZ());
	}

}
