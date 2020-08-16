package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import org.bukkit.Material;
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

public class AccountListGui extends MultiPageGui<Bank> {

    public AccountListGui(Bank bank) {
        super(bank, 18, 26);
    }

    @Override
    void initializeMenu() {
        @SuppressWarnings("rawtypes")
        Menu.Builder pageTemplate = ChestMenu.builder(3).title("Account List").redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010101010")
                .pattern("101010101")
                .pattern("010101010").build();
        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(createSlotItem(Material.ARROW, "Previous Page", Collections.emptyList()))
                .previousButtonSlot(PREV_SLOT)
                .nextButton(createSlotItem(Material.ARROW, "Next Page", Collections.emptyList()))
                .nextButtonSlot(NEXT_SLOT);
        for (Account account : guiSubject.getAccounts()) {
            ItemStack item = createSlotItem(Material.CHEST, account.getColorizedName(), Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGui(account).setPrevGui(this).open(player);
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
        pages = builder.build();
    }

    @Override
    void setCloseHandler(Menu.CloseHandler handler) {
        pages.forEach(page -> page.setCloseHandler(handler));
    }

    @Override
    GuiType getType() {
        return GuiType.ACCOUNT_LIST;
    }
}
