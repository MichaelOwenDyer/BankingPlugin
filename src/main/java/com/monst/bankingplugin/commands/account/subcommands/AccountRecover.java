package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.gui.AccountRecoveryGui;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class AccountRecover extends AccountSubCommand {

    public AccountRecover() {
        super("recover", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to recover invalid accounts");

        Set<Account> invalidAccounts = accountUtils.getInvalidAccounts();

        if (invalidAccounts.isEmpty()) {
            sender.sendMessage(String.format(Messages.NONE_FOUND, "accounts", "recover"));
            return true;
        }

        if (sender instanceof Player)
            new AccountRecoveryGui(invalidAccounts).open(((Player) sender));
        return true;
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_RECOVER) && !accountUtils.getInvalidAccounts().isEmpty()
                ? Messages.COMMAND_USAGE_ACCOUNT_RECOVER : "";
    }

    public static void recover(Player p, Block b, Account toMigrate) {
        AccountUtils accountUtils = plugin.getAccountUtils();
        BankUtils bankUtils = plugin.getBankUtils();
        Location newLocation = b.getLocation();
        if (accountUtils.isAccount(newLocation)) {
            if (toMigrate.equals(accountUtils.getAccount(newLocation))) {
                plugin.debugf("%s clicked the same chest to migrate to.", p.getName());
                p.sendMessage(Messages.SAME_ACCOUNT);
                return;
            }
            plugin.debugf("%s clicked an already existing account chest to migrate to", p.getName());
            p.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
            return;
        }

        if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            p.sendMessage(Messages.CHEST_BLOCKED);
            plugin.debug("Chest is blocked.");
            return;
        }
        Bank newBank = bankUtils.getBank(newLocation); // May or may not be the same as previous bank
        if (newBank == null) {
            p.sendMessage(Messages.CHEST_NOT_IN_BANK);
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
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
            return;
        }

        if (newAccount.create(true)) {
            plugin.debugf("Account recovered (#%d)", newAccount.getID());
            accountUtils.removeInvalidAccount(toMigrate);
            accountUtils.addAccount(newAccount, true); // Database entry is replaced
            p.sendMessage(Messages.ACCOUNT_RECOVERED);
        } else {
            plugin.debug("Could not recover account");
            p.sendMessage(Messages.ERROR_OCCURRED);
        }
    }
}
