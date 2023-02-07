package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.event.bank.BankResizeEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.external.BankVisualization;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankResize extends PlayerSubCommand {

    BankResize(BankingPlugin plugin) {
		super(plugin, "resize");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_RESIZE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_RESIZE;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException, EventCancelledException {
        BankRegion bankRegion = parseBankRegion(player, args);

        Bank bank;
        if (args.length == 0) {
            bank = plugin.getBankService().findContaining(player);
            if (bank == null)
                throw err(Message.MUST_STAND_IN_OR_SPECIFY_BANK);
        } else {
            bank = plugin.getBankService().findByName(args[0]);
            if (bank == null)
                throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));
        }

        if (bank.isPlayerBank() && !bank.isOwner(player) && Permissions.BANK_RESIZE_OTHER.notOwnedBy(player))
            throw err(Message.NO_PERMISSION_BANK_RESIZE_OTHER);

        if (bank.isAdminBank() && Permissions.BANK_RESIZE_ADMIN.notOwnedBy(player))
            throw err(Message.NO_PERMISSION_BANK_RESIZE_ADMIN);

        if (plugin.config().disabledWorlds.contains(bankRegion.getWorld()))
            throw err(Message.WORLD_DISABLED);

        long volume = bankRegion.getVolume();
        if (bank.isPlayerBank()) {
            long volumeLimit = PlayerSubCommand.getPermissionLimit(player, Permissions.BANK_NO_SIZE_LIMIT, plugin.config().maximumBankVolume.get());
            if (volumeLimit >= 0 && volume > volumeLimit)
                throw err(Message.BANK_SELECTION_TOO_LARGE
                        .with(Placeholder.BANK_SIZE).as(volume)
                        .and(Placeholder.MAXIMUM).as(volumeLimit)
                        .and(Placeholder.DIFFERENCE).as(volume - volumeLimit));

            long minimumVolume = plugin.config().minimumBankVolume.get();
            if (volume < minimumVolume)
                throw err(Message.BANK_SELECTION_TOO_SMALL
                        .with(Placeholder.BANK_SIZE).as(volume)
                        .and(Placeholder.MINIMUM).as(minimumVolume)
                        .and(Placeholder.DIFFERENCE).as(minimumVolume - volume));
        }

        Set<Bank> overlappingBanks = plugin.getBankService().findOverlapping(bankRegion);
        overlappingBanks.remove(bank);
        if (!overlappingBanks.isEmpty()) {
            if (plugin.isGriefPreventionIntegrated())
                new BankVisualization(plugin, overlappingBanks).show(player);
            throw err(Message.BANK_SELECTION_OVERLAPS_EXISTING
                    .with(Placeholder.NUMBER_OF_BANKS).as(overlappingBanks.size()));
        }

        int cutAccounts = 0;
        for (Account account : bank.getAccounts())
            if (account.getLocation().findChest().isPresent() && !bankRegion.contains(account.getLocation()))
                cutAccounts++;
        if (cutAccounts > 0)
            throw err(Message.BANK_SELECTION_CUTS_ACCOUNTS.with(Placeholder.NUMBER_OF_ACCOUNTS).as(cutAccounts));

        new BankResizeEvent(player, bank, bankRegion).fire();

        bank.setRegion(bankRegion);
        plugin.getBankService().update(bank);
        if (plugin.isGriefPreventionIntegrated())
            new BankVisualization(plugin, bank).show(player);
        plugin.debugf("%s has resized bank #%d", player.getName(), bank.getID());
        player.sendMessage(Message.BANK_RESIZED.with(Placeholder.BANK_SIZE).as(volume).translate(plugin));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService()
                    .findNamesByPlayerAllowedToModify(player,
                            Permissions.BANK_RESIZE_OTHER.ownedBy(player),
                            Permissions.BANK_RESIZE_ADMIN.ownedBy(player), true)
                    .stream()
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        Block lookingAt = player.getTargetBlock(null, 300);
        String coordinate = "";
        switch (args.length % 3) {
            case 2: coordinate += lookingAt.getX(); break;
            case 0: coordinate += lookingAt.getY(); break;
            case 1: coordinate += lookingAt.getZ(); break;
            default: throw new IllegalStateException();
        }
        if (coordinate.startsWith(args[args.length - 1]))
            return Collections.singletonList(coordinate);
        return Collections.emptyList();
    }

}
