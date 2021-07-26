package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.gui.AccountRecoveryGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRecover extends SubCommand.AccountSubCommand {

    AccountRecover(BankingPlugin plugin) {
        super(plugin, "recover", true);
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
        new AccountRecoveryGUI(plugin.getAccountRepository()::getMissingAccounts).open((Player) sender);
        return true;
    }

    public static void recover(BankingPlugin plugin, Player p, Account toRecover, Block b) {
        if (plugin.getAccountRepository().isAccount(b)) {
            plugin.debugf("%s clicked an already existing account chest to recover the account to", p.getName());
            p.sendMessage(Message.CHEST_ALREADY_ACCOUNT.translate());
            return;
        }

        Chest c = (Chest) b.getState();
        AccountLocation accountLocation = AccountLocation.from(c.getInventory().getHolder());

        if (accountLocation.isBlocked()) {
            p.sendMessage(Message.CHEST_BLOCKED.translate());
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank bank = accountLocation.getBank();
        if (bank == null) {
            p.sendMessage(Message.CHEST_NOT_IN_BANK.translate());
            plugin.debug("Chest is not in a bank.");
            return;
        }

        AccountRecoverEvent event = new AccountRecoverEvent(p, toRecover, accountLocation);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to recover an account to a protected chest.");
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED.translate());
            return;
        }

        plugin.debugf("Account recovered (#%d)", toRecover.getID());
        toRecover.setLocation(accountLocation);
        toRecover.setBank(bank);
        plugin.getAccountRepository().removeMissingAccount(toRecover);
        plugin.getAccountRepository().update(toRecover, toRecover.callUpdateChestName(), AccountField.BANK, AccountField.LOCATION);
        p.sendMessage(Message.ACCOUNT_RECOVERED.translate());
    }
}
