package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShulkerBoxGUI extends SinglePageGUI {
    
    private final ShulkerBox shulkerBox;

    public ShulkerBoxGUI(BankingPlugin plugin, Player player, ShulkerBox shulkerBox) {
        super(plugin, player);
        this.shulkerBox = shulkerBox;
    }
    
    @Override
    Inventory createInventory() {
        return Bukkit.createInventory(this, 3 * 9,
                shulkerBox.getCustomName() != null ? shulkerBox.getCustomName() : "Shulker Box");
    }
    
    @Override
    Map<Integer, ItemStack> createItems(Player player) {
        Map<Integer, ItemStack> items = new HashMap<>();
        ItemStack[] contents = shulkerBox.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null)
                continue;
            items.put(slot, item);
        }
        return items;
    }
    
}
