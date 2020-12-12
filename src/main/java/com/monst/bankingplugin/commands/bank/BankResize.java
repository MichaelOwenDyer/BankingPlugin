package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.BankRepository;
import com.monst.bankingplugin.utils.Callback;
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
    protected String getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_RESIZE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to resize a bank");

        if (!p.hasPermission(Permissions.BANK_RESIZE)) {
            plugin.debug(p.getName() + " does not have permission to resize a bank");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_RESIZE));
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
                    p.sendMessage(LangUtils.getMessage(Message.BANK_SELECT_REGION));
                    return true;
                }
            } else {
                plugin.debug("WorldEdit is not enabled");
                p.sendMessage(LangUtils.getMessage(Message.WORLDEDIT_NOT_ENABLED));
                return true;
            }
        } else {
            try {
                selection = parseCoordinates(args, p.getLocation());
            } catch (NumberFormatException e) {
                plugin.debug("Could not parse coordinates in command args: \"" + Arrays.toString(args) + "\"");
                p.sendMessage(LangUtils.getMessage(Message.BANK_COORDINATE_PARSE_ERROR));
                return false;
            }
        }

        if (selection == null)
            return false;

        bank = bankRepo.getByIdentifier(args[1]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            p.sendMessage(LangUtils.getMessage(Message.BANK_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }
        if (bank.isPlayerBank() && !bank.isOwner(p) && !p.hasPermission(Permissions.BANK_RESIZE_OTHER)) {
            plugin.debug(p.getName() + " does not have permission to resize another player's bank");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_RESIZE_OTHER));
            return true;
        }
        if (bank.isAdminBank() && !p.hasPermission(Permissions.BANK_RESIZE_ADMIN)) {
            plugin.debug(p.getName() + " does not have permission to resize an admin bank");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_RESIZE_ADMIN));
            return true;
        }
        if (Config.disabledWorlds.contains(selection.getWorld().getName())) {
            plugin.debug("BankingPlugin is disabled in world " + selection.getWorld().getName());
            p.sendMessage(LangUtils.getMessage(Message.WORLD_DISABLED));
            return true;
        }
        long volume = selection.getVolume();
        long volumeLimit = BankRepository.getVolumeLimit(p);
        if (bank.isPlayerBank() && volumeLimit >= 0 && volume > volumeLimit) {
            plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_TOO_LARGE,
                    new Replacement(Placeholder.BANK_SIZE, volume),
                    new Replacement(Placeholder.MAXIMUM, volumeLimit),
                    new Replacement(Placeholder.DIFFERENCE, () -> volume - volumeLimit)
            ));
            return true;
        }
        if (bank.isPlayerBank() && volume < Config.minimumBankVolume) {
            plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume + ")");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_TOO_SMALL,
                    new Replacement(Placeholder.BANK_SIZE, volume),
                    new Replacement(Placeholder.MINIMUM, Config.minimumBankVolume),
                    new Replacement(Placeholder.DIFFERENCE, () -> Config.minimumBankVolume - volume)
            ));
            return true;
        }
        Set<Selection> overlappingSelections = bankRepo.getOverlappingSelectionsIgnoring(selection, bank.getSelection());
        if (!overlappingSelections.isEmpty()) {
            plugin.debug("New selection is overlaps with an existing bank selection");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_OVERLAPS_EXISTING));
            if (plugin.hasGriefPrevention() && Config.enableGriefPreventionIntegration)
                VisualizationManager.visualizeOverlap(p, overlappingSelections);
            return true;
        }
        long cutAccounts = bank.getAccounts().stream().filter(account -> !selection.contains(account.getLocation())).count();
        if (cutAccounts > 0) {
            plugin.debug("New selection does not contain all accounts");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_CUTS_ACCOUNTS, new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, cutAccounts)));
            return true;
        }

        BankResizeEvent event = new BankResizeEvent(p, bank, selection);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank resize event cancelled");
            return true;
        }

        bankRepo.remove(bank, false);
        bank.setSelection(selection);
        bankRepo.add(bank, true, Callback.of(plugin,
                result -> {
                    plugin.debug(p.getName() + " has resized bank \"" + bank.getName() + "\" (#" + bank.getID() + ")");
                    p.sendMessage(LangUtils.getMessage(Message.BANK_RESIZED, new Replacement(Placeholder.BANK_SIZE, selection::getVolume)));
                },
                error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                ))
        ));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 2) {
            return bankRepo.getAll().stream()
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
