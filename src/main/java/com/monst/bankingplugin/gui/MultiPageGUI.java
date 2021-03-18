package com.monst.bankingplugin.gui;

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

/**
 * A GUI with multiple pages.
 * @param <T> the type of the many objects represented in this GUI.
 */
abstract class MultiPageGUI<T> extends GUI<Collection<T>> {

    private static final int PREV_PAGE_SLOT = 27;
    private static final int NEXT_PAGE_SLOT = 35;
    private static final int FILTER_SLOT = 29;
    private static final int SORTER_SLOT = 33;

    private final Supplier<? extends Collection<? extends T>> source;
    private Collection<? extends T> mostRecentItems;

    private final List<MenuItemFilter<? super T>> filters = new ArrayList<>(Collections.singleton(MenuItemFilter.of(ChatColor.GRAY + "All", t -> true)));
    private int currentFilter = 0;
    private SlotSettings filterSlotSettings = null;

    private final List<MenuItemSorter<? super T>> sorters = new ArrayList<>(Collections.singleton(MenuItemSorter.of(ChatColor.GRAY + "Unsorted", (t1, t2) -> 0)));
    private int currentSorter = 0;
    private SlotSettings sorterSlotSettings = null;

    private List<Menu> menuPages;
    private int currentPage = 0;

    MultiPageGUI(Supplier<? extends Collection<? extends T>> source, List<MenuItemFilter<? super T>> filters, List<MenuItemSorter<? super T>> sorters) {
        this.source = source;
        this.filters.addAll(filters);
        this.sorters.addAll(sorters);
    }

    @Override
    void open(boolean firstTime) {
        subscribe(getSubject());
        if (firstTime)
            shortenGUIChain();
        update();
    }

    @Override
    public void update() {
        update(true);
    }

    private void update(boolean reloadItems) {
        if (!isInForeground())
            return;
        this.menuPages = createMenuPages(reloadItems);
        filterSlotSettings = null;
        sorterSlotSettings = null;
        if (menuPages.isEmpty())
            return;
        currentPage = Math.max(0, Math.min(currentPage, menuPages.size() - 1));
        menuPages.get(currentPage).open(viewer);
    }

    @Override
    void close(Player player) {
        parentGUI = null;
        menuPages.get(currentPage).close(player);
    }

    private List<Menu> createMenuPages(boolean reloadItems) {
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
                .addSlotSettings(getMenuItems(reloadItems))
                .newMenuModifier(this::addStandardModifications)
                .build();
    }

    private List<SlotSettings> getMenuItems(boolean reloadItems) {
        if (reloadItems || mostRecentItems == null)
            mostRecentItems = source.get();
        return mostRecentItems.stream()
                .filter(filters.get(currentFilter))
                .sorted(sorters.get(currentSorter))
                .map(this::createSlotSettings)
                .collect(Collectors.toList());
    }

    private void addStandardModifications(Menu page) {
        addPageTracker(page);
        if (filters.size() > 1)
            page.getSlot(FILTER_SLOT).setSettings(getFilterSlotSettings());
        if (sorters.size() > 1)
            page.getSlot(SORTER_SLOT).setSettings(getSorterSlotSettings());
        page.setCloseHandler(CLOSE_HANDLER);
        addCustomModifications(page);
    }

    @SuppressWarnings("SimplifyOptionalCallChains")
    private void addPageTracker(Menu page) {
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

    private SlotSettings getFilterSlotSettings() {
        if (filterSlotSettings != null)
            return filterSlotSettings;
        ItemStack filterItem = createSlotItem(
                Material.HOPPER,
                "Filtering:",
                Collections.singletonList(filters.get(currentFilter).getName())
        );
        Slot.ClickHandler filterHandler = (player, info) -> {
            if (info.getClickType().isLeftClick()) {
                currentFilter++;
                currentFilter %= filters.size();
                this.update(false);
            } else if (info.getClickType().isRightClick()) {
                currentFilter += filters.size() - 1;
                currentFilter %= filters.size();
                this.update(false);
            }
        };
        return filterSlotSettings = SlotSettings.builder()
                .itemTemplate(new StaticItemTemplate(filterItem))
                .clickHandler(filterHandler)
                .build();
    }

    private SlotSettings getSorterSlotSettings() {
        if (sorterSlotSettings != null)
            return sorterSlotSettings;
        ItemStack sorterItem = createSlotItem(
                Material.POLISHED_ANDESITE_STAIRS,
                "Sorting:",
                Collections.singletonList(sorters.get(currentSorter).getName())
        );
        Slot.ClickHandler sorterHandler = (player, info) -> {
            if (info.getClickType().isLeftClick()) {
                currentSorter++;
                currentSorter %= sorters.size();
                this.update(false);
            } else if (info.getClickType().isRightClick()) {
                currentSorter += sorters.size() - 1;
                currentSorter %= sorters.size();
                this.update(false);
            }
        };
        return sorterSlotSettings = SlotSettings.builder()
                .itemTemplate(new StaticItemTemplate(sorterItem))
                .clickHandler(sorterHandler)
                .build();
    }

    /**
     * Gets the name of this GUI, which is displayed in the title bar.
     * @return the GUI title
     */
    abstract String getTitle();

    abstract SlotSettings createSlotSettings(T t);

    /**
     * Can be overridden by subclasses to further customize the menu pages
     */
    void addCustomModifications(Menu page) {

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
                && getType() == other.getType()
                && Utils.samePlayer(viewer, other.viewer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters, sorters, viewer, inForeground, currentPage, getType());
    }

}
