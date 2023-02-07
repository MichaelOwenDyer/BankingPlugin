package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashMap;
import java.util.Map;

public class AccountContentsGUI extends SinglePageGUI {

    private final Account account;
    private final Inventory accountInventory;
    private final Map<Integer, Runnable> clickHandlers;
    // TODO: Implement remote editing

    public AccountContentsGUI(BankingPlugin plugin, Player player, Account account) throws IllegalArgumentException {
        super(plugin, player);
        this.account = account;
        this.accountInventory = account.getLocation().findChest().orElseThrow(IllegalArgumentException::new).getInventory();
        this.clickHandlers = createClickHandlers(accountInventory.getContents());
        // TODO: Subscribe to account changes?
    }
    
    @Override
    Inventory createInventory() {
        return Bukkit.createInventory(this, accountInventory.getSize(), account.getName());
    }
    
    @Override
    Map<Integer, ItemStack> createItems(Player player) {
        Map<Integer, ItemStack> items = new HashMap<>();
        ItemStack[] contents = accountInventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null)
                continue;
            items.put(slot, item);
        }
        return items;
    }
    
    Map<Integer, Runnable> createClickHandlers(ItemStack[] contents) {
        Map<Integer, Runnable> clickHandlers = new HashMap<>();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null)
                continue;
            if (!(item.getItemMeta() instanceof BlockStateMeta))
                continue;
            BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
            if (!(im.getBlockState() instanceof ShulkerBox))
                continue;
            ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
            clickHandlers.put(slot, () -> child(new ShulkerBoxGUI(plugin, player, shulkerBox)).open());
        }
        return clickHandlers;
    }
    
    @Override
    public void click(int slot, ClickType type) {
        if (clickHandlers.containsKey(slot))
            clickHandlers.get(slot).run();
    }
    
}
