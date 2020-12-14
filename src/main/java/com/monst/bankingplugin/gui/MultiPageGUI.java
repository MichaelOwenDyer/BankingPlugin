package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

abstract class MultiPageGUI<T> extends GUI<T> {

    private static final int FILTER_SLOT = 29;
    private static final int SORTER_SLOT = 33;

    private final int PREV_PAGE_SLOT = 27;
    private final int NEXT_PAGE_SLOT = 35;

    private final Supplier<Set<? extends T>> source;

    private final List<MenuItemFilter<T>> filters = new ArrayList<>(Collections.singleton(MenuItemFilter.of(ChatColor.GRAY + "All", t -> true)));
    private int currentFilter = 0;

    private final List<MenuItemSorter<T>> sorters = new ArrayList<>(Collections.singleton(MenuItemSorter.of(ChatColor.GRAY + "Unsorted", (t1, t2) -> 0)));
    private int currentSorter = 0;

    private List<Menu> menuPages;
    private int currentPage = 0;

    MultiPageGUI(Supplier<Set<? extends T>> source, List<MenuItemFilter<T>> filters, List<MenuItemSorter<T>> sorters) {
        this.source = source;
        this.filters.addAll(filters);
        this.sorters.addAll(sorters);
    }

    @Override
    void open(boolean initialize) {
        subscribe(getSubject());
        if (initialize)
            shortenGUIChain();
        update();
    }

    @Override
    public void update() {
        if (!isInForeground())
            return;
        initializeMenu();
        addPageTracker();
        addFilteringSortingOptions();
        setCloseHandler((player, info) -> {
            OPEN_PARENT.close(player, info);
            unsubscribe(getSubject());
        });
        if (menuPages == null || menuPages.isEmpty())
            return;
        currentPage = Math.min(menuPages.size() - 1, currentPage);
        menuPages.get(currentPage).open(viewer);
    }

    @Override
    void close(Player player) {
        parentGUI = null;
        menuPages.get(currentPage).close(player);
    }

    @Override
    void initializeMenu() {
        Menu.Builder<?> pageTemplate = ChestMenu.builder(4).title(getTitle()).redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010101010")
                .pattern("101010101")
                .pattern("010101010").build();
        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(createSlotItem(Material.ARROW, "Previous Page", Collections.emptyList()))
                .previousButtonSlot(PREV_PAGE_SLOT)
                .nextButton(createSlotItem(Material.ARROW, "Next Page", Collections.emptyList()))
                .nextButtonSlot(NEXT_PAGE_SLOT);
        addItems(builder);
        menuPages = builder.build();
    }

    @SuppressWarnings("SimplifyOptionalCallChains")
    void addPageTracker() {
        for (Menu page : menuPages) {
            Slot prevSlot = page.getSlot(PREV_PAGE_SLOT);
            Slot.ClickHandler prevPageHandler = prevSlot.getClickHandler().orElse(null);
            if (prevPageHandler != null) {
                prevSlot.setClickHandler((player, info) -> {
                    prevPageHandler.click(player, info);
                    currentPage--;
                });
            }
            Slot nextSlot = page.getSlot(NEXT_PAGE_SLOT);
            Slot.ClickHandler nextPageHandler = nextSlot.getClickHandler().orElse(null);
            if (nextPageHandler != null) {
                nextSlot.setClickHandler((player, info) -> {
                    nextPageHandler.click(player, info);
                    currentPage++;
                });
            }
        }
    }

    void addFilteringSortingOptions() {
        ItemStack filterItem = createSlotItem(Material.HOPPER, "Filtering:", Collections.singletonList(filters.get(currentFilter).getName()));
        Slot.ClickHandler filterHandler = (player, info) -> {
            if (info.getClickType().isLeftClick()) {
                currentFilter++;
                currentFilter %= filters.size();
                this.update();
            } else if (info.getClickType().isRightClick()) {
                currentFilter += filters.size() - 1;
                currentFilter %= filters.size();
                this.update();
            }
        };
        ItemStack sorterItem = createSlotItem(Material.POLISHED_ANDESITE_STAIRS, "Sorting:", Collections.singletonList(sorters.get(currentSorter).getName()));
        Slot.ClickHandler sorterHandler = (player, info) -> {
            if (info.getClickType().isLeftClick()) {
                currentSorter++;
                currentSorter %= sorters.size();
                this.update();
            } else if (info.getClickType().isRightClick()) {
                currentSorter += sorters.size() - 1;
                currentSorter %= sorters.size();
                this.update();
            }
        };
        if (filters.size() > 1)
            for (Menu page : menuPages)
                page.getSlot(FILTER_SLOT).setSettings(SlotSettings.builder().itemTemplate(new StaticItemTemplate(filterItem)).clickHandler(filterHandler).build());

        if (sorters.size() > 1)
            for (Menu page : menuPages)
                page.getSlot(SORTER_SLOT).setSettings(SlotSettings.builder().itemTemplate(new StaticItemTemplate(sorterItem)).clickHandler(sorterHandler).build());
    }

    @Override
    void setCloseHandler(Menu.CloseHandler handler) {
        menuPages.forEach(page -> page.setCloseHandler(handler));
    }

    abstract String getTitle();

    abstract void addItems(PaginatedMenuBuilder builder);

    List<T> getMenuItems() {
        return source.get().stream().filter(filters.get(currentFilter)).sorted(sorters.get(currentSorter)).collect(Collectors.toList());
    }

    abstract Observable getSubject();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MultiPageGUI<?> other = (MultiPageGUI<?>) o;
        return inForeground == other.inForeground
                && currentPage == other.currentPage
                && getType() == other.getType()
                && Utils.samePlayer(viewer, other.viewer)
                && filters.equals(other.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters, sorters, viewer, inForeground, currentPage, getType());
    }

}
