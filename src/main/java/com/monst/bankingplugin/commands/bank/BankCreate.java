package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BankCreate extends BankCommand.SubCommand {

    BankCreate() {
        super("create", true); // TODO: Allow bank creation from console?
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_CREATE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create a bank");

        if (!p.hasPermission(Permissions.BANK_CREATE)) {
            plugin.debug(p.getName() + " does not have permission to create a bank");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_CREATE));
            return true;
        }

        if (args.length < 2)
            return false;

        String name = args[1];

        Selection selection;
        if (args.length <= 3) {
            if (Config.enableWorldEditIntegration.get() && plugin.hasWorldEdit()) {
                selection = WorldEditReader.getSelection(plugin, p);
                if (selection == null) {
                    plugin.debug(p.getName() + " tried to create a bank with no WorldEdit selection");
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
                plugin.debug("Could not parse coordinates in command args");
                p.sendMessage(LangUtils.getMessage(Message.BANK_COORDINATE_PARSE_ERROR));
                return false;
            }
        }

        if (selection == null)
            return false;

        if (Config.disabledWorlds.get().contains(selection.getWorld().getName())) {
            plugin.debug("BankingPlugin is disabled in world " + selection.getWorld().getName());
            p.sendMessage(LangUtils.getMessage(Message.WORLD_DISABLED));
            return true;
        }

        boolean isAdminBank = args[args.length - 1].equalsIgnoreCase("admin");

        if (isAdminBank && !p.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            plugin.debug(p.getName() + " does not have permission to create an admin bank");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_CREATE_ADMIN));
            return true;
        }
        if (!isAdminBank) {
            int limit = Utils.getBankLimit(p);
            if (limit != -1 && bankRepo.getOwnedBy(p).size() >= limit) {
                p.sendMessage(LangUtils.getMessage(Message.BANK_LIMIT_REACHED, new Replacement(Placeholder.LIMIT, limit)));
                plugin.debug(p.getName() + " has reached their bank limit");
                return true;
            }
        }
        Set<Selection> overlappingSelections = bankRepo.getOverlappingSelections(selection);
        if (!overlappingSelections.isEmpty()) {
            plugin.debug("Region is not exclusive");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_OVERLAPS_EXISTING,
                    new Replacement(Placeholder.NUMBER_OF_BANKS, overlappingSelections::size)
            ));
            if (plugin.hasGriefPrevention() && Config.enableGriefPreventionIntegration.get())
                VisualizationManager.visualizeOverlap(p, overlappingSelections);
            return true;
        }
        long volume = selection.getVolume();
        long volumeLimit = getVolumeLimit(p);
        if (!isAdminBank && volumeLimit >= 0 && volume > volumeLimit) {
            plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_TOO_LARGE,
                    new Replacement(Placeholder.BANK_SIZE, volume),
                    new Replacement(Placeholder.MAXIMUM, volumeLimit),
                    new Replacement(Placeholder.DIFFERENCE, () -> volume - volumeLimit)
            ));
            return true;
        }
        if (!isAdminBank && volume < Config.minimumBankVolume.get()) {
            plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume.get() + ")");
            p.sendMessage(LangUtils.getMessage(Message.BANK_SELECTION_TOO_SMALL,
                    new Replacement(Placeholder.BANK_SIZE, volume),
                    new Replacement(Placeholder.MINIMUM, Config.minimumBankVolume::get),
                    new Replacement(Placeholder.DIFFERENCE, () -> Config.minimumBankVolume.get() - volume)
            ));
            return true;
        }
        if (bankRepo.getByName(name) != null) {
            plugin.debug("Name is not unique");
            p.sendMessage(LangUtils.getMessage(Message.NAME_NOT_UNIQUE, new Replacement(Placeholder.BANK_NAME, name)));
            return true;
        }
        if (!Utils.isAllowedName(name)) {
            plugin.debug("Name is not allowed");
            p.sendMessage(LangUtils.getMessage(Message.NAME_NOT_ALLOWED, new Replacement(Placeholder.BANK_NAME, name)));
            return true;
        }

        if (!isAdminBank) {
            double creationPrice = Config.bankCreationPrice.get();
            if (!PayrollOffice.allowPayment(p, creationPrice * -1)) {
                plugin.debug(p.getName() + " does not have enough money to create a bank");
                double balance = plugin.getEconomy().getBalance(p);
                p.sendMessage(LangUtils.getMessage(Message.BANK_CREATE_INSUFFICIENT_FUNDS,
                        new Replacement(Placeholder.PRICE, creationPrice),
                        new Replacement(Placeholder.AMOUNT_REMAINING, creationPrice - balance),
                        new Replacement(Placeholder.PLAYER_BALANCE, balance)
                ));
                return true;
            }
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(LangUtils.getMessage(Message.BANK_CREATE_FEE_PAID,
                        new Replacement(Placeholder.PRICE, creationPrice)
                ));
            else
                return true;
        }

        Bank bank = Bank.mint(name, isAdminBank ? null : p, selection);

        BankCreateEvent event = new BankCreateEvent(p, bank);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS)) {
            plugin.debug("No permission to create bank without WorldGuard flag present");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_CREATE_PROTECTED));
            return true;
        }

        bankRepo.add(bank, true);
        plugin.debug(p.getName() + " has created a new " + (bank.isAdminBank() ? "admin " : "") + "bank.");
        p.sendMessage(LangUtils.getMessage(Message.BANK_CREATED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
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
