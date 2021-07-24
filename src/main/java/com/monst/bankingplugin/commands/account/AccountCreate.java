package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateCommandEvent;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class AccountCreate extends SubCommand.AccountSubCommand {

    AccountCreate(BankingPlugin plugin) {
		super(plugin, "create", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CREATE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create an account");

        if (!hasPermission(p, Permissions.ACCOUNT_CREATE)) {
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_CREATE));
            plugin.debug(p.getName() + " is not permitted to create an account");
            return true;
        }

        int limit = Utils.getAccountLimit(p);
        if (limit != -1 && plugin.getAccountRepository().getOwnedBy(p).size() >= limit) {
            p.sendMessage(Messages.get(Message.ACCOUNT_LIMIT_REACHED, new Replacement(Placeholder.LIMIT, limit)));
            plugin.debug(p.getName() + " has reached their account limit");
            return true;
        }

        AccountCreateCommandEvent event = new AccountCreateCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account create command event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to create an account");
        p.sendMessage(Messages.get(Message.CLICK_CHEST_CREATE));
        ClickType.setCreateClickType(p);
        return true;
    }

    /**
     * Creates a new account at the specified block.
     *
     * @param p  Player who executed the command will receive the message
     *                          and become the owner of the account
     * @param b  Clicked chest block to create the account at
     */
    public static void create(Player p, Block b) {
        ClickType.removeClickType(p);

        if (plugin.getAccountRepository().isAccount(b)) {
            p.sendMessage(Messages.get(Message.CHEST_ALREADY_ACCOUNT));
            plugin.debug("Chest is already an account.");
            return;
        }

        Chest c = (Chest) b.getState();
        InventoryHolder ih = c.getInventory().getHolder();
        AccountLocation accountLocation = AccountLocation.from(ih);

        if (accountLocation.isBlocked()) {
            p.sendMessage(Messages.get(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank bank = accountLocation.getBank();
        if (bank == null) {
            p.sendMessage(Messages.get(Message.CHEST_NOT_IN_BANK));
            plugin.debug("Chest is not in a bank.");
            return;
        }

        if (!Config.allowSelfBanking.get() && bank.isOwner(p)) {
            p.sendMessage(Messages.get(Message.NO_SELF_BANKING, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
            plugin.debug(p.getName() + " is not permitted to create an account at their own bank");
            return;
        }
        int playerAccountLimit = bank.getPlayerBankAccountLimit().get();
        if (playerAccountLimit > 0 && bank.getAccounts(account -> account.isOwner(p)).size() >= playerAccountLimit) {
            p.sendMessage(Messages.get(Message.ACCOUNT_LIMIT_AT_BANK_REACHED,
                    new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                    new Replacement(Placeholder.LIMIT, playerAccountLimit)
            ));
            plugin.debug(p.getName() + " is not permitted to create another account at bank " + bank.getName());
            return;
        }

        Account account = bank.openAccount(p, accountLocation);

        AccountCreateEvent event = new AccountCreateEvent(p, account);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        double creationPrice = bank.getAccountCreationPrice().get();
        creationPrice *= accountLocation.getSize();
        creationPrice *= bank.isOwner(p) ? 0 : 1;

        if (creationPrice > 0) {
            double balance = plugin.getEconomy().getBalance(p);
            if (creationPrice > balance) {
                p.sendMessage(Messages.get(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS,
                        new Replacement(Placeholder.PRICE, creationPrice),
                        new Replacement(Placeholder.PLAYER_BALANCE, balance),
                        new Replacement(Placeholder.AMOUNT_REMAINING, creationPrice - balance)
                ));
                return;
            }
            // Account owner pays the bank owner the creation fee
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(Messages.get(Message.ACCOUNT_CREATE_FEE_PAID,
                        new Replacement(Placeholder.PRICE, creationPrice),
                        new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
                ));
            else
                return;
            // Bank owner receives the payment from the customer
            if (bank.isPlayerBank()) {
                OfflinePlayer bankOwner = account.getBank().getOwner();
                if (PayrollOffice.deposit(bankOwner, creationPrice))
                    Mailman.notify(bankOwner, Messages.get(Message.ACCOUNT_CREATE_FEE_RECEIVED,
                            new Replacement(Placeholder.PLAYER, p::getName),
                            new Replacement(Placeholder.AMOUNT, creationPrice),
                            new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
                    ));
            }
        }

        if (account.create()) {
            plugin.debug("Account created");
            plugin.getAccountRepository().add(account, true, account.callUpdateChestName());
            p.sendMessage(Messages.get(Message.ACCOUNT_CREATED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
        } else {
            plugin.debugf("Could not create account");
            p.sendMessage(Messages.get(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, "Could not create account")));
        }
    }
}
