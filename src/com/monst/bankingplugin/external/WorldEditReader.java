package com.monst.bankingplugin.external;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Polygonal2DSelection;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
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

public class WorldEditReader {

	public static Selection getSelection(BankingPlugin plugin, Player p) {

		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		Region region;
		try {
			region = worldEdit.getWorldEdit().getSessionManager().findByName(p.getName())
					.getSelection(BukkitAdapter.adapt(p.getWorld()));

			if (region instanceof CuboidRegion) {
				CuboidRegion cuboid = (CuboidRegion) region;
				BlockVector3 vector1 = cuboid.getPos1();
				BlockVector3 vector2 = cuboid.getPos2();
				Location loc1 = new Location(BukkitAdapter.adapt(cuboid.getWorld()), vector1.getBlockX(),
						vector1.getBlockY(), vector1.getBlockZ());
				Location loc2 = new Location(BukkitAdapter.adapt(cuboid.getWorld()), vector2.getBlockX(),
						vector2.getBlockY(), vector2.getBlockZ());
				return new CuboidSelection(loc1.getWorld(), loc1, loc2);
			} else if (region instanceof Polygonal2DRegion) {
				Polygonal2DRegion polygon = (Polygonal2DRegion) region;
				int minY = polygon.getMinimumY();
				int maxY = polygon.getMaximumY();
				World world = BukkitAdapter.adapt(polygon.getWorld());
				List<BlockVector2D> points = new ArrayList<>();
				polygon.getPoints()
						.forEach(point -> points.add(new BlockVector2D(point.getBlockX(), point.getBlockZ())));
				return new Polygonal2DSelection(world, points, minY, maxY);
			}

		} catch (IncompleteRegionException e1) {}

		return null;
	}

	public static void setSelection(BankingPlugin plugin, Selection sel, Player p) {
		
		WorldEditPlugin worldEdit = plugin.getWorldEdit();
		RegionSelector regionSelector;
		if (sel instanceof CuboidSelection) {
			BlockVector3 min = BlockVector3.at(sel.getMinimumPoint().getBlockX(), sel.getMinimumPoint().getBlockX(),
					sel.getMinimumPoint().getBlockZ());
			BlockVector3 max = BlockVector3.at(sel.getMaximumPoint().getBlockX(), sel.getMaximumPoint().getBlockX(),
					sel.getMaximumPoint().getBlockZ());
			regionSelector = new CuboidRegionSelector(BukkitAdapter.adapt(sel.getWorld()), min, max);
		} else {
			List<BlockVector2> points = ((Polygonal2DSelection) sel).getNativePoints().stream()
					.map(point -> BlockVector2.at(point.getBlockX(), point.getBlockZ())).collect(Collectors.toList());
			int minY = sel.getMinimumPoint().getBlockY();
			int maxY = sel.getMaximumPoint().getBlockY();
			regionSelector = new Polygonal2DRegionSelector(BukkitAdapter.adapt(sel.getWorld()), points, minY, maxY);
		}

		worldEdit.getWorldEdit().getSessionManager().get(BukkitAdapter.adapt(p))
				.setRegionSelector(BukkitAdapter.adapt(sel.getWorld()), regionSelector);
		
	}

}
