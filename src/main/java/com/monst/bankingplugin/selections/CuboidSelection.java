package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
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
				Math.min(loc1.getBlockX(), loc2.getBlockX()),
				Math.min(loc1.getBlockY(), loc2.getBlockY()),
				Math.min(loc1.getBlockZ(), loc2.getBlockZ())
		);
		BlockVector3D max = new BlockVector3D(
				Math.max(loc1.getBlockX(), loc2.getBlockX()),
				Math.max(loc1.getBlockY(), loc2.getBlockY()),
				Math.max(loc1.getBlockZ(), loc2.getBlockZ())
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
	public int getMinX() { return min.getBlockX(); }
	@Override
	public int getMaxX() { return max.getBlockX(); }
	@Override
	public int getMinY() { return min.getBlockY(); }
	@Override
	public int getMaxY() { return max.getBlockY(); }
	@Override
	public int getMinZ() { return min.getBlockZ(); }
	@Override
	public int getMaxZ() { return max.getBlockZ(); }

	@Override
	public String getCoordinates() {
		return "(" + getMinX() + ", " + getMinZ() + ", " + getMinZ()
				+ ") -> (" 
				+ getMaxX() + ", " + getMaxY() + ", " + getMaxZ() + ")";
	}

	@Override
	public long getVolume() {
		return (getMaxX() - getMinX() + 1)
			 * (getMaxY() - getMinY() + 1)
			 * (getMaxZ() - getMinZ() + 1);
	}

	@Override
	public boolean overlaps(Selection sel) {
		if (getMinY() > sel.getMaxY() || getMaxY() < sel.getMinY())
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
		vertices.add(min);
		vertices.add(new BlockVector3D(getMaxX(), getMinZ(), getMinZ()));
		vertices.add(new BlockVector3D(getMinX(), getMaxY(), getMinZ()));
		vertices.add(new BlockVector3D(getMaxX(), getMaxY(), getMinZ()));
		vertices.add(new BlockVector3D(getMinX(), getMinZ(), getMaxZ()));
		vertices.add(new BlockVector3D(getMaxX(), getMinZ(), getMaxZ()));
		vertices.add(new BlockVector3D(getMinX(), getMaxY(), getMaxZ()));
		vertices.add(max);
		return vertices;
	}

	@Override
	public boolean contains(Location loc) {
		if (loc.getWorld() != null && !loc.getWorld().equals(getWorld()))
			return false;
		return contains(BlockVector3D.fromLocation(loc));
	}

	@Override
	public boolean contains(BlockVector3D bv) {
		int x = bv.getBlockX();
		int y = bv.getBlockY();
		int z = bv.getBlockZ();
		return x <= getMaxX() && x >= getMinX()
				&& y <= getMaxY() && y >= getMinZ()
				&& z <= getMaxZ() && z >= getMinZ();
	}

	@Override
	public boolean contains(BlockVector2D bv) {
		return bv.getBlockX() <= getMaxX()
			&& bv.getBlockX() >= getMinX()
			&& bv.getBlockZ() <= getMaxZ()
			&& bv.getBlockZ() >= getMinZ();
	}

	@Override
	public SelectionType getType() {
		return SelectionType.CUBOID;
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

}
