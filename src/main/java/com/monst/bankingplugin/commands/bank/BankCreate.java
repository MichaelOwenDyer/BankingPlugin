package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BankCreate extends SubCommand.BankSubCommand {

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
        PLUGIN.debug(p.getName() + " wants to create a bank");

        if (!p.hasPermission(Permissions.BANK_CREATE)) {
            PLUGIN.debug(p.getName() + " does not have permission to create a bank");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_CREATE));
            return true;
        }

        if (args.length < 2)
            return false;

        String name = args[1];

        BankRegion bankRegion;
        if (args.length <= 3) {
            if (PLUGIN.isWorldEditIntegrated()) {
                bankRegion = WorldEditReader.getBankRegion(PLUGIN, p);
                if (bankRegion == null) {
                    PLUGIN.debug(p.getName() + " tried to create a bank with no WorldEdit selection");
                    p.sendMessage(Messages.get(Message.BANK_SELECT_REGION));
                    return true;
                }
            } else {
                PLUGIN.debug("WorldEdit is not enabled");
                p.sendMessage(Messages.get(Message.WORLDEDIT_NOT_ENABLED));
                return true;
            }
        } else {
            try {
                bankRegion = parseCoordinates(args, p.getLocation().getBlock());
            } catch (IntegerParseException e) {
                p.sendMessage(e.getLocalizedMessage());
                PLUGIN.debugf("Could not parse coordinate: \"%s\"", e.getLocalizedMessage());
                return false;
            }
        }

        if (bankRegion == null)
            return false;

        if (Config.disabledWorlds.get().contains(bankRegion.getWorld())) {
            PLUGIN.debug("BankingPlugin is disabled in world " + bankRegion.getWorld().getName());
            p.sendMessage(Messages.get(Message.WORLD_DISABLED, new Replacement(Placeholder.WORLD, bankRegion.getWorld().getName())));
            return true;
        }

        boolean isAdminBank = args[args.length - 1].equalsIgnoreCase("admin");

        if (isAdminBank && !p.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            PLUGIN.debug(p.getName() + " does not have permission to create an admin bank");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_CREATE_ADMIN));
            return true;
        }
        if (!isAdminBank) {
            int limit = Utils.getBankLimit(p);
            if (limit != -1 && bankRepo.getOwnedBy(p).size() >= limit) {
                p.sendMessage(Messages.get(Message.BANK_LIMIT_REACHED, new Replacement(Placeholder.LIMIT, limit)));
                PLUGIN.debug(p.getName() + " has reached their bank limit");
                return true;
            }
        }
        Set<BankRegion> overlappingRegions = bankRepo.getOverlappingRegions(bankRegion);
        if (!overlappingRegions.isEmpty()) {
            PLUGIN.debug("Region is not exclusive");
            p.sendMessage(Messages.get(Message.BANK_SELECTION_OVERLAPS_EXISTING,
                    new Replacement(Placeholder.NUMBER_OF_BANKS, overlappingRegions::size)
            ));
            if (PLUGIN.isGriefPreventionIntegrated())
                VisualizationManager.visualizeOverlap(p, overlappingRegions);
            return true;
        }
        long volume = bankRegion.getVolume();
        long volumeLimit = Utils.getBankVolumeLimit(p);
        if (!isAdminBank && volumeLimit >= 0 && volume > volumeLimit) {
            PLUGIN.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
            p.sendMessage(Messages.get(Message.BANK_SELECTION_TOO_LARGE,
                    new Replacement(Placeholder.BANK_SIZE, volume),
                    new Replacement(Placeholder.MAXIMUM, volumeLimit),
                    new Replacement(Placeholder.DIFFERENCE, () -> volume - volumeLimit)
            ));
            return true;
        }
        if (!isAdminBank && volume < Config.minimumBankVolume.get()) {
            PLUGIN.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume.get() + ")");
            p.sendMessage(Messages.get(Message.BANK_SELECTION_TOO_SMALL,
                    new Replacement(Placeholder.BANK_SIZE, volume),
                    new Replacement(Placeholder.MINIMUM, Config.minimumBankVolume),
                    new Replacement(Placeholder.DIFFERENCE, () -> Config.minimumBankVolume.get() - volume)
            ));
            return true;
        }
        if (bankRepo.getByName(name) != null) {
            PLUGIN.debug("Name is not unique");
            p.sendMessage(Messages.get(Message.NAME_NOT_UNIQUE, new Replacement(Placeholder.NAME, name)));
            return true;
        }
        if (!Config.nameRegex.matches(name)) {
            PLUGIN.debug("Name is not allowed");
            p.sendMessage(Messages.get(Message.NAME_NOT_ALLOWED,
                    new Replacement(Placeholder.NAME, name),
                    new Replacement(Placeholder.PATTERN, Config.nameRegex)
            ));
            return true;
        }

        if (!isAdminBank) {
            double creationPrice = Config.bankCreationPrice.get();
            if (!PayrollOffice.allowPayment(p, creationPrice * -1)) {
                PLUGIN.debug(p.getName() + " does not have enough money to create a bank");
                double balance = PLUGIN.getEconomy().getBalance(p);
                p.sendMessage(Messages.get(Message.BANK_CREATE_INSUFFICIENT_FUNDS,
                        new Replacement(Placeholder.PRICE, creationPrice),
                        new Replacement(Placeholder.AMOUNT_REMAINING, creationPrice - balance),
                        new Replacement(Placeholder.PLAYER_BALANCE, balance)
                ));
                return true;
            }
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(Messages.get(Message.BANK_CREATE_FEE_PAID,
                        new Replacement(Placeholder.PRICE, creationPrice)
                ));
            else
                return true;
        }

        Bank bank = Bank.open(name, isAdminBank ? null : p, bankRegion);

        BankCreateEvent event = new BankCreateEvent(p, bank);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS)) {
            PLUGIN.debug("No permission to create bank without WorldGuard flag present");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_CREATE_PROTECTED));
            return true;
        }

        bankRepo.add(bank, true);
        PLUGIN.debug(p.getName() + " has created a new " + (bank.isAdminBank() ? "admin " : "") + "bank.");
        p.sendMessage(Messages.get(Message.BANK_CREATED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        ArrayList<String> returnCompletions = new ArrayList<>();

        if (args.length == 0 || Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("admin")))
            return Collections.emptyList();

        if (args.length == 1)
            return Collections.singletonList("<name>");

        if (args.length % 3 == 2 && p.hasPermission(Permissions.BANK_CREATE_ADMIN))
            returnCompletions.add("admin");

        if (args.length >= 8)
            return returnCompletions;

        String coord = getCoordLookingAt(p, args.length);
        if (coord.startsWith(args[args.length - 1]))
            returnCompletions.add(coord);
        return returnCompletions;
    }

}
