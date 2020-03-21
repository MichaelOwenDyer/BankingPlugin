package com.monst.bankingplugin.utils;

import org.bukkit.ChatColor;

import com.monst.bankingplugin.config.Config;

public class Messages {
	
	private static final String ACCOUNT_CMD = Config.mainCommandNameAccount;
	private static final String BANK_CMD = Config.mainCommandNameBank;
	private static final String CONTROL_CMD = Config.mainCommandNameControl;
	
	public static final String BANK_CREATED = ChatColor.GOLD + "Bank created!";
	public static final String ACCOUNT_CREATED = ChatColor.GOLD + "Account created!";
	public static final String ACCOUNT_CREATE_FEE_PAID = ChatColor.GOLD + "You have been charged " + ChatColor.GREEN + "$| " + ChatColor.GOLD + "to create an account.";
	public static final String BANK_ALREADY_EXISTS = ChatColor.RED + "A bank by that name already exists!";
	public static final String CHEST_ALREADY_ACCOUNT = ChatColor.RED + "That chest is already an account.";
	public static final String CHEST_BLOCKED = ChatColor.RED + "Chest is blocked!";
	public static final String ACCOUNT_REMOVED = ChatColor.GOLD + "The account has been removed!";
	public static final String BANK_REMOVED = ChatColor.GOLD + "The bank has been removed!";
	public static final String PLAYER_REIMBURSED = ChatColor.GOLD + "You were reimbursed " + ChatColor.GREEN + "$|" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_BALANCE_NOT_ZERO = ChatColor.RED + "That account is not empty. Are you sure?";
	public static final String ACCOUNT_OPENED = ChatColor.GOLD + "You have opened |'s account chest.";
	public static final String ALL_ACCOUNTS_REMOVED = ChatColor.GREEN + "All accounts were successfully removed.";
	public static final String CANNOT_BREAK_ACCOUNT = ChatColor.RED + "You cannot break that account chest.";
	public static final String CHEST_NOT_ACCOUNT = ChatColor.RED + "That chest is not an account.";
	public static final String CHEST_NOT_IN_BANK = ChatColor.RED + "That chest is not located within a bank.";
	public static final String NOT_STANDING_IN_BANK = ChatColor.RED + "You must stand in or name the bank you want to remove.";
	public static final String ACCOUNT_CREATE_INSUFFICIENT_FUNDS = ChatColor.RED + "You do not have sufficient funds to create an account.";
	//public static final String BANK_CREATE_INSUFFICIENT_FUNDS = "You do not have sufficient funds to create a bank.";
	public static final String INTEREST_EARNED = ChatColor.GOLD + "You have earned " + ChatColor.GREEN + "$| " + ChatColor.GOLD + "in interest on your account!";
	public static final String MULTIPLIER_INCREASED = ChatColor.GOLD + "Your account multiplier has increased to |.";
	public static final String MULTIPLIER_DECREASED = ChatColor.RED + "Your account multiplier has decreased to |.";
	public static final String ERROR_OCCURRED = ChatColor.DARK_RED + "An error occurred! ";
	public static final String PLAYER_NOT_FOUND = ChatColor.RED + "No player was found under the name \"|\".";
	public static final String BANK_NOT_FOUND = ChatColor.RED + "No bank was found under the identifier \"|\".";
	public static final String NO_ACCOUNTS_FOUND = ChatColor.RED + "No accounts found!";
	public static final String NO_PLAYER_ACCOUNTS = ChatColor.RED + "That player does not own any accounts.";
	public static final String NO_BANK_ACCOUNTS = ChatColor.RED + "There are no accounts registered at that bank.";
	public static final String NO_SELECTION_FOUND = ChatColor.RED + "Select a WorldEdit region first.";
	public static final String COORDINATES_PARSE_ERROR = ChatColor.RED + "Invalid coordinates.";
	public static final String NAME_NOT_UNIQUE = ChatColor.RED + "A bank with that name already exists.";
	public static final String SELECTION_NOT_EXCLUSIVE = ChatColor.RED + "Your selection overlaps with another bank.";
	public static final String WORLDEDIT_NOT_ENABLED = ChatColor.RED + "WorldEdit is not currently enabled. Please enter coordinates manually.";
	public static final String CLICK_CHEST_CREATE = ChatColor.GOLD + "Click a chest to create an account.";
	public static final String CLICK_CHEST_REMOVE = ChatColor.GOLD + "Click an account chest to remove.";
	public static final String CLICK_CHEST_INFO = ChatColor.GOLD + "Click an account chest to view info.";
	public static final String CLICK_CHEST_SET = ChatColor.GOLD + "Click an account chest to set.";
	public static final String CLICK_TO_CONFIRM = ChatColor.GOLD + "Click chest again to confirm.";
	public static final String RELOADED_BANKS = ChatColor.GOLD + "Successfully reloaded | banks and | accounts.";
	// public static final String BANK_LIMIT_REACHED = "You are not allowed to create any more banks!";
	public static final String ACCOUNT_LIMIT = ChatColor.GOLD + "You own |/| allowed accounts.";
	public static final String ACCOUNT_LIMIT_REACHED = ChatColor.RED + "You are not allowed to create any more accounts!";
	public static final String ACCOUNT_COMMAND_SCHEDULED = ChatColor.GREEN + "Your command has been scheduled to execute in | seconds. Use | to cancel.";
	public static final String BANK_COMMAND_SCHEDULED = ChatColor.GREEN + "Your command has been scheduled to execute in | seconds. Use | to cancel.";
	public static final String SCHEDULED_COMMAND_CANCELLED = ChatColor.RED + "Your scheduled command has been cancelled.";
	public static final String SCHEDULED_COMMAND_NOT_EXIST = ChatColor.RED + "You do not have a currently scheduled command.";
	public static final String PLAYER_COMMAND_ONLY = ChatColor.RED + "Only players can use this command.";
	public static final String ABOUT_TO_REMOVE_ACCOUNTS = ChatColor.GOLD + "You are about to delete | accounts. ";
	public static final String ABOUT_TO_REMOVE_BANKS = ChatColor.GOLD + "You are about to delete | banks and | accounts. ";
	public static final String ACCOUNTS_REMOVED = ChatColor.GREEN + "Your command has been executed and | accounts were removed.";
	public static final String BANKS_REMOVED = ChatColor.GREEN + "Your command has been executed and | banks were removed.";
	public static final String EXECUTE_AGAIN_TO_CONFIRM = ChatColor.GOLD + "Execute command again to confirm.";
	public static final String INTEREST_PAYOUT_TRIGGERED = ChatColor.GOLD + "Interest payout event has been triggered.";
	public static final String NICKNAME_SET = ChatColor.GOLD + "Account nickname has been set.";
	public static final String MULTIPLIER_SET = ChatColor.GOLD + "Account multiplier has been set to |.";
	public static final String INTEREST_DELAY_SET = ChatColor.GOLD + "Account interest delay has been set.";
	public static final String NOT_A_NUMBER = ChatColor.RED + "\"|\" is not a number!";
	public static final String NOT_A_FIELD = ChatColor.RED + "\"|\" is not a valid field!";
	
