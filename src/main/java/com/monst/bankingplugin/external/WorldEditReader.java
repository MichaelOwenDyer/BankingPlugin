package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.geo.Vector2D;
import com.monst.bankingplugin.geo.Vector3D;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.geo.regions.CuboidBankRegion;
import com.monst.bankingplugin.geo.regions.PolygonalBankRegion;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorldEditReader {

	public static BankRegion getBankRegion(BankingPlugin plugin, Player p) {

		Region region;
		try {
			LocalSession session = plugin.getWorldEdit().getWorldEdit().getSessionManager().findByName(p.getName());
			if (session == null)
				return null;
			region = session.getSelection(BukkitAdapter.adapt(p.getWorld()));
		} catch (IncompleteRegionException ignored) {
			return null;
		}
		World world = Optional.ofNullable(region.getWorld()).map(BukkitAdapter::adapt).orElse(null);
		if (world == null)
			return null;
		if (region instanceof CuboidRegion) {
			CuboidRegion cuboid = (CuboidRegion) region;
			BlockVector3 vector1 = cuboid.getPos1();
			BlockVector3 vector2 = cuboid.getPos2();
			Vector3D loc1 = new Vector3D(vector1.getBlockX(), vector1.getBlockY(), vector1.getBlockZ());
			Vector3D loc2 = new Vector3D(vector2.getBlockX(), vector2.getBlockY(), vector2.getBlockZ());
			return CuboidBankRegion.of(world, loc1, loc2);
		} else if (region instanceof Polygonal2DRegion) {
			Polygonal2DRegion polygon = (Polygonal2DRegion) region;
			int minY = polygon.getMinimumY();
			int maxY = polygon.getMaximumY();
			Vector2D[] points = polygon.getPoints().stream().map(pt -> new Vector2D(pt.getBlockX(), pt.getBlockZ())).toArray(Vector2D[]::new);
			return PolygonalBankRegion.of(world, points, minY, maxY);
		} else if (region instanceof CylinderRegion) {
			CylinderRegion cylinder = (CylinderRegion) region;
			double radiusX = cylinder.getRadius().getX();
			double radiusZ = cylinder.getRadius().getZ();
			Vector2D center = new Vector2D((int) cylinder.getCenter().getX(), (int) cylinder.getCenter().getZ());
		}
		return null;
	}

	public static void setSelection(BankingPlugin plugin, BankRegion region, Player p) {

		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		RegionSelector regionSelector;
		if (region.isCuboid()) {
			BlockVector3 min = BlockVector3.at(region.getMinX(), region.getMinY(), region.getMinZ());
			BlockVector3 max = BlockVector3.at(region.getMaxX(), region.getMaxY(), region.getMaxZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(region.getWorld()), min, max);
		} else if (region.isPolygonal()) {
			List<BlockVector2> points = Arrays.stream(((PolygonalBankRegion) region).getVertices())
					.map(point -> BlockVector2.at(point.getX(), point.getZ())).collect(Collectors.toList());
			int minY = region.getMinY();
			int maxY = region.getMaxY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(region.getWorld()), points, minY, maxY);
		} else
			throw new IllegalStateException();

		plugin.debugf("%s has selected the bank at %s", p.getName(), region.getCoordinates());
		regionSelector.setWorld(BukkitAdapter.adapt(region.getWorld()));
		worldEdit.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(p))
				.setRegionSelector(BukkitAdapter.adapt(region.getWorld()), regionSelector);
	}

}
