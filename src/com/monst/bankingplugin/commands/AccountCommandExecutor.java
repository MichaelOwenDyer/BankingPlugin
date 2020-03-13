package com.monst.bankingplugin.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountPreCreateEvent;
import com.monst.bankingplugin.events.account.AccountPreInfoEvent;
import com.monst.bankingplugin.events.account.AccountPreRemoveEvent;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.ClickType.EnumClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;

public class AccountCommandExecutor implements CommandExecutor, SchedulableCommand {

	private BankingPlugin plugin;
	private AccountUtils accountUtils;


	public AccountCommandExecutor(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<AccountSubCommand> subCommands = plugin.getAccountCommand().getSubCommands().stream()
				.map(cmd -> (AccountSubCommand) cmd).collect(Collectors.toList());

		AccountSubCommand subCommand = null;

		for (AccountSubCommand accountSubCommand : subCommands)
			if (accountSubCommand.getName().equalsIgnoreCase(args[0])) {
				subCommand = accountSubCommand;
				break;
			}

		if (subCommand == null) {
			plugin.getLogger().severe("Null command!");
			plugin.debug("Null command! Sender: " + sender.getName()
					+ ", command: " + command.getName() + " " +  Arrays.stream(args).collect(Collectors.joining(" ")));
			return false;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;

			switch (subCommand.getName().toLowerCase()) {

			case "create":
				promptAccountCreate(p, args);
				break;
			case "remove":
				promptAccountRemove(p);
				break;
			case "info":
				promptAccountInfo(p, args);
				break;
			case "list":
				return promptAccountList(p, args);
			case "limits":
				promptAccountLimit(p, args);
				break;
			case "removeall":
				return promptAccountRemoveAll(p, args);
			default:
				return false;
			}
		} else {

			switch (subCommand.getName().toLowerCase()) {

			case "list":
				return promptAccountList(sender, args);
			case "removeall":
				return promptAccountRemoveAll(sender, args);
			default:
				return false;
			}
		}

		return true;
	}


