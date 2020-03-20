package com.monst.bankingplugin.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankCreateEvent;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.md_5.bungee.api.ChatColor;

public class BankCommandExecutor implements CommandExecutor, SchedulableCommand<Bank> {

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
					p.sendMessage(Messages.COMMAND_USAGE_BANK_CREATE);
				return true;
			case "remove":
				promptBankRemove(p, args);
				break;
			case "info":
				promptBankInfo(p, args);
				break;
			case "list":
				promptBankList(p, args);
				break;
			case "removeall":
				if (!promptBankRemoveAll(p, args));
					p.sendMessage(Messages.COMMAND_USAGE_ACCOUNT_REMOVEALL);
				return true;
			default:
				return false;
			}
		} else {

			switch (subCommand.getName().toLowerCase()) {
			case "remove":
				// promptBankRemove(sender, args);
				return false;
			case "info":
				// promptBankInfo(sender, args);
				return false;
			case "list":
				// promptBankList(sender, args);
				return false;
			case "removeall":
				return promptBankRemoveAll(sender, args);
			default:
				return false;
			}
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

		if (args.length == 1) {
			return false;
		}

		if (args.length == 2) {
			if (Config.enableWorldEditIntegration && plugin.hasWorldEdit()) {
				WorldEditPlugin worldEdit = plugin.getWorldEdit();
				Selection selection = worldEdit.getSelection(p);
				if (selection == null) {
					plugin.debug(p.getName() + " tried to create a bank with no worldedit selection");
					p.sendMessage(Messages.NO_SELECTION_FOUND);
					return true;
				}
				if (!bankUtils.isExclusiveSelection(selection)) {
					plugin.debug("Selection is not exclusive");
					p.sendMessage(Messages.SELECTION_NOT_EXCLUSIVE);
					return true;
				}
				if (!bankUtils.isUniqueName(args[1])) {
					plugin.debug("Name is not unique");
					p.sendMessage(Messages.NAME_NOT_UNIQUE);
					return true;
				}

				Bank bank = new Bank(plugin, args[1], selection);

				BankCreateEvent event = new BankCreateEvent(p, bank);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					plugin.debug("Account create event cancelled");
					return true;
				}

				if (bank.create(true)) {
					bankUtils.addBank(bank, true);
					plugin.debug(p.getName() + " has created a new bank.");
					p.sendMessage(Messages.BANK_CREATED);
					return true;
				} else {
					plugin.debug("An error occured creating the bank");
					p.sendMessage(Messages.ERROR_OCCURRED);
					return true;
				}
			} else {
				plugin.debug("WorldEdit is not enabled");
				p.sendMessage(Messages.WORLDEDIT_NOT_ENABLED);
				return true;
			}
		} else if (args.length == 5) {
			if (!bankUtils.isUniqueName(args[1])) {
				plugin.debug("Name is not unique");
				p.sendMessage(Messages.NAME_NOT_UNIQUE);
				return true;
			}

			String argX = args[2];
			String argY = args[3];
			String argZ = args[4];
			
			int x1, x2, y1, y2, z1, z2;
			try {
				x1 = argX.startsWith("~") ? Integer.parseInt(argX.substring(1, argX.length())) : Integer.parseInt(argX);
				y1 = argY.startsWith("~") ? Integer.parseInt(argY.substring(1, argY.length())) : Integer.parseInt(argY);
				z1 = argZ.startsWith("~") ? Integer.parseInt(argZ.substring(1, argZ.length())) : Integer.parseInt(argZ);
			} catch (NumberFormatException e) {
				plugin.debug("Could not parse coordinates in command args");
				p.sendMessage(Messages.COORDINATES_PARSE_ERROR);
				return false;
			}
			x2 = p.getLocation().getBlockX();
			y2 = p.getLocation().getBlockY();
			z2 = p.getLocation().getBlockZ();
			
			if (argX.startsWith("~"))
				x1 += x2;
			if (argY.startsWith("~"))
				y1 += y2;
			if (argZ.startsWith("~"))
				z1 += z2;
			
			Location loc1 = new Location(p.getWorld(), x1, y1, z1);
			Location loc2 = new Location(p.getWorld(), x2, y2, z2);
			Selection selection = new CuboidSelection(p.getWorld(), loc1, loc2);
			
			if (!bankUtils.isExclusiveSelection(selection)) {
				plugin.debug("Selection is not exclusive");
				p.sendMessage(Messages.SELECTION_NOT_EXCLUSIVE);
				return true;
			}
			Bank bank = new Bank(plugin, args[1], selection);
			if (bank.create(true)) {
				bankUtils.addBank(bank, true);
				plugin.debug(p.getName() + " has created a new bank.");
				p.sendMessage(Messages.BANK_CREATED);
				return true;
			} else {
				plugin.debug("An error occured creating the bank");
				p.sendMessage(Messages.ERROR_OCCURRED);
				return true;
			}
		} else if (args.length >= 8) {
			if (!bankUtils.isUniqueName(args[1])) {
				plugin.debug("Name is not unique");
				p.sendMessage(Messages.NAME_NOT_UNIQUE);
				return true;
			}

			String argX1 = args[2];
			String argY1 = args[3];
			String argZ1 = args[4];
			String argX2 = args[5];
			String argY2 = args[6];
			String argZ2 = args[7];
			
			int x1, y1, z1, x2, y2, z2;
			try {
				x1 = argX1.startsWith("~") ? Integer.parseInt(argX1.substring(1, argX1.length())) : Integer.parseInt(argX1);
				y1 = argY1.startsWith("~") ? Integer.parseInt(argY1.substring(1, argY1.length())) : Integer.parseInt(argY1);
				z1 = argZ1.startsWith("~") ? Integer.parseInt(argZ1.substring(1, argZ1.length())) : Integer.parseInt(argZ1);
				x2 = argX2.startsWith("~") ? Integer.parseInt(argX2.substring(1, argX2.length())) : Integer.parseInt(argX2);
				y2 = argY2.startsWith("~") ? Integer.parseInt(argY2.substring(1, argY2.length())) : Integer.parseInt(argY2);
				z2 = argZ2.startsWith("~") ? Integer.parseInt(argZ2.substring(1, argZ2.length())) : Integer.parseInt(argZ2);
			} catch (NumberFormatException e) {
				plugin.debug("Could not parse coordinates in command args");
				p.sendMessage(Messages.COORDINATES_PARSE_ERROR);
				return false;
			}
			if (argX1.startsWith("~"))
				x1 += p.getLocation().getBlockX();
			if (argY1.startsWith("~"))
				y1 += p.getLocation().getBlockY();
			if (argZ1.startsWith("~"))
				z1 += p.getLocation().getBlockZ();
			if (argX2.startsWith("~"))
				x2 += p.getLocation().getBlockX();
			if (argY2.startsWith("~"))
				y2 += p.getLocation().getBlockY();
			if (argZ2.startsWith("~"))
				z2 += p.getLocation().getBlockZ();

			Location loc1 = new Location(p.getWorld(), x1, y1, z1);
			Location loc2 = new Location(p.getWorld(), x2, y2, z2);
			Selection selection = new CuboidSelection(p.getWorld(), loc1, loc2);

			if (!bankUtils.isExclusiveSelection(selection)) {
				plugin.debug("Selection is not exclusive");
				p.sendMessage(Messages.SELECTION_NOT_EXCLUSIVE);
				return true;
			}
			Bank bank = new Bank(plugin, args[1], selection);

			BankCreateEvent event = new BankCreateEvent(p, bank);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				plugin.debug("Account create event cancelled");
				return true;
			}

			if (bank.create(true)) {
				bankUtils.addBank(bank, true);
				plugin.debug(p.getName() + " has created a new bank.");
				p.sendMessage(Messages.BANK_CREATED);
				return true;
			} else {
				plugin.debug("An error occured creating the bank");
				p.sendMessage(Messages.ERROR_OCCURRED);
				return true;
			}
		}
		return false;
	}

	/**
	 * A given player removes a bank
	 * 
	 * @param p The command executor
	 */
	private void promptBankRemove(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to remove a bank");

		if (!p.hasPermission(Permissions.BANK_REMOVE)) {
			plugin.debug(p.getName() + " does not have permission to remove a bank");
			p.sendMessage(Messages.NO_PERMISSION_BANK_REMOVE);
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
				p.sendMessage(Messages.getWithValue(Messages.BANK_NOT_FOUND, args[1]));
				return;
			}
		}

		int delay = Config.removeDelay;
		boolean needsScheduling = delay != 0;
		boolean confirmationEnabled = Config.confirmOnRemove;

		if (confirmationEnabled && needsScheduling) {
			if (commandConfirmed(p, List.of(bank), args))
				scheduleCommand(p, List.of(bank), args, delay);
		} else if (confirmationEnabled) {
			if (commandConfirmed(p, List.of(bank), args)) {
				bankUtils.removeBank(bank, true);
				plugin.debug("Bank was removed from the database");
				p.sendMessage(Messages.BANK_REMOVED);
			}
		} else if (needsScheduling) {
			scheduleCommand(p, List.of(bank), args, delay);
		} else {
			bankUtils.removeBank(bank, true);
			plugin.debug("Bank was removed from the database");
			p.sendMessage(Messages.BANK_REMOVED);
		}
	}

	private void promptBankInfo(Player p, String[] args) {
		plugin.debug(p.getName() + " wants to show bank info");


	}

	private void promptBankList(Player p, String[] args) {
		if (bankUtils.getBanksCopy().isEmpty()) {
			p.sendMessage(ChatColor.RED + "There are no banks to list.");
			return;
		}
		p.sendMessage("");
		int i = 1;
		for (Bank bank : bankUtils.getBanksCopy())
			p.sendMessage(ChatColor.GOLD + "" + i++ + ": " + bank.getInfoAsString());
		p.sendMessage("");
	}

	private boolean promptBankRemoveAll(CommandSender sender, String[] args) {
		return false;
	}

	@Override
	public void scheduleCommand(Player p, Collection<Bank> banks, String[] args, int ticks) {
		UUID uuid = p.getUniqueId();
		scheduled.remove(uuid);
		Optional.ofNullable(scheduled.get(uuid)).ifPresent(task -> {
			task.cancel();
			p.sendMessage(Messages.SCHEDULED_COMMAND_CANCELLED);
		});
		scheduled.put(uuid, new BukkitRunnable() {
			@Override
			public void run() {
				int count = banks.size();
				for (Bank bank : banks)
					bankUtils.removeBank(bank, true);
				plugin.debug(count + " bank(s) removed from the database");
				p.sendMessage(Messages.getWithValue(Messages.BANKS_REMOVED, count));
			}
		}.runTaskLater(BankingPlugin.getInstance(), ticks));
		p.sendMessage(Messages.getWithValues(Messages.BANK_COMMAND_SCHEDULED,
				new String[] { String.valueOf(Math.round((float) ticks / 20)), "/bank " + args[0] + " cancel" }));

	}

	@Override
	public boolean commandConfirmed(Player p, Collection<Bank> banks, String[] args) {
		if (unconfirmed.containsKey(p.getUniqueId()) && unconfirmed.get(p.getUniqueId()).equals(args)) {
			removeUnconfirmedCommand(p);
			return true;
		} else {
			addUnconfirmedCommand(p, args);
			int accounts = banks.stream().mapToInt(bank -> bank.getAccounts().size()).sum();
			p.sendMessage(Messages.getWithValues(Messages.ABOUT_TO_REMOVE_BANKS, new Integer[] { banks.size(), accounts }));
			p.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
			return false;
		}
	}
}