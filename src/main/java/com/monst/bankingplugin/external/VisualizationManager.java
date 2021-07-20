package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.VisualizationElement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class VisualizationManager {

    public static void revertVisualization(Player p) {
        Visualization.Revert(p);
    }

    public static void visualizeRegion(Player p, Bank bank) {
        visualizeRegion(p, bank.getRegion(), bank.isAdminBank());
    }

    public static void visualizeRegion(Player p, BankRegion sel, boolean isAdmin) {
        visualize(p, Collections.singleton(sel), isAdmin ? VisualizationType.ADMIN : VisualizationType.NORMAL);
    }

    public static void visualizeOverlap(Player p, Collection<BankRegion> bankRegions) {
        visualize(p, bankRegions, VisualizationType.OVERLAP);
    }

    private static void visualize(Player p, Collection<BankRegion> bankRegions, VisualizationType type) {
        Callback<Visualization> callback = Callback.of(visualization -> {
            visualization.elements.forEach(element -> element.realBlock = element.location.getBlock().getBlockData());
            Visualization.Apply(p, visualization);
        });
        Utils.bukkitRunnable(() -> {
            Visualization visualization = new Visualization();
            for (BankRegion region : bankRegions)
                visualization.elements.addAll(getRegionElements(region, type));
            visualization.elements.removeIf(element -> outOfRange(p.getLocation(), element.location));
            Callback.callSyncResult(callback, visualization);
        }).runTaskAsynchronously(BankingPlugin.getInstance());
    }

    private static List<VisualizationElement> getRegionElements(BankRegion sel, VisualizationType type) {
        final int step = 10;
        final World world = sel.getWorld();
        List<VisualizationElement> newElements = new ArrayList<>();

        if (sel.isCuboid()) {
            // Add blocks at vertices
            sel.getCorners().stream()
                    .map(location -> new VisualizationElement(location, type.cornerBlockData, null))
                    .forEach(newElements::add);

            Block min = sel.getMinimumBlock();
            Block max = sel.getMaximumBlock();
            MinMax[] dimensions = new MinMax[] {
                    new MinMax(min.getX(), max.getX()),
                    new MinMax(min.getY(), max.getY()),
                    new MinMax(min.getZ(), max.getZ())
            };

            // Add blocks that are directly adjacent to corner blocks
            for (int i = 0; i < 3; i++) {
                for (int a : new int[] { dimensions[i].getMin() + 1, dimensions[i].getMax() - 1 }) {
                    for (int b : new int[] { dimensions[(i + 1) % 3].getMin(), dimensions[(i + 1) % 3].getMax() }) {
                        for (int c : new int[] { dimensions[(i + 2) % 3].getMin(), dimensions[(i + 2) % 3].getMax() }) {
                            Location loc = null;
                            switch (i) {
                                case 0: loc = new Location(world, a, b, c); break;
                                case 1: loc = new Location(world, c, a, b); break;
                                case 2: loc = new Location(world, b, c, a);
                            }
                            newElements.add(new VisualizationElement(loc, type.accentBlockData, null));
                        }
                    }
                }
            }

            // Add blocks that form the lines between the corners in intervals of the integer "step"
            for (int i = 0; i < 3; i++) {
                for (int a = dimensions[i].getMin() + step; a < dimensions[i].getMax() - step / 2; a += step) {
                    for (int b : new int[] { dimensions[(i + 1) % 3].getMin(), dimensions[(i + 1) % 3].getMax() }) {
                        for (int c : new int[] { dimensions[(i + 2) % 3].getMin(), dimensions[(i + 2) % 3].getMax() }) {
                            Location loc = null;
                            switch (i) {
                                case 0: loc = new Location(world, a, b, c); break;
                                case 1: loc = new Location(world, c, a, b); break;
                                case 2: loc = new Location(world, b, c, a);
                            }
                            newElements.add(new VisualizationElement(loc, type.accentBlockData, null));
                        }
                    }
                }
            }
        } else {
            // Add blocks at vertices
            sel.getCorners().stream()
                    .map(loc -> new VisualizationElement(loc, type.cornerBlockData, null))
                    .forEach(newElements::add);

            List<BlockVector2D> points = sel.getVertices();
            for (int i = 0; i < points.size(); i++) {

                BlockVector2D current = points.get(i);

                // Add blocks that are immediately vertically adjacent to corner blocks
                for (int y : new int[] { sel.getMinY() + 1, sel.getMaxY() - 1 }) {
                    Location loc = new Location(world, current.getX(), y, current.getZ());
                    newElements.add(new VisualizationElement(
                            loc,
                            type.accentBlockData,
                            null
                    ));
                }

                // Add blocks that form the vertical lines at the corners in intervals of the integer "step"
                for (int y = sel.getMinY() + step; y < sel.getMaxY() - (step / 2); y += step) {
                    Location loc = new Location(world, current.getX(), y, current.getZ());
                    newElements.add(new VisualizationElement(
                            loc,
                            type.accentBlockData,
                            null
                    ));
                }

                // Get the vertex after the current one; will eventually loop back to the first vertex
                BlockVector2D next = points.get((i + 1) % points.size());

                int diffX = next.getX() - current.getX();
                int diffZ = next.getZ() - current.getZ();
                double distanceToNext = QuickMath.vectorMagnitude(diffX, diffZ);

                // These two doubles store the direction from the current vertex to the next. Through vector addition they form a diagonal of length 1
                float unitX = (float) (diffX / distanceToNext);
                float unitZ = (float) (diffZ / distanceToNext);

                // The following blocks are placed at both minY and maxY
                for (int y : new int[] { sel.getMinY(), sel.getMaxY() }) {
                    // Add the block that is immediately adjacent to the current vertex and pointing in the direction of the next vertex
                    Location unitAway = new Location(world, current.getX() + 0.5 + unitX, y, current.getZ() + 0.5 + unitZ);
                    newElements.add(new VisualizationElement(
                            unitAway,
                            type.accentBlockData,
                            null
                    ));

                    // Add the block that is immediately adjacent to the next vertex and pointing in the direction of the current vertex
                    Location unitAwayNext = new Location(world, next.getX() + 0.5 - unitX, y, next.getZ() + 0.5 - unitZ);
                    newElements.add(new VisualizationElement(
                            unitAwayNext,
                            type.accentBlockData,
                            null
                    ));
                }

                final double stopAt = distanceToNext - step / 2.0;
                if (step > stopAt)
                    continue;

                // Add blocks that form the lines between the vertices in intervals of the integer "step"
                final double stepX = step * unitX;
                final double stepZ = step * unitZ;
                double nextX = current.getX() + 0.5 + stepX;
                double nextZ = current.getZ() + 0.5 + stepZ;
                for (int hop = step; hop < stopAt; hop += step) {
                    for (int y : new int[] { sel.getMinY(), sel.getMaxY() })
                        newElements.add(new VisualizationElement(
                                new Location(world, (int) nextX, y, (int) nextZ),
                                type.accentBlockData,
                                null
                        ));
                    nextX += stepX;
                    nextZ += stepZ;
                }
            }
        }

        return newElements;
    }

    private static boolean outOfRange(Location currentPos, Location check) {
        int range = 100;
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
        ADMIN (Material.GLOWSTONE.createBlockData(), Material.PUMPKIN.createBlockData()),
        OVERLAP (Material.REDSTONE_ORE.createBlockData(), Material.NETHERRACK.createBlockData());

        private final BlockData cornerBlockData;
        private final BlockData accentBlockData;

        VisualizationType(BlockData cornerBlockData, BlockData accentBlockData) {
            this.cornerBlockData = cornerBlockData;
            this.accentBlockData = accentBlockData;
        }

    }

}
