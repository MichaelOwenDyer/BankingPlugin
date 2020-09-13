package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.selections.BlockVector2D;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.Utils;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.VisualizationElement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;

public class VisualizationManager {

    private static final BankingPlugin plugin = BankingPlugin.getInstance();

    public static void visualizeSelection(Player p, Selection sel) {
        visualize(Collections.singleton(sel), VisualizationType.NORMAL, p);
    }

    public static void visualizeOverlap(Player p, Collection<Selection> selections) {
        visualize(selections, VisualizationType.OVERLAP, p);
    }

    private static void visualize(Collection<Selection> selections, VisualizationType type, Player p) {
        Utils.bukkitRunnable(() -> {
            Visualization visualization = new Visualization();
            for (Selection sel : selections)
                visualization.elements.addAll(getSelectionElements(sel, p.getLocation(), type));
            Visualization.Apply(p, visualization);
        }).runTaskAsynchronously(plugin);
    }

    private static List<VisualizationElement> getSelectionElements(Selection sel, Location playerLoc, VisualizationType type) {

        World world = sel.getWorld();
        List<VisualizationElement> newElements = new ArrayList<>();

        if (sel instanceof CuboidSelection) {

            sel.getVertices().forEach(location -> newElements.add(new VisualizationElement(
                    location,
                    type.getCornerBlockData(),
                    world.getBlockAt(location).getBlockData()
            )));

            Location min = sel.getMinimumPoint();
            Location max = sel.getMaximumPoint();
            MinMax[] dimensions = new MinMax[] {
                    new MinMax(min.getBlockX(), max.getBlockX()),
                    new MinMax(min.getBlockY(), max.getBlockY()),
                    new MinMax(min.getBlockZ(), max.getBlockZ())
            };

            // Add blocks that are directly adjacent to corner blocks
            for (int i = 0; i < 3; i++) {
                for (int a : new int[] {dimensions[i].getMin() + 1, dimensions[i].getMax() - 1}) {
                    for (int b : new int[] {dimensions[(i + 1) % 3].getMin(), dimensions[(i + 1) % 3].getMax()}) {
                        for (int c : new int[] {dimensions[(i + 2) % 3].getMin(), dimensions[(i + 2) % 3].getMax()}) {
                            Location loc = null;
                            switch (i) {
                                case 0: loc = new Location(world, a, b, c); break;
                                case 1: loc = new Location(world, c, a, b); break;
                                case 2: loc = new Location(world, b, c, a);
                            }
                            newElements.add(new VisualizationElement(loc, type.getAccentBlockData(), world.getBlockAt(loc).getBlockData()));
                        }
                    }
                }
            }

            // Add blocks that form the lines between the corners in intervals of the integer "step"
            final int step = 10;
            for (int i = 0; i < 3; i++) {
                for (int a = dimensions[i].getMin() + step; a < dimensions[i].getMax() - step / 2; a += step) {
                    for (int b : new int[] {dimensions[(i + 1) % 3].getMin(), dimensions[(i + 1) % 3].getMax()}) {
                        for (int c : new int[] {dimensions[(i + 2) % 3].getMin(), dimensions[(i + 2) % 3].getMax()}) {
                            Location loc = null;
                            switch (i) {
                                case 0: loc = new Location(world, a, b, c); break;
                                case 1: loc = new Location(world, c, a, b); break;
                                case 2: loc = new Location(world, b, c, a);
                            }
                            newElements.add(new VisualizationElement(loc, type.getAccentBlockData(), world.getBlockAt(loc).getBlockData()));
                        }
                    }
                }
            }

            Set<BlockVector2D> blocks = sel.getBlocks();
            newElements.removeIf(e -> !blocks.contains(new BlockVector2D(e.location.getBlockX(), e.location.getBlockZ())));

        } else {

            sel.getVertices().forEach(loc -> newElements.add(new VisualizationElement(
                    loc,
                    type.getCornerBlockData(),
                    world.getBlockAt(loc).getBlockData()
            )));

            // Set<BlockVector2D> blocks = sel.getBlocks();
            // newElements.removeIf(e -> !blocks.contains(new BlockVector2D(e.location.getBlockX(), e.location.getBlockZ())));

        }
        newElements.removeIf(e -> outOfRange(playerLoc, e.location));
        return newElements;
    }

    private static boolean outOfRange(Location currentPos, Location check) {
        int range = 75;
        int x = check.getBlockX();
        int y = check.getBlockY();
        int z = check.getBlockZ();
        return x <= currentPos.getBlockX() - range || x >= currentPos.getBlockX() + range
                || y <= currentPos.getBlockY() - range || y >= currentPos.getBlockY() + range
                || z <= currentPos.getBlockZ() - range || z >= currentPos.getBlockZ() + range;
    }

    private static class MinMax extends Pair<Integer, Integer> {
        MinMax(Integer min, Integer max) {
            super(min, max);
        }
        int getMin() { return super.getFirst(); }
        int getMax() { return super.getSecond(); }
    }

    private enum VisualizationType {

        NORMAL (Material.GLOWSTONE.createBlockData(), Material.GOLD_BLOCK.createBlockData()),
        OVERLAP (Material.REDSTONE_ORE.createBlockData(), Material.NETHERRACK.createBlockData());

        private final BlockData cornerBlockData;
        private final BlockData accentBlockData;

        VisualizationType(BlockData cornerBlockData, BlockData accentBlockData) {
            this.cornerBlockData = cornerBlockData;
            this.accentBlockData = accentBlockData;
        }

        private BlockData getCornerBlockData() {
            return cornerBlockData;
        }

        private BlockData getAccentBlockData() {
            return accentBlockData;
        }

    }

}
