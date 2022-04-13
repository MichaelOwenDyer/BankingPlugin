package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.geo.Vector2;
import com.monst.bankingplugin.entity.geo.Vector3;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.entity.geo.region.CuboidBankRegion;
import com.monst.bankingplugin.entity.geo.region.PolygonalBankRegion;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorldEditReader {

	public static Optional<BankRegion> getBankRegion(BankingPlugin plugin, Player player) {

		Region region;
		try {
			LocalSession session = plugin.getWorldEdit().getWorldEdit().getSessionManager().findByName(player.getName());
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
			Vector3 loc1 = new Vector3(vector1.getBlockX(), vector1.getBlockY(), vector1.getBlockZ());
			Vector3 loc2 = new Vector3(vector2.getBlockX(), vector2.getBlockY(), vector2.getBlockZ());
			return Optional.of(new CuboidBankRegion(world, loc1, loc2));
		} else if (region instanceof Polygonal2DRegion) {
			Polygonal2DRegion polygon = (Polygonal2DRegion) region;
			int minY = polygon.getMinimumY();
			int maxY = polygon.getMaximumY();
			List<Vector2> points = polygon.getPoints().stream().map(pt -> new Vector2(pt.getBlockX(), pt.getBlockZ())).collect(Collectors.toList());
			return Optional.of(new PolygonalBankRegion(world, points, minY, maxY));
		}
		return Optional.empty();
	}

	public static void setSelection(BankingPlugin plugin, BankRegion region, Player player) {

		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		RegionSelector regionSelector;
		if (region.isCuboid()) {
			BlockVector3 min = BlockVector3.at(region.getMinX(), region.getMinY(), region.getMinZ());
			BlockVector3 max = BlockVector3.at(region.getMaxX(), region.getMaxY(), region.getMaxZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(region.getWorld()), min, max);
		} else if (region.isPolygonal()) {
			List<BlockVector2> points = ((PolygonalBankRegion) region).getVertices().stream()
					.map(point -> BlockVector2.at(point.getX(), point.getZ())).collect(Collectors.toList());
			int minY = region.getMinY();
			int maxY = region.getMaxY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(region.getWorld()), points, minY, maxY);
		} else
			throw new IllegalStateException();

		plugin.debugf("%s has selected the bank at %s", player.getName(), region.toString());
		regionSelector.setWorld(BukkitAdapter.adapt(region.getWorld()));
		worldEdit.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(player))
				.setRegionSelector(BukkitAdapter.adapt(region.getWorld()), regionSelector);
	}

}
