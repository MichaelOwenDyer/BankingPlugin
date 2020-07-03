package com.monst.bankingplugin.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.events.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
import com.monst.bankingplugin.events.bank.BankResizeEvent;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class BankCommandExecutor implements CommandExecutor, Confirmable<Bank> {

	private BankingPlugin plugin;
	private BankUtils bankUtils;
	
	public BankCommandExecutor(BankingPlugin plugin) {
		this.plugin = plugin;
		this.bankUtils = plugin.getBankUtils();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		List<BankSubCommand> subCommands = plugin.getBankCommand().getSubCommands().stream()
				.map(cmd -> (BankSubCommand) cmd).collect(Collectors.toList());
		
		BankSubCommand subCommand = null;

		for (BankSubCommand bankSubCommand : subCommands)
			if (bankSubCommand.getName().equalsIgnoreCase(args[0])) {
				subCommand = bankSubCommand;
				break;
			}

		if (subCommand == null) {
			plugin.getLogger().severe("Null command!");
			plugin.debug("Null command! Sender: " + sender.getName() + ", command: " + command.getName() + " "
					+ Arrays.stream(args).collect(Collectors.joining(" ")));
			return false;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;

			switch (subCommand.getName().toLowerCase()) {

			case "create":
				if (!promptBankCreate(p, args))
					p.sendMessage(subCommand.getHelpMessage(sender));
				return true;
			case "remove":
				promptBankRemove(p, args);
				return true;
			case "info":
				if (!promptBankInfo(p, args))
					p.sendMessage(subCommand.getHelpMessage(sender));
				return true;
			case "list":
				promptBankList(p, args);
				return true;
			case "limits":
				promptBankLimit(p);
				return true;
			case "removeall":
				if (!promptBankRemoveAll(p, args))
					p.sendMessage(subCommand.getHelpMessage(sender));
				return true;
			case "resize":
				if (!promptBankResize(p, args))
					p.sendMessage(subCommand.getHelpMessage(sender));
				return true;
			case "set":
				if (!promptBankSet(p, args))
					p.sendMessage(subCommand.getHelpMessage(sender));
				return true;
			default:
				return false;
			}
		} else {

			switch (subCommand.getName().toLowerCase()) {
			case "remove":
				promptBankRemove(sender, args);
				return true;
			case "info":
				if (!promptBankInfo(sender, args))
					sender.sendMessage(subCommand.getHelpMessage(sender));
				return true;
			case "list":
				promptBankList(sender, args);
				return true;
			case "removeall":
				return promptBankRemoveAll(sender, args);
			default:
				return false;
			}
		}
	}

	private boolean promptBankCreate(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to create a bank");

		if (!p.hasPermission(Permissions.BANK_CREATE)) {
			plugin.debug(p.getName() + " does not have permission to create a bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_CREATE);
			return true;
		}

		Selection selection;
		boolean isAdminBank = false;

		if (args.length == 1)
			return false;

		else if (args.length == 2 || args.length == 3) {
			
			if (Config.enableWorldEditIntegration && plugin.hasWorldEdit()) {

				selection = WorldEditReader.getSelection(plugin, p);

				if (selection == null) {
					plugin.debug(p.getName() + " tried to create a bank with no worldedit selection");
					p.sendMessage(Messages.NO_SELECTION_FOUND);
					return true;
				}
			} else {
				plugin.debug("WorldEdit is not enabled");
				p.sendMessage(Messages.WORLDEDIT_NOT_ENABLED);
				return true;
			}

		} else {
			try {
				selection = bankUtils.parseCoordinates(args, p.getLocation());
			} catch (NumberFormatException e) {
				plugin.debug("Could not parse coordinates in command args");
				p.sendMessage(Messages.COORDINATES_PARSE_ERROR);
				return false;
			}
		}

		if (selection == null)
			return false;

		if ((args.length == 3 && args[2].equalsIgnoreCase("admin"))
				|| (args.length == 6 && args[5].equalsIgnoreCase("admin"))
				|| (args.length == 9 && args[8].equalsIgnoreCase("admin")))
			isAdminBank = true;

		if (isAdminBank && !p.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
			plugin.debug(p.getName() + " does not have permission to create an admin bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_ADMIN_CREATE);
			return true;
		}
		if (!bankUtils.isExclusiveSelection(selection)) {
			plugin.debug("Region is not exclusive");
			p.sendMessage(Messages.SELECTION_NOT_EXCLUSIVE);
			return true;
		}
		if (!bankUtils.isUniqueName(args[1])) {
			plugin.debug("Name is not unique");
			p.sendMessage(Messages.NAME_NOT_UNIQUE);
			return true;
		}
		if (!bankUtils.isAllowedName(args[1])) {
			plugin.debug("Name is not allowed");
			p.sendMessage(Messages.NAME_NOT_ALLOWED);
			return true;
		}

		Bank bank;
		if (isAdminBank)
			bank = new Bank(plugin, args[1], selection);
		else
			bank = new Bank(plugin, args[1], p, null, selection);

		BankCreateEvent event = new BankCreateEvent(p, bank);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Bank create event cancelled");
			return true;
		}

		double creationPrice = isAdminBank ? Config.creationPriceBank.getKey() : Config.creationPriceBank.getValue();
		if (creationPrice > 0 && !isAdminBank) {
			if (plugin.getEconomy().getBalance(p) < creationPrice) {
				plugin.debug(p.getName() + " does not have enough money to create a bank");
				p.sendMessage(Messages.BANK_CREATE_INSUFFICIENT_FUNDS);
				return true;
			}
			OfflinePlayer player = p.getPlayer();
			EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, p.getLocation().getWorld().getName(),
					creationPrice);
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				p.sendMessage(Messages.ERROR_OCCURRED);
				return true;
			} else
				p.sendMessage(String.format(Messages.BANK_CREATE_FEE_PAID,
						BigDecimal.valueOf(r.amount).setScale(2, RoundingMode.HALF_EVEN)));
		}

		if (bank.create(true)) {
			bankUtils.addBank(bank, true);
			plugin.debug(p.getName() + " has created a new " + (bank.isAdminBank() ? "admin " : "") + "bank.");
			p.sendMessage(Messages.BANK_CREATED);
			return true;
		} else {
			plugin.debug("An error occured creating the bank");
			p.sendMessage(Messages.ERROR_OCCURRED);
			return true;
		}
	}

	/**
	 * A given player removes a bank
	 * 
	 * @param sender The command executor
	 */
	private void promptBankRemove(final CommandSender sender, String[] args) { // XXX
		plugin.debug(sender.getName() + " wants to remove a bank");

		Bank bank;
		if (args.length == 1) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				bank = bankUtils.getBank(p.getLocation());
				if (bank == null) {
					plugin.debug(p.getName() + " wasn't standing in a bank");
					p.sendMessage(Messages.NOT_STANDING_IN_BANK);
					return;
				}
			} else {
				sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
				return;
			}
		} else {
			bank = bankUtils.lookupBank(args[1]);
			if (bank == null) {
				plugin.debug("No bank could be found under the identifier " + args[1]);
				sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				return;
			}
		}

		if (!bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_REMOVE_OTHER) && sender instanceof Player
				&& !bank.isOwner((Player) sender)) {
			plugin.debug(sender.getName() + " does not have permission to remove another player's bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_OTHER_REMOVE);
			return;
		}

		if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)) {
			plugin.debug(sender.getName() + " does not have permission to remove an admin bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_ADMIN_REMOVE);
			return;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (Config.confirmOnRemove)
				if (!commandConfirmed(p, args)) {
					p.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_BANKS, 1, "", bank.getAccounts().size(), bank.getAccounts().size() == 1 ? "" : "s")
							+ Messages.EXECUTE_AGAIN_TO_CONFIRM);
					return;
				}
		}

		BankRemoveEvent event = new BankRemoveEvent(sender, bank);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Bank remove event cancelled");
			return;
		}

		if (sender instanceof Player) {
			Player executor = (Player) sender;
			double creationPrice = bank.isAdminBank() ? Config.creationPriceBank.getKey()
					: Config.creationPriceBank.getValue();
			boolean reimburse = bank.isAdminBank() ? Config.reimburseBankCreation.getKey()
					: Config.reimburseBankCreation.getValue();
			if (creationPrice > 0 && reimburse && (bank.isAdminBank() || bank.isOwner(executor))) {
				OfflinePlayer owner = executor.getPlayer();
				EconomyResponse r = plugin.getEconomy().depositPlayer(owner, bank.getSelection().getWorld().getName(),
						creationPrice);

				if (!r.transactionSuccess()) {
					plugin.debug("Economy transaction failed: " + r.errorMessage);
					executor.sendMessage(Messages.ERROR_OCCURRED);
				} else {
					executor.sendMessage(String.format(Messages.PLAYER_REIMBURSED,
							BigDecimal.valueOf(r.amount).setScale(2, RoundingMode.HALF_EVEN)).toString());
				}
			}
		}

		bankUtils.removeBank(bank, true);
		plugin.debug("Bank #" + bank.getID() + " removed from the database");
		sender.sendMessage(Messages.BANK_REMOVED);
	}

	private boolean promptBankInfo(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " wants to show bank info");

		if (args.length == 1) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				Bank bank = bankUtils.getBank(p.getLocation());
				if (bank != null)
					p.spigot().sendMessage(bank.toText());
				else {
					plugin.debug(p.getName() + " wasn't standing in a bank");
					p.sendMessage(Messages.NOT_STANDING_IN_BANK);
				}
			} else
				sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
		} else if (args.length == 2) {
			if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					Bank bank = bankUtils.getBank(p.getLocation());
					if (bank != null)
						p.spigot().sendMessage(bank.toStringVerbose());
					else {
						plugin.debug(p.getName() + " wasn't standing in a bank");
						p.sendMessage(Messages.NOT_STANDING_IN_BANK);
					}
				} else
					sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
			} else {
				Bank bank = bankUtils.lookupBank(args[1]);
				if (bank != null)
					sender.sendMessage(bank.toString());
				else {
					plugin.debug("No bank could be found under the identifier " + args[1]);
					sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				}
			}
		} else if (args.length == 3) {
			if (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed")) {
				Bank bank = bankUtils.lookupBank(args[1]);
				if (bank != null)
					sender.spigot().sendMessage(bank.toStringVerbose());
				else {
					plugin.debug("No bank could be found under the identifier " + args[1]);
					sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				}
			}
		} else
			return false;

		return true;
	}

	private void promptBankList(CommandSender sender, String[] args) {

		if (args.length == 1) {
			if (bankUtils.getBanksCopy().isEmpty()) {
				sender.sendMessage(Messages.NO_BANKS);
			} else {
				sender.sendMessage("");
				int i = 1;
				for (Bank bank : bankUtils.getBanksCopy())
					sender.sendMessage(ChatColor.GOLD + "" + i++ + ": " + bank.toString());
				sender.sendMessage("");
			}
		} else if (args.length >= 2) {
			if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed")) {
				if (sender.hasPermission(Permissions.BANK_LIST_VERBOSE)) {
					if (bankUtils.getBanksCopy().isEmpty()) {
						sender.sendMessage(Messages.NO_BANKS);
					} else {
						sender.sendMessage("");
						for (Bank bank : bankUtils.getBanksCopy())
							sender.spigot().sendMessage(bank.toStringVerbose());
						sender.sendMessage("");
					}
				} else
					sender.sendMessage(Messages.NO_PERMISSION_BANK_LIST_VERBOSE);
			}
		}
	}

	private void promptBankLimit(final Player p) {
		int used = bankUtils.getNumberOfBanks(p);
		Object limit = bankUtils.getBankLimit(p) < 0 ? "∞" : bankUtils.getBankLimit(p);
		plugin.debug(p.getName() + " is viewing their bank limits: " + used + " / " + limit);
		p.sendMessage(String.format(Messages.BANK_LIMIT, used, limit));
	}

	private boolean promptBankRemoveAll(CommandSender sender, String[] args) { // XXX
		plugin.debug(sender.getName() + " wants to remove all banks");
		if (args.length > 1)
			return false;

		if (!sender.hasPermission(Permissions.BANK_REMOVEALL)) {
			plugin.debug(sender.getName() + " does not have permission to remove all banks");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_REMOVEALL);
			return true;
		}

		Collection<Bank> banks = bankUtils.getBanksCopy();
		Collection<Account> accounts = banks.stream().flatMap(bank -> bank.getAccounts().stream())
				.collect(Collectors.toSet());

		BankRemoveAllEvent event = new BankRemoveAllEvent(sender, banks);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Bank remove all event cancelled");
			return true;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (Config.confirmOnRemoveAll)
				if (!commandConfirmed(p, args)) {
					p.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_BANKS, banks.size(),
							banks.size() == 1 ? "" : "s", accounts.size(), accounts.size() == 1 ? "" : "s"));
					p.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
					return true;
				}
		}

		bankUtils.removeBank(banks, true);
		plugin.debug("Bank #s " + banks.stream().map(bank -> "" + bank.getID()).collect(Collectors.joining(", ", "", ""))
				+ " removed from the database.");
		sender.sendMessage(String.format(Messages.BANKS_REMOVED, banks.size(), banks.size() == 1 ? "" : "s",
				accounts.size(), accounts.size() == 1 ? "" : "s"));

		return true;
	}

	private boolean promptBankResize(Player p, String[] args) {
		plugin.debug(p.getName() + " wants to resize a bank");

		if (!p.hasPermission(Permissions.BANK_RESIZE)) {
			plugin.debug(p.getName() + " does not have permission to resize a bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_RESIZE);
			return true;
		}

		Bank bank;
		Selection selection;

		if (args.length == 1)
			return false;

		else if (args.length == 2) {

			if (Config.enableWorldEditIntegration && plugin.hasWorldEdit()) {

				selection = WorldEditReader.getSelection(plugin, p);

				if (selection == null) {
					plugin.debug(p.getName() + " tried to resize a bank with no worldedit selection");
					p.sendMessage(Messages.NO_SELECTION_FOUND);
					return true;
				}
			} else {
				plugin.debug("WorldEdit is not enabled");
				p.sendMessage(Messages.WORLDEDIT_NOT_ENABLED);
				return true;
			}

		} else {
			try {
				selection = bankUtils.parseCoordinates(args, p.getLocation());
			} catch (NumberFormatException e) {
				plugin.debug("Could not parse coordinates in command args");
				p.sendMessage(Messages.COORDINATES_PARSE_ERROR);
				return false;
			}
		}

		if (selection == null)
			return false;

		bank = bankUtils.lookupBank(args[1]);
		if (bank == null) {
			plugin.debug("No bank could be found under the identifier " + args[1]);
			p.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
			return true;
		}
		if (!bankUtils.isExclusiveSelectionWithoutThis(selection, bank)) {
			plugin.debug("New selection is not exclusive");
			p.sendMessage(Messages.SELECTION_NOT_EXCLUSIVE);
			return true;
		}
		if (!bankUtils.containsAllAccounts(bank, selection)) {
			plugin.debug("New selection does not contain all accounts");
			p.sendMessage(Messages.SELECTION_CUTS_ACCOUNTS);
			return true;
		}

		BankResizeEvent event = new BankResizeEvent(p, bank, selection);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Bank resize event cancelled");
			return true;
		}

		bankUtils.resizeBank(bank, selection);
		bankUtils.addBank(bank, true, new Callback<Integer>(plugin) {
			@Override
			public void onResult(Integer result) {
				plugin.debug(p.getName() + " has resized bank \"" + bank.getName() + "\" (#" + bank.getID() + ")");
				p.sendMessage(Messages.BANK_RESIZED);
			}

			@Override
			public void onError(Throwable e) {
				plugin.debug(e);
				p.sendMessage(Messages.ERROR_OCCURRED);
			}
		});

		return true;
	}

	private boolean promptBankSet(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " wants to configure a bank");

		if (args.length < 4)
			return false;

		Bank bank = bankUtils.lookupBank(args[1]);
		if (bank == null) {
			plugin.debug("No bank could be found under the identifier " + args[1]);
			sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
			return true;
		}

		if ((sender instanceof Player && !bank.isTrusted((Player) sender))
				&& !sender.hasPermission(Permissions.BANK_SET_OTHER)) {
			plugin.debug(sender.getName() + " does not have permission to configure a bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_OTHER_SET);
			return true;
		}

		if (Field.getByName(args[2]) == null) {
			plugin.debug("No account config field could be found with name " + args[2]);
			sender.sendMessage(String.format(Messages.NOT_A_FIELD, args[2]));
			return true;
		}

		AccountConfig config = bank.getAccountConfig();
		
		Field field = Field.getByName(args[2]);
		if (field == null) {
			sender.sendMessage(Messages.NOT_A_FIELD);
			return true;
		}
		
		if (field == Field.MULTIPLIERS) {
			StringBuilder sb = new StringBuilder(args[3]);
			for (int i = 4; i < args.length; i++)
				sb.append(" " + args[i]);
			args[3] = sb.toString();
		}
		
		try {
			if (config.setOrDefault(field, args[3])) {
				if (field.getDataType() == 0)
					args[3] = Utils.formatNumber(Double.parseDouble(args[3].replace(",", "")));
				else if (field.getDataType() == 3)
					args[3] = Utils.listifyList(args[3]);
				sender.sendMessage(String.format(Messages.BANK_FIELD_SET, args[2], args[3], bank.getName()));
			} else
				sender.sendMessage(Messages.FIELD_NOT_OVERRIDABLE);
		} catch (NumberFormatException e) {
			switch (field.getDataType()) {
			case 0:
				plugin.debug("Failed to parse double: " + args[3]);
				sender.sendMessage(String.format(Messages.NOT_A_NUMBER, args[3]));
				break;
			case 1:
				plugin.debug("Failed to parse integer: " + args[3]);
				sender.sendMessage(String.format(Messages.NOT_AN_INTEGER, args[3]));
				break;
			case 2:
				plugin.debug("Failed to parse integer: " + args[3]);
				sender.sendMessage(String.format(Messages.NOT_AN_INTEGER, args[3]));
				break;
			case 3:
				plugin.debug("Failed to parse list: " + args[3]);
				sender.sendMessage(String.format(Messages.NOT_A_LIST, args[3]));
				break;
			}
			return true;
		}

		plugin.getDatabase().addBank(bank, null);
		plugin.debug(sender.getName() + " has set " + args[2] + " at " + bank.getName() + " to " + args[3]);
		return true;
	}

}