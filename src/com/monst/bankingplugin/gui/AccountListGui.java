package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Collections;
import java.util.List;

public class AccountListGui extends Gui<Bank> {

    private List<Menu> pages;
    private int currentPage = 0;

    private final int prevButtonSlot = 18;
    private final int nextButtonSlot = 26;

    public AccountListGui(Bank bank) {
        super(BankingPlugin.getInstance(), bank);
    }

    @Override
    void open(Player player, boolean update) {
        if (update) {
            createMenu();
            setClickHandler();
            setCloseHandler(CLOSE_HANDLER);
            shortenGuiChain();
        }
        if (pages.isEmpty())
            return;
        pages.get(currentPage).open(player);
    }

    @Override
    void createMenu() {
        @SuppressWarnings("rawtypes")
        Menu.Builder pageTemplate = ChestMenu.builder(3).title("Account List").redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010101010")
                .pattern("101010101")
                .pattern("010101010").build();
        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(createSlotItem(Material.ARROW, "Previous Page", Collections.emptyList()))
                .previousButtonSlot(prevButtonSlot)
                .nextButton(createSlotItem(Material.ARROW, "Next Page", Collections.emptyList()))
                .nextButtonSlot(nextButtonSlot);
        for (Account account : guiSubject.getAccounts()) {
            ItemStack item = createSlotItem(Material.CHEST, account.getColorizedName(), Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGui(account).setPrevGui(this).open(player);
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
        pages = builder.build();
    }

    private void setClickHandler() {
        for (Menu page : pages) {
            for (Slot slot : new Slot[]{page.getSlot(prevButtonSlot), page.getSlot(nextButtonSlot)}) {
                Slot.ClickHandler prevHandler = slot.getClickHandler().orElse(null);
                if (prevHandler != null)
                    slot.setClickHandler((player, info) -> {
                        prevHandler.click(player, info);
                        currentPage--;
                    });
            }
        }
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
