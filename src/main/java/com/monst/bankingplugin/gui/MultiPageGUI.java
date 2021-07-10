package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.utils.Callback;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A GUI with multiple pages.
 * @param <T> the type of the many objects represented in this GUI.
 */
abstract class MultiPageGUI<T> extends GUI<Collection<T>> {

    private static final int PREV_PAGE_SLOT = 27;
    private static final int NEXT_PAGE_SLOT = 35;
    private static final int FILTER_SLOT = 29;
    private static final int SORTER_SLOT = 33;

    private final Consumer<Callback<Collection<T>>> itemAccessor;
    private Collection<T> mostRecentItems;

    private final PaginatedMenuBuilder builder;

    private final ItemFilterer itemFilterer;
    private final ItemSorter itemSorter;

    private List<Menu> menuPages;
    private int currentPage;

    MultiPageGUI(Consumer<Callback<Collection<T>>> asynchronousAccessor,
                 List<MenuItemFilter<? super T>> filters, List<MenuItemSorter<? super T>> sorters) {
        this.itemAccessor = asynchronousAccessor;
        this.itemFilterer = new ItemFilterer(filters);
        this.itemSorter = new ItemSorter(sorters);
        this.builder = createBuilder();
    }

    MultiPageGUI(Supplier<Collection<T>> synchronousAccessor,
                 List<MenuItemFilter<? super T>> filters, List<MenuItemSorter<? super T>> sorters) {
        this(callback -> callback.onResult(synchronousAccessor.get()), filters, sorters);
    }

    @Override
    public void open(Player player) {
        subscribe();
        shortenGUIChain();
        itemAccessor.accept(Callback.of(newElements -> {
            mostRecentItems = newElements;
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
        if (!isInForeground()) {
            needsUpdate = true;
            return;
        }
        needsUpdate = false;
        itemAccessor.accept(Callback.of(newElements -> {
            mostRecentItems = newElements;
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
                .newMenuModifier(this::addStandardModifications)
                .newMenuModifier(this::addCustomModifications);
    }

    private void addStandardModifications(Menu page) {
        page.setCloseHandler(CLOSE_HANDLER);
    }

    /**
     * Can be overridden by subclasses to further customize the menu pages
     */
    void addCustomModifications(Menu page) {

    }

    private List<Menu> createMenuPages() {
        try {
            Field field = PaginatedMenuBuilder.class.getDeclaredField("items");
            field.setAccessible(true);
            field.set(builder, getMenuItems());
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        List<Menu> menuPages = builder.build();
        itemFilterer.apply(menuPages, FILTER_SLOT);
        itemSorter.apply(menuPages, SORTER_SLOT);
        linkPages(menuPages);
        return menuPages;
    }

    private List<SlotSettings> getMenuItems() {
        return mostRecentItems.stream()
                .filter(itemFilterer.getCurrent())
                .sorted(itemSorter.getCurrent())
                .map(this::createSlotSettings)
                .collect(Collectors.toList());
    }

    private void linkPages(List<Menu> menuPages) {
        for (int i = 1 ; i < menuPages.size() ; i++)
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

    private abstract class ItemManager<C extends MenuItemController> {
        List<C> list;
        int current;
        ItemManager(C neutralController, List<C> controllers) {
            list = new ArrayList<>();
            list.add(neutralController);
            list.addAll(controllers);
        }
        void click(Player player, ClickInformation click) {
            if (click.getClickType().isLeftClick()) {
                current++;
                current %= list.size();
                reloadPages();
            } else if (click.getClickType().isRightClick()) {
                current += list.size() - 1;
                current %= list.size();
                reloadPages();
            }
        }
        C getCurrent() {
            return list.get(current);
        }
        void apply(List<Menu> menuPages, int slotIndex) {
            SlotSettings slotSettings = SlotSettings.builder()
                    .itemTemplate(getSlotItem())
                    .clickHandler(this::click)
                    .build();
            for (Menu page : menuPages)
                page.getSlot(slotIndex).setSettings(slotSettings);
        }
        abstract StaticItemTemplate getSlotItem();
    }

    private class ItemSorter extends ItemManager<MenuItemSorter<? super T>> {
        ItemSorter(List<MenuItemSorter<? super T>> sorters) {
            super(MenuItemSorter.unsorted(), sorters);
        }
        @Override
        StaticItemTemplate getSlotItem() {
            return new StaticItemTemplate(createSlotItem(
                    Material.POLISHED_ANDESITE_STAIRS,
                    "Sorting:",
                    Collections.singletonList(getCurrent().getName())
            ));
        }
    }

    private class ItemFilterer extends ItemManager<MenuItemFilter<? super T>> {
        ItemFilterer(List<MenuItemFilter<? super T>> filters) {
            super(MenuItemFilter.all(), filters);
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MultiPageGUI<?> other = (MultiPageGUI<?>) o;
        return inForeground == other.inForeground
                && currentPage == other.currentPage
                && getType() == other.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemFilterer, itemSorter, inForeground, currentPage, getType());
    }

}
