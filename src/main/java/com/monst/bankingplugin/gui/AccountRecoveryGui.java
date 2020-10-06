package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Observable;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AccountRecoveryGui extends MultiPageGui<Collection<Account>> {

    public AccountRecoveryGui(Supplier<? extends Collection<Account>> accounts) {
        super(accounts, 18, 26);
    }

    @Override
    Menu.Builder<?> getPageTemplate() {
        return ChestMenu.builder(3).title("Account Recovery").redraw(true);
    }

    @Override
    void addItems(PaginatedMenuBuilder builder) {
        for (Account account : guiSubjects.get()) {
            ItemStack item = createSlotItem(account.getOwner(),
                    ChatColor.DARK_RED + "Invalid Account", getRecoveryLore(account));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> {
                player.sendMessage(LangUtils.getMessage(Message.CLICK_CHEST_RECOVER));
                ClickType.setPlayerClickType(player, ClickType.recover(account));
                close(player);
            };
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
    }

    private List<String> getRecoveryLore(Account account) {
        String worldName = account.getLocation().getWorld() == null ? "" : " in \"" + account.getLocation().getWorld().getName() + "\"";
        return wordWrapAll(40,
                "Account ID: " + ChatColor.DARK_GRAY + account.getID(),
                "Owner: " + account.getOwnerDisplayName(),
                "Location: " + ChatColor.AQUA + account.getCoordinates() + worldName,
                "Click to recover account."
        );
    }

    @Override
    Observable getSubject() {
        return plugin.getAccountUtils();
    }

    @Override
    GuiType getType() {
        return GuiType.ACCOUNT_RECOVERY;
    }
}
