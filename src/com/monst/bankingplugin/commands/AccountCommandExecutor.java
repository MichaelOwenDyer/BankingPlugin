package com.monst.bankingplugin.commands;

import java.util.Arrays;
import java.util.Collection;
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

import com.monst.bankingplugin.Account;
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

public class AccountCommandExecutor implements CommandExecutor, SchedulableCommand<Account> {

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
				if (!promptAccountList(p, args))
					p.sendMessage(subCommand.getHelpMessage(p));
				break;
			case "limits":
				promptAccountLimit(p);
				break;
			case "removeall":
				if (!promptAccountRemoveAll(p, args))
					p.sendMessage(subCommand.getHelpMessage(p));
				break;
			case "set":
				if (!promptAccountSet(p, args))
					p.sendMessage(subCommand.getHelpMessage(p));
				break;
			default:
				return false;
			}
		} else {

			switch (subCommand.getName().toLowerCase()) {

			case "list":
				if (!promptAccountList(sender, args))
					sender.sendMessage(Messages.COMMAND_USAGE_ACCOUNT_LIST);
				return true;
			case "removeall":
				if (!promptAccountRemoveAll(sender, args))
					sender.sendMessage(Messages.COMMAND_USAGE_ACCOUNT_REMOVEALL);
				return true;
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
		String permission = forSelf ? Permissions.ACCOUNT_CREATE : Permissions.ACCOUNT_OTHER_CREATE;
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
			if (!owner.getUniqueId().equals(p.getUniqueId()))
				plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
						+ owner.getUniqueId());
			else {
				forSelf = true;
				owner = p;
			}
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
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_CREATE);
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
				Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(p);
				if (!accounts.isEmpty()) {
					int i = 1;
					for (Account account : accounts)
						p.sendMessage(ChatColor.GOLD + "" + i++ + ": " + account.toString());
				} else
					p.sendMessage(Messages.NO_ACCOUNTS_FOUND);
			} else {
				plugin.debug("Only players can list their own accounts");
				sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
			}
		} else if (args.length == 2) {
			if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					plugin.debug(p.getName() + " has listed their own accounts detailed");
					Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(p);
					if (!accounts.isEmpty()) {
						int i = 1;
						for (Account account : accounts)
							p.sendMessage(ChatColor.GOLD + "" + i++ + ": " + account.toStringVerbose());
					} else
						p.sendMessage(Messages.NO_ACCOUNTS_FOUND);
				} else {
					plugin.debug("Only players can list their own accounts detailed");
					sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
				}
			} else if (args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) {
				if (sender.hasPermission(Permissions.ACCOUNT_OTHER_LIST)) {
					plugin.debug(sender.getName() + " has listed all accounts");
					Collection<Account> accounts = accountUtils.getAccountsCopy();
					if (!accounts.isEmpty()) {
						int i = 1;
						for (Account account : accounts)
							sender.sendMessage(ChatColor.GOLD + "" + i++ + ": " + account.toString());
					} else
						sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);

				} else {
					plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_LIST);
				}
			} else {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if ((sender instanceof Player && ((Player) sender).getUniqueId().equals(owner.getUniqueId()))
							|| sender.hasPermission(Permissions.ACCOUNT_OTHER_LIST)) {
						plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
						Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(owner);
						if (!accounts.isEmpty()) {
							int i = 1;
							for (Account account : accounts)
								sender.sendMessage(ChatColor.GOLD + "" + i++ + ": " + account.toString());
						} else
							sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
					} else {
						plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_LIST);
					}
				} else
					sender.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
			}
		} else if (args.length == 3) {
			if ((args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) && (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed"))
					|| (args[2].equalsIgnoreCase("-a") || args[2].equalsIgnoreCase("all")) && (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed"))) {
				if (sender.hasPermission(Permissions.ACCOUNT_OTHER_LIST_VERBOSE)) {
					plugin.debug(sender.getName() + " has listed all accounts verbose");
					Collection<Account> accounts = accountUtils.getAccountsCopy();
					if (!accounts.isEmpty()) {
						int i = 1;
						for (Account account : accounts)
							sender.sendMessage(ChatColor.GOLD + "" + i++ + ": " + account.toStringVerbose());
					} else
						sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
				} else {
					plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_LIST_VERBOSE);
				}
			} else if (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed")) {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (!owner.hasPlayedBefore()) {
					plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if ((sender instanceof Player && ((Player) sender).getUniqueId().equals(owner.getUniqueId()))
							|| sender.hasPermission(Permissions.ACCOUNT_OTHER_LIST_VERBOSE)) {
						plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
						Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(owner);
						if (!accounts.isEmpty()) {
							int i = 1;
							for (Account account : accounts)
								sender.sendMessage(ChatColor.GOLD + "" + i++ + ": " + account.toStringVerbose());
						} else
							sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
					} else {
						plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_LIST_VERBOSE);
					}
				} else
					sender.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
			}
		} else
			return false;
		return true;
	}

	private void promptAccountLimit(final Player p) {
		int used = accountUtils.getNumberOfAccounts(p);
		Object limit = accountUtils.getAccountLimit(p) < 0 ? "∞" : accountUtils.getAccountLimit(p);
		plugin.debug(p.getName() + " is viewing their account limits: " + used + " / " + limit);
		p.sendMessage(Messages.getWithValues(Messages.ACCOUNT_LIMIT, new Object[] { used, limit }));
	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountRemoveAll(final CommandSender sender, String[] args) {

		if (args.length == 1) {
			if (sender instanceof Player) { // account removeall
				Collection<Account> accounts = accountUtils.getPlayerAccountsCopy((Player) sender);
				if (!accounts.isEmpty())
					scheduleRemoveAll(sender, accounts, args);
				else
					sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
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
				if (sender.hasPermission(Permissions.ACCOUNT_OTHER_REMOVE)) {
					Collection<Account> accounts = accountUtils.getAccountsCopy();
					if (!accounts.isEmpty())
						scheduleRemoveAll(sender, accounts, args);
					else
						sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
				} else {
					plugin.debug(sender.getName() + " does not have permission to remove all accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_REMOVE);
				}
			} else {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if ((sender instanceof Player && ((Player) sender).getUniqueId().equals(owner.getUniqueId()))
							|| sender.hasPermission(Permissions.ACCOUNT_OTHER_REMOVE)) { // account removeall player
						Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(owner);
						if (!accounts.isEmpty())
							scheduleRemoveAll(sender, accounts, args);
						else
							sender.sendMessage(Messages.NO_PLAYER_ACCOUNTS);
					} else {
						plugin.debug(sender.getName() + " does not have permission to remove all accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_OTHER_REMOVE);
					}
				} else
					sender.sendMessage(Messages.getWithValue(Messages.PLAYER_NOT_FOUND, args[1]));
			}
		} else if (args.length == 3) {
			if (sender instanceof Player) {
				if (args[1].equalsIgnoreCase("-b") || args[1].equalsIgnoreCase("bank")) { // account removeall bank
					Bank bank = plugin.getBankUtils().lookupBank(args[2]);
					if (bank != null) {
						Collection<Account> accounts = accountUtils.getAccountsCopy().stream().filter(account -> 
						account.isOwner((Player) sender) && account.getBank().equals(bank))
								.collect(Collectors.toList());
						if (!accounts.isEmpty())
							scheduleRemoveAll(sender, accounts, args);
						else
							sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
					} else
						sender.sendMessage(Messages.getWithValue(Messages.BANK_NOT_FOUND, args[2]));
				}
			} else {
				plugin.debug("Only players can remove all of their own accounts at a certain bank");
				sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
			}
		} else if (args.length == 4) {
			if ((args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all"))
					&& (args[2].equalsIgnoreCase("-b") || args[2].equalsIgnoreCase("bank"))) {

				Bank bank = plugin.getBankUtils().lookupBank(args[3]);
				if (bank != null) {
					Collection<Account> accounts = accountUtils.getBankAccountsCopy(bank);
					if (!accounts.isEmpty())
						scheduleRemoveAll(sender, accounts, args);
					else
						sender.sendMessage(Messages.NO_BANK_ACCOUNTS);
				} else
					sender.sendMessage(Messages.getWithValue(Messages.BANK_NOT_FOUND, args[2]));
			}
		} else
			return false;
		return true;
	}
	
	public boolean promptAccountSet(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to configure an account");
		if (args[1].equalsIgnoreCase("nickname")) {
			if (args.length < 2)
				return false;
			if (p.hasPermission(Permissions.ACCOUNT_SET_NICKNAME)) {
				String nickname;
				if (args.length < 3)
					nickname = "";
				else
					nickname = args[2];
				if (args.length > 3) {
					StringBuilder sb = new StringBuilder(nickname);
					for (int i = 3; i < args.length; i++)
						sb.append(" " + args[i]);
					nickname = sb.toString();
				}
				// TODO: Add pattern matching?
				p.sendMessage(Messages.CLICK_CHEST_SET);
				ClickType.setPlayerClickType(p, new ClickType.SetClickType(new String[] { "nickname", nickname }));
			} else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME);
		} else if (args[1].equalsIgnoreCase("multiplier")) {
			if (args.length < 3)
				return false;
			if (p.hasPermission(Permissions.ACCOUNT_SET_MULTIPLIER)) {
				try {
					String sign;
					String multiplier;
					if (args[2].startsWith("+")) {
						sign = "+";
						multiplier = "" + Integer.parseInt(args[2].substring(1));
					} else if (args[2].startsWith("-")) {
						sign = "-";
						multiplier = "" + Integer.parseInt(args[2].substring(1));
					} else {
						sign = "";
						multiplier = "" + Integer.parseInt(args[2]);
					}
					p.sendMessage(Messages.CLICK_CHEST_SET);
					ClickType.setPlayerClickType(p, new ClickType.SetClickType(new String[] { "multiplier", sign, multiplier }));
				} catch (NumberFormatException e) {
					p.sendMessage(Messages.getWithValue(Messages.NOT_A_NUMBER, "\"" + args[2] + "\""));
				}
			} else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
		} else if (args[1].equalsIgnoreCase("interest-delay")) {
			if (args.length < 3)
				return false;
			if (p.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
				try {
					Integer.parseInt(args[2]);

					p.sendMessage(Messages.CLICK_CHEST_SET);
					ClickType.setPlayerClickType(p, new ClickType.SetClickType(args));
				} catch (NumberFormatException e) {
					p.sendMessage(Messages.getWithValue(Messages.NOT_A_NUMBER, "\"" + args[2] + "\""));
				}
			} else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
		} else
			p.sendMessage(Messages.getWithValue(Messages.NOT_A_FIELD, "\"" + args[1] + "\""));
		return true;
	}

	private void scheduleRemoveAll(final CommandSender sender, Collection<Account> accounts, String[] args) {

		int delay = Config.removeDelay;
		boolean needsScheduling = delay != 0;
		boolean confirmationEnabled = Config.confirmOnRemoveAll;

		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (confirmationEnabled && needsScheduling) {
				if (commandConfirmed(p, accounts, args))
					scheduleCommand(p, accounts, args, delay);
			} else if (confirmationEnabled) {
				if (commandConfirmed(p, accounts, args))
					accountUtils.removeAll(p, accounts);
			} else if (needsScheduling) {
				scheduleCommand(p, accounts, args, delay);
			} else
				accountUtils.removeAll(p, accounts);
		} else {
			accountUtils.removeAll(sender, accounts);
		}
	}

	@Override
	public void scheduleCommand(Player p, Collection<Account> accounts, String[] args, int ticks) {
		UUID uuid = p.getUniqueId();
		scheduled.remove(uuid);
		Optional.ofNullable(scheduled.get(uuid)).ifPresent(task -> {
			task.cancel();
			p.sendMessage(Messages.SCHEDULED_COMMAND_CANCELLED);
		});
		scheduled.put(uuid, new BukkitRunnable() {
			@Override
			public void run() {
				int count = accountUtils.removeAll(p, accounts);
				p.sendMessage(Messages.getWithValue(Messages.ACCOUNTS_REMOVED, count));
			}
		}.runTaskLater(plugin, ticks));
		p.sendMessage(Messages.getWithValues(Messages.ACCOUNT_COMMAND_SCHEDULED,
				new String[] { String.valueOf(Math.round((float) ticks / 20)), "/account removeall cancel" }));
	}

	@Override
	public boolean commandConfirmed(Player p, Collection<Account> accounts, String[] args) {
		if (unconfirmed.containsKey(p.getUniqueId()) && Arrays.equals(unconfirmed.get(p.getUniqueId()), args)) {
			removeUnconfirmedCommand(p);
			return true;
		} else {
			addUnconfirmedCommand(p, args);
			p.sendMessage(Messages.getWithValue(Messages.ABOUT_TO_REMOVE_ACCOUNTS, accounts.size())
					+ Messages.EXECUTE_AGAIN_TO_CONFIRM);
			return false;
		}
	}
}
