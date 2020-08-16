package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.utils.Ownable;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.List;

abstract class MultiPageGui<T extends Ownable> extends Gui<T> {

    final int PREV_SLOT;
    final int NEXT_SLOT;

    List<Menu> pages;
    int currentPage = 0;

    MultiPageGui(T guiSubject, int prevSlot, int nextSlot) {
        super(guiSubject);
        PREV_SLOT = prevSlot;
        NEXT_SLOT = nextSlot;
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

    void setClickHandler() {
        for (Menu page : pages) {
            for (Slot slot : new Slot[]{page.getSlot(PREV_SLOT), page.getSlot(NEXT_SLOT)}) {
                //noinspection SimplifyOptionalCallChains
                Slot.ClickHandler prevHandler = slot.getClickHandler().orElse(null);
                if (prevHandler != null)
                    slot.setClickHandler((player, info) -> {
                        prevHandler.click(player, info);
                        currentPage--;
                    });
            }
        }
    }

}
