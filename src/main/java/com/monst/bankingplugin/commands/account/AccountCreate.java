package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateCommandEvent;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.math.BigDecimal;

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
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_CREATE.translate());
            plugin.debug(p.getName() + " is not permitted to create an account");
            return true;
        }

        int limit = Utils.getAccountLimit(p);
        if (limit != -1 && plugin.getAccountRepository().getOwnedBy(p).size() >= limit) {
            p.sendMessage(Message.ACCOUNT_LIMIT_REACHED.with(Placeholder.LIMIT).as(limit).translate());
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
        p.sendMessage(Message.CLICK_CHEST_CREATE.translate());
        ClickType.setCreateClickType(p);
        return true;
    }

    /**
     * Creates a new account at the specified block.
     *
     * @param p  Player who executed the command
     * @param b  Clicked chest block to create the account at
     */
    public static void create(BankingPlugin plugin, Player p, Block b) {
        if (plugin.getAccountRepository().isAccount(b)) {
            p.sendMessage(Message.CHEST_ALREADY_ACCOUNT.translate());
            plugin.debug("Chest is already an account.");
            return;
        }

        Chest c = (Chest) b.getState();
        InventoryHolder ih = c.getInventory().getHolder();
        AccountLocation accountLocation = AccountLocation.from(ih);

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

        if (!Config.allowSelfBanking.get() && bank.isOwner(p)) {
            p.sendMessage(Message.NO_SELF_BANKING.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
            plugin.debug(p.getName() + " is not permitted to create an account at their own bank");
            return;
        }
        int playerAccountLimit = bank.getPlayerBankAccountLimit().get();
        if (playerAccountLimit > 0 && bank.getAccounts(account -> account.isOwner(p)).size() >= playerAccountLimit) {
            p.sendMessage(Message.ACCOUNT_LIMIT_AT_BANK_REACHED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.LIMIT).as(playerAccountLimit)
                    .translate());
            plugin.debug(p.getName() + " is not permitted to create another account at bank " + bank.getName());
            return;
        }

        Account account = bank.openAccount(p, accountLocation);

        AccountCreateEvent event = new AccountCreateEvent(p, account);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED.translate());
            return;
        }

        BigDecimal creationPrice;
        if (bank.isOwner(p))
            creationPrice = BigDecimal.ZERO;
        else
            creationPrice = bank.getAccountCreationPrice().get().multiply(BigDecimal.valueOf(accountLocation.getSize()));

        if (creationPrice.signum() > 0) {
            BigDecimal balance = BigDecimal.valueOf(plugin.getEconomy().getBalance(p));
            if (creationPrice.compareTo(balance) > 0) {
                p.sendMessage(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS
                        .with(Placeholder.PRICE).as(creationPrice)
                        .and(Placeholder.PLAYER_BALANCE).as(balance)
                        .and(Placeholder.AMOUNT_REMAINING).as(creationPrice.subtract(balance))
                        .translate());
                return;
            }
            // Account owner pays the bank owner the creation fee
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(Message.ACCOUNT_CREATE_FEE_PAID
                        .with(Placeholder.PRICE).as(creationPrice)
                        .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                        .translate());
            else
                return;
            // Bank owner receives the payment from the customer
            if (bank.isPlayerBank()) {
                OfflinePlayer bankOwner = account.getBank().getOwner();
                if (PayrollOffice.deposit(bankOwner, creationPrice))
                    Utils.notify(bankOwner, Message.ACCOUNT_CREATE_FEE_RECEIVED
                            .with(Placeholder.PLAYER).as(p.getName())
                            .and(Placeholder.AMOUNT).as(creationPrice)
                            .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                            .translate());
            }
        }

        if (account.create()) {
            plugin.debug("Account created");
            plugin.getAccountRepository().add(account, true, account.callUpdateChestName());
            p.sendMessage(Message.ACCOUNT_CREATED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
        } else {
            plugin.debugf("Could not create account");
            p.sendMessage(Message.ERROR_OCCURRED.with(Placeholder.ERROR).as("Could not create account").translate());
        }
    }
}
