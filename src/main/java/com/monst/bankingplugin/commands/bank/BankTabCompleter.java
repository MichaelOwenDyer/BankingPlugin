package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTabCompleter implements TabCompleter {

    private final BankingPlugin plugin;
    private final BankUtils bankUtils;

    public BankTabCompleter(BankingPlugin plugin) {
        this.plugin = plugin;
        this.bankUtils = plugin.getBankUtils();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        final String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                return completeBankCreate((Player) sender, args);
            case "remove":
                return completeBankRemove(sender, args);
            case "info":
                return completeBankInfo(args);
            case "list": case "removeall":
                break;
            case "resize":
                return completeBankResize((Player) sender, args);
            case "rename":
                return completeBankRename(sender, args);
            case "set":
                return completeBankSet(sender, args);
            case "transfer":
                return completeBankTransfer((Player) sender, args);
            case "select":
                return completeBankSelect((Player) sender, args);
        }
        return Collections.emptyList();
    }

    private List<String> completeBankCreate(Player p, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();

        if (args.length == 1 || Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("admin")))
            return Collections.emptyList();

        if (args.length == 2)
            return Collections.singletonList("<name>");

        if (args.length % 3 == 0 && p.hasPermission(Permissions.BANK_CREATE_ADMIN))
                returnCompletions.add("admin");

        if (args.length >= 9)
            return returnCompletions;

        String coord = getCoordLookingAt(p, args);
        if (coord.startsWith(args[args.length - 1]))
            returnCompletions.add("" + coord);
        return returnCompletions;
    }

    private List<String> completeBankRemove(CommandSender sender, String[] args) {
        List<String> bankNames = bankUtils.getBanksCopy().stream()
                .filter(bank -> (sender instanceof Player && bank.isOwner((Player) sender))
                        || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_REMOVE_OTHER))
                        || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)))
                .map(Bank::getName)
                .sorted()
                .collect(Collectors.toList());
        if (args.length == 2)
            return Utils.filter(bankNames, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
        return Collections.emptyList();
    }

    private List<String> completeBankInfo(String[] args) {
        if (args.length == 2)
            return bankUtils.getBanksCopy().stream()
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    private List<String> completeBankSet(CommandSender sender, String[] args) {
        List<String> bankNames = bankUtils.getBanksCopy().stream()
                .filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
                        || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                        || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
                .map(Bank::getName)
                .sorted()
                .collect(Collectors.toList());
        List<String> fieldNames = BankField.stream()
                .filter(BankField::isOverrideAllowed)
                .map(BankField::getName)
                .sorted()
                .collect(Collectors.toList());

        if (args.length == 2)
            return Utils.filter(bankNames, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
        else if (args.length == 3 && bankUtils.lookupBank(args[1]) != null)
            return Utils.filter(fieldNames, name -> name.contains(args[2].toLowerCase()));
        else if (args.length == 4) {
            Bank bank = bankUtils.lookupBank(args[1]);
            BankField field = BankField.getByName(args[2]);
            if (bank != null && field != null)
                return Collections.singletonList(bank.getFormatted(field));
        }
        return Collections.emptyList();
    }

    private List<String> completeBankTransfer(Player p, String[] args) {
        List<String> bankNames = bankUtils.getBanksCopy().stream()
                .filter(bank -> bank.isOwner(p)
                        || (bank.isPlayerBank() && p.hasPermission(Permissions.BANK_TRANSFER_OTHER))
                        || (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRANSFER_ADMIN)))
                .map(Bank::getName)
                .sorted()
                .collect(Collectors.toList());
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        if (!p.hasPermission(Permissions.BANK_TRANSFER_OTHER) && !p.hasPermission(Permissions.BANK_TRANSFER_ADMIN))
            onlinePlayers.remove(p.getName());

        if (args.length == 2) {
            List<String> returnCompletions = Utils.filter(bankNames, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
            if (plugin.getBankUtils().isBank(p.getLocation()))
                returnCompletions.addAll(Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[1].toLowerCase())));
            return returnCompletions;
        } else if (args.length == 3)
            return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
        return Collections.emptyList();
    }

    private List<String> completeBankResize(Player p, String[] args) {
        List<String> bankNames = bankUtils.getBanksCopy().stream()
                .map(Bank::getName)
                .sorted()
                .collect(Collectors.toList());

        if (args.length == 2)
            return Utils.filter(bankNames, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
        else if (args.length > 2) {
            String coord = getCoordLookingAt(p, args);
            if (coord.startsWith(args[args.length - 1]))
                return Collections.singletonList("" + coord);
        }
        return Collections.emptyList();
    }

    private List<String> completeBankRename(CommandSender sender, String[] args) {
        List<String> bankNames = bankUtils.getBanksCopy().stream()
                .filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
                        || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                        || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
                .map(Bank::getName)
                .sorted()
                .collect(Collectors.toList());
        if (args.length == 2) {
            if (args[1].isEmpty() && sender instanceof Player && bankUtils.isBank(((Player) sender).getLocation()))
                return Collections.singletonList(bankUtils.getBank(((Player) sender).getLocation()).getName());
            return Utils.filter(bankNames, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
        }
        return Collections.emptyList();
    }

    private List<String> completeBankSelect(Player p, String[] args) {
        if (!p.hasPermission(Permissions.BANK_SELECT))
            return Collections.emptyList();
        if (args.length == 2) {
            if (args[1].isEmpty() && bankUtils.isBank(p.getLocation()))
                return Collections.singletonList(bankUtils.getBank(p.getLocation()).getName());
            return bankUtils.getBanksCopy().stream()
                    .map(Bank::getName)
                    .sorted()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getCoordLookingAt(Player p, String[] args) {
        Location loc = p.getTargetBlock(null, 150).getLocation();
        String coord = "";
        switch (args.length % 3) {
            case 0: coord = "" + loc.getBlockX(); break;
            case 1: coord = "" + loc.getBlockY(); break;
            case 2: coord = "" + loc.getBlockZ();
        }
        return coord;
    }
}
