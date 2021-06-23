package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

/**
 * This class represents a region of space in the shape of a rectangular prism. It is defined by a {@link World} and
 * a minimum and a maximum {@link BlockVector3D} point in space.
 */
public class CuboidBankRegion extends BankRegion {

	private final Block min;
	private final Block max;

	/**
	 * Creates a new {@link CuboidBankRegion} with the specified attributes
	 *
	 * @param world the world the region is in
	 * @param loc1 the first corner bound (any combination of upper/lower x, y, z values)
	 * @param loc2 the other corner bound
	 * @return a new CuboidBankRegion
	 */
	public static CuboidBankRegion of(World world, BlockVector3D loc1, BlockVector3D loc2) {
		Block min = world.getBlockAt(
				Math.min(loc1.getX(), loc2.getX()),
				Math.min(loc1.getY(), loc2.getY()),
				Math.min(loc1.getZ(), loc2.getZ())
		);
		Block max = world.getBlockAt(
				Math.max(loc1.getX(), loc2.getX()),
				Math.max(loc1.getY(), loc2.getY()),
				Math.max(loc1.getZ(), loc2.getZ())
		);
		return new CuboidBankRegion(world, min, max);
	}

	private CuboidBankRegion(World world, Block min, Block max) {
		super(world);
		this.min = min;
		this.max = max;
	}

	@Override
	public Block getMinimumBlock() {
		return min;
	}

	@Override
	public Block getMaximumBlock() {
		return max;
	}

	@Override
	public Block getCenterPoint() {
		int centerX = (getMaxX() + getMinX()) / 2;
		int centerY = (getMaxY() + getMinY()) / 2;
		int centerZ = (getMaxZ() + getMinZ()) / 2;
		return world.getBlockAt(centerX, centerY, centerZ);
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
	public boolean overlaps(BankRegion region) {
		if (isDisjunct(region))
			return false;
		Set<BlockVector2D> blocks = getFootprint();
		return region.getFootprint().stream().anyMatch(blocks::contains);
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
	public List<BlockVector2D> getVertices() {
		List<BlockVector2D> vertices = new LinkedList<>();
		for (int x : new int[] { getMinX(), getMaxX() })
			for (int z : new int[] { getMinZ(), getMaxZ() })
				vertices.add(new BlockVector2D(x, z));
		return vertices;
	}

	@Override
	public boolean contains(int x, int z) {
		return x <= getMaxX()
			&& x >= getMinX()
			&& z <= getMaxZ()
			&& z >= getMinZ();
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
		CuboidBankRegion otherRegion = (CuboidBankRegion) o;
		return Objects.equals(getWorld(), otherRegion.getWorld())
			&& Objects.equals(getMaximumBlock(), otherRegion.getMaximumBlock())
			&& Objects.equals(getMinimumBlock(), otherRegion.getMinimumBlock());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getWorld(), getMinX(), getMaxX(), getMinY(), getMaxY(), getMinZ(), getMaxZ());
	}

}
