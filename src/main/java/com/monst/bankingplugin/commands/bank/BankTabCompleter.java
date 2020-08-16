package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountConfig;
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

    public BankTabCompleter(BankingPlugin plugin) {
        this.plugin = plugin;
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

        if (Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("admin")))
            return Collections.emptyList();

        if (args.length == 2 && args[1].isEmpty())
            returnCompletions.add("<name>");

        boolean hasName = false;
        if (args.length > 1 && !args[1].isEmpty() && !"admin".startsWith(args[1]))
            try {
                Integer.parseInt(args[1].replace("~",""));
            } catch (NumberFormatException e) {
                hasName = true;
            }

        if (p.hasPermission(Permissions.BANK_CREATE_ADMIN))
            switch (args.length % 3) {
                case 0:
                    if (hasName)
                        returnCompletions.add("admin");
                    break;
                case 2:
                    if (!hasName)
                        returnCompletions.add("admin");
            }

        if (args.length >= 9)
            return returnCompletions;

        Location loc = p.getTargetBlock(null, 150).getLocation();
        String coord = "";
        switch ((args.length + (hasName ? 0 : 1)) % 3) {
            case 0: coord = "" + loc.getBlockX(); break;
            case 1: coord = "" + loc.getBlockY(); break;
            case 2: coord = "" + loc.getBlockZ();
        }
        if (!args[args.length - 1].isEmpty()) {
            if (coord.startsWith(args[args.length - 1]))
                returnCompletions.add(coord);
        } else
            returnCompletions.add(coord);
        return returnCompletions;
    }

    private List<String> completeBankRemove(CommandSender sender, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> banks = plugin.getBankUtils().getBanksCopy().stream()
                .filter(bank -> (sender instanceof Player && bank.isOwner((Player) sender))
                        || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_REMOVE_OTHER))
                        || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)))
                .map(Bank::getName)
                .collect(Collectors.toList());
        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String bankName : banks)
                    if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(bankName);
                return returnCompletions;
            }
            return banks;
        }
        return Collections.emptyList();
    }

    private List<String> completeBankInfo(String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> banks = plugin.getBankUtils().getBanksCopy().stream()
                .map(Bank::getName)
                .collect(Collectors.toList());

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String bankName : banks)
                    if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(bankName);
                return returnCompletions;
            }
            return banks;
        }
        return Collections.emptyList();
    }

    private List<String> completeBankSet(CommandSender sender, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        BankUtils bankUtils = plugin.getBankUtils();
        List<String> banks = bankUtils.getBanksCopy().stream()
                .filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
                        || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                        || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
                .map(Bank::getName)
                .collect(Collectors.toList());
        List<AccountConfig.Field> fields = AccountConfig.Field.stream()
                .filter(AccountConfig::isOverrideAllowed)
                .collect(Collectors.toList());

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String bankName : banks)
                    if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(bankName);
                return returnCompletions;
            }
            return banks;
        } else if (args.length == 3) {
            Bank bank = bankUtils.lookupBank(args[1]);
            if (bank != null) {
                if (!args[2].isEmpty()) {
                    for (AccountConfig.Field field : fields)
                        if (field.getName().contains(args[2].toLowerCase()))
                            returnCompletions.add(field.getName());
                    return returnCompletions;
                }
                return fields.stream().map(AccountConfig.Field::getName).collect(Collectors.toList());
            }
        } else if (args.length == 4) {
            Bank bank = bankUtils.lookupBank(args[1]);
            AccountConfig.Field field = AccountConfig.Field.getByName(args[2]);
            if (bank != null && field != null)
                return Collections.singletonList(bank.getAccountConfig().getFormatted(field));
        }
        return Collections.emptyList();
    }

    private List<String> completeBankTransfer(Player p, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> banks = plugin.getBankUtils().getBanksCopy().stream()
                .filter(bank -> bank.isOwner(p)
                        || (bank.isPlayerBank() && p.hasPermission(Permissions.BANK_TRANSFER_OTHER))
                        || (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRANSFER_ADMIN)))
                .map(Bank::getName)
                .collect(Collectors.toList());
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        if (!p.hasPermission(Permissions.BANK_TRANSFER_OTHER) && !p.hasPermission(Permissions.BANK_TRANSFER_ADMIN))
            onlinePlayers.remove(p.getName());

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String bankName : banks)
                    if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(bankName);
                if (plugin.getBankUtils().isBank(p.getLocation()))
                    for (String playerName : onlinePlayers)
                        if (playerName.toLowerCase().startsWith(args[1].toLowerCase()))
                            returnCompletions.add(playerName);
                return returnCompletions;
            } else {
                returnCompletions.addAll(banks);
                if (plugin.getBankUtils().isBank(p.getLocation()))
                    banks.addAll(onlinePlayers);
                return banks;
            }
        } else if (args.length == 3) {
            if (!args[2].isEmpty()) {
                for (String playerName : onlinePlayers)
                    if (playerName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(playerName);
                return returnCompletions;
            }
            return onlinePlayers;
        }
        return Collections.emptyList();
    }

    private List<String> completeBankResize(Player p, String[] args) {

        if (args.length < 2)
            return Collections.emptyList();

        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> banks = plugin.getBankUtils().getBanksCopy().stream().map(Bank::getName)
                .collect(Collectors.toList());

        if (args.length == 2) {
            for (String name : banks)
                if (!args[1].isEmpty()) {
                    if (name.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(name);
                } else
                    returnCompletions.add(name);
        } else {
            Location loc = p.getTargetBlock(null, 150).getLocation();
            String coord = "";
            switch (args.length % 3) {
                case 0: coord = "" + loc.getBlockX(); break;
                case 1: coord = "" + loc.getBlockY(); break;
                case 2: coord = "" + loc.getBlockZ();
            }
            if (!args[args.length - 1].isEmpty()) {
                if (coord.startsWith(args[args.length - 1]))
                    returnCompletions.add(coord);
            } else
                returnCompletions.add(coord);
        }
        return returnCompletions;
    }

    private List<String> completeBankRename(CommandSender sender, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        BankUtils bankUtils = plugin.getBankUtils();
        List<String> banks = bankUtils.getBanksCopy().stream()
                .filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
                        || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                        || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
                .map(Bank::getName)
                .collect(Collectors.toList());
        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String bankName : banks)
                    if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(bankName);
                return returnCompletions;
            } else {
                if (sender instanceof Player && bankUtils.isBank(((Player) sender).getLocation()))
                    return Collections.singletonList(bankUtils.getBank(((Player) sender).getLocation()).getName());
                return banks;
            }
        }
        return Collections.emptyList();
    }

    private List<String> completeBankSelect(Player p, String[] args) {
        if (!p.hasPermission(Permissions.BANK_SELECT))
            return Collections.emptyList();
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> banks = plugin.getBankUtils().getBanksCopy().stream()
                .map(Bank::getName)
                .collect(Collectors.toList());
        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String bankName : banks)
                    if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(bankName);
                return returnCompletions;
            } else {
                if (plugin.getBankUtils().isBank(p.getLocation()))
                    return Collections.singletonList(plugin.getBankUtils().getBank(p.getLocation()).getName());
                return banks;
            }
        }
        return Collections.emptyList();
    }
}
