package com.monst.bankingplugin.utils;

import org.bukkit.ChatColor;

public class Messages {
	
	public static final String BANK_CREATED = ChatColor.GREEN + "Bank created!";
	public static final String ACCOUNT_CREATED = "Account created!";
	public static final String BANK_ALREADY_EXISTS = "A bank by that name already exists!";
	public static final String CHEST_ALREADY_ACCOUNT = "That chest is already an account.";
	public static final String CHEST_BLOCKED = "Chest is blocked!";
	public static final String ACCOUNT_REMOVED = "The account has been removed!";
	public static final String BANK_REMOVED = "The bank has been removed!";
	public static final String ACCOUNT_REMOVED_REFUND = "The account has been removed and your account has been refunded.";
	public static final String ACCOUNT_BALANCE_NOT_ZERO = "The account could not be removed because it is not empty.";
	public static final String ACCOUNT_OPENED = "You have opened |'s account chest.";
	public static final String ALL_ACCOUNTS_REMOVED = "All accounts were successfully removed.";
	public static final String CANNOT_BREAK_ACCOUNT = "You cannot break that account chest.";
	public static final String CHEST_NOT_ACCOUNT = "That chest is not an account.";
	public static final String CHEST_NOT_IN_BANK = "That chest is not located within a bank.";
	public static final String NOT_STANDING_IN_BANK = "You must be standing in the bank you want to remove.";
	public static final String ACCOUNT_CREATE_INSUFFICIENT_FUNDS = "You do not have sufficient funds to create an account.";
	//public static final String BANK_CREATE_INSUFFICIENT_FUNDS = "You do not have sufficient funds to create a bank.";
	public static final String DEPOSIT_SUCCESS = "Your deposit was successful.";
	public static final String WITHDRAWAL_SUCCESS = "Your withdrawal was successful.";
	public static final String INTEREST_EARNED = "You have earned interest on your account!";
	public static final String MULTIPLIER_INCREASED = "Your account multiplier has increased to |.";
	public static final String MULTIPLIER_DECREASED = "Your account multiplier has decreased to |.";
	public static final String ERROR_OCCURRED = "An error occurred! ";
	public static final String PLAYER_NOT_FOUND = "No player was found under the name \"|\".";
	public static final String BANK_NOT_FOUND = "No bank was found under the identifier \"|\".";
	public static final String NO_ACCOUNTS_FOUND = "No accounts found under those criteria!";
	public static final String NO_SELECTION_FOUND = "Select a WorldEdit region first.";
	public static final String COORDINATES_PARSE_ERROR = "Invalid coordinates.";
	public static final String NAME_NOT_UNIQUE = "A bank with that name already exists.";
	public static final String SELECTION_NOT_EXCLUSIVE = "Your selection overlaps with another bank.";
	public static final String WORLDEDIT_NOT_ENABLED = "WorldEdit is not currently enabled. Please enter coordinates manually.";
	public static final String CLICK_CHEST_CREATE = "Click a chest to create an account.";
	public static final String CLICK_CHEST_REMOVE = "Click an account chest to remove it.";
	public static final String CLICK_CHEST_INFO = "Click an account chest to view info.";
	public static final String CLICK_CHEST_OPEN = "Click an account chest to open it.";
	public static final String CLICK_TO_CONFIRM = "Click chest again to confirm.";
	public static final String RELOADED_BANKS = "Successfully reloaded | banks and | accounts.";
	// public static final String BANK_LIMIT_REACHED = "You are not allowed to create any more banks!";
	public static final String ACCOUNT_LIMIT = "You own |/| allowed accounts.";
	public static final String ACCOUNT_LIMIT_REACHED = "You are not allowed to create any more accounts!";
	public static final String ACCOUNT_INFO_MULTIPLIER = "Current multiplier: ";
	public static final String ACCOUNT_COMMAND_SCHEDULED = "Your command has been scheduled to execute in | seconds. Use | to cancel.";
	public static final String BANK_COMMAND_SCHEDULED = "Your command has been scheduled to execute in | seconds. Use | to cancel.";
	public static final String SCHEDULED_COMMAND_CANCELLED = "Your scheduled command has been cancelled.";
	public static final String SCHEDULED_COMMAND_NOT_EXIST = "You do not have a currently scheduled command.";
	public static final String PLAYER_COMMAND_ONLY = "Only players can use this command.";
	public static final String ABOUT_TO_REMOVE_ACCOUNTS = "You are about to delete | accounts. ";
	public static final String ABOUT_TO_REMOVE_BANKS = "You are about to delete | banks and | accounts. ";
	public static final String ACCOUNTS_REMOVED = "Your command has been executed and | accounts were removed.";
	public static final String BANKS_REMOVED = "Your command has been executed and | banks were removed.";
	public static final String EXECUTE_AGAIN_TO_CONFIRM = "Execute command again to confirm.";
	
