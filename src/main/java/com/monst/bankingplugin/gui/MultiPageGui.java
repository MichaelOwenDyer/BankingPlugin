package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Ownable;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
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
import java.util.Objects;
import java.util.function.Supplier;

abstract class MultiPageGui<C extends Collection<? extends Ownable>> extends Gui<C> {

    private final int PREV_PAGE_SLOT;
    private final int NEXT_PAGE_SLOT;

    final Supplier<? extends C> guiSubjects;
    List<Menu> menuPages;
    private int currentPage = 0;

    MultiPageGui(Supplier<? extends C> guiSubjects, int prevPageSlot, int nextPageSlot) {
        this.guiSubjects = guiSubjects;
        this.PREV_PAGE_SLOT = prevPageSlot;
        this.NEXT_PAGE_SLOT = nextPageSlot;
    }

    @Override
    void open(boolean initialize) {
        subscribe(getSubject());
        if (initialize)
            shortenGuiChain();
        update();
    }

    @Override
    public void update() {
        if (!isInForeground())
            return;
        initializeMenu();
        setClickHandler();
        setCloseHandler((player, info) -> {
            OPEN_PREVIOUS.close(player, info);
            unsubscribe(getSubject());
        });
        if (menuPages == null || menuPages.isEmpty())
            return;
        currentPage = Math.min(menuPages.size() - 1, currentPage);
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

    abstract Observable getSubject();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MultiPageGui<?> other = (MultiPageGui<?>) o;
        return inForeground == other.inForeground
                && currentPage == other.currentPage
                && getType() == other.getType()
                && Utils.samePlayer(viewer, other.viewer)
                && guiSubjects.get().equals(other.guiSubjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guiSubjects.get(), viewer, inForeground, currentPage, getType());
    }

}
