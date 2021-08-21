package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.external.VisualizationManager;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class BankCreate extends SubCommand.BankSubCommand {

    BankCreate(BankingPlugin plugin) {
		super(plugin, "create", true); // TODO: Allow bank creation from console?
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
            p.sendMessage(Message.NO_PERMISSION_BANK_CREATE.translate());
            return true;
        }

        if (args.length < 2)
            return false;

        String name = args[1];

        BankRegion bankRegion;
        if (args.length <= 3) {
            if (plugin.isWorldEditIntegrated()) {
                bankRegion = WorldEditReader.getBankRegion(plugin, p);
                if (bankRegion == null) {
                    plugin.debug(p.getName() + " tried to create a bank with no WorldEdit selection");
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

        if (Config.disabledWorlds.get().contains(bankRegion.getWorld())) {
            plugin.debug("BankingPlugin is disabled in world " + bankRegion.getWorld().getName());
            p.sendMessage(Message.WORLD_DISABLED.with(Placeholder.WORLD).as(bankRegion.getWorld().getName()).translate());
            return true;
        }

        boolean isAdminBank = args[args.length - 1].equalsIgnoreCase("admin");

        if (isAdminBank && !p.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            plugin.debug(p.getName() + " does not have permission to create an admin bank");
            p.sendMessage(Message.NO_PERMISSION_BANK_CREATE_ADMIN.translate());
            return true;
        }
        if (!isAdminBank) {
            int limit = Utils.getBankLimit(p);
            if (limit != -1 && plugin.getBankRepository().getOwnedBy(p).size() >= limit) {
                p.sendMessage(Message.BANK_LIMIT_REACHED.with(Placeholder.LIMIT).as(limit).translate());
                plugin.debug(p.getName() + " has reached their bank limit");
                return true;
            }
        }
        Set<BankRegion> overlappingRegions = plugin.getBankRepository().getOverlappingRegions(bankRegion);
        if (!overlappingRegions.isEmpty()) {
            plugin.debug("Region is not exclusive");
            p.sendMessage(Message.BANK_SELECTION_OVERLAPS_EXISTING
                    .with(Placeholder.NUMBER_OF_BANKS).as(overlappingRegions.size())
                    .translate());
            if (plugin.isGriefPreventionIntegrated())
                VisualizationManager.visualizeOverlap(p, overlappingRegions);
            return true;
        }
        long volume = bankRegion.getVolume();
        long volumeLimit = Utils.getBankVolumeLimit(p);
        if (!isAdminBank && volumeLimit >= 0 && volume > volumeLimit) {
            plugin.debugf("Bank is too large (%d blocks, limit: %d)", volume, volumeLimit);
            p.sendMessage(Message.BANK_SELECTION_TOO_LARGE
                    .with(Placeholder.BANK_SIZE).as(volume)
                    .and(Placeholder.MAXIMUM).as(volumeLimit)
                    .and(Placeholder.DIFFERENCE).as(volume - volumeLimit)
                    .translate());
            return true;
        }
        if (!isAdminBank && volume < Config.minimumBankVolume.get()) {
            plugin.debugf("Bank is too small (%d blocks, minimum: %d)", volume, Config.minimumBankVolume.get());
            p.sendMessage(Message.BANK_SELECTION_TOO_SMALL
                    .with(Placeholder.BANK_SIZE).as(volume)
                    .and(Placeholder.MINIMUM).as(Config.minimumBankVolume.get())
                    .and(Placeholder.DIFFERENCE).as(Config.minimumBankVolume.get() - volume)
                    .translate());
            return true;
        }
        if (plugin.getBankRepository().getByName(name) != null) {
            plugin.debug("Name is not unique");
            p.sendMessage(Message.NAME_NOT_UNIQUE.with(Placeholder.NAME).as(name).translate());
            return true;
        }
        if (!Config.nameRegex.matches(name)) {
            plugin.debug("Name is not allowed");
            p.sendMessage(Message.NAME_NOT_ALLOWED
                    .with(Placeholder.NAME).as(name)
                    .and(Placeholder.PATTERN).as(Config.nameRegex.get())
                    .translate());
            return true;
        }

        if (!isAdminBank) {
            BigDecimal creationPrice = Config.bankCreationPrice.get();
            if (!PayrollOffice.allowPayment(p, creationPrice.negate())) {
                plugin.debug(p.getName() + " does not have enough money to create a bank");
                BigDecimal balance = BigDecimal.valueOf(plugin.getEconomy().getBalance(p));
                p.sendMessage(Message.BANK_CREATE_INSUFFICIENT_FUNDS
                        .with(Placeholder.PRICE).as(creationPrice)
                        .and(Placeholder.PLAYER_BALANCE).as(balance)
                        .and(Placeholder.AMOUNT_REMAINING).as(creationPrice.subtract(balance))
                        .translate());
                return true;
            }
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(Message.BANK_CREATE_FEE_PAID.with(Placeholder.PRICE).as(creationPrice).translate());
            else
                return true;
        }

        Bank bank = Bank.open(plugin, name, isAdminBank ? null : p, bankRegion);

        BankCreateEvent event = new BankCreateEvent(p, bank);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS)) {
            plugin.debug("No permission to create bank without WorldGuard flag present");
            p.sendMessage(Message.NO_PERMISSION_BANK_CREATE_PROTECTED.translate());
            return true;
        }

        plugin.getBankRepository().add(bank, true);
        plugin.debugf("%s has created a new%s bank.", p.getName(), bank.isAdminBank() ? "admin " : "");
        p.sendMessage(Message.BANK_CREATED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        Player p = ((Player) player);
        ArrayList<String> returnCompletions = new ArrayList<>();

        if (args.length == 0 || Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("admin")))
            return Collections.emptyList();

        if (args.length == 1)
            return Collections.singletonList("<name>");

        if (args.length % 3 == 2 && p.hasPermission(Permissions.BANK_CREATE_ADMIN))
            returnCompletions.add("admin");

        if (args.length >= 8)
            return returnCompletions;

        String coord = "" + getCoordLookingAt(p, args.length);
        if (coord.startsWith(args[args.length - 1]))
            returnCompletions.add(coord);
        return returnCompletions;
    }

}
