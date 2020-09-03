package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Ownable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

abstract class MultiPageGui<C extends Collection<? extends Ownable>> extends Gui<C> {

    private final int PREV_PAGE_SLOT;
    private final int NEXT_PAGE_SLOT;

    final C guiSubjects;
    List<Menu> menuPages;
    private int currentPage = 0;

    MultiPageGui(C guiSubjects, int prevPageSlot, int nextPageSlot) {
        this.guiSubjects = guiSubjects;
        PREV_PAGE_SLOT = prevPageSlot;
        NEXT_PAGE_SLOT = nextPageSlot;
    }

    @Override
    void open(boolean update) {
        if (update) {
            initializeMenu();
            setClickHandler();
            setCloseHandler(OPEN_PREVIOUS);
            shortenGuiChain();
        }
        if (menuPages.isEmpty())
            return;
        menuPages.get(currentPage).open(viewer);
    }

    @Override
    void close(Player player) {
        prevGui = null;
        menuPages.get(currentPage).close(player);
    }

    @Override
    void initializeMenu() {
        Menu.Builder<?> pageTemplate = getPageTemplate();
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
    void setClickHandler() {
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

    @Override
    void setCloseHandler(Menu.CloseHandler handler) {
        menuPages.forEach(page -> page.setCloseHandler(handler));
    }

    abstract Menu.Builder<?> getPageTemplate();

    abstract void addItems(PaginatedMenuBuilder builder);

}
