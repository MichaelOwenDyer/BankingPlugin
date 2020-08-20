package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.utils.Ownable;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.List;

abstract class MultiPageGui<T extends Ownable> extends Gui<T> {

    final int PREV_PAGE_SLOT;
    final int NEXT_PAGE_SLOT;

    List<Menu> pages;
    int currentPage = 0;

    MultiPageGui(T guiSubject, int prevPageSlot, int nextPageSlot) {
        super(guiSubject);
        PREV_PAGE_SLOT = prevPageSlot;
        NEXT_PAGE_SLOT = nextPageSlot;
    }

    @Override
    void open(Player player, boolean update) {
        if (update) {
            initializeMenu();
            setClickHandler();
            setCloseHandler(OPEN_PREVIOUS);
            shortenGuiChain();
        }
        if (pages.isEmpty())
            return;
        pages.get(currentPage).open(player);
    }

    @SuppressWarnings("SimplifyOptionalCallChains")
    void setClickHandler() {
        for (Menu page : pages) {
            Slot prevSlot = page.getSlot(PREV_PAGE_SLOT);
            Slot.ClickHandler prevPageHandler = prevSlot.getClickHandler().orElse(null);
            if (prevPageHandler != null)
                prevSlot.setClickHandler((player, info) -> {
                    prevPageHandler.click(player, info);
                    currentPage--;
                });
            Slot nextSlot = page.getSlot(NEXT_PAGE_SLOT);
            Slot.ClickHandler nextPageHandler = nextSlot.getClickHandler().orElse(null);
            if (nextPageHandler != null)
                nextSlot.setClickHandler((player, info) -> {
                    nextPageHandler.click(player, info);
                    currentPage++;
                });
        }
    }
}
