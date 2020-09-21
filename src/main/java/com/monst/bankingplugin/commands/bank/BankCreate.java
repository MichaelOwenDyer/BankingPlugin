package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BankCreate extends BankCommand.SubCommand {

    BankCreate() {
        super("create", true); // TODO: Allow bank creation from console?
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_CREATE : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create a bank");

        if (!p.hasPermission(Permissions.BANK_CREATE)) {
            plugin.debug(p.getName() + " does not have permission to create a bank");
            p.sendMessage(Messages.NO_PERMISSION_BANK_CREATE);
            return true;
        }

        if (args.length < 2)
            return false;

        String name = args[1];

        Selection selection;
        if (args.length <= 2) {
            if (Config.enableWorldEditIntegration && plugin.hasWorldEdit()) {
                selection = WorldEditReader.getSelection(plugin, p);
                if (selection == null) {
                    plugin.debug(p.getName() + " tried to create a bank with no WorldEdit selection");
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
                plugin.debug("Could not parse coordinates in command args");
                p.sendMessage(Messages.COORDINATES_PARSE_ERROR);
                return false;
            }
        }

        if (selection == null)
            return false;

        if (Config.disabledWorlds.contains(selection.getWorld().getName())) {
            plugin.debug("BankingPlugin is disabled in world " + selection.getWorld().getName());
            p.sendMessage(Messages.WORLD_DISABLED);
            return true;
        }

        boolean isAdminBank = args[args.length - 1].equalsIgnoreCase("admin");

        if (isAdminBank && !p.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            plugin.debug(p.getName() + " does not have permission to create an admin bank");
            p.sendMessage(Messages.NO_PERMISSION_BANK_CREATE_ADMIN);
            return true;
        }
        if (!isAdminBank) {
            int limit = BankUtils.getBankLimit(p);
            if (limit != -1 && bankUtils.getNumberOfBanks(p) >= limit) {
                p.sendMessage(Messages.BANK_LIMIT_REACHED);
                plugin.debug(p.getName() + " has reached their bank limit");
                return true;
            }
        }
        Set<Selection> overlappingSelections = bankUtils.getOverlappingSelections(selection);
        if (!overlappingSelections.isEmpty()) {
            plugin.debug("Region is not exclusive");
            p.sendMessage(Messages.SELECTION_OVERLAPS_EXISTING);
            if (plugin.hasGriefPrevention() && Config.enableGriefPreventionIntegration)
                VisualizationManager.visualizeOverlap(p, overlappingSelections);
            return true;
        }
        long volume = selection.getVolume();
        long volumeLimit = BankUtils.getVolumeLimit(p);
        if (!isAdminBank && volumeLimit != -1 && volume > volumeLimit) {
            plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
            p.sendMessage(String.format(Messages.SELECTION_TOO_LARGE, volumeLimit, volume - volumeLimit));
            return true;
        }
        if (!isAdminBank && volume < Config.minimumBankVolume) {
            plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume + ")");
            p.sendMessage(String.format(Messages.SELECTION_TOO_SMALL, Config.minimumBankVolume, Config.minimumBankVolume - volume));
            return true;
        }
        if (!bankUtils.isUniqueName(name)) {
            plugin.debug("Name is not unique");
            p.sendMessage(Messages.NAME_NOT_UNIQUE);
            return true;
        }
        if (!Utils.isAllowedName(name)) {
            plugin.debug("Name is not allowed");
            p.sendMessage(Messages.NAME_NOT_ALLOWED);
            return true;
        }

        Bank bank = isAdminBank ?
                Bank.mint(name, selection) :
                Bank.mint(name, p, selection);

        BankCreateEvent event = new BankCreateEvent(p, bank);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS)) {
            plugin.debug("No permission to create bank without WorldGuard flag present");
            p.sendMessage(Messages.NO_PERMISSION_BANK_CREATE_PROTECTED);
            return true;
        }

        double creationPrice = isAdminBank ? Config.bankCreationPriceAdmin : Config.bankCreationPricePlayer;
        if (creationPrice > 0 && plugin.getEconomy().getBalance(p) < creationPrice) {
            plugin.debug(p.getName() + " does not have enough money to create a bank");
            p.sendMessage(Messages.BANK_CREATE_INSUFFICIENT_FUNDS);
            return true;
        }

        String worldName = p.getLocation().getWorld() != null ? p.getLocation().getWorld().getName() : "World";
        if (!Utils.withdrawPlayer(p.getPlayer(), worldName, creationPrice, Callback.of(plugin,
                result -> p.sendMessage(String.format(Messages.BANK_CREATE_FEE_PAID, Utils.format(creationPrice))),
                error -> p.sendMessage(Messages.ERROR_OCCURRED))
        ))
            return true;

        bankUtils.addBank(bank, true);
        plugin.debug(p.getName() + " has created a new " + (bank.isAdminBank() ? "admin " : "") + "bank.");
        p.sendMessage(Messages.BANK_CREATED);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        ArrayList<String> returnCompletions = new ArrayList<>();

        if (args.length == 1 || Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("admin")))
            return Collections.emptyList();

        if (args.length == 2)
            return Collections.singletonList("<name>");

        if (args.length % 3 == 0 && p.hasPermission(Permissions.BANK_CREATE_ADMIN))
            returnCompletions.add("admin");

        if (args.length >= 9)
            return returnCompletions;

        String coord = getCoordLookingAt(p, args.length);
        if (coord.startsWith(args[args.length - 1]))
            returnCompletions.add("" + coord);
        return returnCompletions;
    }

}
