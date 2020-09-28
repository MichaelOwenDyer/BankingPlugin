package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankResize extends BankCommand.SubCommand {

    BankResize() {
        super("resize", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_RESIZE : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to resize a bank");

        if (!p.hasPermission(Permissions.BANK_CREATE)) {
            plugin.debug(p.getName() + " does not have permission to resize a bank");
            p.sendMessage(Messages.NO_PERMISSION_BANK_RESIZE);
            return true;
        }

        Bank bank;
        Selection selection;

        if (args.length == 1)
            return false;

        else if (args.length == 2) {
            if (Config.enableWorldEditIntegration && plugin.hasWorldEdit()) {
                selection = WorldEditReader.getSelection(plugin, p);
                if (selection == null) {
                    plugin.debug(p.getName() + " tried to resize a bank with no WorldEdit selection");
                    p.sendMessage(Messages.SELECT_WORLDEDIT_REGION);
                    return true;
                }
            } else {
                plugin.debug("WorldEdit is not enabled");
                p.sendMessage(Messages.WORLDEDIT_NOT_ENABLED);
                return true;
            }
        } else {
            try {
                selection = bankUtils.parseCoordinates(args, p.getLocation(), 1);
            } catch (NumberFormatException e) {
                plugin.debug("Could not parse coordinates in command args: \"" + Arrays.toString(args) + "\"");
                p.sendMessage(Messages.COORDINATES_PARSE_ERROR);
                return false;
            }
        }

        if (selection == null)
            return false;

        bank = bankUtils.lookupBank(args[1]);
        if (bank == null) {
            plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
            p.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
            return true;
        }
        if (bank.isPlayerBank() && !bank.isOwner(p) && !p.hasPermission(Permissions.BANK_RESIZE_OTHER)) {
            plugin.debug(p.getName() + " does not have permission to resize another player's bank");
            p.sendMessage(Messages.NO_PERMISSION_BANK_RESIZE_OTHER);
            return true;
        }
        if (bank.isAdminBank() && !p.hasPermission(Permissions.BANK_RESIZE_ADMIN)) {
            plugin.debug(p.getName() + " does not have permission to resize an admin bank");
            p.sendMessage(Messages.NO_PERMISSION_BANK_RESIZE_ADMIN);
            return true;
        }
        if (Config.disabledWorlds.contains(selection.getWorld().getName())) {
            plugin.debug("BankingPlugin is disabled in world " + selection.getWorld().getName());
            p.sendMessage(Messages.WORLD_DISABLED);
            return true;
        }
        long volume = selection.getVolume();
        long volumeLimit = BankUtils.getVolumeLimit(p);
        if (bank.isPlayerBank() && volumeLimit >= 0 && volume > volumeLimit) {
            plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
            p.sendMessage(String.format(Messages.SELECTION_TOO_LARGE_RESIZE, volumeLimit, volume - volumeLimit));
            return true;
        }
        if (bank.isPlayerBank() && volume < Config.minimumBankVolume) {
            plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume + ")");
            p.sendMessage(String.format(Messages.SELECTION_TOO_SMALL_RESIZE, Config.minimumBankVolume, Config.minimumBankVolume - volume));
            return true;
        }
        Set<Selection> overlappingSelections = bankUtils.getOverlappingSelectionsIgnoring(selection, bank.getSelection());
        if (!overlappingSelections.isEmpty()) {
            plugin.debug("New selection is overlaps with an existing bank selection");
            p.sendMessage(Messages.SELECTION_OVERLAPS_EXISTING);
            if (plugin.hasGriefPrevention() && Config.enableGriefPreventionIntegration)
                VisualizationManager.visualizeOverlap(p, overlappingSelections);
            return true;
        }
        if (bank.getAccounts().stream().anyMatch(account -> !selection.contains(account.getLocation()))) {
            plugin.debug("New selection does not contain all accounts");
            p.sendMessage(Messages.SELECTION_CUTS_ACCOUNTS);
            return true;
        }

        BankResizeEvent event = new BankResizeEvent(p, bank, selection);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank resize event cancelled");
            return true;
        }

        bankUtils.removeBank(bank, false);
        bank.setSelection(selection);
        bankUtils.addBank(bank, true, Callback.of(plugin,
                result -> {
                    plugin.debug(p.getName() + " has resized bank \"" + bank.getName() + "\" (#" + bank.getID() + ")");
                    p.sendMessage(Messages.BANK_RESIZED);
                },
                error -> p.sendMessage(Messages.ERROR_OCCURRED)));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 2) {
            return bankUtils.getBanks().stream()
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length > 2) {
            String coord = getCoordLookingAt(p, args.length);
            if (coord.startsWith(args[args.length - 1]))
                return Collections.singletonList("" + coord);
        }
        return Collections.emptyList();
    }

}
