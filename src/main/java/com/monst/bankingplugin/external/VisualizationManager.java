package com.monst.bankingplugin.external;

import com.monst.bankingplugin.selections.BlockVector2D;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Polygonal2DSelection;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.VisualizationElement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Set;

class VisualizationManager {

    private static final BlockData CORNER_BLOCK_DATA = Material.GLOWSTONE.createBlockData();
    private static final BlockData ACCENT_BLOCK_DATA = Material.GOLD_BLOCK.createBlockData();
    private static final BlockData AIR = Material.AIR.createBlockData();

    static void visualize(Player p, CuboidSelection sel) {
        Visualization.Apply(p, fromSelection(sel, p.getLocation()));
    }

    private static Visualization fromSelection(CuboidSelection sel, Location playerLocation) {
        Visualization visualization = new Visualization();
        addClaimElements(visualization, sel, playerLocation);
        return visualization;
    }

    private static void addClaimElements(Visualization visualization, CuboidSelection sel, Location pLoc) {

        World world = sel.getWorld();

        Location min = sel.getMinimumPoint();
        Location max = sel.getMaximumPoint();
        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        ArrayList<VisualizationElement> newElements = new ArrayList<>();

        sel.getVertices().forEach(loc -> newElements.add(new VisualizationElement(
                new Location(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
                CORNER_BLOCK_DATA,
                AIR
        )));

        for (int x : new int[]{minX + 1, maxX - 1})
            for (int y : new int[]{minY, maxY})
                for (int z : new int[]{minZ, maxZ})
                    newElements.add(new VisualizationElement(new Location(world, x, y, z), ACCENT_BLOCK_DATA, AIR));

        for (int y : new int[]{minY + 1, maxY - 1})
            for (int z : new int[]{minZ, maxZ})
                for (int x : new int[]{minX, maxX})
                    newElements.add(new VisualizationElement(new Location(world, x, y, z), ACCENT_BLOCK_DATA, AIR));

        for (int z : new int[]{minZ + 1, maxZ - 1})
            for (int x : new int[]{minX, maxX})
                for (int y : new int[]{minY, maxY})
                    newElements.add(new VisualizationElement(new Location(world, x, y, z), ACCENT_BLOCK_DATA, AIR));

        final int step = 10;

        for (int x = minX; x < maxX - step / 2; x += step) {
            for (int y : new int[]{minY, maxY})
                for (int z : new int[]{minZ, maxZ})
                    newElements.add(new VisualizationElement(new Location(world, x, y, z), ACCENT_BLOCK_DATA, AIR));
        }

        for (int y = minY; y < maxY - step / 2; y += step) {
            for (int z : new int[]{minZ, maxZ})
                for (int x : new int[]{minX, maxX})
                    newElements.add(new VisualizationElement(new Location(world, x, y, z), ACCENT_BLOCK_DATA, AIR));
        }

        for (int z = minZ; z < maxZ - step / 2; z += step) {
            for (int x : new int[]{minX, maxX})
                for (int y : new int[]{minY, maxY})
                    newElements.add(new VisualizationElement(new Location(world, x, y, z), ACCENT_BLOCK_DATA, AIR));
        }

        newElements.removeIf(e -> !inRange(pLoc, e.location));
        Set<BlockVector2D> blocks = sel.getBlocks();
        newElements.removeIf(e -> blocks.contains(new BlockVector2D(e.location.getBlockX(), e.location.getBlockZ())));

        visualization.elements.addAll(newElements);
    }

    private void addClaimElements(Visualization vis, Polygonal2DSelection sel, Location pLoc) {
        // TODO: Implement
    }

    private static boolean inRange(Location currentPos, Location check) {
        int x = check.getBlockX();
        int y = check.getBlockY();
        int z = check.getBlockZ();
        return x > currentPos.getBlockX() - 75 && x < currentPos.getBlockX() + 75
                && y > currentPos.getBlockY() - 75 && y < currentPos.getBlockY() + 75
                && z > currentPos.getBlockZ() - 75 && z < currentPos.getBlockZ() + 75;
    }

}
