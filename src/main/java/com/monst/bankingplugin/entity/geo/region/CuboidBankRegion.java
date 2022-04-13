package com.monst.bankingplugin.entity.geo.region;

import com.monst.bankingplugin.entity.geo.Vector2;
import com.monst.bankingplugin.entity.geo.Vector3;
import jakarta.persistence.*;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a region of space in the shape of a rectangular prism. It is defined by a {@link World} and
 * a minimum and a maximum {@link Vector3} point in space.
 */
@Entity
@DiscriminatorValue(value = "Cuboid")
public class CuboidBankRegion extends BankRegion {

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "x", column = @Column(name = "x1")),
			@AttributeOverride(name = "y", column = @Column(name = "y1")),
			@AttributeOverride(name = "z", column = @Column(name = "z1"))
	})
	private Vector3 min;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "x", column = @Column(name = "x2")),
			@AttributeOverride(name = "y", column = @Column(name = "y2")),
			@AttributeOverride(name = "z", column = @Column(name = "z2"))
	})
	private Vector3 max;

	public CuboidBankRegion() {}

	/**
	 * @param world the world the region is in
	 * @param v1 the first corner bound (any combination of upper/lower x, y, z values)
	 * @param v2 the other corner bound
	 */
	public CuboidBankRegion(World world, Vector3 v1, Vector3 v2) {
		super(world);
		this.min = Vector3.min(v1, v2);
		this.max = Vector3.max(v1, v2);
	}

	@Override
	public Vector3 getCenterPoint() {
		int centerX = (getMaxX() + getMinX()) / 2;
		int centerY = (getMaxY() + getMinY()) / 2;
		int centerZ = (getMaxZ() + getMinZ()) / 2;
		return new Vector3(centerX, centerY, centerZ);
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
	public long getVolume() {
		return (long) getLength() * getWidth() * getHeight();
	}

	@Override
	public boolean overlaps(BankRegion region) {
		if (isDisjunct(region))
			return false;
		Set<Vector2> blocks = getFootprint();
		return region.getFootprint().stream().anyMatch(blocks::contains);
	}

	@Override
	public Set<Vector2> getFootprint() {
		Set<Vector2> blocks = new HashSet<>();
		int maxX = getMaxX();
		int maxZ = getMaxZ();
		for (int x = getMinX(); x <= maxX; x++)
			for (int z = getMinZ(); z <= maxZ; z++)
				blocks.add(new Vector2(x, z));
		return blocks;
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
