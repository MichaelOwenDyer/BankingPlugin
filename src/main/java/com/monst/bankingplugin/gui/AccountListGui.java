package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Collection;
import java.util.Collections;

public class AccountListGui extends MultiPageGui<Collection<Account>, Account> {

    public AccountListGui(Collection<Account> accounts) {
        super(accounts, 18, 26);
    }

    @Override
    Menu.Builder<?> getPageTemplate() {
        return ChestMenu.builder(3).title("Account List").redraw(true);
    }

    @Override
    void addItems(PaginatedMenuBuilder builder) {
        for (Account account : guiSubjects) {
            ItemStack item = createSlotItem(account.getOwner(), account.getColorizedName(), Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGui(account).setPrevGui(this).open(player);
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
    }

    @Override
    GuiType getType() {
        return GuiType.ACCOUNT_LIST;
    }
}
