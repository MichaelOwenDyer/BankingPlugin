package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Polygonal2DSelection;
import com.monst.bankingplugin.selections.Selection;
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("all")
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
				Location loc1 = new Location(BukkitAdapter.adapt(cuboid.getWorld()), vector1.getBlockX(),
						vector1.getBlockY(), vector1.getBlockZ());
				Location loc2 = new Location(BukkitAdapter.adapt(cuboid.getWorld()), vector2.getBlockX(),
						vector2.getBlockY(), vector2.getBlockZ());
				return CuboidSelection.of(loc1.getWorld(), loc1, loc2);
			} else if (region instanceof Polygonal2DRegion) {
				Polygonal2DRegion polygon = (Polygonal2DRegion) region;
				int minY = polygon.getMinimumY();
				int maxY = polygon.getMaximumY();
				World world = BukkitAdapter.adapt(polygon.getWorld());
				List<Polygonal2DSelection.BlockVector2D> points = new ArrayList<>();
				polygon.getPoints().stream()
						.forEach(point -> points.add(new Polygonal2DSelection.BlockVector2D(point.getBlockX(), point.getBlockZ())));
				return Polygonal2DSelection.of(world, points, minY, maxY);
			}

		} catch (IncompleteRegionException ignored) {}

		return null;
	}

	public static void setSelection(BankingPlugin plugin, Selection sel, Player p) {
		
		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		RegionSelector regionSelector;
		if (sel instanceof CuboidSelection) {
			BlockVector3 min = BlockVector3.at(sel.getMinimumPoint().getBlockX(), sel.getMinimumPoint().getBlockY(), sel.getMinimumPoint().getBlockZ());
			BlockVector3 max = BlockVector3.at(sel.getMaximumPoint().getBlockX(), sel.getMaximumPoint().getBlockY(), sel.getMaximumPoint().getBlockZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(sel.getWorld()), min, max);
		} else {
			List<BlockVector2> points = ((Polygonal2DSelection) sel).getNativePoints().stream()
					.map(point -> BlockVector2.at(point.getBlockX(), point.getBlockZ())).collect(Collectors.toList());
			int minY = sel.getMinimumPoint().getBlockY();
			int maxY = sel.getMaximumPoint().getBlockY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(sel.getWorld()), points, minY, maxY);
		}
		
		plugin.debug(p.getName() + " has selected the bank at " + sel.getCoordinates());
		regionSelector.setWorld(BukkitAdapter.adapt(sel.getWorld()));
		worldEdit.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(p))
				.setRegionSelector(BukkitAdapter.adapt(sel.getWorld()), regionSelector);
	}

}
