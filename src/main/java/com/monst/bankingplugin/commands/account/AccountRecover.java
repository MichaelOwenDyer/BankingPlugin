package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.gui.AccountRecoveryGUI;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.block.Block;
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
        PLUGIN.debug(sender.getName() + " wants to recover invalid accounts");

        if (accountRepo.getNotFoundAccounts().isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.ACCOUNTS_NOT_FOUND));
            return true;
        }

        new AccountRecoveryGUI(accountRepo::getNotFoundAccounts).open(((Player) sender));
        return true;
    }

    public static void recover(Player p, Account toRecover, Block b) {
        if (accountRepo.isAccount(b)) {
            PLUGIN.debugf("%s clicked an already existing account chest to recover the account to", p.getName());
            p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            return;
        }

        Chest c = (Chest) b.getState();
        AccountLocation accountLocation = AccountLocation.from(c.getInventory().getHolder());

        if (accountLocation.isBlocked()) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            PLUGIN.debug("Chest is blocked.");
            return;
        }

        Bank bank = accountLocation.getBank();
        if (bank == null) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
            PLUGIN.debug("Chest is not in a bank.");
            return;
        }

        AccountRecoverEvent event = new AccountRecoverEvent(p, toRecover, accountLocation);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            PLUGIN.debug("No permission to recover an account to a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        PLUGIN.debugf("Account recovered (#%d)", toRecover.getID());
        toRecover.setLocation(accountLocation);
        toRecover.setBank(bank);
        accountRepo.removeInvalidAccount(toRecover);
        accountRepo.update(toRecover, toRecover.callUpdateChestName(), AccountField.BANK, AccountField.LOCATION);
        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_RECOVERED));
    }
}
