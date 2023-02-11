package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.entity.geo.region.CuboidBankRegion;
import com.monst.bankingplugin.entity.geo.region.PolygonalBankRegion;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldEditReader {

	public static Optional<BankRegion> getBankRegion(BankingPlugin plugin, Player player) {

		Region region;
		try {
			LocalSession session = plugin.getWorldEdit().getSessionManager().findByName(player.getName());
			if (session == null)
				return Optional.empty();
			region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
		} catch (IncompleteRegionException ignored) {
			return Optional.empty();
		}
		World world = Optional.ofNullable(region.getWorld()).map(BukkitAdapter::adapt).orElse(null);
		if (world == null)
			return Optional.empty();
		if (region instanceof CuboidRegion) {
			CuboidRegion cuboid = (CuboidRegion) region;
			BlockVector3 vector1 = cuboid.getPos1();
			BlockVector3 vector2 = cuboid.getPos2();
			return Optional.of(new CuboidBankRegion(world, vector1.getBlockX(), vector1.getBlockY(), vector1.getBlockZ(),
					vector2.getBlockX(), vector2.getBlockY(), vector2.getBlockZ()));
		} else if (region instanceof Polygonal2DRegion) {
			Polygonal2DRegion polygon = (Polygonal2DRegion) region;
			int[] pointsX = polygon.getPoints().stream().mapToInt(BlockVector2::getBlockX).toArray();
			int[] pointsZ = polygon.getPoints().stream().mapToInt(BlockVector2::getBlockZ).toArray();
			return Optional.of(new PolygonalBankRegion(world, pointsX, pointsZ, polygon.getMinimumY(), polygon.getMaximumY()));
		}
		return Optional.empty();
	}

	public static void setSelection(BankingPlugin plugin, BankRegion region, Player player) {

		RegionSelector regionSelector;
		if (region.isCuboid()) {
			BlockVector3 min = BlockVector3.at(region.getMinX(), region.getMinY(), region.getMinZ());
			BlockVector3 max = BlockVector3.at(region.getMaxX(), region.getMaxY(), region.getMaxZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(region.getWorld()), min, max);
		} else if (region.isPolygonal()) {
			int[] pointsX = ((PolygonalBankRegion) region).getPointsX();
			int[] pointsZ = ((PolygonalBankRegion) region).getPointsZ();
			List<BlockVector2> points = new ArrayList<>(pointsX.length);
			for (int i = 0; i < pointsX.length; i++)
				points.add(BlockVector2.at(pointsX[i], pointsZ[i]));
			int minY = region.getMinY();
			int maxY = region.getMaxY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(region.getWorld()), points, minY, maxY);
		} else
			throw new IllegalStateException();

		plugin.debug("%s has selected the bank at %s", player.getName(), region.toString());
		regionSelector.setWorld(BukkitAdapter.adapt(region.getWorld()));
		plugin.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(player))
				.setRegionSelector(BukkitAdapter.adapt(region.getWorld()), regionSelector);
	}

}
