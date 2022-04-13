package com.monst.bankingplugin.entity.geo.region;

import com.monst.bankingplugin.converter.VerticesConverter;
import com.monst.bankingplugin.entity.geo.Vector2;
import com.monst.bankingplugin.entity.geo.Vector3;
import com.monst.polylabel.PolyLabel;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a region of space in the shape of a polygonal prism. It is defined by a {@link World},
 * a minimum and a maximum y-coordinate, and an ordered list of {@link Vector2} (x,z) coordinate pairs to
 * represent the vertices. An edge of this region is a line formed between two neighboring coordinate pairs on the list.
 */
@Entity
@DiscriminatorValue(value = "Polygon")
public class PolygonalBankRegion extends BankRegion {

	@Convert(converter = VerticesConverter.class)
	private List<Vector2> vertices;
	private int minY;
	private int maxY;

	public PolygonalBankRegion() {}

	/**
	 * Creates a new {@link PolygonalBankRegion} with the specified attributes
	 *
	 * @param world the world the region is in
	 * @param vertices the vertices of the region
	 * @param minY the lower y-coordinate bound
	 * @param maxY the upper y-coordinate bound
	 */
	public PolygonalBankRegion(World world, List<Vector2> vertices, int minY, int maxY) {
		super(world);
		if (vertices == null || vertices.size() < 3)
			throw new IllegalArgumentException("Vertices cannot be fewer than 3!");
		this.vertices = Collections.unmodifiableList(vertices);
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
	public Vector3 getCenterPoint() {
		Integer[][][] polygon = new Integer[1][vertices.size()][2];
		for (int i = 0; i < vertices.size(); i++)
			polygon[0][i] = new Integer[] { vertices.get(i).getX(), vertices.get(i).getZ() };
		PolyLabel result = PolyLabel.polyLabel(polygon);
		return new Vector3((int) Math.round(result.getX()), (maxY + minY) / 2, (int) Math.round(result.getY()));
	}

	private transient Integer minX = null;
	@Override
	public int getMinX() {
		if (minX == null)
			minX = vertices.stream().mapToInt(Vector2::getX).min().orElseThrow(IllegalStateException::new);
		return minX;
	}

	private transient Integer maxX = null;
	@Override
	public int getMaxX() {
		if (maxX == null)
			maxX = vertices.stream().mapToInt(Vector2::getX).max().orElseThrow(IllegalStateException::new);
		return maxX;
	}

	@Override
	public int getMinY() {
		return minY;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	private transient Integer minZ = null;
	@Override
	public int getMinZ() {
		if (minZ == null)
			minZ = vertices.stream().mapToInt(Vector2::getZ).min().orElseThrow(IllegalStateException::new);
		return minZ;
	}

	private transient Integer maxZ = null;
	@Override
	public int getMaxZ() {
		if (maxZ == null)
			maxZ = vertices.stream().mapToInt(Vector2::getZ).max().orElseThrow(IllegalStateException::new);
		return maxZ;
	}

	public List<Vector2> getVertices() {
		return vertices;
	}

	@Override
	public List<Block> getCorners() {
		List<Block> vertices = new LinkedList<>();
		for (Vector2 v : this.vertices) {
			vertices.add(world.getBlockAt(v.getX(), getMinY(), v.getZ()));
			vertices.add(world.getBlockAt(v.getX(), getMaxY(), v.getZ()));
		}
		return vertices;
	}

	@Override
	public long getVolume() {
		return (long) getFootprint().size() * getHeight();
	}

	@Override
	public boolean overlaps(BankRegion region) {
		if (isDisjunct(region))
			return false;
		Set<Vector2> footprint = region.getFootprint();
		return getFootprint().stream().anyMatch(footprint::contains);
	}

	@Override
	public boolean contains(int pointX, int pointZ) {
		int nextX, nextZ, x1, z1, x2, z2;
		int prevX = vertices.get(vertices.size() - 1).getX();
		int prevZ = vertices.get(vertices.size() - 1).getZ();

		long crossProduct;
		boolean inside = false;
		for (Vector2 point : vertices) {
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
		sb.append(vertices.stream()
				.limit(8)
				.map(vertex -> "(" + vertex.getX() + ", " + vertex.getZ() + ")")
				.collect(Collectors.joining(", "))
		);
		if (vertices.size() > 8)
			sb.append(", ...");
		sb.append(" at ").append(minY).append(" ≤ y ≤ ").append(maxY);
		return sb.toString();
	}

}
