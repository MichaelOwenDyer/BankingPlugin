package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

class GenericTabCompleter implements TabCompleter {

	private final BankingPlugin plugin;

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
			case "create": case "remove": case "info": case "limits":
				break;
			case "list":
				return completeAccountList(sender, args);
			case "removeall":
				return completeAccountRemoveAll(sender, args);
			case "set":
				return completeAccountSet((Player) sender, args);
			case "trust": case "untrust":
				return completeAccountTrust((Player) sender, args);
			case "transfer":
				return completeAccountTransfer((Player) sender, args);
			default:
				return Collections.emptyList();
			}
			return Collections.emptyList();
		} else if (command.getName().equalsIgnoreCase(bankCommand)) {
			switch (subCommand) {
			case "create":
				return completeBankCreate((Player) sender, args);
			case "remove":
				return completeBankRemove(sender, args);
			case "info":
				return completeBankInfo(sender, args);
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
			default:
				return Collections.emptyList();
			}
			return Collections.emptyList();
		} else if (command.getName().equalsIgnoreCase(controlCommand)) {
			switch (subCommand) {
			case "config":
				return completeControlConfig(sender, args);
			case "reload": case "update":
				break;
			default:
				return Collections.emptyList();
			}
			return Collections.emptyList();
		}
		return Collections.emptyList();
	}

	private List<String> completeAccountList(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		onlinePlayers.remove(sender.getName());

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER) && ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase())))
						returnCompletions.add("all");
				returnCompletions.addAll(onlinePlayers.stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList()));
				return returnCompletions;
			} else {
				onlinePlayers.add("all");
				return onlinePlayers;
			}
		}
		return Collections.emptyList();
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
		return Collections.emptyList();
	}

	private List<String> completeAccountSet(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> fields = Arrays.asList("nickname", "multiplier", "interest-delay");

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String s : fields)
					if (s.startsWith(args[1]))
						returnCompletions.add(s);
				return returnCompletions;
			} else
				return fields;
		}
		return Collections.emptyList();
	}

	private List<String> completeAccountTrust(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin); 
		onlinePlayers.remove(p.getName());

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String name : onlinePlayers)
					if (name.startsWith(args[1]))
						returnCompletions.add(name);
				return returnCompletions;
			}
			return onlinePlayers;
		}
		return Collections.emptyList();
	}

	private List<String> completeAccountTransfer(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
		if (!p.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER))
			onlinePlayers.remove(p.getName());
		if (p.hasPermission(Permissions.BANK_CREATE_ADMIN))
			onlinePlayers.add("ADMIN");

		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String name : onlinePlayers)
					if (name.startsWith(args[1]))
						returnCompletions.add(name);
				return returnCompletions;
			}
			return onlinePlayers;
		}
		return Collections.emptyList();
	}

	private List<String> completeBankCreate(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		if ((args.length == 2 || args.length == 5 || args.length == 8)
				&& p.hasPermission(Permissions.BANK_CREATE_ADMIN))
			returnCompletions.add("admin");

		if ((args.length > 2 && args[1].equalsIgnoreCase("admin"))
				|| (args.length > 5 && args[4].equalsIgnoreCase("admin")))
			return Collections.emptyList();

		if (args.length > 7)
			return returnCompletions;

		Location loc = p.getTargetBlock(null, 150).getLocation();
		String coord = "";
		switch (args.length % 3) {
			case 2: coord = "" + loc.getBlockX(); break;
			case 0: coord = "" + loc.getBlockY(); break;
			case 1: coord = "" + loc.getBlockZ();
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
		List<Bank> banks = plugin.getBankUtils().getBanksCopy().stream()
				.filter(bank -> (sender instanceof Player && bank.isOwner((Player) sender))
						|| (!bank.isAdminBank() && sender.hasPermission(Permissions.BANK_REMOVE_OTHER))
						|| (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)))
				.collect(Collectors.toList());
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String bankName : banks.stream().map(Bank::getName).collect(Collectors.toList()))
					if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(bankName);
				return returnCompletions;
			}
			return banks.stream().map(Bank::getName).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private List<String> completeBankInfo(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<Bank> banks = new ArrayList<>(plugin.getBankUtils().getBanksCopy());
		
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String bankName : banks.stream().map(Bank::getName).collect(Collectors.toList()))
					if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(bankName);
				return returnCompletions;
			}
			return banks.stream().map(Bank::getName).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	private List<String> completeBankSet(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		BankUtils bankUtils = plugin.getBankUtils();
		List<Bank> banks = bankUtils.getBanksCopy().stream()
				.filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
						|| (!bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
						|| (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
				.collect(Collectors.toList());
		List<Field> fields = Field.stream().filter(AccountConfig::isOverrideAllowed).collect(Collectors.toList());
		
		if (args.length == 2) {
			if (sender instanceof Player && bankUtils.isBank(((Player) sender).getLocation())) {
				banks.remove(bankUtils.getBank(((Player) sender).getLocation()));
				if (!args[1].isEmpty()) {
					for (Field field : fields)
						if (field.getName().startsWith(args[1].toLowerCase()))
							returnCompletions.add(field.getName());
					for (Bank bank : banks)
						if (bank.getName().startsWith(args[1].toLowerCase()))
							returnCompletions.add(bank.getName());
				} else {
					returnCompletions.addAll(fields.stream().map(Field::getName).collect(Collectors.toList()));
					returnCompletions.addAll(banks.stream().map(Bank::getName).collect(Collectors.toList()));
				}
				return returnCompletions;
			} else {
				if (!args[1].isEmpty()) {
					for (Bank bank : banks)
						if (bank.getName().toLowerCase().startsWith(args[1].toLowerCase()))
								returnCompletions.add(bank.getName());
					return returnCompletions;
				} else
					return banks.stream().map(Bank::getName).collect(Collectors.toList());
			}
		} else if (args.length == 3) {
			Bank bank = bankUtils.lookupBank(args[1]);
			if (bank != null) {
				if (!args[2].isEmpty()) {
					for (Field f : fields)
						if (f.getName().contains(args[2].toLowerCase()))
							returnCompletions.add(f.getName());
					return returnCompletions;
				} else
					return fields.stream().map(Field::getName).collect(Collectors.toList());
			}
			if (!(sender instanceof Player))
				return new ArrayList<>();
			bank = bankUtils.getBank(((Player) sender).getLocation());
			Field field = Field.getByName(args[1]);
			if (bank != null && field != null) {
				if (field.getDataType().equals(List.class)) // TODO: Generify
					return Collections.singletonList(Utils.formatList(bank.getAccountConfig().get(field)));
				return Collections.singletonList(Utils.format(bank.getAccountConfig().get(field)));
			}
		} else if (args.length == 4) {
			Bank bank = bankUtils.lookupBank(args[1]);
			Field field = Field.getByName(args[2]);
			if (bank != null && field != null) {
				if (field.getDataType().equals(List.class)) // TODO: Generify
					return Collections.singletonList(Utils.formatList(bank.getAccountConfig().get(field)));
				return Collections.singletonList(Utils.format(bank.getAccountConfig().get(field)));
			}
		}
		return Collections.emptyList();
	}

	private List<String> completeBankTransfer(Player p, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();
		List<String> banks = plugin.getBankUtils().getBanksCopy().stream()
				.filter(bank -> bank.isOwner(p)
						|| (!bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRANSFER_OTHER))
						|| (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRANSFER_ADMIN)))
				.map(Bank::getName).collect(Collectors.toList());
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
			} else {
				return onlinePlayers;
			}
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
					if (name.startsWith(args[1]))
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
		List<Bank> banks = bankUtils.getBanksCopy().stream()
				.filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
						|| (!bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
						|| (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
				.collect(Collectors.toList());
		if (args.length == 2) {
			if (!args[1].isEmpty()) {
				for (String bankName : banks.stream().map(Bank::getName).collect(Collectors.toList()))
					if (bankName.toLowerCase().startsWith(args[1].toLowerCase()))
						returnCompletions.add(bankName);
				return returnCompletions;
			} else {
				if (sender instanceof Player && bankUtils.isBank(((Player) sender).getLocation()))
					return Collections.singletonList(bankUtils.getBank(((Player) sender).getLocation()).getName());
				else
					return banks.stream().map(Bank::getName).collect(Collectors.toList());
			}
		} 
		return Collections.emptyList();
	}

	private List<String> completeControlConfig(CommandSender sender, String[] args) {
		ArrayList<String> returnCompletions = new ArrayList<>();

		if (args.length == 2)
			if (!args[1].isEmpty()) {
				if ("set".startsWith(args[1].toLowerCase()))
					return Collections.singletonList("set");
			} else
				return Collections.singletonList("set");

		Set<String> configValues = plugin.getConfig().getKeys(true);
		plugin.getConfig().getKeys(true).forEach(s -> {
			if (s.contains("."))
				configValues.remove(s.substring(0, s.lastIndexOf('.')));
		});

		if (args.length == 3) {
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
		return Collections.emptyList();
	}
}
