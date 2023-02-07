package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.util.Update;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UpdateGUI extends SinglePageGUI {
    
    private final Update update;
    private final List<String> versionLore;

    public UpdateGUI(BankingPlugin plugin, Player player, Update update) {
        super(plugin, player);
        this.update = update;
        this.versionLore = Collections.singletonList(ChatColor.GOLD.toString() + ChatColor.BOLD + update.getVersion());
    }
    
    @Override
    Inventory createInventory() {
        return Bukkit.createInventory(this, InventoryType.HOPPER, "          Update Available");
    }
    
    @Override
    Map<Integer, ItemStack> createItems(Player player) {
        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(0, item(Material.BOOK, "Current Version", plugin.getDescription().getVersion()));
        items.put(4, item(Material.ENCHANTED_BOOK, "New Version", clickToDownload()));
        return items;
    }
    
    private List<String> clickToDownload() {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(ChatColor.GREEN + "Click to download.");
        return lore;
    }
    
    @Override
    public void click(int slot, ClickType type) {
        if (slot != 4)
            return;
        switch (update.getState()) {
            case DOWNLOADING:
                update.pauseDownload(); break;
            case INITIAL:
            case PAUSED:
            case ERROR:
                update.download()
                        .onDownloadPercentageChange(percentage -> setStatus(downloading(percentage)))
                        .onPause(percentage -> setStatus(paused(percentage)))
                        .onValidating(() -> setStatus(validating()))
                        .onDownloadComplete(() -> setStatus(updateComplete()))
                        .catchError(error -> setStatus(error()));
        }
    }
    
    private void setStatus(List<String> lore) {
        ItemStack item = inventory.getItem(4);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(4, item); // TODO: Line necessary, or does setItemMeta() already update the inventory?
    }
    
    private List<String> downloading(int percentage) {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(ChatColor.RED + "Downloading... (" + percentage + "%)");
        lore.add(ChatColor.RED + "Click to pause download.");
        return lore;
    }
    
    private List<String> paused(int percentage) {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(ChatColor.GREEN + "Download paused. (" + percentage + "%)");
        lore.add(ChatColor.RED + "Click to resume download.");
        return lore;
    }
    
    private List<String> validating() {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(ChatColor.GREEN + "Download complete.");
        lore.add(ChatColor.RED + "Validating... ");
        return lore;
    }
    
    private List<String> updateComplete() {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(ChatColor.GREEN + "Download complete. File successfully validated.");
        return lore;
    }
    
    private List<String> error() {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(ChatColor.DARK_RED + "Download failed. Click to retry.");
        return lore;
    }
    
}
