package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountListGui extends Gui<Bank> {

    private List<Menu> pages;

    private static final Material NEXT_BUTTON = Material.ARROW;
    private static final Material PREVIOUS_BUTTON = Material.ARROW;

    private int page = 0;

    public AccountListGui(Bank bank) {
        super(BankingPlugin.getInstance(), bank);
    }

    @Override
    public void open(Player player) {
        pages = getPaginatedMenu();
        if (pages.isEmpty())
            return;
        setCloseHandler(CLOSE_HANDLER);
        pages.get(page).open(player);
    }

    private ArrayList<Menu> getPaginatedMenu() {
        Menu.Builder pageTemplate = ChestMenu.builder(3).title("Account List").redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010101010")
                .pattern("101010101")
                .pattern("010101010").build();
        int prevButtonSlot = 18;
        int nextButtonSlot = 26;
        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(createSlotItem(PREVIOUS_BUTTON, "Previous Page", Collections.emptyList()))
                .previousButtonSlot(prevButtonSlot)
                .nextButton(createSlotItem(NEXT_BUTTON, "Next Page", Collections.emptyList()))
                .nextButtonSlot(nextButtonSlot);
        for (Account account : guiSubject.getAccounts()) {
            ItemStack item = createSlotItem(Material.CHEST, account.getColorizedName(), Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGui(account).setPrevGui(this).open(player);
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
        ArrayList<Menu> pages = (ArrayList<Menu>) builder.build();
        for (Menu menu : pages) {
            Slot prevSlot = menu.getSlot(prevButtonSlot);
            Slot.ClickHandler prevHandler = prevSlot.getClickHandler().orElse(null);
            prevSlot.setClickHandler((player, info) -> {
                if (prevHandler != null)
                    prevHandler.click(player, info);
                page--;
            });
            Slot nextSlot = menu.getSlot(nextButtonSlot);
            Slot.ClickHandler nextHandler = nextSlot.getClickHandler().orElse(null);
            nextSlot.setClickHandler((player, info) -> {
                if (nextHandler != null)
                    nextHandler.click(player, info);
                page++;
            });
        }
        return pages;
    }

    @Override
    Menu getMenu() {
        return null;
    }

    @Override
    void evaluateClearance(Player player) {
    }

    @Override
    ItemStack createSlotItem(int i) {
        return null;
    }

    @Override
    Slot.ClickHandler createClickHandler(int i) {
        return null;
    }

    @Override
    void setCloseHandler(Menu.CloseHandler handler) {
        for (Menu page : pages)
            page.setCloseHandler(handler);
    }

    @Override
    GuiType getType() {
        return GuiType.ACCOUNT_LIST;
    }
}