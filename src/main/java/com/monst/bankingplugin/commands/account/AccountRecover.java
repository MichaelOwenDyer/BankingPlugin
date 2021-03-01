package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountRecoverEvent;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.gui.AccountRecoveryGUI;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
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

        if (accountRepo.getInvalidAccounts().isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.ACCOUNTS_NOT_FOUND));
            return true;
        }

        new AccountRecoveryGUI(accountRepo::getInvalidAccounts).open(((Player) sender));
        return true;
    }

    public static void recover(Player p, Block b, Account toMigrate) {
        Location newLocation = b.getLocation();
        if (accountRepo.isAccount(newLocation)) {
            plugin.debugf("%s clicked an already existing account chest to recover the account to", p.getName());
            p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            return;
        }
        if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }
        Bank newBank = plugin.getBankRepository().getAt(newLocation); // May or may not be the same as previous bank
        if (newBank == null) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
            plugin.debug("Chest is not in a bank.");
            return;
        }

        Account newAccount = Account.clone(toMigrate);
        newAccount.setBank(newBank);
        newAccount.setChestLocation(ChestLocation.from(b));

        AccountRecoverEvent event = new AccountRecoverEvent(p, newAccount, newLocation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to recover an account to a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        if (newAccount.create(true)) {
            plugin.debugf("Account recovered (#%d)", newAccount.getID());
            accountRepo.removeInvalidAccount(toMigrate);
            accountRepo.add(newAccount, true, newAccount.callUpdateName()); // Database entry is replaced
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_RECOVERED));
        } else {
            plugin.debug("Could not recover account");
            p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, "Could not recover account.")));
        }
    }
}
