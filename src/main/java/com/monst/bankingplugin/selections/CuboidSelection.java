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
public class CuboidSelection implements Selection {

	private final World world;
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
		this.world = world;
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
		int centerX = (max.getBlockX() + min.getBlockX()) / 2;
		int centerY = (max.getBlockY() + min.getBlockY()) / 2;
		int centerZ = (max.getBlockZ() + min.getBlockZ()) / 2;
		return new BlockVector3D(centerX, centerY, centerZ);
	}

	@Override
	public int getMinY() {
		return min.getBlockY();
	}

	@Override
	public int getMaxY() {
		return max.getBlockY();
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public String getCoordinates() {
		BlockVector3D min = getMinimumPoint();
		BlockVector3D max = getMaximumPoint();
		return "(" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() 
				+ ") -> (" 
				+ max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ")";
	}

	@Override
	public long getVolume() {
		return (max.getBlockX() - min.getBlockX() + 1)
			 * (max.getBlockZ() - min.getBlockZ() + 1)
			 * (max.getBlockY() - min.getBlockY() + 1);
	}

	@Override
	public boolean overlaps(Selection sel) {
		if (getMinY() > sel.getMaxY() || getMaxY() < sel.getMinY())
			return false;
		Set<BlockVector2D> blocks = getBlocks();
		return sel.getBlocks().stream().anyMatch(blocks::contains);
	}

	@Override
	public Set<BlockVector2D> getBlocks() {
		Set<BlockVector2D> blocks = new HashSet<>();
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
				blocks.add(new BlockVector2D(x, z));
		return blocks;
	}

	@Override
	public Collection<BlockVector3D> getVertices() {
		Set<BlockVector3D> vertices = new HashSet<>();
		vertices.add(min);
		vertices.add(new BlockVector3D(max.getBlockX(), min.getBlockY(), min.getBlockZ()));
		vertices.add(new BlockVector3D(min.getBlockX(), max.getBlockY(), min.getBlockZ()));
		vertices.add(new BlockVector3D(max.getBlockX(), max.getBlockY(), min.getBlockZ()));
		vertices.add(new BlockVector3D(min.getBlockX(), min.getBlockY(), max.getBlockZ()));
		vertices.add(new BlockVector3D(max.getBlockX(), min.getBlockY(), max.getBlockZ()));
		vertices.add(new BlockVector3D(min.getBlockX(), max.getBlockY(), max.getBlockZ()));
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
		return x <= max.getBlockX() && x >= min.getBlockX()
				&& y <= max.getBlockY() && y >= min.getBlockY()
				&& z <= max.getBlockZ() && z >= min.getBlockZ();
	}

	@Override
	public boolean contains(BlockVector2D bv) {
		int x = bv.getBlockX();
		int z = bv.getBlockZ();
		return x <= max.getBlockX() && x >= min.getBlockX()
				&& z <= max.getBlockZ() && z >= min.getBlockZ();
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
