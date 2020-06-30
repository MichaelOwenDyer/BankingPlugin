package com.monst.bankingplugin.external;

import java.util.ArrayList;
import java.util.List;

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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

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

}
