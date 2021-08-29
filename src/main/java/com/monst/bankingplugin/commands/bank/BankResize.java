package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankResize extends SubCommand.BankSubCommand {

    BankResize(BankingPlugin plugin) {
		super(plugin, "resize", true);
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_RESIZE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        plugin.debug(p.getName() + " wants to resize a bank");

        if (Permission.BANK_RESIZE.notOwnedBy(p)) {
            plugin.debug(p.getName() + " does not have permission to resize a bank");
            p.sendMessage(Message.NO_PERMISSION_BANK_RESIZE.translate());
            return true;
        }

        Bank bank;
        BankRegion bankRegion;

        if (args.length == 0)
            return false;

        else if (args.length == 1) {
            if (plugin.isWorldEditIntegrated()) {
                bankRegion = WorldEditReader.getBankRegion(plugin, p);
                if (bankRegion == null) {
                    plugin.debug(p.getName() + " tried to resize a bank with no WorldEdit selection");
                    p.sendMessage(Message.BANK_SELECT_REGION.translate());
                    return true;
                }
            } else {
                plugin.debug("WorldEdit is not enabled");
                p.sendMessage(Message.WORLDEDIT_NOT_ENABLED.translate());
                return true;
            }
        } else {
            try {
                bankRegion = parseCoordinates(args, p.getLocation().getBlock());
            } catch (IntegerParseException e) {
                p.sendMessage(e.getLocalizedMessage());
                plugin.debugf("Could not parse coordinate: \"%s\"", e.getLocalizedMessage());
                return false;
            }
        }

        if (bankRegion == null)
            return false;

        bank = plugin.getBankRepository().getByIdentifier(args[0]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[0]);
            p.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]).translate());
            return true;
        }
        if (bank.isPlayerBank() && !bank.isOwner(p) && Permission.BANK_RESIZE_OTHER.notOwnedBy(p)) {
            plugin.debug(p.getName() + " does not have permission to resize another player's bank");
            p.sendMessage(Message.NO_PERMISSION_BANK_RESIZE_OTHER.translate());
            return true;
        }
        if (bank.isAdminBank() && Permission.BANK_RESIZE_ADMIN.notOwnedBy(p)) {
            plugin.debug(p.getName() + " does not have permission to resize an admin bank");
            p.sendMessage(Message.NO_PERMISSION_BANK_RESIZE_ADMIN.translate());
            return true;
        }
        if (Config.disabledWorlds.get().contains(bankRegion.getWorld())) {
            plugin.debug("BankingPlugin is disabled in world " + bankRegion.getWorld().getName());
            p.sendMessage(Message.WORLD_DISABLED.with(Placeholder.WORLD).as(bankRegion.getWorld().getName()).translate());
            return true;
        }
        long volume = bankRegion.getVolume();
        long volumeLimit = Utils.getBankVolumeLimit(p);
        if (bank.isPlayerBank() && volumeLimit >= 0 && volume > volumeLimit) {
            plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
            p.sendMessage(Message.BANK_SELECTION_TOO_LARGE
                    .with(Placeholder.BANK_SIZE).as(volume)
                    .and(Placeholder.MAXIMUM).as(volumeLimit)
                    .and(Placeholder.DIFFERENCE).as(volume - volumeLimit)
                    .translate());
            return true;
        }
        if (bank.isPlayerBank() && volume < Config.minimumBankVolume.get()) {
            plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume.get() + ")");
            p.sendMessage(Message.BANK_SELECTION_TOO_SMALL
                    .with(Placeholder.BANK_SIZE).as(volume)
                    .and(Placeholder.MINIMUM).as(Config.minimumBankVolume.get())
                    .and(Placeholder.DIFFERENCE).as(Config.minimumBankVolume.get() - volume)
                    .translate());
            return true;
        }
        Set<BankRegion> overlappingRegions = plugin.getBankRepository().getOverlappingRegions(bankRegion);
        overlappingRegions.remove(bank.getRegion());
        if (!overlappingRegions.isEmpty()) {
            plugin.debug("New region overlaps with an existing bank region");
            p.sendMessage(Message.BANK_SELECTION_OVERLAPS_EXISTING
                    .with(Placeholder.NUMBER_OF_BANKS).as(overlappingRegions.size())
                    .translate());
            if (plugin.isGriefPreventionIntegrated())
                VisualizationManager.visualizeOverlap(p, overlappingRegions);
            return true;
        }
        long cutAccounts = bank.getAccounts(account -> !bankRegion.contains(account.getLocation())).size();
        if (cutAccounts > 0) {
            plugin.debug("New region does not contain all accounts");
            p.sendMessage(Message.BANK_SELECTION_CUTS_ACCOUNTS.with(Placeholder.NUMBER_OF_ACCOUNTS).as(cutAccounts).translate());
            return true;
        }

        BankResizeEvent event = new BankResizeEvent(p, bank, bankRegion);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Bank resize event cancelled");
            return true;
        }

        bank.setRegion(bankRegion);
        plugin.getBankRepository().update(bank, BankField.REGION);
        plugin.debugf("%s has resized bank #%d", p.getName(), bank.getID());
        p.sendMessage(Message.BANK_RESIZED.with(Placeholder.BANK_SIZE).as(volume).translate());
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankRepository().getAll().stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        String coord = "" + getCoordLookingAt(player, args.length);
        if (coord.startsWith(args[args.length - 1]))
            return Collections.singletonList(coord);
        return Collections.emptyList();
    }

}
