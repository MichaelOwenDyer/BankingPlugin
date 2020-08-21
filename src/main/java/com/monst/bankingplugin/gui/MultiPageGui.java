package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.utils.Ownable;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.Collection;
import java.util.List;

abstract class MultiPageGui<C extends Collection<K>, K extends Ownable> extends Gui<C> {

    final int PREV_PAGE_SLOT;
    final int NEXT_PAGE_SLOT;

    final C guiSubjects;
    List<Menu> menuPages;
    int currentPage = 0;

    MultiPageGui(C guiSubjects, int prevPageSlot, int nextPageSlot) {
        this.guiSubjects = guiSubjects;
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
        if (menuPages.isEmpty())
            return;
        menuPages.get(currentPage).open(player);
    }

    @Override
    void setCloseHandler(Menu.CloseHandler handler) {
        menuPages.forEach(page -> page.setCloseHandler(handler));
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
}
