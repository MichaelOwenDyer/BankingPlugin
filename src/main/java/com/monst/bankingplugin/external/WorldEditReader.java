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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
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
		}
		return null;
	}

	public static void setSelection(BankingPlugin plugin, BankRegion reg, Player p) {

		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		RegionSelector regionSelector;
		if (reg.isCuboid()) {
			BlockVector3 min = BlockVector3.at(reg.getMinimumBlock().getX(), reg.getMinimumBlock().getY(), reg.getMinimumBlock().getZ());
			BlockVector3 max = BlockVector3.at(reg.getMaximumBlock().getX(), reg.getMaximumBlock().getY(), reg.getMaximumBlock().getZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(reg.getWorld()), min, max);
		} else if (reg.isPolygonal()) {
			List<BlockVector2> points = Arrays.stream(reg.getVertices())
					.map(point -> BlockVector2.at(point.getX(), point.getZ())).collect(Collectors.toList());
			int minY = reg.getMinimumBlock().getY();
			int maxY = reg.getMaximumBlock().getY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(reg.getWorld()), points, minY, maxY);
		} else
			throw new IllegalStateException();

		plugin.debug(p.getName() + " has selected the bank at " + reg.getCoordinates());
		regionSelector.setWorld(BukkitAdapter.adapt(reg.getWorld()));
		worldEdit.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(p))
				.setRegionSelector(BukkitAdapter.adapt(reg.getWorld()), regionSelector);
	}

}
