package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.entity.geo.region.PolygonalBankRegion;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.VisualizationElement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class BankVisualization {

    public static void revert(Player player) {
        Visualization.Revert(player);
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

    private final BankingPlugin plugin;
    private final Collection<Bank> banks;
    private final VisualizationType type;

    public BankVisualization(BankingPlugin plugin, Bank bank) {
        this.plugin = plugin;
        this.banks = Collections.singleton(bank);
        this.type = bank.isPlayerBank() ? VisualizationType.NORMAL : VisualizationType.ADMIN;
    }

    public BankVisualization(BankingPlugin plugin, Collection<Bank> banks) {
        this.plugin = plugin;
        this.banks = banks;
        this.type = VisualizationType.OVERLAP;
    }

    public void show(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Visualization visualization = new Visualization();
            for (Bank bank : banks)
                visualization.elements.addAll(getRegionElements(bank.getRegion(), type));
            visualization.elements.removeIf(element -> outOfRange(player.getLocation(), element.location));
            Bukkit.getScheduler().runTask(plugin, () -> Visualization.Apply(player, visualization));
        });
    }

    private static List<VisualizationElement> getRegionElements(BankRegion sel, VisualizationType type) {
        final int step = 10;
        final World world = sel.getWorld();
        List<VisualizationElement> newElements = new ArrayList<>();

        // Add blocks at vertices
        sel.getCorners().stream()
                .map(block -> new CornerElement(block.getLocation(), type))
                .forEach(newElements::add);

        if (sel.isCuboid()) {

            MinMax[] dimensions = new MinMax[] {
                    new MinMax(sel.getMinX(), sel.getMaxX()),
                    new MinMax(sel.getMinY(), sel.getMaxY()),
                    new MinMax(sel.getMinZ(), sel.getMaxZ())
            };

            for (int i = 0; i < 3; i++) {
                IntStream.Builder builder = IntStream.builder();
                // Add blocks that are directly adjacent to corner blocks
                builder.add(dimensions[i].min + 1).add(dimensions[i].max - 1);
                // Add blocks that form the lines between the corners in intervals of the integer "step"
                for (int a = dimensions[i].min + step; a < dimensions[i].max - step / 2; a += step)
                    builder.accept(a);
                for (int a : builder.build().toArray()) {
                    for (int b : new int[] { dimensions[(i + 1) % 3].min, dimensions[(i + 1) % 3].max }) {
                        for (int c : new int[] { dimensions[(i + 2) % 3].min, dimensions[(i + 2) % 3].max }) {
                            Location loc;
                            switch (i) {
                                case 0: loc = new Location(world, a, b, c); break;
                                case 1: loc = new Location(world, c, a, b); break;
                                case 2: loc = new Location(world, b, c, a); break;
                                default: throw new IllegalStateException();
                            }
                            newElements.add(new AccentElement(loc, type));
                        }
                    }
                }
            }
        } else if (sel.isPolygonal()) {

            // Construct some helpful arrays of y-values
            final int[] minMaxYs = new int[] { sel.getMinY() + 1, sel.getMaxY() - 1 };
            IntStream.Builder yBuilder = IntStream.builder().add(minMaxYs[0]).add(minMaxYs[1]);
            for (int y = sel.getMinY() + step; y < sel.getMaxY() - (step / 2); y += step)
                yBuilder.accept(y);
            final int[] allYs = yBuilder.build().toArray();

            int[] pointsX = ((PolygonalBankRegion) sel).getPointsX();
            int[] pointsZ = ((PolygonalBankRegion) sel).getPointsZ();
            for (int index = 0; index < pointsX.length; index++) {

                int currentX = pointsX[index];
                int currentZ = pointsZ[index];

                for (int y : allYs) {
                    // Add blocks that are immediately vertically adjacent to corner blocks
                    // Add blocks that form the vertical lines at the corners in intervals of the integer "step"
                    Location loc = new Location(world, currentX, y, currentZ);
                    newElements.add(new AccentElement(loc, type));
                }

                // Get the vertex after the current one; will eventually loop back to the first vertex
                int nextX = pointsX[(index + 1) % pointsX.length];
                int nextZ = pointsZ[(index + 1) % pointsZ.length];

                int diffX = nextX - currentX;
                int diffZ = nextZ - currentZ;
                double distanceToNext = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffZ, 2));

                // These two doubles store the direction from the current vertex to the next. Through vector addition they form a diagonal of length 1
                float unitX = (float) (diffX / distanceToNext);
                float unitZ = (float) (diffZ / distanceToNext);

                // The following blocks are placed at minY and maxY
                for (int y : minMaxYs) {
                    // Add the block that is immediately adjacent to the current vertex and pointing in the direction of the next vertex
                    Location unitAway = new Location(world, currentX + 0.5 + unitX, y, currentZ + 0.5 + unitZ);
                    newElements.add(new AccentElement(unitAway, type));

                    // Add the block that is immediately adjacent to the next vertex and pointing in the direction of the current vertex
                    Location unitAwayNext = new Location(world, nextX + 0.5 - unitX, y, nextZ + 0.5 - unitZ);
                    newElements.add(new AccentElement(unitAwayNext, type));
                }

                final double stopAt = distanceToNext - step / 2.0;
                if (step > stopAt)
                    continue;

                // Add blocks that form the lines between the vertices in intervals of the integer "step"
                final double stepX = step * unitX;
                final double stepZ = step * unitZ;
                double nextXStep = currentX + 0.5 + stepX;
                double nextZStep = currentZ + 0.5 + stepZ;
                for (int hop = step; hop < stopAt; hop += step) {
                    for (int y : allYs)
                        newElements.add(new AccentElement(new Location(world, (int) nextXStep, y, (int) nextZStep), type));
                    nextXStep += stepX;
                    nextZStep += stepZ;
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

    private static class CornerElement extends VisualizationElement {
        public CornerElement(Location location, VisualizationType type) {
            super(location, type.cornerBlockData, location.getBlock().getBlockData());
        }
    }

    private static class AccentElement extends VisualizationElement {
        public AccentElement(Location location, VisualizationType type) {
            super(location, type.accentBlockData, location.getBlock().getBlockData());
        }
    }

    private static class MinMax {
        private final int min;
        private final int max;
        public MinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

}
