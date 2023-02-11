package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.option.MenuItemFilter;
import com.monst.bankingplugin.gui.option.MenuItemSorter;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.IntStream;

/**
 * A GUI with multiple pages.
 * @param <T> the type of the many objects represented in this GUI.
 */
abstract class MultiPageGUI<T> extends GUI {
    
    private static final String SLOT_MASK = "010101010"
                                          + "101010101"
                                          + "010101010";
    private static final int[] ITEM_SLOTS;
    static {
        char[] chars = SLOT_MASK.toCharArray();
        ITEM_SLOTS = IntStream.range(0, chars.length)
                .filter(i -> chars[i] == '1')
                .toArray();
    }

    private static final int PREV_PAGE_SLOT = 27;
    private static final int NEXT_PAGE_SLOT = 35;
    
    private static final int FILTER_SLOT = 29;
    private static final int SORTER_SLOT = 33;

    private final Map<Integer, T> displayedItems = new HashMap<>();

    private final ItemFilter itemFilter = new ItemFilter();
    private final ItemSorterSlot itemSorterSlot = new ItemSorterSlot();

    private int currentPage;
    
    MultiPageGUI(BankingPlugin plugin, Player player) {
        super(plugin, player);
    }
    
    @Override
    Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(this, 4 * 9, getTitle());
        inventory.setItem(FILTER_SLOT, itemFilter.createItem());
        inventory.setItem(SORTER_SLOT, itemSorterSlot.createItem());
        createExtraItems().forEach(inventory::setItem);
        return inventory;
    }

    @Override
    public void open() {
        this.inventory = createInventory();
        shortenGUIChain();
        countItems().then(count -> {
            int pageSize = ITEM_SLOTS.length;
            int maxPage = Math.max(0, count - 1) / pageSize;
            currentPage = Math.min(currentPage, maxPage);
            if (currentPage > 0)
                inventory.setItem(PREV_PAGE_SLOT, item(Material.ARROW, "Previous page"));
            if (currentPage < maxPage)
                inventory.setItem(NEXT_PAGE_SLOT, item(Material.ARROW, "Next page"));
            int offset = currentPage * pageSize;
            fetchItems(offset, pageSize).then(this::populateInventory).finallyDo(() -> player.openInventory(inventory));
        });
    }
    
    void reloadPage() {
        countItems().then(count -> {
            int pageSize = ITEM_SLOTS.length;
            int maxPage = Math.max(0, count - 1) / pageSize;
            currentPage = Math.min(currentPage, maxPage);
            int offset = currentPage * pageSize;
            fetchItems(offset, pageSize).then(this::populateInventory);
        });
    }
    
    private void populateInventory(List<T> items) {
        displayedItems.clear();
        Iterator<T> iterator = items.iterator();
        for (int slot : ITEM_SLOTS) {
            if (iterator.hasNext()) {
                T item = iterator.next();
                inventory.setItem(slot, createItem(item));
                displayedItems.put(slot, item);
            } else
                inventory.setItem(slot, null);
        }
    }
    
    abstract Promise<Integer> countItems();
    
    abstract Promise<List<T>> fetchItems(int offset, int limit);

    /**
     * Gets the name of this GUI, which is displayed in the title bar.
     * @return the GUI title
     */
    abstract String getTitle();

    abstract ItemStack createItem(T t);
    
    Map<Integer, ItemStack> createExtraItems() {
        return Collections.emptyMap();
    }
    
    @Override
    public void click(int slot, ClickType clickType) {
        if (slot == NEXT_PAGE_SLOT)
            nextPage();
        else if (slot == PREV_PAGE_SLOT)
            previousPage();
        else if (slot == 36)
            itemFilter.click(clickType);
        else if (slot == 44)
            itemSorterSlot.click(clickType);
        else if (displayedItems.containsKey(slot))
            click(displayedItems.get(slot), clickType);
    }
    
    void click(T t, ClickType click) {
    
    }
    
    private void previousPage() {
        if (currentPage == 0)
            return;
        currentPage--;
        reloadPage();
    }
    
    private void nextPage() {
        currentPage++;
        reloadPage();
    }

    private abstract class OptionCycler<Option> {
        private final List<Option> list = new ArrayList<>(6);
        private int currentIndex;
        OptionCycler(Option standardOption, List<Option> customOptions) {
            list.add(standardOption);
            list.addAll(customOptions);
        }
        public void click(ClickType click) {
            if (click.isLeftClick()) {
                currentIndex++;
                currentIndex %= list.size();
                reloadPage();
            } else if (click.isRightClick()) {
                currentIndex += list.size() - 1;
                currentIndex %= list.size();
                reloadPage();
            }
        }
        Option getCurrent() {
            return list.get(currentIndex);
        }
    }

    private class ItemFilter extends OptionCycler<MenuItemFilter<? super T>> {
        ItemFilter() {
            super(MenuItemFilter.all(), getFilters());
        }
        private ItemStack createItem() {
            return item(Material.HOPPER, "Filtering:", getCurrent().getName());
        }
    }

    private class ItemSorterSlot extends OptionCycler<MenuItemSorter<? super T>> {
        ItemSorterSlot() {
            super(MenuItemSorter.unsorted(), getSorters());
        }
        private ItemStack createItem() {
            return item(Material.QUARTZ_STAIRS, "Sorting:", getCurrent().getName());
        }
    }
    
    List<MenuItemFilter<? super T>> getFilters() {
        return Collections.emptyList();
    }
    
    List<MenuItemSorter<? super T>> getSorters() {
        return Collections.emptyList();
    }

}
