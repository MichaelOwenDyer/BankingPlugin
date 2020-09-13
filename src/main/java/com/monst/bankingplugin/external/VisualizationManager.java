package com.monst.bankingplugin.external;

import com.monst.bankingplugin.selections.BlockVector2D;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Polygonal2DSelection;
import com.monst.bankingplugin.selections.Selection;
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

    static void visualize(Player p, Selection sel) {
        Visualization.Apply(p, fromSelection(sel, p.getLocation()));
    }

    private static Visualization fromSelection(Selection sel, Location playerLocation) {
        Visualization visualization = new Visualization();
        addClaimElements(visualization, sel, playerLocation);
        return visualization;
    }

    private static void addClaimElements(Visualization visualization, Selection sel, Location pLoc) {
        if (sel instanceof CuboidSelection)
            addClaimElements(visualization, (CuboidSelection) sel, pLoc);
        else
            addClaimElements(visualization, (Polygonal2DSelection) sel, pLoc);
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
                loc,
                CORNER_BLOCK_DATA,
                world.getBlockAt(loc).getBlockData()
        )));

        for (int x : new int[]{minX + 1, maxX - 1})
            for (int y : new int[]{minY, maxY})
                for (int z : new int[]{minZ, maxZ}) {
                    Location loc = new Location(world, x, y, z);
                    newElements.add(new VisualizationElement(loc, ACCENT_BLOCK_DATA, world.getBlockAt(loc).getBlockData()));
                }

        for (int y : new int[]{minY + 1, maxY - 1})
            for (int z : new int[]{minZ, maxZ})
                for (int x : new int[]{minX, maxX}) {
                    Location loc = new Location(world, x, y, z);
                    newElements.add(new VisualizationElement(loc, ACCENT_BLOCK_DATA, world.getBlockAt(loc).getBlockData()));
                }

        for (int z : new int[]{minZ + 1, maxZ - 1})
            for (int x : new int[]{minX, maxX})
                for (int y : new int[]{minY, maxY}) {
                    Location loc = new Location(world, x, y, z);
                    newElements.add(new VisualizationElement(loc, ACCENT_BLOCK_DATA, world.getBlockAt(loc).getBlockData()));
                }

        final int step = 10;

        for (int x = minX + step; x < maxX - step / 2; x += step) {
            for (int y : new int[]{minY, maxY})
                for (int z : new int[]{minZ, maxZ}) {
                    Location loc = new Location(world, x, y, z);
                    newElements.add(new VisualizationElement(loc, ACCENT_BLOCK_DATA, world.getBlockAt(loc).getBlockData()));
                }
        }

        for (int y = minY + step; y < maxY - step / 2; y += step) {
            for (int z : new int[]{minZ, maxZ})
                for (int x : new int[]{minX, maxX}) {
                    Location loc = new Location(world, x, y, z);
                    newElements.add(new VisualizationElement(loc, ACCENT_BLOCK_DATA, world.getBlockAt(loc).getBlockData()));
                }
        }

        for (int z = minZ + step; z < maxZ - step / 2; z += step) {
            for (int x : new int[]{minX, maxX})
                for (int y : new int[]{minY, maxY}) {
                    Location loc = new Location(world, x, y, z);
                    newElements.add(new VisualizationElement(loc, ACCENT_BLOCK_DATA, world.getBlockAt(loc).getBlockData()));
                }
        }

        newElements.removeIf(e -> outOfRange(pLoc, e.location));
        Set<BlockVector2D> blocks = sel.getBlocks();
        newElements.removeIf(e -> !blocks.contains(new BlockVector2D(e.location.getBlockX(), e.location.getBlockZ())));

        visualization.elements.addAll(newElements);
    }

    private static void addClaimElements(Visualization visualization, Polygonal2DSelection sel, Location pLoc) {
        World world = sel.getWorld();
        ArrayList<VisualizationElement> newElements = new ArrayList<>();

        sel.getVertices().forEach(loc -> newElements.add(new VisualizationElement(
                loc,
                CORNER_BLOCK_DATA,
                world.getBlockAt(loc).getBlockData()
        )));

        newElements.removeIf(e -> outOfRange(pLoc, e.location));
        // Set<BlockVector2D> blocks = sel.getBlocks();
        // newElements.removeIf(e -> !blocks.contains(new BlockVector2D(e.location.getBlockX(), e.location.getBlockZ())));

        visualization.elements.addAll(newElements);
    }

    private static boolean outOfRange(Location currentPos, Location check) {
        int x = check.getBlockX();
        int y = check.getBlockY();
        int z = check.getBlockZ();
        return x <= currentPos.getBlockX() - 75 || x >= currentPos.getBlockX() + 75
                || y <= currentPos.getBlockY() - 75 || y >= currentPos.getBlockY() + 75
                || z <= currentPos.getBlockZ() - 75 || z >= currentPos.getBlockZ() + 75;
    }

}
