package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountPreCreateEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountCreate extends AccountSubCommand {

    public AccountCreate() {
        super("create", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_CREATE : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create an account");

        boolean forSelf = args.length == 1;
        boolean hasPermission = hasPermission(p, forSelf ? Permissions.ACCOUNT_CREATE : Permissions.ACCOUNT_CREATE_OTHER);

        if (!hasPermission) {
            if (!forSelf) {
                p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_OTHER);
                plugin.debug(p.getName() + " is not permitted to create an account in another player's name");
                return true;
            }
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE);
            plugin.debug(p.getName() + " is not permitted to create an account");
            return true;
        }

        OfflinePlayer owner = forSelf ? p.getPlayer() : Utils.getPlayer(args[1]);
        if (!forSelf && owner == null) {
            p.sendMessage(Messages.PLAYER_NOT_FOUND);
            plugin.debug("Could not find player with name \"" + args[1] + "\"");
            return true;
        }

        if (forSelf) {
            int limit = accountUtils.getAccountLimit(p);
            if (limit != -1 && accountUtils.getNumberOfAccounts(p) >= limit) {
                p.sendMessage(Messages.ACCOUNT_LIMIT_REACHED);
                plugin.debug(p.getName() + " has reached their account limit");
                return true;
            }
        }

        AccountPreCreateEvent event = new AccountPreCreateEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-create event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to create an account");
        p.sendMessage(Messages.CLICK_CHEST_CREATE);
        ClickType.setPlayerClickType(p, ClickType.create(owner));
        return true;
    }

    /**
     * Create a new account
     *
     * @param executor  Player who executed the command will receive the message
     *                          and become the owner of the account
     * @param b         Block where the account will be located
     */
    public static void create(Player executor, OfflinePlayer owner, Block b) {
        AccountUtils accountUtils = plugin.getAccountUtils();
        BankUtils bankUtils = plugin.getBankUtils();
        Location location = b.getLocation();

        if (accountUtils.isAccount(location)) {
            executor.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
            plugin.debug("Chest is already an account.");
            return;
        }
        if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            executor.sendMessage(Messages.CHEST_BLOCKED);
            plugin.debug("Chest is blocked.");
            return;
        }
        if (!bankUtils.isBank(location)) {
            executor.sendMessage(Messages.CHEST_NOT_IN_BANK);
            plugin.debug("Chest is not in a bank.");
            plugin.debug(executor.getName() + " is creating new account...");
            return;
        }

        boolean forSelf = Utils.samePlayer(executor, owner);

        Bank bank = bankUtils.getBank(location);
        if (!Config.allowSelfBanking && forSelf && bank.isOwner(executor)) {
            executor.sendMessage(Messages.NO_SELF_BANKING);
            plugin.debug(executor.getName() + " is not permitted to create an account at their own bank");
            return;
        }
        if (!forSelf) {
            int playerAccountLimit = bank.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT);
            if (playerAccountLimit > 0 && bank.getAccountsCopy(account -> account.isOwner(executor)).size() >= playerAccountLimit) {
                executor.sendMessage(Messages.PER_BANK_ACCOUNT_LIMIT_REACHED);
                plugin.debug(executor.getName() + " is not permitted to create another account at bank " + bank.getName());
                return;
            }
        }

        Account account = Account.mint(owner, location);

        AccountCreateEvent event = new AccountCreateEvent(executor, owner, account);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !executor.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
            return;
        }

        double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
        creationPrice *= ((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1;

        if (creationPrice > 0 && creationPrice > plugin.getEconomy().getBalance(executor)
                && forSelf && !bank.isOwner(executor)) {
            executor.sendMessage(Messages.ACCOUNT_CREATE_INSUFFICIENT_FUNDS);
            return;
        }

        OfflinePlayer accountOwner = executor.getPlayer();
        double finalCreationPrice = creationPrice;
        // Account owner pays the bank owner the creation fee
        if (!Utils.withdrawPlayer(accountOwner, location.getWorld().getName(), creationPrice,
                Callback.of(plugin,
                        result -> executor.sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_PAID, Utils.format(finalCreationPrice))),
                        throwable -> executor.sendMessage(Messages.ERROR_OCCURRED))))
            return;

        // Bank owner receives the payment from the customer
        if (creationPrice > 0 && bank.isPlayerBank() && !bank.isOwner(executor)) {
            OfflinePlayer bankOwner = account.getBank().getOwner();
            Utils.depositPlayer(bankOwner, location.getWorld().getName(), creationPrice, Callback.of(plugin,
                    result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_CREATE_FEE_RECEIVED,
                            accountOwner.getName(), Utils.format(finalCreationPrice)), bankOwner),
                    throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
        }

        if (account.create(true)) {
            plugin.debug("Account created");
            accountUtils.addAccount(account, true, account.callUpdateName());
            executor.sendMessage(Messages.ACCOUNT_CREATED);
        } else {
            plugin.debugf("Could not create account");
            executor.sendMessage(Messages.ERROR_OCCURRED);
        }
    }
}
