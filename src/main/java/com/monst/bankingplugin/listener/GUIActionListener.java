package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.gui.GUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class GUIActionListener implements Listener {
    
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        if (!(ih instanceof GUI))
            return;
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE)
            return;
        e.setCancelled(true); // Do not allow item manipulation of any kind on GUIs
        ((GUI) ih).click(e.getSlot(), e.getClick());
    }
    
    @EventHandler
    public void onMouseDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof GUI)
            e.setCancelled(true);
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        if (ih instanceof GUI)
            ((GUI) ih).onClose();
    }
    
}
