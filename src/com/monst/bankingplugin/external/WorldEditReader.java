package com.monst.bankingplugin.external;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Selection;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
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
			}

		} catch (IncompleteRegionException e1) {
		}

		return null;
	}

}
