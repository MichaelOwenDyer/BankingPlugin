package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.Confirmable;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.*;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.gui.BankGui;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.selections.Selection.SelectionType;
import com.monst.bankingplugin.utils.*;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BankCommandExecutor implements CommandExecutor, Confirmable {

	private final BankingPlugin plugin;
	private final BankUtils bankUtils;
	
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
					+ String.join(" ", args));
			return false;
		}

		switch (subCommand.getName().toLowerCase()) {

		case "create":
			if (!promptBankCreate((Player) sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "remove":
			promptBankRemove(sender, args);
			break;
		case "info":
			promptBankInfo(sender, args);
			break;
		case "list":
			promptBankList(sender, args);
			break;
		case "limits":
			promptBankLimits((Player) sender);
			break;
		case "removeall":
			if (!promptBankRemoveAll(sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "resize":
			if (!promptBankResize((Player) sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "rename":
			if (!promptBankRename(sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "set":
			if (!promptBankSet(sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "select":
			promptBankSelect((Player) sender, args);
			break;
		case "transfer":
			if (!promptBankTransfer(sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		default:
			return false;
		}
		return true;
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

		if (args.length == 1 || args.length == 2) {
			if (Config.enableWorldEditIntegration && plugin.hasWorldEdit()) {
				selection = WorldEditReader.getSelection(plugin, p);
				if (selection == null) {
					plugin.debug(p.getName() + " tried to create a bank with no WorldEdit selection");
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

		if (Config.disabledWorlds.contains(selection.getWorld().getName())) {
			plugin.debug("BankingPlugin is disabled in world " + selection.getWorld().getName());
			p.sendMessage(Messages.WORLD_DISABLED);
			return true;
		}

		if ((args.length == 2 || args.length == 5 || args.length == 8)
				&& args[args.length - 1].equalsIgnoreCase("admin"))
			isAdminBank = true;

		if (isAdminBank && !p.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
			plugin.debug(p.getName() + " does not have permission to create an admin bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_CREATE_ADMIN);
			return true;
		}
		int limit = bankUtils.getBankLimit(p);
		if (!isAdminBank && limit != -1 && bankUtils.getNumberOfBanks(p) >= limit) {
			p.sendMessage(Messages.BANK_LIMIT_REACHED);
			plugin.debug(p.getName() + " has reached their bank limit");
			return true;
		}
		if (!bankUtils.isExclusiveSelection(selection)) {
			plugin.debug("Region is not exclusive");
			p.sendMessage(Messages.SELECTION_OVERLAPS_EXISTING);
			return true;
		}
		int volume = selection.getVolume();
		int volumeLimit = bankUtils.getVolumeLimit(p);
		if (!isAdminBank && volumeLimit != -1 && volume > volumeLimit) {
			plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
			p.sendMessage(String.format(Messages.SELECTION_TOO_LARGE, Utils.format(volumeLimit), Utils.format(volume - volumeLimit)));
			return true;
		}
		if (!isAdminBank && volume < Config.minimumBankVolume) {
			plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume + ")");
			p.sendMessage(String.format(Messages.SELECTION_TOO_SMALL, Utils.format(Config.minimumBankVolume), Utils.format(Config.minimumBankVolume - volume)));
			return true;
		}

		Bank bank;
		if (isAdminBank)
			bank = new Bank(plugin, selection);
		else
			bank = new Bank(plugin, p, null, selection);

		BankCreateEvent event = new BankCreateEvent(p, bank);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Bank create event cancelled");
			return true;
		}

		double creationPrice = isAdminBank ? Config.bankCreationPrice.getKey() : Config.bankCreationPrice.getValue();
		if (creationPrice > 0 && plugin.getEconomy().getBalance(p) < creationPrice) {
			plugin.debug(p.getName() + " does not have enough money to create a bank");
			p.sendMessage(Messages.BANK_CREATE_INSUFFICIENT_FUNDS);
			return true;
		}

		String worldName = p.getLocation().getWorld() != null ? p.getLocation().getWorld().getName() : "World";
		if (!Utils.withdrawPlayer(p.getPlayer(), worldName, creationPrice, new Callback<Void>(plugin) {
			@Override
			public void onResult(Void result) {
				p.sendMessage(String.format(Messages.BANK_CREATE_FEE_PAID, Utils.format(creationPrice)));
			}
			@Override
			public void onError(Throwable throwable) {
				plugin.debug(throwable);
				p.sendMessage(Messages.ERROR_OCCURRED);
			}
		}))
			return true;

		bankUtils.addBank(bank, true);
		plugin.debug(p.getName() + " has created a new " + (bank.isAdminBank() ? "admin " : "") + "bank.");
		p.sendMessage(Messages.BANK_CREATED);
		return true;
	}

	/**
	 * A given player removes a bank
	 * 
	 * @param sender The command executor
	 */
	private void promptBankRemove(final CommandSender sender, String[] args) {
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

		if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
				|| sender.hasPermission(Permissions.BANK_REMOVE_OTHER))) {
			if (sender instanceof Player && bank.isTrusted(((Player) sender))) {
				plugin.debug(sender.getName() + " does not have permission to remove another player's bank as a co-owner");
				sender.sendMessage(Messages.MUST_BE_OWNER);
				return;
			}
			plugin.debug(sender.getName() + " does not have permission to remove another player's bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_REMOVE_OTHER);
			return;
		}

		if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)) {
			plugin.debug(sender.getName() + " does not have permission to remove an admin bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_REMOVE_ADMIN);
			return;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (Config.confirmOnRemove)
				if (needsConfirmation(p, args)) {
					p.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_BANKS, 1, "", bank.getAccounts().size(),
							bank.getAccounts().size() == 1 ? "" : "s"));
					p.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
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
			double creationPrice = bank.isAdminBank() ? Config.bankCreationPrice.getKey() : Config.bankCreationPrice.getValue();
			boolean reimburse = bank.isAdminBank() ? Config.reimburseBankCreation.getKey() : Config.reimburseBankCreation.getValue();
			creationPrice *= reimburse ? 1 : 0;

			Player executor = (Player) sender;
			if (creationPrice > 0 && (bank.isAdminBank() || bank.isOwner(executor))) {
				double finalCreationPrice = creationPrice;
				Utils.depositPlayer(executor.getPlayer(), bank.getSelection().getWorld().getName(), finalCreationPrice, new Callback<Void>(plugin) {
					@Override
					public void onResult(Void result) {
						executor.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED,
								Utils.format(finalCreationPrice)));
					}
					@Override
					public void onError(Throwable throwable) {
						plugin.debug(throwable);
						executor.sendMessage(Messages.ERROR_OCCURRED);
					}
				});
			}
		}

		bankUtils.removeBank(bank, true);
		plugin.debug("Bank #" + bank.getID() + " removed from the database");
		sender.sendMessage(Messages.BANK_REMOVED);
		Utils.notifyPlayers(
				String.format(Messages.PLAYER_REMOVED_BANK, sender.getName(), bank.getName()), sender,
				bank.getTrustedPlayers(), bank.getCustomers());
	}

	private void promptBankInfo(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " wants to show bank info");

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
				plugin.debug("No bank could be found under the identifier \"" + args[1] + "\"");
				sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, "\"" + args[1] + "\""));
				return;
			}
		}

		plugin.debug(sender.getName() + " is displaying bank info");
		if (sender instanceof Player)
			new BankGui(bank).open((Player) sender);
		else
			sender.spigot().sendMessage(bank.getInformation(sender));
	}

	@SuppressWarnings("unused")
	private void promptBankList(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " is listing banks.");

		List<Bank> banks;

		// TODO: Allow for more specific bank searching

		banks = bankUtils.getBanksCopy().stream().sorted(Comparator.comparing(Bank::getTotalValue)).collect(Collectors.toList());

		if (banks.isEmpty()) {
			sender.sendMessage(Messages.NO_BANKS_FOUND);
			return;
		}

		int i = 0;
		for (Bank bank : bankUtils.getBanksCopy())
			sender.spigot().sendMessage(new TextComponent(ChatColor.GOLD + "" + ++i + ". "),
					new TextComponent(bank.getColorizedName() + " "),
					bank.getInfoButton(sender));
	}

	private void promptBankLimits(final Player p) {
		int banksUsed = bankUtils.getNumberOfBanks(p);
		Object bankLimit = bankUtils.getBankLimit(p) < 0 ? "∞" : bankUtils.getBankLimit(p);
		plugin.debug(p.getName() + " is viewing their bank limits: " + banksUsed + " / " + bankLimit);
		p.sendMessage(String.format(Messages.BANK_LIMIT, banksUsed, bankLimit));
	}

	private boolean promptBankRemoveAll(CommandSender sender, String[] args) {
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
				if (needsConfirmation(p, args)) {
					p.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_BANKS, banks.size(),
							banks.size() == 1 ? "" : "s", accounts.size(), accounts.size() == 1 ? "" : "s"));
					p.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
					return true;
				}
		}

		bankUtils.removeBank(banks, true);
		plugin.debug("Bank(s) " + banks.stream().map(bank -> "#" + bank.getID())
				.collect(Collectors.joining(", ", "", ""))
				+ " removed from the database.");
		sender.sendMessage(String.format(Messages.BANKS_REMOVED, banks.size(), banks.size() == 1 ? "" : "s", accounts.size(), accounts.size() == 1 ? "" : "s"));
		for (Bank bank : banks)
			Utils.notifyPlayers(String.format(Messages.PLAYER_REMOVED_BANK, sender.getName(), bank.getColorizedName()), sender, bank.getTrustedPlayers());

		return true;
	}

	private boolean promptBankResize(Player p, String[] args) {
		plugin.debug(p.getName() + " wants to resize a bank");

		if (!p.hasPermission(Permissions.BANK_CREATE)) {
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
					plugin.debug(p.getName() + " tried to resize a bank with no WorldEdit selection");
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
				selection = bankUtils.parseCoordinates(args, p.getLocation(), 1);
			} catch (NumberFormatException e) {
				plugin.debug("Could not parse coordinates in command args: \"" + Arrays.toString(args) + "\"");
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
		if (bank.isPlayerBank() && !bank.isOwner(p) && !p.hasPermission(Permissions.BANK_RESIZE_OTHER)) {
			plugin.debug(p.getName() + " does not have permission to resize another player's bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_RESIZE_OTHER);
			return true;
		}
		if (bank.isAdminBank() && !p.hasPermission(Permissions.BANK_RESIZE_ADMIN)) {
			plugin.debug(p.getName() + " does not have permission to resize an admin bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_RESIZE_ADMIN);
			return true;
		}
		if (Config.disabledWorlds.contains(selection.getWorld().getName())) {
			plugin.debug("BankingPlugin is disabled in world " + selection.getWorld().getName());
			p.sendMessage(Messages.WORLD_DISABLED);
			return true;
		}
		int volume = selection.getVolume();
		int volumeLimit = bankUtils.getVolumeLimit(p);
		if (bank.isPlayerBank() && volumeLimit != -1 && volume > volumeLimit) {
			plugin.debug("Bank is too large (" + volume + " blocks, limit: " + volumeLimit + ")");
			p.sendMessage(String.format(Messages.SELECTION_TOO_LARGE_RESIZE, volumeLimit, volume - volumeLimit));
			return true;
		}
		if (bank.isPlayerBank() && volume < Config.minimumBankVolume) {
			plugin.debug("Bank is too small (" + volume + " blocks, minimum: " + Config.minimumBankVolume + ")");
			p.sendMessage(String.format(Messages.SELECTION_TOO_SMALL_RESIZE, Config.minimumBankVolume, Config.minimumBankVolume - volume));
			return true;
		}
		if (!bankUtils.isExclusiveSelectionWithoutThis(selection, bank)) {
			plugin.debug("New selection is overlaps with an existing bank selection");
			p.sendMessage(Messages.SELECTION_OVERLAPS_EXISTING);
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

	private boolean promptBankRename(CommandSender sender, String[] args) { // TODO: Bug here where name is not changed
		plugin.debug(sender.getName() + " is renaming a bank");

		if (args.length < 2)
			return false;

		Bank bank;
		StringBuilder sb;
		String newName;
		if (args.length == 2) {
			if (!(sender instanceof Player)) {
				plugin.debug("Must be player");
				sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
				return true;
			}
			bank = bankUtils.getBank(((Player) sender).getLocation());
			if (bank == null) {
				plugin.debug(sender.getName() + " was not standing in a bank");
				sender.sendMessage(Messages.NOT_STANDING_IN_BANK);
				return true;
			}
			newName = args[1];
		} else {
			bank = bankUtils.lookupBank(args[1]);
			if (bank == null) {
				plugin.debug("Could not find bank with name " + args[1]);
				sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				return true;
			}
			sb = new StringBuilder(args[2]);
			for (int i = 3; i < args.length; i++)
				sb.append(" ").append(args[i]);
			newName = sb.toString();
		}

		if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
			plugin.debug(sender.getName() + " does not have permission to change the name of an admin bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_ADMIN);
			return true;
		}
		if (!(bank.isAdminBank() || (sender instanceof Player && bank.isTrusted((Player) sender))
				|| sender.hasPermission(Permissions.BANK_SET_OTHER))) {
			plugin.debug(sender.getName() + " does not have permission to change the name of another player's bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_ADMIN);
			return true;
		}

		if (bank.getName().contentEquals(newName)) {
			plugin.debug("Same name");
			sender.sendMessage(Messages.NAME_ALREADY);
			return true;
		}
		if (!bankUtils.isUniqueName(newName)) {
			plugin.debug("Name is not unique");
			sender.sendMessage(Messages.NAME_NOT_UNIQUE);
			return true;
		}
		if (!Utils.isAllowedName(newName)) {
			plugin.debug("Name is not allowed");
			sender.sendMessage(Messages.NAME_NOT_ALLOWED);
			return true;
		}

		plugin.debug(sender.getName() + " is changing the name of bank " + bank.getName() + " to " + newName);
		sender.sendMessage(Messages.NAME_CHANGED);
		bank.setName(newName);
		return true;
	}

	private boolean promptBankSet(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " wants to configure a bank");

		if (args.length < 3)
			return false;

		if (args.length == 3 && !(sender instanceof Player)) {
			plugin.debug("Player command only");
			sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
			return true;
		}

		Bank bank;
		boolean standingInBank = true;
		String fieldName;
		String value;

		if (args.length == 3) {
			bank = bankUtils.getBank(((Player) sender).getLocation());
			fieldName = args[1];
			value = args[2];
		} else {
			if (args[1].equalsIgnoreCase("multipliers")) {
				bank = bankUtils.getBank(((Player) sender).getLocation());
				fieldName = args[1];
				StringBuilder sb = new StringBuilder(args[2]);
				for (int i = 3; i < args.length; i++)
					sb.append(" ").append(args[i]);
				value = sb.toString();
			} else if (args[2].equalsIgnoreCase("multipliers")) {
				bank = bankUtils.lookupBank(args[1]);
				fieldName = args[2];
				StringBuilder sb = new StringBuilder(args[3]);
				for (int i = 4; i < args.length; i++)
					sb.append(" ").append(args[i]);
				value = sb.toString();
			} else {
				bank = bankUtils.lookupBank(args[1]);
				standingInBank = false;
				fieldName = args[2];
				value = args[3];
			}
		}

		if (bank == null) {
			if (standingInBank) {
				plugin.debug(sender.getName() + " was not standing in a bank");
				sender.sendMessage(Messages.NOT_STANDING_IN_BANK);
				return true;
			}
			plugin.debug("No bank could be found under the identifier " + args[1]);
			sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
			return true;
		}

		if (bank.isPlayerBank() && !((sender instanceof Player && bank.isTrusted((Player) sender))
				|| sender.hasPermission(Permissions.BANK_SET_OTHER))) {
			plugin.debug(sender.getName() + " does not have permission to configure another player's bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_OTHER);
			return true;
		}

		if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
			plugin.debug(sender.getName() + " does not have permission to configure an admin bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_ADMIN);
			return true;
		}

		AccountConfig config = bank.getAccountConfig();
		
		Field field = Field.getByName(fieldName);
		if (field == null) {
			plugin.debug("No account config field could be found with name " + fieldName);
			sender.sendMessage(String.format(Messages.NOT_A_FIELD, fieldName));
			return true;
		}

		String previousValue = "" + bank.getAccountConfig().get(field);
		Bank finalBank = bank;
		Callback<String> callback = new Callback<String>(plugin) {
			@Override
			public void onResult(String result) {
				plugin.debug(sender.getName() + " has changed " + field.getName() + " at " + finalBank.getName() + " from " + previousValue + " to " + result);
				sender.sendMessage(String.format(Messages.BANK_FIELD_SET, "You", field.getName(), previousValue, result, finalBank.getColorizedName()));
				Utils.notifyPlayers(String.format(Messages.BANK_FIELD_SET, sender.getName(), field.getName(), previousValue, result, finalBank.getColorizedName()),
						sender, bank.getTrustedPlayers(), bank.getCustomers());
			}
			@Override
			public void onError(Throwable throwable) {
				plugin.debug(((ArgumentParseException) throwable).getErrorMessage());
				sender.sendMessage(((ArgumentParseException) throwable).getErrorMessage());
			}
		};
		if (!config.set(field, value, callback))
			sender.sendMessage(Messages.FIELD_NOT_OVERRIDABLE);

		bankUtils.addBank(bank, true);
		return true;
	}

	private void promptBankSelect(Player p, String[] args) {
		plugin.debug(p.getName() + " wants to select a bank");

		if (!plugin.hasWorldEdit()) {
			plugin.debug("WorldEdit is not enabled");
			p.sendMessage(Messages.WORLDEDIT_NOT_ENABLED);
			return;
		}

		if (!p.hasPermission(Permissions.BANK_SELECT)) {
			plugin.debug(p.getName() + " does not have permission to select a bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_SELECT);
			return;
		}

		Bank bank;
		if (args.length == 1) {
			bank = bankUtils.getBank(p.getLocation());
			if (bank == null) {
				plugin.debug(p.getName() + " wasn't standing in a bank");
				p.sendMessage(Messages.NOT_STANDING_IN_BANK);
				return;
			}
		} else {
			bank = bankUtils.lookupBank(args[1]);
			if (bank == null) {
				plugin.debug("No bank could be found under the identifier " + args[1]);
				p.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				return;
			}
		}

		WorldEditReader.setSelection(plugin, bank.getSelection(), p);
		plugin.debug(p.getName() + " has selected a bank");
		p.sendMessage(String.format(Messages.BANK_SELECTED,
				bank.getSelection().getType() == SelectionType.CUBOID ? "cuboid" : "polygon"));

	}

	@SuppressWarnings("deprecation")
	private boolean promptBankTransfer(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " wants to transfer bank ownership");

		if (!sender.hasPermission(Permissions.BANK_TRANSFER)) {
			plugin.debug(sender.getName() + " does not have permission to transfer bank ownership");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER);
			return true;
		}

		if (args.length < 2)
			return false;

		Bank bank;
		OfflinePlayer newOwner = null;
		if (args.length == 2) {
			if (!(sender instanceof Player)) {
				plugin.debug("Must be player");
				sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
				return true;
			}
			bank = bankUtils.getBank(((Player) sender).getLocation());
			if (bank == null) {
				plugin.debug(sender.getName() + " wasn't standing in a bank");
				sender.sendMessage(Messages.NOT_STANDING_IN_BANK);
				return true;
			}
			if (!args[1].equalsIgnoreCase("admin")) {
				newOwner = Bukkit.getOfflinePlayer(args[1]);
				if (!newOwner.hasPlayedBefore()) {
					sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
					return true;
				}
			}
		} else {
			bank = bankUtils.lookupBank(args[1]);
			if (bank == null) {
				plugin.debug("No bank could be found under the identifier " + args[1]);
				sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				return true;
			}
			if (!args[2].equalsIgnoreCase("admin")) {
				newOwner = Bukkit.getOfflinePlayer(args[2]);
				if (!newOwner.hasPlayedBefore()) {
					sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[2]));
					return true;
				}
			}
		}

		if (sender instanceof Player && newOwner != null && bank.isOwner(newOwner)) {
			boolean isExecutor = Utils.samePlayer((Player) sender, newOwner);
			plugin.debug(newOwner.getName() + " is already owner of bank");
			sender.sendMessage(
					String.format(Messages.ALREADY_OWNER_BANK, isExecutor ? "You" : newOwner.getName(), isExecutor ? "are" : "is"));
			return true;
		}
		if (bank.isAdminBank() && newOwner == null) {
			plugin.debug("Bank is already an admin bank");
			sender.sendMessage(Messages.ALREADY_ADMIN_BANK);
			return true;
		}
		if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRANSFER_ADMIN)) {
			plugin.debug(sender.getName() + " does not have permission to transfer an admin bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER_ADMIN);
			return true;
		}
		if (newOwner == null && !sender.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
			plugin.debug(sender.getName() + " does not have permission to transfer a bank to the admins");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER_TO_ADMIN);
			return true;
		}
		if (!(bank.isAdminBank() || (sender instanceof Player && bank.isOwner((Player) sender))
				|| sender.hasPermission(Permissions.BANK_TRANSFER_OTHER))) {
			if (sender instanceof Player && bank.isTrusted((Player) sender)) {
				plugin.debug(sender.getName() + " does not have permission to transfer ownership as a co-owner");
				sender.sendMessage(Messages.MUST_BE_OWNER);
				return true;
			}
			plugin.debug(sender.getName() + " does not have permission to transfer ownership of another player's bank");
			sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER_OTHER);
			return true;
		}

		if (sender instanceof Player)
			if (Config.confirmOnTransfer && needsConfirmation((Player) sender, args)) {
				sender.sendMessage(String.format(Messages.ABOUT_TO_TRANSFER,
						(bank.isOwner((Player) sender)
							? "your bank"
							: bank.getOwnerDisplayName() + "'s bank"),
						newOwner.getName()));
				sender.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
				return true;
			}

		BankTransferEvent event = new BankTransferEvent(sender, bank, newOwner);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Bank transfer ownership event cancelled");
			return true;
		}

		sender.sendMessage(String.format(Messages.OWNERSHIP_TRANSFERRED, "You", bank.getColorizedName(), newOwner != null ? newOwner.getName() : "ADMIN"));
		Utils.notifyPlayers(
				String.format(Messages.OWNERSHIP_TRANSFERRED, sender.getName(), bank.getColorizedName(), newOwner != null ? newOwner.getName() : "ADMIN"),
				newOwner != null ? newOwner.getPlayer() : null, Collections.singleton(newOwner));
		if (newOwner != null && newOwner.isOnline())
			newOwner.getPlayer().sendMessage(String.format(Messages.OWNERSHIP_TRANSFER_RECEIVED, "bank", bank.getColorizedName()));
		boolean hasDefaultName = bank.isDefaultName();
		bank.transferOwnership(newOwner);
		if (hasDefaultName)
			bank.setToDefaultName();
		bankUtils.addBank(bank, true);
		return true;
	}
}