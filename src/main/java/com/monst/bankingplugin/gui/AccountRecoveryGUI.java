package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class AccountRecoveryGUI extends MultiPageGUI<Account> {

    private final Function<Account, ClickAction.ChestAction> onClickFunction;

    public AccountRecoveryGUI(BankingPlugin plugin, Player player, Function<Account, ClickAction.ChestAction> onClickFunction) {
        super(plugin, player);
        this.onClickFunction = onClickFunction;
    }
    
    @Override
    Promise<Integer> countItems() {
        return Promise.fulfill(0);
    }
    
    @Override
    Promise<List<Account>> fetchItems(int offset, int limit) {
        return plugin.getAccountService().findAllMissing(offset, limit);
    }
    
    @Override
    String getTitle() {
        return "Account Recovery";
    }
    
    @Override
    ItemStack createItem(Account account) {
        return head(account.getOwner(), ChatColor.DARK_RED + "Invalid Account", getRecoveryLore(account));
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
    void click(Account account, ClickType click) {
        player.sendMessage(Message.CLICK_CHEST_RECOVER.translate(plugin));
        ClickAction.setBlockClickAction(player, onClickFunction.apply(account));
        exit();
    }
    
}