	public static final String NO_PERMISSION = "You do not have permission to ";
	public static final String NO_PERMISSION_BANK = NO_PERMISSION + "manage banks.";
	public static final String NO_PERMISSION_BANK_CREATE = NO_PERMISSION + "create a bank.";
	public static final String NO_PERMISSION_BANK_REMOVE = NO_PERMISSION + "remove a bank.";
	public static final String NO_PERMISSION_BANK_INFO = NO_PERMISSION + "view bank info.";
	public static final String NO_PERMISSION_BANK_INFO_OTHER = NO_PERMISSION + "view someone else's bank info.";
	public static final String NO_PERMISSION_ACCOUNT = NO_PERMISSION + "manage accounts.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE = NO_PERMISSION + "create an account.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE_OTHER = NO_PERMISSION + "create an account in someone else's name.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE_PROTECTED = NO_PERMISSION + "create an account on a protected chest.";
	public static final String NO_PERMISSION_ACCOUNT_REMOVE_OTHER = NO_PERMISSION + "remove |'s account.";
	public static final String NO_PERMISSION_ACCOUNT_INFO_OTHER = NO_PERMISSION + "view |'s account info.";
	public static final String NO_PERMISSION_ACCOUNT_EDIT_OTHER = NO_PERMISSION + "view |'s account info.";
	public static final String NO_PERMISSION_ACCOUNT_LIST_OTHER = NO_PERMISSION + "view a list of others' accounts.";
	public static final String NO_PERMISSION_ACCOUNT_LIST_OTHER_VERBOSE = NO_PERMISSION + "view a detailed list of others' accounts.";
	public static final String NO_PERMISSION_ACCOUNT_VIEW_OTHER = NO_PERMISSION + "look into someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_OTHER = NO_PERMISSION + "extend someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED = NO_PERMISSION + "extend a protected account chest.";
	public static final String NO_PERMISSION_RELOAD = NO_PERMISSION + "reload the plugin.";
	public static final String NO_PERMISSION_CONFIG = NO_PERMISSION + "configure the plugin.";
	public static final String NO_PERMISSION_UPDATE = NO_PERMISSION + "update the plugin.";
	
	public static final String COMMAND_DESC_ACCOUNT = "Manage accounts.";
	public static final String COMMAND_DESC_ACCOUNT_CREATE = "Create an account.";
	public static final String COMMAND_DESC_ACCOUNT_REMOVE = "Remove an account.";
	public static final String COMMAND_DESC_ACCOUNT_INFO = "Display account info.";
	public static final String COMMAND_DESC_ACCOUNT_LIST = "Display a list of all your accounts.";
	public static final String COMMAND_DESC_ACCOUNT_LIMITS = "Display number of allowed accounts.";
	public static final String COMMAND_DESC_ACCOUNT_OPEN = "Open an account chest.";
	public static final String COMMAND_DESC_ACCOUNT_REMOVEALL = "Remove all accounts.";
	public static final String COMMAND_DESC_ACCOUNT_RELOAD = "Reload accounts.";
	public static final String COMMAND_DESC_BANK = "Manage banks.";
	public static final String COMMAND_DESC_BANK_CREATE = "Create a bank.";
	public static final String COMMAND_DESC_BANK_REMOVE = "Remove a bank.";
	public static final String COMMAND_DESC_BANK_INFO = "Display bank info.";
	public static final String COMMAND_DESC_BANK_LIST = "Show a list of all banks.";
	public static final String COMMAND_DESC_BANK_LIMITS = "Display number of allowed banks.";
	public static final String COMMAND_DESC_BANK_REMOVEALL = "Remove all banks and all accounts.";
	public static final String COMMAND_DESC_BANK_RELOAD = "Reload banks and all the accounts associated with them.";
	public static final String COMMAND_DESC_CONTROL = "Manage this plugin.";
	public static final String COMMAND_DESC_RELOAD = "Reload the plugin.";
	public static final String COMMAND_DESC_CONFIG = "Change the plugin configuration.";
	public static final String COMMAND_DESC_UPDATE = "Update the plugin.";
	
	public static final String CHANGED_CONFIG_ADDED = "Added the config value";
	public static final String CHANGED_CONFIG_REMOVED = "Removed the config value.";
	public static final String CHANGED_CONFIG_SET = "Set the config value.";

	public static final String OFFLINE_INTEREST_REVENUE = "You earned $| in interest while you were offline!";
	public static final String OFFLINE_TRANSACTION_REVENUE = "Your account increased in value by $| while you were offline.";
	public static final String OFFLINE_TRANSACTION_EXPENDITURE = "Your account decreased in value by $| while you were offline.";
	
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
