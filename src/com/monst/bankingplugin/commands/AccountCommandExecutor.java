package com.monst.bankingplugin.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

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
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.chat.TextComponent;

public class AccountCommandExecutor implements CommandExecutor, Confirmable<Account> {

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

		switch (subCommand.getName().toLowerCase()) {

		case "create":
			promptAccountCreate((Player) sender, args);
			break;
		case "remove":
			promptAccountRemove((Player) sender);
			break;
		case "info":
			promptAccountInfo((Player) sender, args);
			break;
		case "list":
			if (!promptAccountList(sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "limits":
			promptAccountLimits((Player) sender);
			break;
		case "removeall":
			if (!promptAccountRemoveAll(sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "set":
			if (!promptAccountSet((Player) sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "trust":
			if (!promptAccountTrust((Player) sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "untrust":
			if (!promptAccountUntrust((Player) sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		case "migrate":
			promptAccountMigrate((Player) sender);
			break;
		case "transfer":
			if (!promptAccountTransfer((Player) sender, args))
				sender.sendMessage(subCommand.getHelpMessage(sender));
			break;
		default:
			return false;
		}
		return true;
	}


	/**
	 * A given player creates a account
	 * 
	 * @param p    The command executor
	 * @param args Arguments of the entered command
	 */
	private void promptAccountCreate(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to create an account");

		boolean hasPermission = p.hasPermission(Permissions.ACCOUNT_CREATE);
		if (!hasPermission) {
			for (PermissionAttachmentInfo permInfo : p.getEffectivePermissions()) {
				String perm = permInfo.getPermission();
				if (perm.startsWith(Permissions.ACCOUNT_CREATE) && p.hasPermission(perm)) {
					hasPermission = true;
					break;
				}
			}
		}
		
		if (!hasPermission) {
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE);
			plugin.debug(p.getName() + " is not permitted to create an account");
			return;
		}

		int limit = accountUtils.getAccountLimit(p);
		if (limit != -1 && accountUtils.getNumberOfAccounts(p) >= limit) {
			p.sendMessage(Messages.ACCOUNT_LIMIT_REACHED);
			plugin.debug(p.getName() + " has reached their account limit");
			return;
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
		plugin.debug(p.getName() + " wants to retrieve account info");

		AccountPreInfoEvent event = new AccountPreInfoEvent(p);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Account pre-info event cancelled");
			return;
		}

		plugin.debug(p.getName() + " can now click an account to get info");
		p.sendMessage(Messages.CLICK_CHEST_INFO);
		ClickType.setPlayerClickType(p, new ClickType(EnumClickType.INFO));
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
						sender.spigot().sendMessage(new TextComponent(ChatColor.GOLD + "" + i++ + ": "), account.getInfo());
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
						for (Account account : accounts)
							p.spigot().sendMessage(account.getInfoVerbose());
					} else
						p.sendMessage(Messages.NO_ACCOUNTS_FOUND);
				} else {
					plugin.debug("Only players can list their own accounts");
					sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
				}
			} else if (args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) {
				if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
					plugin.debug(sender.getName() + " has listed all accounts");
					Collection<Account> accounts = accountUtils.getAccountsCopy();
					if (!accounts.isEmpty()) {
						int i = 1;
						for (Account account : accounts)
							sender.spigot().sendMessage(new TextComponent(ChatColor.GOLD + "" + i++ + ": "), account.getInfo());
						} else
						sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);

				} else {
					plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER);
				}
			} else {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if ((sender instanceof Player && ((Player) sender).getUniqueId().equals(owner.getUniqueId()))
							|| sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
						plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
						Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(owner);
						if (!accounts.isEmpty()) {
							int i = 1;
							for (Account account : accounts)
								sender.spigot().sendMessage(new TextComponent(ChatColor.GOLD + "" + i++ + ": "), account.getInfo());
						} else
							sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
					} else {
						plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER);
					}
				} else
					sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
			}
		} else if (args.length == 3) {
			if ((args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) && (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed"))
					|| (args[2].equalsIgnoreCase("-a") || args[2].equalsIgnoreCase("all")) && (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("detailed"))) {
				if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
					plugin.debug(sender.getName() + " has listed all accounts verbose");
					Collection<Account> accounts = accountUtils.getAccountsCopy();
					if (!accounts.isEmpty()) {
						int i = 1;
						for (Account account : accounts)
							sender.spigot().sendMessage(new TextComponent(ChatColor.GOLD + "" + i++ + ": "), account.getInfoVerbose());
					} else
						sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
				} else {
					plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER_VERBOSE);
				}
			} else if (args[2].equalsIgnoreCase("-d") || args[2].equalsIgnoreCase("detailed")) {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if ((sender instanceof Player && ((Player) sender).getUniqueId().equals(owner.getUniqueId()))
							|| sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
						plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
						Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(owner);
						if (!accounts.isEmpty()) {
							int i = 1;
							for (Account account : accounts)
								sender.spigot().sendMessage(new TextComponent(ChatColor.GOLD + "" + i++ + ": "), account.getInfoVerbose());
						} else
							sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
					} else {
						plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER_VERBOSE);
					}
				} else
					sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
			}
		} else
			return false;
		return true;
	}

	private void promptAccountLimits(final Player p) {
		int used = accountUtils.getNumberOfAccounts(p);
		Object limit = accountUtils.getAccountLimit(p) < 0 ? "∞" : accountUtils.getAccountLimit(p);
		plugin.debug(p.getName() + " is viewing their account limits: " + used + " / " + limit);
		p.sendMessage(String.format(Messages.ACCOUNT_LIMIT, used, limit));
	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountRemoveAll(final CommandSender sender, String[] args) {

		if (args.length == 1) {
			if (sender instanceof Player) { // account removeall
				Collection<Account> accounts = accountUtils.getPlayerAccountsCopy((Player) sender);
				if (!accounts.isEmpty())
					confirmRemoveAll(sender, accounts, args);
				else
					sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
			} else {
				plugin.debug("Only players can remove all of their own accounts");
				sender.sendMessage(ChatColor.RED + Messages.PLAYER_COMMAND_ONLY);
			}
		} else if (args.length == 2) {
			if (args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) { // account removeall all
				if (sender.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) {
					Collection<Account> accounts = accountUtils.getAccountsCopy();
					if (!accounts.isEmpty())
						confirmRemoveAll(sender, accounts, args);
					else
						sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
				} else {
					plugin.debug(sender.getName() + " does not have permission to remove all accounts");
					sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
				}
			} else {
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				if (owner.hasPlayedBefore()) {
					plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
							+ owner.getUniqueId());
					if ((sender instanceof Player && ((Player) sender).getUniqueId().equals(owner.getUniqueId()))
							|| sender.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER)) { // account removeall player
						Collection<Account> accounts = accountUtils.getPlayerAccountsCopy(owner);
						if (!accounts.isEmpty())
							confirmRemoveAll(sender, accounts, args);
						else
							sender.sendMessage(Messages.NO_PLAYER_ACCOUNTS);
					} else {
						plugin.debug(sender.getName() + " does not have permission to remove all accounts");
						sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
					}
				} else
					sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
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
							confirmRemoveAll(sender, accounts, args);
						else
							sender.sendMessage(Messages.NO_ACCOUNTS_FOUND);
					} else
						sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[2]));
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
						confirmRemoveAll(sender, accounts, args);
					else
						sender.sendMessage(Messages.NO_BANK_ACCOUNTS);
				} else
					sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[2]));
			}
		} else
			return false;
		return true;
	}

	private void confirmRemoveAll(final CommandSender sender, Collection<Account> accounts, String[] args) { // XXX

		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (Config.confirmOnRemoveAll)
				if (!commandConfirmed(p, args)) {
					p.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_ACCOUNTS, accounts.size(),
							accounts.size() == 1 ? "" : "s") + Messages.EXECUTE_AGAIN_TO_CONFIRM);
					return;
				}
		}
		// TODO: Send AccountRemoveAllEvent

		accountUtils.removeAccount(accounts, true);
		sender.sendMessage(String.format(Messages.ACCOUNTS_REMOVED, accounts.size(), accounts.size() == 1 ? "" : "s",
				accounts.size() == 1 ? "was" : "were"));

	}
	
	private boolean promptAccountSet(final Player p, String[] args) {
		plugin.debug(p.getName() + " wants to configure an account");
		if (args.length < 2)
			return false;

		switch (args[1].toLowerCase()) {

		case "nickname":
			if (p.hasPermission(Permissions.ACCOUNT_CREATE)) {
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
				if (!Utils.isAllowedName(args[1])) {
					plugin.debug("Name is not allowed");
					p.sendMessage(Messages.NAME_NOT_ALLOWED);
					return true;
				}
				p.sendMessage(Messages.CLICK_CHEST_SET);
				ClickType.setPlayerClickType(p, new ClickType.SetClickType(new String[] { "nickname", nickname }));
			} else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME);
			break;

		case "multiplier":
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
					p.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
				}
			} else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
			break;

		case "interest-delay":
			if (args.length < 3)
				return false;
			if (p.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
				try {
					Integer.parseInt(args[2]);

					p.sendMessage(Messages.CLICK_CHEST_SET);
					ClickType.setPlayerClickType(p, new ClickType.SetClickType(args));
				} catch (NumberFormatException e) {
					p.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
				}
			} else
				p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
			break;

		default:
			p.sendMessage(String.format(Messages.NOT_A_FIELD, args[1]));
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountTrust(Player p, String[] args) {
		if (args.length < 2)
			return false;

		plugin.debug(p.getName() + " wants to trust a player to an account");

		if (!p.hasPermission(Permissions.ACCOUNT_TRUST)) {
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRUST);
			return true;
		}
		OfflinePlayer playerToTrust = Bukkit.getOfflinePlayer(args[1]);
		if (!playerToTrust.hasPlayedBefore()) {
			p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
			return false;
		}
		if (playerToTrust.getUniqueId().equals(p.getUniqueId()))
			return false;

		plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
				+ playerToTrust.getUniqueId());

		p.sendMessage(String.format(Messages.CLICK_CHEST_TRUST, playerToTrust.getName()));
		ClickType.setPlayerClickType(p, new ClickType.TrustClickType(playerToTrust));
		plugin.debug(p.getName() + " is trusting " + playerToTrust.getName() + " to an account");
		return true;
	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountUntrust(Player p, String[] args) {
		if (args.length < 2)
			return false;

		plugin.debug(p.getName() + " wants to untrust a player from an account");

		if (!p.hasPermission(Permissions.ACCOUNT_TRUST)) {
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_UNTRUST);
			return true;
		}
		OfflinePlayer playerToUntrust = Bukkit.getOfflinePlayer(args[1]);
		if (!playerToUntrust.hasPlayedBefore()) {
			p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
			return false;
		}
		if (playerToUntrust.getUniqueId().equals(p.getUniqueId()))
			return false;

		plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
				+ playerToUntrust.getUniqueId());

		p.sendMessage(Messages.CLICK_CHEST_UNTRUST);
		ClickType.setPlayerClickType(p, new ClickType.UntrustClickType(playerToUntrust));
		plugin.debug(p.getName() + " is untrusting " + playerToUntrust.getName() + " from an account");
		return true;
	}

	private void promptAccountMigrate(Player p) {
		plugin.debug(p.getName() + " wants to migrate an account");

		if (!p.hasPermission(Permissions.ACCOUNT_CREATE)) {
			plugin.debug(p.getName() + " does not have permission to migrate an account");
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE);
			return;
		}

		p.sendMessage(Messages.CLICK_CHEST_MIGRATE_FIRST);
		ClickType.setPlayerClickType(p, new ClickType.MigrateClickType(null));
		plugin.debug(p.getName() + " is migrating an account");
	}

	@SuppressWarnings("deprecation")
	private boolean promptAccountTransfer(Player p, String[] args) {
		plugin.debug(p.getName() + " wants to transfer ownership of an account");
		
		if (!p.hasPermission(Permissions.ACCOUNT_TRANSFER)) {
			plugin.debug(p.getName() + " does not have permission to transfer ownership of an account");
			p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRANSFER);
			return true;
		}

		if (args.length < 2)
			return false;
		
		OfflinePlayer newOwner = Bukkit.getOfflinePlayer(args[1]);
		if (newOwner == null || !newOwner.hasPlayedBefore()) {
			p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
			return false;
		}
		plugin.debug("Used deprecated method to lookup offline player \"" + args[1] + "\" and found uuid: "
				+ newOwner.getUniqueId());
		
		p.sendMessage(String.format(Messages.CLICK_CHEST_TRANSFER, newOwner.getName()));
		ClickType.setPlayerClickType(p, new ClickType.TransferClickType(newOwner));
		plugin.debug(p.getName() + " is transfering ownership of an account to " + newOwner.getName());
		return true;
	}

}