	public static final String NO_PERMISSION = ChatColor.RED + "You do not have permission to ";
	public static final String NO_PERMISSION_BANK_CREATE = NO_PERMISSION + "create a bank.";
	public static final String NO_PERMISSION_BANK_REMOVE = NO_PERMISSION + "remove a bank.";
	public static final String NO_PERMISSION_BANK_INFO = NO_PERMISSION + "view bank info.";
	public static final String NO_PERMISSION_BANK_INFO_VERBOSE = NO_PERMISSION + "view detailed bank info.";
	public static final String NO_PERMISSION_BANK_LIST = NO_PERMISSION + "view a list of banks.";
	public static final String NO_PERMISSION_BANK_LIST_VERBOSE = NO_PERMISSION + "view a detailed list of banks.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE = NO_PERMISSION + "create an account.";
	public static final String NO_PERMISSION_ACCOUNT_SET_NICKNAME = NO_PERMISSION + "set an account nickname.";
	public static final String NO_PERMISSION_ACCOUNT_SET_MULTIPLIER = NO_PERMISSION + "set an account multiplier.";
	public static final String NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY = NO_PERMISSION + "set an account interest delay.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_SET_NICKNAME = NO_PERMISSION + "set someone else's account nickname.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_SET_MULTIPLIER = NO_PERMISSION + "set someone else's account multiplier.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_SET_INTEREST_DELAY = NO_PERMISSION + "set someone else's account interest delay.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_CREATE = NO_PERMISSION + "create an account in someone else's name.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE_PROTECTED = NO_PERMISSION + "create an account on a protected chest.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_REMOVE = NO_PERMISSION + "remove someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_INFO = NO_PERMISSION + "view someone else's account info.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_INFO_VERBOSE = NO_PERMISSION + "view someone else's detailed account info.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_LIST = NO_PERMISSION + "view a list of others' accounts.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_LIST_VERBOSE = NO_PERMISSION + "view a detailed list of others' accounts.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_VIEW = NO_PERMISSION + "open someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_EDIT = NO_PERMISSION + "edit someone else's account contents.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_OTHER = NO_PERMISSION + "extend someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED = NO_PERMISSION + "extend a protected account chest.";
	public static final String NO_PERMISSION_RELOAD = NO_PERMISSION + "reload the plugin.";
	public static final String NO_PERMISSION_CONFIG = NO_PERMISSION + "configure the plugin.";
	public static final String NO_PERMISSION_UPDATE = NO_PERMISSION + "update the plugin.";
	
