package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.exceptions.BankNotFoundException;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.gui.AccountRecoveryGUI;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
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

        if (accountRepo.getNotFoundAccounts().isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.ACCOUNTS_NOT_FOUND));
            return true;
        }

        new AccountRecoveryGUI(accountRepo::getNotFoundAccounts).open(((Player) sender));
        return true;
    }

    public static void recover(Player p, Chest c, Account toRecover) {

        ChestLocation chestLocation = ChestLocation.from(c);
        if (accountRepo.isAccount(chestLocation)) {
            plugin.debugf("%s clicked an already existing account chest to recover the account to", p.getName());
            p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            return;
        }

        try {
            chestLocation.checkSpaceAbove();
        } catch (ChestBlockedException e) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank newBank;
        try {
            newBank = chestLocation.getBank();
        } catch (BankNotFoundException e) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
            plugin.debug("Chest is not in a bank.");
            return;
        }

        Account newAccount = Account.clone(toRecover);
        newAccount.setBank(newBank);
        newAccount.setChestLocation(chestLocation);

        AccountRecoverEvent event = new AccountRecoverEvent(p, newAccount, chestLocation);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to recover an account to a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        if (newAccount.create(true)) {
            plugin.debugf("Account recovered (#%d)", newAccount.getID());
            accountRepo.removeInvalidAccount(toRecover);
            accountRepo.update(newAccount, newAccount.callUpdateChestName(), AccountField.BANK, AccountField.LOCATION);
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_RECOVERED));
        } else {
            plugin.debug("Could not recover account");
            p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, "Could not recover account.")));
        }
    }
}
