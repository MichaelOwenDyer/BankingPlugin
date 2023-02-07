package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.event.bank.BankCreateEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.external.BankVisualization;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class BankCreate extends PlayerSubCommand {

    BankCreate(BankingPlugin plugin) {
		super(plugin, "create");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_CREATE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_CREATE;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException {
        BankRegion bankRegion = parseBankRegion(player, args);

        if (plugin.config().disabledWorlds.contains(bankRegion.getWorld()))
            throw err(Message.WORLD_DISABLED);

        boolean isAdminBank = args[args.length - 1].equalsIgnoreCase("admin");
        if (isAdminBank && Permissions.BANK_CREATE_ADMIN.notOwnedBy(player))
            throw err(Message.NO_PERMISSION_BANK_CREATE_ADMIN);

        if (!isAdminBank) {
            long bankLimit = PlayerSubCommand.getPermissionLimit(player, Permissions.BANK_NO_LIMIT, plugin.config().defaultBankLimit.get());
            if (bankLimit >= 0 && plugin.getBankService().countByOwner(player) >= bankLimit)
                throw err(Message.BANK_LIMIT_REACHED.with(Placeholder.LIMIT).as(bankLimit));

            long regionVolume = bankRegion.getVolume();
            long volumeLimit = PlayerSubCommand.getPermissionLimit(player, Permissions.BANK_NO_SIZE_LIMIT, plugin.config().maximumBankVolume.get());
            if (volumeLimit >= 0 && regionVolume > volumeLimit)
                throw err(Message.BANK_SELECTION_TOO_LARGE
                        .with(Placeholder.BANK_SIZE).as(regionVolume)
                        .and(Placeholder.MAXIMUM).as(volumeLimit)
                        .and(Placeholder.DIFFERENCE).as(regionVolume - volumeLimit));

            long minimumVolume = plugin.config().minimumBankVolume.get();
            if (regionVolume < minimumVolume)
                throw err(Message.BANK_SELECTION_TOO_SMALL
                        .with(Placeholder.BANK_SIZE).as(regionVolume)
                        .and(Placeholder.MINIMUM).as(minimumVolume)
                        .and(Placeholder.DIFFERENCE).as(minimumVolume - regionVolume));
        }

        Set<Bank> overlappingBanks = plugin.getBankService().findOverlapping(bankRegion);
        if (!overlappingBanks.isEmpty()) {
            if (plugin.isGriefPreventionIntegrated())
                new BankVisualization(plugin, overlappingBanks).show(player);
            throw err(Message.BANK_SELECTION_OVERLAPS_EXISTING
                    .with(Placeholder.NUMBER_OF_BANKS).as(overlappingBanks.size()));
        }

        String name = args[0];
        if (plugin.getBankService().findByName(name) != null)
            throw err(Message.NAME_NOT_UNIQUE.with(Placeholder.BANK_NAME).as(name));

        if (plugin.config().nameRegex.doesNotMatch(name))
            throw err(Message.NAME_NOT_ALLOWED
                    .with(Placeholder.NAME).as(name)
                    .and(Placeholder.PATTERN).as(plugin.config().nameRegex.get()));

        OfflinePlayer owner = isAdminBank ? null : player;
        Bank bank = new Bank(name, owner, bankRegion);

        try {
            new BankCreateEvent(player, bank).fire();
        } catch (EventCancelledException e) {
            if (Permissions.BYPASS_EXTERNAL_PLUGINS.notOwnedBy(player))
                throw err(Message.NO_PERMISSION_BANK_CREATE_PROTECTED);
        }

        if (!isAdminBank) {
            BigDecimal creationPrice = plugin.config().bankCreationPrice.get();
            final double finalCreationPrice = creationPrice.doubleValue();
            if (!plugin.getPaymentService().withdraw(player, finalCreationPrice)) {
                double balance = plugin.getEconomy().getBalance(player);
                throw err(Message.BANK_CREATE_INSUFFICIENT_FUNDS
                        .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
                        .and(Placeholder.PLAYER_BALANCE).as(plugin.getEconomy().format(balance))
                        .and(Placeholder.AMOUNT_REMAINING).as(plugin.getEconomy().format(
                                creationPrice.subtract(BigDecimal.valueOf(balance)).doubleValue())));
            }
            player.sendMessage(Message.BANK_CREATE_FEE_PAID
                    .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
                    .translate(plugin));
        }

        plugin.getBankService().save(bank);
        plugin.getSchedulerService().scheduleAll();
        if (plugin.isGriefPreventionIntegrated())
            new BankVisualization(plugin, bank).show(player);
        plugin.debugf("%s has created a new%s bank.", player.getName(), bank.isAdminBank() ? "admin " : "");
        player.sendMessage(Message.BANK_CREATED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();

        if (args.length == 0 || Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("admin")))
            return Collections.emptyList();

        if (args.length == 1 && args[0].isEmpty())
            return Collections.singletonList("<name>");

        if (args.length % 3 == 2 && Permissions.BANK_CREATE_ADMIN.ownedBy(player))
            returnCompletions.add("admin");

        if (args.length >= 8)
            return returnCompletions;

        Block lookingAt = player.getTargetBlock(null, 150);
        String coordinate = "";
        switch (args.length % 3) {
            case 2: coordinate += lookingAt.getX(); break;
            case 0: coordinate += lookingAt.getY(); break;
            case 1: coordinate += lookingAt.getZ(); break;
            default: throw new IllegalStateException();
        }
        if (coordinate.startsWith(args[args.length - 1]))
            returnCompletions.add(coordinate);
        return returnCompletions;
    }

}
