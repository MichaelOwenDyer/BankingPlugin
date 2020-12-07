package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.gui.AccountRecoveryGui;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRecover extends AccountCommand.SubCommand {

    AccountRecover() {
        super("recover", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_RECOVER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_RECOVER;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to recover invalid accounts");

        if (accountUtils.getInvalidAccounts().isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.ACCOUNTS_NOT_FOUND));
            return true;
        }

        if (sender instanceof Player)
            new AccountRecoveryGui(accountUtils::getInvalidAccounts).open(((Player) sender));
        return true;
    }

    public static void recover(Player p, Block b, Account toMigrate) {
        AccountUtils accountUtils = plugin.getAccountUtils();
        BankUtils bankUtils = plugin.getBankUtils();
        Location newLocation = b.getLocation();
        if (accountUtils.isAccount(newLocation)) {
            plugin.debugf("%s clicked an already existing account chest to recover the account to", p.getName());
            p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            return;
        }
        if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }
        Bank newBank = bankUtils.getBank(newLocation); // May or may not be the same as previous bank
        if (newBank == null) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
            plugin.debug("Chest is not in a bank.");
            return;
        }

        Account newAccount = Account.clone(toMigrate);
        newAccount.setBank(newBank);
        newAccount.setLocation(newLocation);

        AccountRecoverEvent event = new AccountRecoverEvent(p, newAccount, newLocation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to recover an account to a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        if (newAccount.create(true)) {
            plugin.debugf("Account recovered (#%d)", newAccount.getID());
            accountUtils.removeInvalidAccount(toMigrate);
            accountUtils.addAccount(newAccount, true, newAccount.callUpdateName()); // Database entry is replaced
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_RECOVERED));
        } else {
            plugin.debug("Could not recover account");
            p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, "Could not recover account.")));
        }
    }
}
