package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.util.Callback;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.ClickInformation;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A GUI and multiple pages.
 * @param <T> the type of the many objects represented in this GUI.
 */
abstract class MultiPageGUI<T> extends GUI<Collection<T>> {

    private static final int PREV_PAGE_SLOT = 27;
    private static final int NEXT_PAGE_SLOT = 35;

    private final Consumer<Callback<Collection<T>>> itemAccessor;
    private Collection<T> mostRecentItems;

    private final PaginatedMenuBuilder pageBuilder;

    private final ItemFilterSlot itemFilterSlot = new ItemFilterSlot();
    private final ItemSorterSlot itemSorterSlot = new ItemSorterSlot();

    private List<Menu> menuPages;
    private int currentPage;

    MultiPageGUI(BankingPlugin plugin, Consumer<Callback<Collection<T>>> itemAccessor) {
        super(plugin);
        this.itemAccessor = itemAccessor;
        this.pageBuilder = createBuilder();
    }

    @Override
    public void open(Player player) {
        itemAccessor.accept(Callback.of(plugin, newElements -> {
            mostRecentItems = newElements;
            subscribe();
            shortenGUIChain();
            menuPages = createMenuPages();
            currentPage().open(player);
        }));
    }

    @Override
    void reopen(Player player) {
        if (needsUpdate)
            update();
        currentPage().open(player);
    }

    @Override
    public void update() {
        if (!isVisible()) {
            needsUpdate = true;
            return;
        }
        itemAccessor.accept(Callback.of(plugin, newElements -> {
            mostRecentItems = newElements;
            needsUpdate = false;
            reloadPages();
        }));
    }

    private void reloadPages() {
        List<Menu> menuPages = createMenuPages();
        Menu currentlyOpen = currentPage();
        currentPage = Math.min(currentPage, menuPages.size() - 1);
        for (Slot slot : menuPages.get(currentPage))
            currentlyOpen.getSlot(slot.getIndex()).setSettings(slot.getSettings());
        if (currentPage > 0)
            linkPages(menuPages.get(currentPage - 1), currentlyOpen);
        if (currentPage < menuPages.size() - 1)
            linkPages(currentlyOpen, menuPages.get(currentPage + 1));
        menuPages.remove(currentPage);
        menuPages.add(currentPage, currentlyOpen);
        this.menuPages = menuPages;
    }

    @Override
    void close(Player player) {
        currentPage().close(player);
    }

    private Menu currentPage() {
        return menuPages.get(currentPage);
    }

    /**
     * Gets the name of this GUI, which is displayed in the title bar.
     * @return the GUI title
     */
    abstract String getTitle();

    abstract SlotSettings createSlotSettings(T t);

    private PaginatedMenuBuilder createBuilder() {
        Menu.Builder<?> pageTemplate = ChestMenu.builder(4).title(getTitle()).redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010101010")
                .pattern("101010101")
                .pattern("010101010").build();
        return PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(createSlotItem(Material.ARROW, "Previous Page", Collections.emptyList()))
                .previousButtonSlot(PREV_PAGE_SLOT)
                .nextButton(createSlotItem(Material.ARROW, "Next Page", Collections.emptyList()))
                .nextButtonSlot(NEXT_PAGE_SLOT)
                .newMenuModifier(this::setCloseHandler)
                .newMenuModifier(this::modify);
    }

    private void setCloseHandler(Menu page) {
        page.setCloseHandler(CLOSE_HANDLER);
    }

    private List<Menu> createMenuPages() {
        try {
            Field field = PaginatedMenuBuilder.class.getDeclaredField("items");
            field.setAccessible(true);
            field.set(pageBuilder, getMenuItems());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.debug(e);
        }
        List<Menu> menuPages = pageBuilder.build();
        itemFilterSlot.addToPagesAtSlot(menuPages, 29);
        itemSorterSlot.addToPagesAtSlot(menuPages, 33);
        linkPages(menuPages);
        return menuPages;
    }

    private List<SlotSettings> getMenuItems() {
        return mostRecentItems.stream()
                .filter(itemFilterSlot.getCurrent())
                .sorted(itemSorterSlot.getCurrent())
                .map(this::createSlotSettings)
                .collect(Collectors.toList());
    }

    private void linkPages(List<Menu> menuPages) {
        for (int i = 1; i < menuPages.size(); i++)
            linkPages(menuPages.get(i - 1), menuPages.get(i));
    }

    private void linkPages(Menu prev, Menu next) {
        prev.getSlot(NEXT_PAGE_SLOT).setClickHandler((p, c) -> {
            next.open(p);
            currentPage++;
        });
        next.getSlot(PREV_PAGE_SLOT).setClickHandler((p, c) -> {
            prev.open(p);
            currentPage--;
        });
    }

    /**
     * Can be overridden by subclasses to further customize the menu pages
     */
    void modify(Menu page) {

    }

    private abstract class ItemManager<C extends MenuItemController> {
        private final List<C> list;
        private int currentIndex;
        ItemManager(C neutralController, List<C> controllers) {
            list = new ArrayList<>();
            list.add(neutralController);
            list.addAll(controllers);
        }
        void click(Player player, ClickInformation click) {
            if (click.getClickType().isLeftClick()) {
                currentIndex++;
                currentIndex %= list.size();
                reloadPages();
            } else if (click.getClickType().isRightClick()) {
                currentIndex += list.size() - 1;
                currentIndex %= list.size();
                reloadPages();
            }
        }
        C getCurrent() {
            return list.get(currentIndex);
        }
        void addToPagesAtSlot(List<Menu> menuPages, int slotIndex) {
            SlotSettings slotSettings = SlotSettings.builder()
                    .itemTemplate(getSlotItem())
                    .clickHandler(this::click)
                    .build();
            for (Menu page : menuPages)
                page.getSlot(slotIndex).setSettings(slotSettings);
        }
        abstract StaticItemTemplate getSlotItem();
    }

    private class ItemFilterSlot extends ItemManager<MenuItemFilter<? super T>> {
        ItemFilterSlot() {
            super(MenuItemFilter.all(), getFilters());
        }
        @Override
        StaticItemTemplate getSlotItem() {
            return new StaticItemTemplate(createSlotItem(
                    Material.HOPPER,
                    "Filtering:",
                    Collections.singletonList(getCurrent().getName())
            ));
        }
    }

    List<MenuItemFilter<? super T>> getFilters() {
        return Collections.emptyList();
    }

    private class ItemSorterSlot extends ItemManager<MenuItemSorter<? super T>> {
        ItemSorterSlot() {
            super(MenuItemSorter.unsorted(), getSorters());
        }
        @Override
        StaticItemTemplate getSlotItem() {
            return new StaticItemTemplate(createSlotItem(
                    Material.QUARTZ_STAIRS,
                    "Sorting:",
                    Collections.singletonList(getCurrent().getName())
            ));
        }
    }

    List<MenuItemSorter<? super T>> getSorters() {
        return Collections.emptyList();
    }

}
