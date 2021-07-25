package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.Vector2D;
import com.monst.polylabel.PolyLabel;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a region of space in the shape of a polygonal prism. It is defined by a {@link World},
 * a minimum and a maximum y-coordinate, and an ordered list of {@link Vector2D} (x,z) coordinate pairs to
 * represent the vertices. An edge of this region is a line formed between two neighboring coordinate pairs on the list.
 */
public class PolygonalBankRegion extends BankRegion {

	private final Vector2D[] vertices;
	private final int minY;
	private final int maxY;

	/**
	 * Creates a new {@link PolygonalBankRegion} and the specified attributes
	 *
	 * @param world the world the region is in
	 * @param points the vertices of the region
	 * @param y1 the first y-coordinate bound (upper or lower)
	 * @param y2 the other y-coordinate bound
	 * @return a new PolygonalBankRegion
	 */
	public static PolygonalBankRegion of(World world, Vector2D[] points, int y1, int y2) {
		y1 = Math.min(Math.max(0, y1), world.getMaxHeight()); // Ensure y1 is between 0 and world.getMaxHeight()
		y2 = Math.min(Math.max(0, y2), world.getMaxHeight()); // Ensure y2 is between 0 and world.getMaxHeight()
		return new PolygonalBankRegion(
				world,
				points,
				Math.min(y1, y2), // Take the lower of the two y-values to be minY
				Math.max(y1, y2) // Take the higher of the two y-values to be maxY
		);
	}

	private PolygonalBankRegion(World world, Vector2D[] vertices, int minY, int maxY) {
		super(world);
		if (vertices == null || vertices.length < 3)
			throw new IllegalArgumentException("Vertices cannot be fewer than 3!");
		this.vertices = vertices;
		this.minY = minY;
		this.maxY = maxY;
	}

	/**
	 * This method finds the "visual center" of this {@link PolygonalBankRegion} using an external library {@link PolyLabel}.
	 * This is <b>not</b> the center of the bounding box; it is the point within the polygon that is furthest from any edge.
	 *
	 * @return the pole of inaccessibility of this PolygonalBankRegion
	 */
	@Override
	public Block getCenterPoint() {
		Integer[][][] polygon = new Integer[1][vertices.length][2];
		for (int i = 0; i < vertices.length; i++)
			polygon[0][i] = new Integer[] { vertices[i].getX(), vertices[i].getZ() };
		PolyLabel result = PolyLabel.polyLabel(polygon);
		return world.getBlockAt((int) Math.round(result.getX()), (maxY + minY) / 2, (int) Math.round(result.getY()));
	}

	@Override
	public int getMinX() {
		return Arrays.stream(vertices).mapToInt(Vector2D::getX).min().orElseThrow(IllegalStateException::new);
	}

	@Override
	public int getMaxX() {
		return Arrays.stream(vertices).mapToInt(Vector2D::getX).max().orElseThrow(IllegalStateException::new);
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
		return Arrays.stream(vertices).mapToInt(Vector2D::getZ).min().orElseThrow(IllegalStateException::new);
	}

	@Override
	public int getMaxZ() {
		return Arrays.stream(vertices).mapToInt(Vector2D::getZ).max().orElseThrow(IllegalStateException::new);
	}

	@Override
	public String getCoordinates() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(Arrays.stream(vertices)
				.limit(8)
				.map(vertex -> "(" + vertex.getX() + ", " + vertex.getZ() + ")")
				.collect(Collectors.joining(", "))
		);
		if (vertices.length > 8)
			sb.append(", ...");
		sb.append(" at ").append(minY).append(" ≤ y ≤ ").append(maxY);
		return sb.toString();
	}

	@Override
	public long getVolume() {
		return (long) (maxY - minY + 1) * getFootprint().size();
	}

	@Override
	public boolean overlaps(BankRegion region) {
		if (isDisjunct(region))
			return false;
		Set<Vector2D> blocks = region.getFootprint();
		return getFootprint().stream().anyMatch(blocks::contains);
	}

	// TODO: Worthy of improvement
	@Override
	public Set<Vector2D> getFootprint() {
		Set<Vector2D> blocks = new HashSet<>();
		Block min = getMinimumBlock();
		Block max = getMaximumBlock();
		for (int x = min.getX(); x <= max.getX(); x++)
			for (int z = min.getZ(); z <= max.getZ(); z++) {
				if (contains(x, z))
					blocks.add(new Vector2D(x, z));
			}
		return blocks;
	}

	/**
	 * @return the ordered list of (x,y) coordinate pairs representing the vertices of this {@link PolygonalBankRegion}
	 */
	@Override
	public Vector2D[] getVertices() {
		return vertices;
	}

	@Override
	public boolean contains(int pointX, int pointZ) {
		int nextX, nextZ, x1, z1, x2, z2;
		int prevX = vertices[vertices.length - 1].getX();
		int prevZ = vertices[vertices.length - 1].getZ();

		long crossProduct;
		boolean inside = false;
		for (Vector2D point : vertices) {
			nextX = point.getX();
			nextZ = point.getZ();
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
						return true; //Location is on edge between vertices
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PolygonalBankRegion other = (PolygonalBankRegion) o;
		return getMinY() == other.getMinY() && getMaxY() == other.getMaxY()
			&& Objects.equals(getWorld(), other.getWorld())
			&& Arrays.equals(getVertices(), other.getVertices());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getWorld(), getMinY(), getMaxY(), Arrays.hashCode(vertices));
	}

}
