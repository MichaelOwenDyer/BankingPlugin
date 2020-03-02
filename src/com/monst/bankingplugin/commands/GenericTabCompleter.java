package com.monst.bankingplugin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
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
				return completeAccountCreate((Player) sender, args);
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

	private List<String> completeAccountCreate(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		onlinePlayers.remove(p.getName());

		if (!args[1].isEmpty()) {
			for (String name : onlinePlayers)
				if (name.toLowerCase().startsWith(args[1].toLowerCase()))
					returnCompletions.add(name);
			return returnCompletions;
		} else
			return onlinePlayers;
	}

	private List<String> completeAccountInfo(Player p, String[] args) {
		if ("-d".startsWith(args[1].toLowerCase()))
			return List.of("-d");
		else
			return new ArrayList<>();
	}

	private List<String> completeAccountList(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		onlinePlayers.remove(sender.getName());
		List<String> flags = List.of("-a", "-d");

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				if ("-d".startsWith(args[1].toLowerCase()) || "detailed".startsWith(args[1].toLowerCase()))
					returnCompletions.add("detailed");
				if ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[2].toLowerCase()))
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
						return List.of("all");
					else
						return new ArrayList<>();
				} else
					return List.of("all");
			} else {
				if (!args[2].isEmpty()) {
					if ("-d".startsWith(args[2].toLowerCase()) || "detailed".startsWith(args[2].toLowerCase()))
						return List.of("detailed");
					else
						return new ArrayList<>();
				} else
					return List.of("detailed");
			}
		} else
			return new ArrayList<>();
	}

	private List<String> completeAccountRemoveAll(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		onlinePlayers.remove(sender.getName());
		List<String> banks = plugin.getBankUtils().getBanksCopy().stream().map(Bank::getName)
				.collect(Collectors.toList());
		
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				if ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase()))
					returnCompletions.add("all");
				if ("-b".startsWith(args[1].toLowerCase()) || "bank".startsWith(args[1].toLowerCase()))
					returnCompletions.add("bank");
				if ("-c".startsWith(args[1].toLowerCase()) || "cancel".startsWith(args[1].toLowerCase()))
					returnCompletions.add("cancel");
				for (String name : onlinePlayers)
					if (name.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(name);
				return returnCompletions;
			} else {
				onlinePlayers.addAll(List.of("all","detailed","cancel"));
				return onlinePlayers;
			}
		} else if (args.length == 3) {
			if (args[1].equalsIgnoreCase("-b") || args[1].equalsIgnoreCase("bank")) {
				if (!args[2].isEmpty()) {
					for (String bank : banks)
						if (bank.toLowerCase().startsWith(args[2].toLowerCase()))
							returnCompletions.add(bank);
					return returnCompletions;
				} else
					return banks;
			}
		} else if (args.length == 4) {
			if ((args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all"))
					&& (args[2].equalsIgnoreCase("-b") || args[2].equalsIgnoreCase("bank"))) {
				if (!args[2].isEmpty()) {
					for (String bank : banks)
						if (bank.toLowerCase().startsWith(args[2].toLowerCase()))
							returnCompletions.add(bank);
					return returnCompletions;
				} else
					return banks;
			}
		}
		return new ArrayList<>();
	}

	private List<String> completeBankCreate(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		return new ArrayList<>();
	}

	private List<String> completeBankInfo(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		return new ArrayList<>();
	}

	private List<String> completeBankList(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		return new ArrayList<>();
	}

	private List<String> completeBankRemoveAll(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		return new ArrayList<>();
	}

	private List<String> completeControlConfig(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> subCommands = Arrays.asList("add", "remove", "set");
		Set<String> configValues = plugin.getConfig().getKeys(true);

		return new ArrayList<>();
	}
}
