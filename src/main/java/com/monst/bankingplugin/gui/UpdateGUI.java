package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.util.Callback;
import com.monst.bankingplugin.util.UpdatePackage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.HopperMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateGUI extends SinglePageGUI<UpdatePackage> {

    public UpdateGUI(BankingPlugin plugin, UpdatePackage updatePackage) {
        super(plugin, updatePackage);
    }

    @Override
    Menu createMenu() {
        return HopperMenu.builder().title("          Update Available").build();
    }

    @Override
    ItemStack createSlotItem(int slot) {
        if (slot == 0)
            return createSlotItem(Material.BOOK, "Current Version", Collections.singletonList(plugin.getDescription().getVersion()));
        if (slot == 4)
            return createSlotItem(Material.ENCHANTED_BOOK, "New Version", getDownloadStatus());
        return null;
    }

    private List<String> getDownloadStatus() {
        List<String> lore = new ArrayList<>();
        lore.add(new ColorStringBuilder().gold().bold(guiSubject.getVersion()).toString());
        switch (guiSubject.getState()) {
            case INITIAL:
                lore.add(ChatColor.GREEN + "Click to download.");
                break;
            case PAUSED:
                lore.add(ChatColor.GREEN + "Download paused. (" + guiSubject.getDownloadPercentage() + "%)");
                lore.add(ChatColor.RED + "Click to resume download.");
                break;
            case DOWNLOADING:
                lore.add(ChatColor.RED + "Downloading... (" + guiSubject.getDownloadPercentage() + "%)");
                lore.add(ChatColor.RED + "Click to pause download.");
                break;
            case VALIDATING:
                lore.add(ChatColor.GREEN + "Download complete.");
                lore.add(ChatColor.RED + "Validating... ");
                break;
            case COMPLETED:
                lore.add(ChatColor.GREEN + "Download complete.");
                break;
            case ERROR:
                lore.add(ChatColor.DARK_RED + "Download failed. Click to retry.");
                break;
            case OUTDATED:
                lore.add(ChatColor.DARK_RED + "Update outdated. A newer version is available.");
        }
        return lore;
    }

    @Override
    Slot.ClickHandler createClickHandler(int slot) {
        if (slot == 4)
            return (player, info) -> {
                switch (guiSubject.getState()) {
                    case DOWNLOADING:
                        guiSubject.pauseDownload(); break;
                    case INITIAL:
                    case PAUSED:
                    case ERROR:
                        guiSubject.download(new Callback<>(plugin));
                }
            };
        return null;
    }

    @Override
    GUIType getType() {
        return GUIType.UPDATE;
    }

}