	public static final String COMMAND_USAGE_ACCOUNT_CREATE = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " create <playername>";
	public static final String COMMAND_USAGE_ACCOUNT_REMOVE = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " remove";
	public static final String COMMAND_USAGE_ACCOUNT_INFO = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " info <detailed>";
	public static final String COMMAND_USAGE_ACCOUNT_LIST = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " list <all> <detailed>";
	public static final String COMMAND_USAGE_ACCOUNT_LIMITS = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " limits";
	public static final String COMMAND_USAGE_ACCOUNT_REMOVEALL = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " removeall <playername / all / cancel>";
	public static final String COMMAND_USAGE_ACCOUNT_SET = ChatColor.RED + "Usage: /" + ACCOUNT_CMD + " set [field] [value]";
	public static final String COMMAND_USAGE_BANK_CREATE = ChatColor.RED + "Usage: /" + BANK_CMD + " create [name] <xyz> <xyz>";
	public static final String COMMAND_USAGE_BANK_REMOVE = ChatColor.RED + "Usage: /" + BANK_CMD + " remove <name>";
	public static final String COMMAND_USAGE_BANK_INFO = ChatColor.RED + "Usage: /" + BANK_CMD + " info <" + BANK_CMD + "name / all>";
	public static final String COMMAND_USAGE_BANK_LIST = ChatColor.RED + "Usage: /" + BANK_CMD + " list";
	//public static final String COMMAND_DESC_BANK_LIMITS = ChatColor.RED + "Usage: /" + bankCmd + " limits";
	public static final String COMMAND_USAGE_BANK_REMOVEALL = ChatColor.RED + "Usage: /" + BANK_CMD + " removeall";
	public static final String COMMAND_USAGE_RELOAD = ChatColor.RED + "Usage: /" + CONTROL_CMD + " reload";
	public static final String COMMAND_USAGE_CONFIG = ChatColor.RED + "Usage: /" + CONTROL_CMD + " config [set / add / remove] [field] <newvalue>";
	public static final String COMMAND_USAGE_UPDATE = ChatColor.RED + "Usage: /" + CONTROL_CMD + " update";
	
	public static final String COMMAND_DESC_ACCOUNT = ChatColor.GOLD + "Create, manage, and remove accounts.";
	public static final String COMMAND_DESC_BANK = ChatColor.GOLD + "Create, manage, and remove banks.";
	public static final String COMMAND_DESC_CONTROL = ChatColor.GOLD + "Configure this plugin.";

	public static final String CHANGED_CONFIG_ADDED = ChatColor.GREEN + "Added the config value.";
	public static final String CHANGED_CONFIG_REMOVED = ChatColor.GREEN + "Removed the config value.";
	public static final String CHANGED_CONFIG_SET = ChatColor.GREEN + "Config value has been set.";

	public static final String OFFLINE_INTEREST_REVENUE = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$| " + ChatColor.GOLD + "in interest while you were offline!";
	public static final String OFFLINE_TRANSACTION_REVENUE = ChatColor.GOLD + "Your account increased in value by " + ChatColor.GREEN + "$| " + ChatColor.GOLD + "while you were offline.";
	public static final String OFFLINE_TRANSACTION_EXPENDITURE = ChatColor.RED + "Your account decreased in value by $| while you were offline.";
	
	public static String getWithValues(String message, Object[] values) {
		String[] messageParts = message.split("\\|");
		if (messageParts.length - 1 != values.length)
			return "Error compiling message.";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
			sb.append(messageParts[i] + values[i]);
		sb.append(messageParts[messageParts.length - 1]);
		return sb.toString();
	}

	public static String getWithValue(String message, Object value) {
		return message.split("\\|")[0] + value + message.split("\\|")[1];
	}

}
