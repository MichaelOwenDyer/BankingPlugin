package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.selections.CuboidSelection;
import com.monst.bankingplugin.geo.selections.PolygonalSelection;
import com.monst.bankingplugin.geo.selections.Selection;
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

	public static Selection getSelection(BankingPlugin plugin, Player p) {

		Region region;
		try {
			LocalSession session = plugin.getWorldEdit().getWorldEdit().getSessionManager().findByName(p.getName());
			if (session == null)
				return null;
			region = session.getSelection(BukkitAdapter.adapt(p.getWorld()));

			if (region instanceof CuboidRegion) {
				CuboidRegion cuboid = (CuboidRegion) region;
				BlockVector3 vector1 = cuboid.getPos1();
				BlockVector3 vector2 = cuboid.getPos2();
				BlockVector3D loc1 = new BlockVector3D(vector1.getBlockX(), vector1.getBlockY(), vector1.getBlockZ());
				BlockVector3D loc2 = new BlockVector3D(vector2.getBlockX(), vector2.getBlockY(), vector2.getBlockZ());
				return CuboidSelection.of(Optional.ofNullable(cuboid.getWorld()).map(BukkitAdapter::adapt).orElse(null), loc1, loc2);
			} else if (region instanceof Polygonal2DRegion) {
				Polygonal2DRegion polygon = (Polygonal2DRegion) region;
				int minY = polygon.getMinimumY();
				int maxY = polygon.getMaximumY();
				World world = Optional.ofNullable(polygon.getWorld()).map(BukkitAdapter::adapt).orElse(null);
				List<BlockVector2D> points = polygon.getPoints().stream()
						.map(point -> new BlockVector2D(point.getBlockX(), point.getBlockZ())).collect(Collectors.toList());
				return PolygonalSelection.of(world, points, minY, maxY);
			}
		} catch (IncompleteRegionException ignored) {}
		return null;
	}

	public static void setSelection(BankingPlugin plugin, Selection sel, Player p) {

		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		RegionSelector regionSelector;
		if (sel instanceof CuboidSelection) {
			BlockVector3 min = BlockVector3.at(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(), sel.getMinimumPoint().getZ());
			BlockVector3 max = BlockVector3.at(sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(), sel.getMaximumPoint().getZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(sel.getWorld()), min, max);
		} else {
			List<BlockVector2> points = ((PolygonalSelection) sel).getVertices().stream()
					.map(point -> BlockVector2.at(point.getX(), point.getZ())).collect(Collectors.toList());
			int minY = sel.getMinimumPoint().getY();
			int maxY = sel.getMaximumPoint().getY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(sel.getWorld()), points, minY, maxY);
		}

		plugin.debug(p.getName() + " has selected the bank at " + sel.getCoordinates());
		regionSelector.setWorld(BukkitAdapter.adapt(sel.getWorld()));
		worldEdit.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(p))
				.setRegionSelector(BukkitAdapter.adapt(sel.getWorld()), regionSelector);
	}

}
