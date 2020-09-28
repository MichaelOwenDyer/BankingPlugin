package com.monst.bankingplugin.commands.account;

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
import org.bukkit.inventory.InventoryHolder;

public class AccountCreate extends AccountCommand.SubCommand {

    AccountCreate() {
        super("create", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_CREATE : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create an account");

        boolean hasPermission = hasPermission(p, Permissions.ACCOUNT_CREATE);

        if (!hasPermission) {
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE);
            plugin.debug(p.getName() + " is not permitted to create an account");
            return true;
        }

        int limit = accountUtils.getAccountLimit(p);
        if (limit != -1 && accountUtils.getNumberOfAccounts(p) >= limit) {
            p.sendMessage(Messages.ACCOUNT_LIMIT_REACHED);
            plugin.debug(p.getName() + " has reached their account limit");
            return true;
        }

        AccountPreCreateEvent event = new AccountPreCreateEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-create event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to create an account");
        p.sendMessage(String.format(Messages.CLICK_CHEST, "create an account"));
        ClickType.setPlayerClickType(p, ClickType.create());
        return true;
    }

    /**
     * Create a new account
     *
     * @param p  Player who executed the command will receive the message
     *                          and become the owner of the account
     * @param b         Block where the account will be located
     */
    public static void create(Player p, Block b) {
        AccountUtils accountUtils = plugin.getAccountUtils();
        BankUtils bankUtils = plugin.getBankUtils();
        Location location = b.getLocation();

        if (accountUtils.isAccount(location)) {
            p.sendMessage(Messages.CHEST_ALREADY_ACCOUNT);
            plugin.debug("Chest is already an account.");
            return;
        }
        if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            p.sendMessage(Messages.CHEST_BLOCKED);
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank bank;
        InventoryHolder holder = ((Chest) b.getState()).getInventory().getHolder();
        if (holder instanceof DoubleChest) {
            DoubleChest dc = ((DoubleChest) holder);
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            bank = left == null ? null : bankUtils.getBank(left.getLocation());
            Bank otherBank = right == null ? null : bankUtils.getBank(right.getLocation());
            if (bank == null || !bank.equals(otherBank)) {
                p.sendMessage(Messages.CHEST_NOT_IN_BANK);
                plugin.debug("Chest is not in a bank.");
                return;
            }
        } else {
            bank = bankUtils.getBank(location);
            if (bank == null) {
                p.sendMessage(Messages.CHEST_NOT_IN_BANK);
                plugin.debug("Chest is not in a bank.");
                return;
            }
        }

        if (!Config.allowSelfBanking && bank.isOwner(p)) {
            p.sendMessage(Messages.NO_SELF_BANKING);
            plugin.debug(p.getName() + " is not permitted to create an account at their own bank");
            return;
        }
        int playerAccountLimit = bank.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT);
        if (playerAccountLimit > 0 && bank.getAccounts(account -> account.isOwner(p)).size() >= playerAccountLimit) {
            p.sendMessage(Messages.PER_BANK_ACCOUNT_LIMIT_REACHED);
            plugin.debug(p.getName() + " is not permitted to create another account at bank " + bank.getName());
            return;
        }

        Account account = Account.mint(p, location);

        AccountCreateEvent event = new AccountCreateEvent(p, account);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED);
            return;
        }

        double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
        creationPrice *= ((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1;
        creationPrice *= bank.isOwner(p) ? 0 : 1;

        if (creationPrice > 0 && creationPrice > plugin.getEconomy().getBalance(p)) {
            p.sendMessage(Messages.ACCOUNT_CREATE_INSUFFICIENT_FUNDS);
            return;
        }

        OfflinePlayer accountOwner = p.getPlayer();
        double finalCreationPrice = creationPrice;
        if (creationPrice > 0)
        // Account owner pays the bank owner the creation fee
        if (!Utils.withdrawPlayer(accountOwner, location.getWorld().getName(), creationPrice,
                Callback.of(plugin,
                        result -> p.sendMessage(String.format(Messages.ACCOUNT_CREATE_FEE_PAID, Utils.format(finalCreationPrice))),
                        error -> p.sendMessage(Messages.ERROR_OCCURRED))))
            return;

        // Bank owner receives the payment from the customer
        if (creationPrice > 0 && bank.isPlayerBank()) {
            OfflinePlayer bankOwner = account.getBank().getOwner();
            Utils.depositPlayer(bankOwner, location.getWorld().getName(), creationPrice, Callback.of(plugin,
                    result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_CREATE_FEE_RECEIVED,
                            accountOwner.getName(), Utils.format(finalCreationPrice)), bankOwner),
                    error -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
        }

        if (account.create(true)) {
            plugin.debug("Account created");
            accountUtils.addAccount(account, true, account.callUpdateName());
            p.sendMessage(Messages.ACCOUNT_CREATED);
        } else {
            plugin.debugf("Could not create account");
            p.sendMessage(Messages.ERROR_OCCURRED);
        }
    }
}