	/**
	 * A given player creates a account
	 * 
	 * @param p    The command executor
	 * @param args Arguments of the entered command
	 */
	@SuppressWarnings("deprecation")
	private void promptAccountCreate(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to create an account");

		boolean forSelf = args.length == 1;
		String permission = forSelf ? Permissions.ACCOUNT_CREATE : Permissions.ACCOUNT_CREATE_OTHER;
		boolean hasPermission = p.hasPermission(permission);
		if (!hasPermission) {
			for (PermissionAttachmentInfo permInfo : p.getEffectivePermissions()) {
				String perm = permInfo.getPermission();
				if (perm.startsWith(permission) && p.hasPermission(perm)) {
					hasPermission = true;
					break;
				}
			}
		}
		
		OfflinePlayer owner;
		if (!forSelf) {
			owner = Bukkit.getOfflinePlayer(args[1]);
			if (!owner.hasPlayedBefore()) {
				p.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
				return;
			}
			plugin.debug("Used depricated method to lookup offline player \"" + args[1] + "\" and found uuid: " + owner.getUniqueId());
		} else
			owner = p;
		
		if (forSelf) {
			if (!hasPermission) {
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE);
				plugin.debug(p.getName() + " is not permitted to create an account");
				return;
			}
		} else {
			if (!hasPermission) {
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_OTHER);
				plugin.debug(p.getName() + " is not permitted to create an account for another player");
				return;
			}
		}

		if (forSelf) {
			int limit = accountUtils.getAccountLimit(p);
			if (limit != -1) {
				if (accountUtils.getNumberOfAccounts(p) >= limit) {
					p.sendMessage(Messages.ACCOUNT_LIMIT_REACHED);
					plugin.debug(p.getName() + " has reached their account limit");
					return;
				}
			}
		}

		AccountPreCreateEvent event = new AccountPreCreateEvent(p, args);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Account pre-create event cancelled");
			return;
		}

		plugin.debug(p.getName() + " can now click a chest to create an account");
		p.sendMessage(Messages.CLICK_CHEST_CREATE);
		ClickType.setPlayerClickType(p, new ClickType(EnumClickType.CREATE));
	}

	/**
	 * A given player removes a account
	 * 
	 * @param p The command executor
	 */
	private void promptAccountRemove(final Player p) {
		plugin.debug(p.getName() + " wants to remove a account");

		AccountPreRemoveEvent event = new AccountPreRemoveEvent(p);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Account pre-remove event cancelled");
			return;
		}

		plugin.debug(p.getName() + " can now click a chest to remove an account");
		p.sendMessage(Messages.CLICK_CHEST_REMOVE);
		ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.REMOVE));
	}

	private void promptAccountInfo(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to retrieve information");

		boolean verbose = false;
		if (args.length == 2)
			if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed"))
				verbose = true;

		AccountPreInfoEvent event = new AccountPreInfoEvent(p, verbose);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Shop pre-info event cancelled");
			return;
		}

		plugin.debug(p.getName() + " can now click a chest to get account info");
		p.sendMessage(Messages.CLICK_CHEST_INFO);
		ClickType.setPlayerClickType(p, new ClickType.InfoClickType(verbose));

	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountList(final CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " wants to list accounts");

		if (args.length == 1) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				plugin.debug(p.getName() + " has listed their own accounts");
				p.sendMessage(accountUtils.getAccountList(p, "", args));
				return true;
			} else {
				plugin.debug("Only players can list their own accounts");
				sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
			}
		} else if (args.length == 2) {
			if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					plugin.debug(p.getName() + " has listed their own accounts detailed");
					p.sendMessage(accountUtils.getAccountList(p, "-d", args));
				} else {
					plugin.debug("Only players can list their own accounts detailed");
					sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
				}
			} else if (args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) {
				if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
					plugin.debug(sender.getName() + " has listed all accounts");
					sender.sendMessage(accountUtils.getAccountList(sender, "-a", args));
				} else {
					plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER);
				}
			} else {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used depricated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
						plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
						sender.sendMessage(accountUtils.getAccountList(sender, "name", args));
					} else {
						plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER);
					}
				} else
					sender.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
			}
			return true;
		} else if (args.length == 3) {
			if ((args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) && (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed"))
					|| (args[2].equalsIgnoreCase("-a") || args[2].equalsIgnoreCase("all")) && (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed"))) {
				if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER_VERBOSE)) {
					plugin.debug(sender.getName() + " has listed all accounts verbose");
					sender.sendMessage(accountUtils.getAccountList(sender, "-a -d", args));
				} else {
					plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER_VERBOSE);
				}
			} else if (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed")) {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (!owner.hasPlayedBefore()) {
					plugin.debug("Used depricated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER_VERBOSE)) {
						plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
						sender.sendMessage(accountUtils.getAccountList(sender, "name -d", args));
					} else {
						plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER_VERBOSE);
					}
				} else
					sender.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
			}
			return true;
		}
		return false;
	}

	private void promptAccountLimit(final Player p, String[] args) {
		int used = accountUtils.getNumberOfAccounts(p);
		int limit = accountUtils.getAccountLimit(p);
		plugin.debug(p.getName() + " is viewing their account limits: " + used + "/" + limit);
		p.sendMessage(Messages.getWithValues(Messages.ACCOUNT_LIMIT, new Integer[] { used, limit }));
	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountRemoveAll(final CommandSender sender, String[] args) {

		if (args.length == 1) {
			if (sender instanceof Player) { // account removeall
				scheduleRemoveAll(sender, "", args);
			} else {
				plugin.debug("Only players can remove all of their own accounts");
				sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
			}
		} else if (args.length == 2) {
			if (args[1].equalsIgnoreCase("-c") || args[1].equalsIgnoreCase("cancel")) { // account removeall cancel
				if (sender instanceof Player)
					unscheduleCommand((Player) sender);
				else {
					plugin.debug("Only players can cancel a scheduled command");
					sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
				}
			} else if (args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) { // account removeall all
				if (sender.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) {
					scheduleRemoveAll(sender, "-a", args);
				} else {
					plugin.debug(sender.getName() + " does not have permission to remove all accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
				}
			} else {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used depricated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if (sender.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) { // account removeall player
						scheduleRemoveAll(sender, "name", args);
					} else {
						plugin.debug(sender.getName() + " does not have permission to remove all accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
					}
				} else
					sender.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
			}
		} else if (args.length == 3) {
			if (sender instanceof Player) {
				if (args[1].equalsIgnoreCase("-b") || args[1].equalsIgnoreCase("bank")) { // account removeall bank
					Bank bank = plugin.getBankUtils().lookupBank(args[2]);
					if (bank != null)
						scheduleRemoveAll(sender, "-b", args);
					else
						sender.sendMessage(Messages.getWithValue(Messages.BANK_NOT_FOUND, args[2]));
				}
			} else {
				plugin.debug("Only players can remove all of their own accounts at a certain bank");
				sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
			}
		} else if (args.length >= 4) {
			if ((args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all"))
					&& (args[2].equalsIgnoreCase("-b") || args[2].equalsIgnoreCase("bank"))) {

				Bank bank = plugin.getBankUtils().lookupBank(args[3]);
				if (bank != null)
					scheduleRemoveAll(sender, "-a -b", args);
				else
					sender.sendMessage(Messages.getWithValue(Messages.BANK_NOT_FOUND, args[2]));
			} else
				return false;
		}
		return true;
	}
	
	private void scheduleRemoveAll(final CommandSender sender, String request, String[] args) {

		int delay = Config.removeDelay;
		boolean needsScheduling = delay != 0;
		boolean confirmationEnabled = Config.confirmOnRemoveAll;

		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (confirmationEnabled && needsScheduling) {
				if (commandConfirmed(p, request, args))
					scheduleCommand(p, request, args, delay);
			} else if (confirmationEnabled) {
				if (commandConfirmed(p, request, args))
					accountUtils.removeAll(p, request, args);
			} else if (needsScheduling) {
				scheduleCommand(p, request, args, delay);
			} else
				accountUtils.removeAll(p, request, args);
		} else {
			accountUtils.removeAll(sender, request, args);
		}
	}

	@Override
	public void scheduleCommand(Player p, Object request, String[] args, int ticks) {
		UUID uuid = p.getUniqueId();
		scheduled.remove(uuid);
		Optional.ofNullable(scheduled.get(uuid)).ifPresent(task -> {
			task.cancel();
			p.sendMessage(Messages.SCHEDULED_COMMAND_CANCELLED);
		});
		scheduled.put(uuid, new BukkitRunnable() {
			@Override
			public void run() {
				int count = accountUtils.removeAll(p, request.toString(), args);
				p.sendMessage(Messages.getWithValue(Messages.ACCOUNTS_REMOVED, count));
			}
		}.runTaskLater(BankingPlugin.getInstance(), ticks));
		p.sendMessage(Messages.getWithValues(Messages.ACCOUNT_COMMAND_SCHEDULED,
				new String[] { String.valueOf(Math.round((float) ticks / 20)), "/account removeall cancel" }));
	}

	@Override
	public boolean commandConfirmed(Player p, Object request, String[] args) {
		if (unconfirmed.containsKey(p.getUniqueId()) && unconfirmed.get(p.getUniqueId()).equals(args)) {
			removeUnconfirmedCommand(p);
			return true;
		} else {
			addUnconfirmedCommand(p, args);
			p.sendMessage(Messages.getWithValue(Messages.ABOUT_TO_REMOVE_ACCOUNTS,
					accountUtils.toRemoveList(p, request.toString(), args).size()) + Messages.EXECUTE_AGAIN_TO_CONFIRM);
			return false;
		}
	}

}
