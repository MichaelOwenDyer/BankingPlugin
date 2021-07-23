package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.ClickType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AccountRecoveryGUI extends AccountListGUI {

    public AccountRecoveryGUI(Supplier<Collection<Account>> source) {
        super(source);
    }

    @Override
    String getTitle() {
        return "Account Recovery";
    }

    @Override
    SlotSettings createSlotSettings(Account account) {
        ItemStack item = createSlotItem(account.getOwner(),
                ChatColor.DARK_RED + "Invalid Account", getRecoveryLore(account));
        ItemStackTemplate template = new StaticItemTemplate(item);
        Slot.ClickHandler clickHandler = (player, info) -> {
            player.sendMessage(Messages.get(Message.CLICK_CHEST_RECOVER));
            ClickType.setRecoverClickType(player, account);
            exit(player);
        };
        return SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build();
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
    GUIType getType() {
        return GUIType.ACCOUNT_RECOVERY;
    }

}
