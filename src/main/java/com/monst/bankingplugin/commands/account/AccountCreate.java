package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateCommandEvent;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.notfound.BankNotFoundException;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.math.BigDecimal;

public class AccountCreate extends SubCommand {

    AccountCreate(BankingPlugin plugin) {
		super(plugin, "create", true);
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CREATE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        plugin.debugf("%s wants to create an account", p.getName());

        if (Permission.ACCOUNT_CREATE.notOwnedBy(sender)) {
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_CREATE.translate());
            plugin.debugf("%s is not permitted to create an account", p.getName());
            return true;
        }

        int limit = Utils.getAccountLimit(p);
        if (limit != -1 && plugin.getAccountRepository().getOwnedBy(p).size() >= limit) {
            p.sendMessage(Message.ACCOUNT_LIMIT_REACHED.with(Placeholder.LIMIT).as(limit).translate());
            plugin.debugf("%s has reached their account limit", p.getName());
            return true;
        }

        AccountCreateCommandEvent event = new AccountCreateCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account create command event cancelled");
            return true;
        }

        plugin.debugf("%s can now click a chest to create an account", p.getName());
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

        InventoryHolder ih = ((Chest) b.getState()).getInventory().getHolder();
        AccountLocation accountLocation = AccountLocation.from(ih);

        Bank bank;
        try {
            accountLocation.checkSpaceAbove();
            bank = accountLocation.findBank(plugin.getBankRepository());
        } catch (ChestBlockedException | BankNotFoundException e) {
            p.sendMessage(e.getMessage());
            plugin.debug(e);
            return;
        }

        if (!Config.allowSelfBanking.get() && bank.isOwner(p)) {
            p.sendMessage(Message.NO_SELF_BANKING.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
            plugin.debugf("%s is not permitted to create an account at their own bank", p.getName());
            return;
        }

        int playerAccountLimit = bank.playerBankAccountLimit().get();
        if (playerAccountLimit > 0 && bank.getAccounts(account -> account.isOwner(p)).size() >= playerAccountLimit) {
            p.sendMessage(Message.ACCOUNT_LIMIT_AT_BANK_REACHED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.LIMIT).as(playerAccountLimit)
                    .translate());
            plugin.debugf("%s is not permitted to create another account at bank %s", p.getName(), bank.getName());
            return;
        }

        Account account = bank.openAccount(p, accountLocation);

        AccountCreateEvent event = new AccountCreateEvent(p, account);
        event.fire();
        if (event.isCancelled() && Permission.ACCOUNT_CREATE_PROTECTED.notOwnedBy(p)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED.translate());
            return;
        }

        BigDecimal creationPrice;
        if (bank.isOwner(p))
            creationPrice = BigDecimal.ZERO;
        else
            creationPrice = bank.accountCreationPrice().get().multiply(BigDecimal.valueOf(accountLocation.getSize()));

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

        plugin.debug("Account created");
        p.sendMessage(Message.ACCOUNT_CREATED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
        plugin.getAccountRepository().add(account, true, account.callUpdateChestName());
    }
}
