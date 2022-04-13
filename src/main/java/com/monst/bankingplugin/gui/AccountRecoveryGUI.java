package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.command.ClickAction;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class AccountRecoveryGUI extends AccountListGUI {

    private final Function<Account, ClickAction.BlockAction> onClickFunction;

    public AccountRecoveryGUI(BankingPlugin plugin, Function<Account, ClickAction.BlockAction> onClickFunction) {
        super(plugin, callback -> plugin.getAccountService().findAllMissing(callback));
        this.onClickFunction = onClickFunction;
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
            player.sendMessage(Message.CLICK_CHEST_RECOVER.translate(plugin));
            ClickAction.setBlockClickAction(player, onClickFunction.apply(account));
            exit(player);
        };
        return SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build();
    }

    private List<String> getRecoveryLore(Account account) {
        Stream.Builder<String> lore = Stream.builder();
        lore.add("Account ID: " + ChatColor.DARK_GRAY + account.getID());
        lore.add("Owner: " + account.getOwner().getName());
        lore.add(account.getLocation().toString());
        lore.add("Click to recover account.");
        return wordWrapAll(40, lore.build());
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_RECOVERY;
    }

}
