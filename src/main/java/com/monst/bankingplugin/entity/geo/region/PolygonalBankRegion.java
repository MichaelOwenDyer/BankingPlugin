package com.monst.bankingplugin.entity.geo.region;

import com.monst.polylabel.PolyLabel;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a region of space in the shape of a polygonal prism. It is defined by a {@link World},
 * a minimum and a maximum y-coordinate, and an ordered list of (x,z) coordinate pairs to
 * represent the vertices. An edge of this region is a line formed between two neighboring coordinate pairs on the list.
 */
public class PolygonalBankRegion extends BankRegion {
	
	private static final int MAX_COORDS_IN_TOSTRING = 5;

	private final int[] pointsX;
	private final int[] pointsZ;
	private final int nPoints;
	private final int minY;
	private final int maxY;
	
	// Lazy initialization
	private Integer minX;
	private Integer minZ;
	private Integer maxX;
	private Integer maxZ;
	private Block centerBlock;
	
	/**
	 * Creates a new {@link PolygonalBankRegion} with the specified attributes
	 *
	 * @param world the world the region is in
	 * @param pointsX the x vertices of the region
	 * @param pointsZ the z vertices of the region
	 * @param y1 the lower y-coordinate bound
	 * @param y2 the upper y-coordinate bound
	 */
	public PolygonalBankRegion(World world, int[] pointsX, int[] pointsZ, int y1, int y2) {
		super(world);
		if (pointsX == null || pointsZ == null || pointsX.length < 3 || pointsZ.length < 3)
			throw new IllegalArgumentException("Points cannot be fewer than 3!");
		if (pointsX.length != pointsZ.length)
			throw new IllegalArgumentException("Points must be of equal length!");
		this.pointsX = pointsX;
		this.pointsZ = pointsZ;
		this.nPoints = pointsX.length;
		this.minY = Math.min(y1, y2);
		this.maxY = Math.max(y1, y2);
	}

	/**
	 * This method finds the "visual center" of this {@link PolygonalBankRegion} using an external library {@link PolyLabel}.
	 * This is <b>not</b> the center of the bounding box; it is the point within the polygon that is furthest from any edge.
	 *
	 * @return the pole of inaccessibility of this PolygonalBankRegion
	 */
	@Override
	public Block getCenterBlock() {
		if (centerBlock == null) {
			Integer[][][] polygon = new Integer[1][nPoints][2];
			for (int i = 0; i < nPoints; i++)
				polygon[0][i] = new Integer[] { pointsX[i], pointsZ[i] };
			PolyLabel result = PolyLabel.polyLabel(polygon);
			centerBlock = world.getBlockAt((int) result.getX(), (maxY + minY) / 2, (int) result.getY());
		}
		return centerBlock;
	}

	@Override
	public int getMinX() {
		if (minX == null) {
			minX = pointsX[0];
			for (int x : pointsX)
				if (x < minX)
					minX = x;
		}
		return minX;
	}
	
	@Override
	public int getMinY() {
		return minY;
	}
	
	@Override
	public int getMinZ() {
		if (minZ == null) {
			minZ = pointsZ[0];
			for (int z : pointsZ)
				if (z < minZ)
					minZ = z;
		}
		return minZ;
	}

	@Override
	public int getMaxX() {
		if (maxX == null) {
			maxX = pointsX[0];
			for (int x : pointsX)
				if (x > maxX)
					maxX = x;
		}
		return maxX;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	@Override
	public int getMaxZ() {
		if (maxZ == null) {
			maxZ = pointsZ[0];
			for (int z : pointsZ)
				if (z > maxZ)
					maxZ = z;
		}
		return maxZ;
	}

	public int[] getPointsX() {
		return pointsX;
	}
	
	public int[] getPointsZ() {
		return pointsZ;
	}

	@Override
	public List<Block> getCorners() {
		List<Block> corners = new LinkedList<>();
		for (int i = 0; i < nPoints; i++) {
			corners.add(world.getBlockAt(pointsX[i], getMinY(), pointsZ[i]));
			corners.add(world.getBlockAt(pointsX[i], getMaxY(), pointsZ[i]));
		}
		return corners;
	}
	
	@Override
	Shape getShape() {
		return new Polygon(pointsX, pointsZ, nPoints);
	}
	
	@Override
	public long getVolume() {
		return (long) getArea() * getHeight();
	}
	
	private int getArea() {
		int area = 0;
		for (int i = 0; i < nPoints; i++) {
			int j = (i + 1) % nPoints;
			area += pointsX[i] * pointsZ[j] - pointsX[j] * pointsZ[i];
		}
		return Math.abs(area / 2);
	}

	@Override
	public boolean contains(int pointX, int pointZ) {
		int nextX, nextZ, x1, z1, x2, z2;
		int prevX = pointsX[nPoints - 1];
		int prevZ = pointsZ[nPoints - 1];

		long crossProduct;
		boolean inside = false;
		for (int i = 0; i < nPoints; i++) {
			nextX = pointsX[i];
			nextZ = pointsZ[i];
			if (nextX == pointX && nextZ == pointZ) // Location is on a vertex
				return true;
			if (nextX > prevX) {
				x1 = prevX;
				x2 = nextX;
				z1 = prevZ;
				z2 = nextZ;
			} else {
				x1 = nextX;
				x2 = prevX;
				z1 = nextZ;
				z2 = prevZ;
			}
			if (x1 <= pointX && pointX <= x2) {
				crossProduct = ((long) (pointZ - z1) * (x2 - x1)) - ((long) (z2 - z1) * (pointX - x1));
				if (crossProduct == 0) {
					if ((z1 <= pointZ) == (pointZ <= z2))
						return true; // Location is on edge between vertices
				} else if (crossProduct < 0 && (x1 != pointX))
					inside = !inside;
			}
			prevX = nextX;
			prevZ = nextZ;
		}
		return inside;
	}


	@Override
	public boolean isPolygonal() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < nPoints && i <= MAX_COORDS_IN_TOSTRING; i++) {
			sb.append(pointsX[i]);
			sb.append(", ");
			sb.append(pointsZ[i]);
			sb.append("; ");
		}
		if (nPoints > MAX_COORDS_IN_TOSTRING)
			sb.append("... (").append(nPoints).append(" total)");
		sb.append(" at ").append(minY).append(" ≤ y ≤ ").append(maxY);
		return sb.toString();
	}

}
