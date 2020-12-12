package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.utils.Observable;
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
import java.util.function.Supplier;

public class AccountListGUI extends MultiPageGUI<Collection<Account>> {

    public AccountListGUI(Supplier<? extends Collection<Account>> accounts) {
        super(accounts, 18, 26);
    }

    @Override
    Menu.Builder<?> getPageTemplate() {
        return ChestMenu.builder(3).title("Account List").redraw(true);
    }

    @Override
    void addItems(PaginatedMenuBuilder builder) {
        for (Account account : guiSubjects.get()) {
            ItemStack item = createSlotItem(account.getOwner(), account.getChestName(),
                    Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGUI(account).setParentGUI(this).open(player);
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
    }

    @Override
    Observable getSubject() {
        return plugin.getAccountRepository();
    }

    @Override
    GuiType getType() {
        return GuiType.ACCOUNT_LIST;
    }
}
