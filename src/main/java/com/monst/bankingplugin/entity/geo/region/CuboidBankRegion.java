package com.monst.bankingplugin.entity.geo.region;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a region of space in the shape of a rectangular prism. It is defined by a {@link World} and
 * a minimum and a maximum point in space.
 */
public class CuboidBankRegion extends BankRegion {

	private final int minX;
	private final int minY;
	private final int minZ;
	private final int maxX;
	private final int maxY;
	private final int maxZ;
	
	/**
	 * @param world the world the region is in
	 * @param x1 the minimum x-coordinate of the region
	 * @param y1 the minimum y-coordinate of the region
	 * @param z1 the minimum z-coordinate of the region
	 * @param x2 the maximum x-coordinate of the region
	 * @param y2 the maximum y-coordinate of the region
	 * @param z2 the maximum z-coordinate of the region
	 *
	 */
	public CuboidBankRegion(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		super(world);
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
	}

	@Override
	public Block getCenterBlock() {
		int centerX = (maxX + minX) / 2;
		int centerY = (maxY + minY) / 2;
		int centerZ = (maxZ + minZ) / 2;
		return world.getBlockAt(centerX, centerY, centerZ);
	}
	
	@Override
	public int getMinX() {
		return minX;
	}
	
	@Override
	public int getMinY() {
		return minY;
	}
	
	@Override
	public int getMinZ() {
		return minZ;
	}
	
	@Override
	public int getMaxX() {
		return maxX;
	}
	
	@Override
	public int getMaxY() {
		return maxY;
	}
	
	@Override
	public int getMaxZ() {
		return maxZ;
	}
	
	@Override
	public long getVolume() {
		return (long) getLength() * getWidth() * getHeight();
	}
	
	@Override
	Shape getShape() {
		return new Rectangle(minX, minZ, getWidth(), getLength());
	}
	
	@Override
	public List<Block> getCorners() {
		List<Block> vertices = new ArrayList<>();
		for (int x : new int[] { getMinX(), getMaxX() })
			for (int y : new int[] { getMinY(), getMaxY() })
				for (int z : new int[] { getMinZ(), getMaxZ() })
					vertices.add(world.getBlockAt(x, y, z));
		return vertices;
	}

	@Override
	public boolean contains(int x, int z) {
		return overlapsX(x, x) && overlapsZ(z, z);
	}

	@Override
	public boolean isCuboid() {
		return true;
	}

	@Override
	public String toString() {
		return "(" + getMinX() + ", " + getMinY() + ", " + getMinZ()
				+ ") -> ("
				+ getMaxX() + ", " + getMaxY() + ", " + getMaxZ() + ")";
	}

}
