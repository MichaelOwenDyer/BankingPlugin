package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

public abstract class SinglePageGUI extends GUI {

    SinglePageGUI(BankingPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        this.inventory = createInventory();
        shortenGUIChain();
        createItems(player).forEach(inventory::setItem);
        player.openInventory(inventory);
    }
    
    Map<Integer, ItemStack> createItems(Player player) {
        return Collections.emptyMap();
    }
    
}
