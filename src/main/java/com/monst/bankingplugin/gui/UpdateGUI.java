package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.util.Observer;
import com.monst.bankingplugin.update.Update;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.*;

import static org.bukkit.ChatColor.*;

public class UpdateGUI extends SinglePageGUI implements Observer {
    
    private final Update update;
    private final List<String> versionLore;

    public UpdateGUI(BankingPlugin plugin, Player player, Update update) {
        super(plugin, player);
        this.update = update;
        update.subscribe(this);
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
    
    @Override
    public void click(int slot, ClickType type) {
        if (slot != 4)
            return;
        switch (update.getState()) {
            case DOWNLOADING:
                update.pauseDownload();
                break;
            case INITIAL:
            case DOWNLOAD_FAILED:
            case PAUSED:
                update.download();
        }
    }
    
    @Override
    public void update() {
        switch (update.getState()) {
            case INITIAL:
                setStatus(clickToDownload()); break;
            case DOWNLOADING:
                setStatus(downloading(update.getDownload().getPercentComplete())); break;
            case PAUSED:
                setStatus(paused(update.getDownload().getPercentComplete())); break;
            case VALIDATING:
                setStatus(validating(update.getDownload().getDuration())); break;
            case SUCCESS:
                setStatus(updateComplete(update.getDownload().getDuration(), update.getDownload().isValidated())); break;
            case DOWNLOAD_FAILED:
                setStatus(downloadError()); break;
        }
    }
    
    private void setStatus(List<String> lore) {
        ItemStack item = inventory.getItem(4);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(4, item); // TODO: Line necessary, or does setItemMeta() already update the inventory?
    }
    
    private List<String> clickToDownload() {
        return lore(GREEN + "Click to download.");
    }
    
    private List<String> downloading(int percentage) {
        return lore(
                RED + "Downloading... (" + percentage + "%)",
                RED + "Click to pause download."
        );
    }
    
    private List<String> paused(int percentage) {
        return lore(
                GREEN + "Download paused. (" + percentage + "%)",
                RED + "Click to resume download."
        );
    }
    
    private List<String> validating(Duration duration) {
        // Display duration formatted as "mm:ss.ss"
        return lore(
                GREEN + "Download complete. (" + formatDuration(duration) + ")",
                RED + "Validating... "
        );
    }
    
    private List<String> updateComplete(Duration duration, boolean validated) {
        List<String> lore = new ArrayList<>(versionLore);
        lore.add(GREEN + "Download complete. (" + formatDuration(duration) + ")");
        if (validated)
            lore.add(GREEN + "Update successfully validated.");
        return lore;
    }
    
    private List<String> downloadError() {
        return lore(DARK_RED + "Download failed. Click to retry.");
    }
    
    private List<String> lore(String... lines) {
        List<String> lore = new ArrayList<>(versionLore);
        lore.addAll(Arrays.asList(lines));
        return lore;
    }
    
    private static String formatDuration(Duration duration) {
        // format as "mm:ss.ss"
        return String.format("%02d:%02d.%02d", duration.toMinutes(), duration.getSeconds() % 60, duration.getNano() / 10_000_000) + "s";
    }
    
}
