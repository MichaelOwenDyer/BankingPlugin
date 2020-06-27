package com.monst.bankingplugin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;

class GenericTabCompleter implements TabCompleter {

	private BankingPlugin plugin;

	GenericTabCompleter(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

		final String accountCommand = Config.mainCommandNameAccount.toLowerCase();
		final String bankCommand = Config.mainCommandNameBank.toLowerCase();
		final String controlCommand = Config.mainCommandNameControl.toLowerCase();

		final String subCommand = args[0].toLowerCase();

		if (command.getName().equalsIgnoreCase(accountCommand)) {
			switch (subCommand) {
			case "create":
				return new ArrayList<>();
			case "remove":
				return new ArrayList<>();
			case "info":
				return completeAccountInfo((Player) sender, args);
			case "list":
				return completeAccountList(sender, args);
			case "limits":
				return new ArrayList<>();
			case "removeall":
				return completeAccountRemoveAll(sender, args);
			case "set":
				return completeAccountSet(sender, args);
			default:
				return new ArrayList<>();
			}
		} else if (command.getName().equalsIgnoreCase(bankCommand)) {
			switch (subCommand) {
			case "create":
				return completeBankCreate((Player) sender, args);
			case "remove":
				return new ArrayList<>();
			case "info":
				return completeBankInfo((Player) sender, args);
			case "list":
				return completeBankList(sender, args);
			case "removeall":
				return completeBankRemoveAll(sender, args);
			case "resize":
				return completeBankCreate((Player) sender, args);
			case "set":
				return completeBankSet(sender, args);
			default:
				return new ArrayList<>();
			}
		} else if (command.getName().equalsIgnoreCase(controlCommand)) {
			switch (subCommand) {
			case "config":
				return completeControlConfig(sender, args);
			case "reload":
				return new ArrayList<>();
			case "update":
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
	}

	private List<String> completeAccountInfo(Player p, String[] args) {
		if (args.length > 2)
			return new ArrayList<>();
		if ("-d".startsWith(args[1].toLowerCase()) || "detailed".startsWith(args[1].toLowerCase()))
			return Arrays.asList("detailed");
		else
			return new ArrayList<>();
	}

	private List<String> completeAccountList(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		onlinePlayers.remove(sender.getName());

		ArrayList<String> flags = new ArrayList<>(Arrays.asList("detailed"));
		if (sender.hasPermission(Permissions.ACCOUNT_OTHER_LIST))
			flags.add("all");

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				if ("-d".startsWith(args[1].toLowerCase()) || "detailed".startsWith(args[1].toLowerCase()))
					returnCompletions.add("detailed");
				if ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase()))
					if (sender.hasPermission(Permissions.ACCOUNT_OTHER_LIST))
						returnCompletions.add("all");
				for (String name : onlinePlayers)
					if (name.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(name);
				return returnCompletions;
			} else {
				onlinePlayers.addAll(flags);
				return onlinePlayers;
			}
		} else if (args.length == 3) {
			if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed")) {
				if (!args[2].isEmpty()) {
					if ("-a".startsWith(args[2].toLowerCase()) || "all".startsWith(args[2].toLowerCase()))
						return Arrays.asList("all");
					else
						return new ArrayList<>();
				} else
					return Arrays.asList("all");
			} else {
				if (!args[2].isEmpty()) {
					if ("-d".startsWith(args[2].toLowerCase()) || "detailed".startsWith(args[2].toLowerCase()))
						return Arrays.asList("detailed");
					else
						return new ArrayList<>();
				} else
					return Arrays.asList("detailed");
			}
		} else
			return new ArrayList<>();
	}

	private List<String> completeAccountRemoveAll(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		onlinePlayers.remove(sender.getName());
		
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				if ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase()))
					returnCompletions.add("all");
				if ("-c".startsWith(args[1].toLowerCase()) || "cancel".startsWith(args[1].toLowerCase()))
					returnCompletions.add("cancel");
				for (String name : onlinePlayers)
					if (name.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(name);
				return returnCompletions;
			} else {
				onlinePlayers.addAll(Arrays.asList("all", "cancel"));
				return onlinePlayers;
			}
		}
		return new ArrayList<>();
	}

	private List<String> completeAccountSet(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> fields = Arrays.asList("nickname", "multiplier", "initial-delay");

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String s : fields)
					if (s.startsWith(args[1]))
						returnCompletions.add(s);
				return returnCompletions;
			} else
				return fields;
		}
		return new ArrayList<>();
	}

	private List<String> completeBankCreate(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		
		if (args.length == 2) {
			if (args[1].isEmpty())
				return Arrays.asList("[bankname]");
			else
				return new ArrayList<>();
		} else if (args.length == 3) {
			Block b = p.getTargetBlock(null, 150);
			if (b == null)
				return returnCompletions;
			String coord = "" + b.getLocation().getBlockX();
			if (!args[2].isEmpty()) {
				if (args[2].startsWith(coord))
					returnCompletions.add(coord);
				return returnCompletions;
			} else {
				returnCompletions.add(coord);
				return returnCompletions;
			}
		} else if (args.length == 4) {
			Block b = p.getTargetBlock(null, 150);
			if (b == null)
				return returnCompletions;
			String coord = "" + b.getLocation().getBlockY();
			if (!args[3].isEmpty()) {
				if (args[3].startsWith(coord))
					returnCompletions.add(coord);
				return returnCompletions;
			} else {
				returnCompletions.add(coord);
				return returnCompletions;
			}
		} else if (args.length == 5) {
			Block b = p.getTargetBlock(null, 150);
			if (b == null)
				return returnCompletions;
			String coord = "" + b.getLocation().getBlockZ();
			if (!args[4].isEmpty()) {
				if (args[4].startsWith(coord))
					returnCompletions.add(coord);
				return returnCompletions;
			} else {
				returnCompletions.add(coord);
				return returnCompletions;
			}
		} else if (args.length == 6) {
			Block b = p.getTargetBlock(null, 150);
			if (b == null)
				return returnCompletions;
			String coord = "" + b.getLocation().getBlockX();
			if (!args[5].isEmpty()) {
				if (args[5].startsWith(coord))
					returnCompletions.add(coord);
				return returnCompletions;
			} else {
				returnCompletions.add(coord);
				return returnCompletions;
			}
		} else if (args.length == 7) {
			Block b = p.getTargetBlock(null, 150);
			if (b == null)
				return returnCompletions;
			String coord = "" + b.getLocation().getBlockY();
			if (!args[6].isEmpty()) {
				if (args[6].startsWith(coord))
					returnCompletions.add(coord);
				return returnCompletions;
			} else {
				returnCompletions.add(coord);
				return returnCompletions;
			}
		} else if (args.length == 8) {
			Block b = p.getTargetBlock(null, 150);
			if (b == null)
				return returnCompletions;
			String coord = "" + b.getLocation().getBlockZ();
			if (!args[7].isEmpty()) {
				if (args[7].startsWith(coord))
					returnCompletions.add(coord);
				return returnCompletions;
			} else {
				returnCompletions.add(coord);
				return returnCompletions;
			}
		}

		return new ArrayList<>();
	}

	private List<String> completeBankInfo(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> banks = plugin.getBankUtils().getBanksCopy().stream().map(Bank::getName).collect(Collectors.toList());
		
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String bank : banks)
					if (bank.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(bank);
				return returnCompletions;
			} else
				return banks;
		} else if (args.length == 3) {
			if (!args[1].isEmpty()) {
				if ("-d".startsWith(args[1].toLowerCase()) || "detailed".startsWith(args[1].toLowerCase()))
					return Arrays.asList("detailed");
			} else
				return Arrays.asList("detailed");
		}
		return new ArrayList<>();
	}

	private List<String> completeBankList(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				if ("-d".startsWith(args[1].toLowerCase()) || "detailed".startsWith(args[1].toLowerCase()))
					returnCompletions.add("detailed");
				return returnCompletions;
			} else
				return Arrays.asList("detailed");
		}
		return new ArrayList<>();
	}

	private List<String> completeBankRemoveAll(CommandSender sender, String[] args) {
		// ArrayList<String> returnCompletions = new ArrayList<>();

		return new ArrayList<>();
	}
	
	private List<String> completeBankSet(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		BankUtils bankUtils = plugin.getBankUtils();
		List<String> bankNames = bankUtils.getBanksCopy().stream().map(Bank::getName).map(Utils::stripColor)
				.collect(Collectors.toList());
		List<Field> fields = Field.stream().filter(field -> AccountConfig.isOverrideAllowed(field)).collect(Collectors.toList());
		
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String s : bankNames) {
					if (s.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(s);
				}
				return returnCompletions;
			} else {
				List<String> ownBanks = bankUtils.getBanksCopy().stream()
						.filter(b -> b.isTrusted((OfflinePlayer) sender)).map(Bank::getName).map(Utils::stripColor)
						.collect(Collectors.toList());
				Bank standingIn = bankUtils.getBank(((Player) sender).getLocation());
				return ownBanks.isEmpty()
						? (standingIn != null ? Arrays.asList(Utils.stripColor(standingIn.getName())) : bankNames)
						: ownBanks;
			}
		} else if (args.length == 3) {
			if (!args[2].isEmpty()) {
				for (Field f : fields)
					if (f.getName().contains(args[2].toLowerCase()))
						returnCompletions.add(f.getName());
				return returnCompletions;
			} else
				return fields.stream().map(field -> field.getName()).collect(Collectors.toList());
		} else if (args.length == 4) {
			if (Field.getByName(args[2]) != null)
				return new ArrayList<>();
		}
		return new ArrayList<>();
	}

	private List<String> completeControlConfig(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> subCommands = Arrays.asList("add", "remove", "set");
		Set<String> configValues = plugin.getConfig().getKeys(true);
		configValues.removeAll(plugin.getConfig().getKeys(false));
		configValues.remove("creation-prices.account");

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String s : subCommands)
					if (s.startsWith(args[1]))
						returnCompletions.add(s);
				return returnCompletions;
			} else
				return subCommands;
		} else if (args.length == 3) {
			if (!args[2].isEmpty()) {
				for (String s : configValues)
					if (s.contains(args[2]))
						returnCompletions.add(s);
				return returnCompletions;
			} else
				return new ArrayList<>(configValues);
		} else if (args.length == 4) {
			List<?> values = plugin.getConfig().getList(args[2]);
			if (values != null) {
				if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))
					for (Object o : values)
						returnCompletions.add("-" + o.toString());
				else
					returnCompletions.add(values.stream().map(o -> "-" + o.toString()).collect(Collectors.joining(" ")));
				return returnCompletions;
			}
			Object value = plugin.getConfig().get(args[2]);
			if (value != null) {
				returnCompletions.add(value.toString());
			}
			return returnCompletions;
		}
		return new ArrayList<>();
	}
}
