package com.monst.bankingplugin.utils;

import org.bukkit.ChatColor;

import com.monst.bankingplugin.config.Config;

public class Messages {
	
	// ACCOUNT, BANK CREATION
	public static final String BANK_CREATED = ChatColor.GOLD + "Bank created!";
	public static final String ACCOUNT_CREATED = ChatColor.GOLD + "Account created!";
	public static final String ACCOUNT_MIGRATED = ChatColor.GOLD + "Account migrated!";
	public static final String ACCOUNT_CREATE_FEE_PAID = ChatColor.GOLD + "You have been charged " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to create an account.";
	public static final String ACCOUNT_EXTEND_FEE_PAID = ChatColor.GOLD + "You have been charged " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to extend your account.";
	public static final String ACCOUNT_CREATE_FEE_RECEIVED = ChatColor.GOLD + "%s has paid you " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to create an account at your bank.";
	public static final String ACCOUNT_EXTEND_FEE_RECEIVED = ChatColor.GOLD + "%s has paid you " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to extend their account at your bank.";
	public static final String BANK_CREATE_FEE_PAID = ChatColor.GOLD + "You have been charged " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to create a bank.";
	public static final String NAME_NOT_UNIQUE = ChatColor.RED + "A bank with that name already exists.";
	public static final String NAME_ALREADY = ChatColor.RED + "The bank name was not changed.";
	public static final String NAME_NOT_ALLOWED = ChatColor.RED + "Name is not allowed.";
	public static final String CHEST_ALREADY_ACCOUNT = ChatColor.RED + "That chest is already an account!";
	public static final String CHEST_BLOCKED = ChatColor.RED + "Chest is blocked!";
	
	// ACCOUNT, BANK REMOVAL
	public static final String ACCOUNT_REMOVED = ChatColor.GOLD + "The account has been removed.";
	public static final String BANK_REMOVED = ChatColor.GOLD + "The bank has been removed.";
	public static final String BANK_RESIZED = ChatColor.GOLD + "The bank has been resized.";
	public static final String BANK_FIELD_SET = ChatColor.GOLD + "Field " + ChatColor.AQUA + "%s" + ChatColor.GOLD + " has been set to " + ChatColor.GREEN + "%s" + ChatColor.GOLD + " at bank " + ChatColor.AQUA + "%s" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_REIMBURSEMENT_RECEIVED = ChatColor.GOLD + "You were reimbursed " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_REIMBURSEMENT_PAID = ChatColor.GOLD + "You reimbursed %s " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_BALANCE_NOT_ZERO = ChatColor.RED + "That account is not empty. Are you sure?";
	public static final String ACCOUNTS_REMOVED = ChatColor.GREEN + "%d account%s %s successfully removed.";
	public static final String BANKS_REMOVED = ChatColor.GREEN + "%d bank%s and %d account%s were removed.";
	
	// NOTIFICATIONS
	public static final String ACCOUNT_OPENED = ChatColor.GOLD + "You opened %s's account chest.";
	public static final String REVENUE_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s " + ChatColor.GOLD + "in revenue from %s!";
	public static final String INTEREST_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s " + ChatColor.GOLD + "in interest on %d account%s!";
	public static final String INTEREST_PAID = ChatColor.RED + "You paid " + ChatColor.GREEN + "$%s " + ChatColor.RED + "in interest on %d account%s!";
	public static final String LOW_BALANCE_FEE_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " in low balance fees on %d account%s.";
	public static final String LOW_BALANCE_FEE_PAID = ChatColor.RED + "You paid " + ChatColor.GREEN + "$%s" + ChatColor.RED + " in low balance fees on %d account%s.";
	public static final String MULTIPLIER_INCREASED = ChatColor.GOLD + "Your account multiplier has increased to " + ChatColor.GREEN + "$%d."; // XXX
	public static final String MULTIPLIER_DECREASED = ChatColor.RED + "Your account multiplier has decreased to " + ChatColor.GREEN + "$%d.";
	public static final String ACCOUNT_LIMIT = ChatColor.GOLD + "You own %d/%s allowed accounts.";
	public static final String BANK_LIMIT = ChatColor.GOLD + "You own %d/%s allowed banks.";
	public static final String ABOUT_TO_REMOVE_ACCOUNTS = ChatColor.GOLD + "You are about to delete %d account%s.";
	public static final String ABOUT_TO_REMOVE_BANKS = ChatColor.GOLD + "You are about to delete %d bank%s and %d account%s.";
	public static final String ABOUT_TO_TRANSFER = ChatColor.RED + "You are about to transfer ownership of %s account to %s. Are you sure?";

	
	// SUCCESS NOTIFICATIONS
	public static final String INTEREST_PAYOUT_TRIGGERED = ChatColor.GOLD + "Interest payout event triggered.";
	public static final String NICKNAME_SET = ChatColor.GOLD + "Account nickname has been set.";
	public static final String MULTIPLIER_SET = ChatColor.GOLD + "Account multiplier has been set to %d.";
	public static final String INTEREST_DELAY_SET = ChatColor.GOLD + "Account interest delay has been set.";
	public static final String ADDED_COOWNER = ChatColor.GOLD + "%s has been added as a co-owner.";
	public static final String REMOVED_COOWNER = ChatColor.GOLD + "%s has been removed as a co-owner.";
	public static final String RELOADED_BANKS = ChatColor.GOLD + "Successfully reloaded BankingPlugin.";
	public static final String ACCOUNT_DEPOSIT = ChatColor.GOLD + "You have deposited " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " into %s account.";
	public static final String ACCOUNT_WITHDRAWAL = ChatColor.GOLD + "You have withdrawn " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " from %s account.";
	public static final String ACCOUNT_NEW_BALANCE = ChatColor.GOLD + "Your new balance is " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + ".";
	public static final String BANK_SELECTED = ChatColor.GOLD + "Bank region selected as %s.";
	public static final String OWNERSHIP_TRANSFERRED = ChatColor.GOLD + "Ownership successfully transferred to %s.";
	public static final String NAME_CHANGED = ChatColor.GOLD + "Bank name was successfully changed.";
	
	// DENY NOTIFICATIONS
	public static final String MUST_BE_OWNER = ChatColor.RED + "You must be the owner of the account to do that.";
	public static final String CANNOT_BREAK_ACCOUNT = ChatColor.RED + "You cannot break that account chest.";
	public static final String CHEST_NOT_IN_BANK = ChatColor.RED + "That chest is not located within a bank.";
	public static final String NOT_STANDING_IN_BANK = ChatColor.RED + "You must stand in or specify the name of a bank.";
	public static final String ACCOUNT_CREATE_INSUFFICIENT_FUNDS = ChatColor.RED + "You do not have sufficient funds to create an account at this bank.";
	public static final String BANK_NOT_FOUND = ChatColor.RED + "No bank was found under the identifier \"%s\".";
	public static final String NO_ACCOUNTS_FOUND = ChatColor.RED + "No accounts found!";
	public static final String NO_PLAYER_ACCOUNTS = ChatColor.RED + "That player does not own any accounts.";
	public static final String NO_BANK_ACCOUNTS = ChatColor.RED + "There are no accounts at that bank.";
	public static final String NO_BANKS = ChatColor.RED + "There are no banks to list.";
	public static final String NO_SELECTION_FOUND = ChatColor.RED + "Select a WorldEdit region first.";
	public static final String COORDINATES_PARSE_ERROR = ChatColor.RED + "Invalid coordinates.";
	public static final String SELECTION_NOT_EXCLUSIVE = ChatColor.RED + "Your selection overlaps with another bank.";
	public static final String SELECTION_CUTS_ACCOUNTS = ChatColor.RED + "Your selection does not contain all accounts at this bank.";
	public static final String WORLDEDIT_NOT_ENABLED = ChatColor.RED + "WorldEdit is not currently enabled. Please enter coordinates manually.";
	public static final String ALREADY_ADMIN_BANK = ChatColor.RED + "That bank is already an admin bank.";
	public static final String ALREADY_OWNER_BANK = ChatColor.RED + "%s %s already owner of that bank.";
	public static final String ALREADY_OWNER_ACCOUNT = ChatColor.RED + "%s %s already owner of that account.";
	public static final String NOT_A_COOWNER = ChatColor.RED + "%s is not a co-owner of that account.";
	public static final String ALREADY_A_COOWNER = ChatColor.RED + "%s is already a co-owner of that account.";
	public static final String BANK_CREATE_INSUFFICIENT_FUNDS = ChatColor.RED + "You do not have sufficient funds to create a bank.";
	public static final String FIELD_NOT_OVERRIDABLE = ChatColor.RED + "That field cannot be overridden!";
	public static final String NOT_A_NUMBER = ChatColor.RED + "\"%s\" is not a number!";
	public static final String NOT_AN_INTEGER = ChatColor.RED + "\"%s\" is not an integer!";
	public static final String NOT_A_BOOLEAN = ChatColor.RED + "\"%s\" is not a boolean value!";
	public static final String NOT_A_FIELD = ChatColor.RED + "\"%s\" is not a valid field!";
	public static final String NOT_A_LIST = ChatColor.RED + "\"%s\" is not a parsable list!";
	public static final String ERROR_OCCURRED = ChatColor.DARK_RED + "An error occurred! ";
	public static final String PLAYER_NOT_FOUND = ChatColor.RED + "No player was found under the name \"%s\".";
	public static final String BANK_LIMIT_REACHED = "You are not allowed to create any more banks!";
	public static final String ACCOUNT_LIMIT_REACHED = ChatColor.RED + "You are not allowed to create any more accounts!";
	public static final String NO_SELF_BANKING = ChatColor.RED + "You are not allowed to create an account at your own bank.";
	public static final String PER_BANK_ACCOUNT_LIMIT_REACHED = ChatColor.RED + "You are not allowed to create another account at this bank.";
	public static final String PLAYER_COMMAND_ONLY = ChatColor.RED + "Only players can use this command.";
	public static final String SAME_ACCOUNT = ChatColor.RED + "Nothing happened. That is the same account.";
	public static final String SELECTION_TOO_LARGE = ChatColor.RED + "You are not allowed to create a bank of that size, as it would exceed the volume limit of %d blocks by %d.";
	public static final String SELECTION_TOO_SMALL = ChatColor.RED + "You are not allowed to create a bank of that size, as it would fall short of the minimum volume of %d blocks by %d.";
	public static final String SELECTION_TOO_LARGE_RESIZE = ChatColor.RED + "You are not allowed to resize a bank to that size, as it would exceed the volume limit of %d blocks by %d.";
	public static final String SELECTION_TOO_SMALL_RESIZE = ChatColor.RED + "You are not allowed to resize a bank to that size, as it would fall short of the minimum volume of %d blocks by %d.";
	
	// CLICK PROMPTS
	public static final String CLICK_CHEST_CREATE = ChatColor.GOLD + "Click a chest to create an account.";
	public static final String CLICK_CHEST_REMOVE = ChatColor.GOLD + "Click an account chest to remove.";
	public static final String CLICK_CHEST_INFO = ChatColor.GOLD + "Click an account chest to view info.";
	public static final String CLICK_CHEST_SET = ChatColor.GOLD + "Click an account chest to set.";
	public static final String CLICK_CHEST_TRUST = ChatColor.GOLD + "Click an account chest to add %s as a co-owner.";
	public static final String CLICK_CHEST_UNTRUST = ChatColor.GOLD + "Click an account chest to remove %s as a co-owner.";
	public static final String CLICK_CHEST_MIGRATE_FIRST = ChatColor.GOLD + "Click an account chest to migrate to another chest.";
	public static final String CLICK_CHEST_MIGRATE_SECOND = ChatColor.GOLD + "Click a chest to migrate the account to.";
	public static final String CLICK_CHEST_TRANSFER = ChatColor.GOLD + "Click an account chest to transfer ownership to %s.";
	public static final String CLICK_TO_CONFIRM = ChatColor.GOLD + "Click chest again to confirm.";
	public static final String EXECUTE_AGAIN_TO_CONFIRM = ChatColor.GOLD + " Execute command again to confirm.";
	
	
	private static final String NO_PERMISSION = ChatColor.RED + "You do not have permission to ";
	public static final String NO_PERMISSION_ACCOUNT_CREATE = NO_PERMISSION + "create an account.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE_PROTECTED = NO_PERMISSION + "create an account on a protected chest.";
	public static final String NO_PERMISSION_ACCOUNT_TRUST = NO_PERMISSION + "add a co-owner to an account.";
	public static final String NO_PERMISSION_ACCOUNT_TRUST_OTHER = NO_PERMISSION + "add a co-owner to someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_UNTRUST = NO_PERMISSION + "remove a co-owner from an account.";
	public static final String NO_PERMISSION_ACCOUNT_UNTRUST_OTHER = NO_PERMISSION + "remove a co-owner from someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_REMOVE_OTHER = NO_PERMISSION + "remove someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_LIST_OTHER = NO_PERMISSION + "view a list of others' accounts.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_VIEW = NO_PERMISSION + "view someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_EDIT = NO_PERMISSION + "edit someone else's account contents.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE = NO_PERMISSION + "migrate an account.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE_OTHER = NO_PERMISSION + "migrate someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE_BANK = NO_PERMISSION + "migrate an account to another bank.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED = NO_PERMISSION + "migrate an account to a protected chest.";
	public static final String NO_PERMISSION_ACCOUNT_TRANSFER = NO_PERMISSION + "transfer ownership of an account.";
	public static final String NO_PERMISSION_ACCOUNT_TRANSFER_OTHER = NO_PERMISSION + "transfer ownership of another player's account.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_OTHER = NO_PERMISSION + "extend someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_SET_NICKNAME = NO_PERMISSION + "set an account nickname.";
	public static final String NO_PERMISSION_ACCOUNT_SET_NICKNAME_OTHER = NO_PERMISSION + "set someone else's account nickname.";
	public static final String NO_PERMISSION_ACCOUNT_SET_MULTIPLIER = NO_PERMISSION + "set an account multiplier.";
	public static final String NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY = NO_PERMISSION + "set an account interest delay.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_CREATE = NO_PERMISSION + "create an account in someone else's name."; // XXX
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED = NO_PERMISSION + "extend a protected account chest.";
	
	public static final String NO_PERMISSION_BANK_CREATE = NO_PERMISSION + "create a bank.";
	public static final String NO_PERMISSION_BANK_CREATE_ADMIN = NO_PERMISSION + "create an admin bank.";
	public static final String NO_PERMISSION_BANK_REMOVE_OTHER = NO_PERMISSION + "remove someone else's bank.";
	public static final String NO_PERMISSION_BANK_REMOVE_ADMIN = NO_PERMISSION + "remove an admin bank.";
	public static final String NO_PERMISSION_BANK_REMOVEALL = NO_PERMISSION + "remove all banks.";
	public static final String NO_PERMISSION_BANK_RESIZE = NO_PERMISSION + "resize a bank.";
	public static final String NO_PERMISSION_BANK_RESIZE_OTHER = NO_PERMISSION + "resize someone else's bank.";
	public static final String NO_PERMISSION_BANK_RESIZE_ADMIN = NO_PERMISSION + "resize an admin bank.";
	public static final String NO_PERMISSION_BANK_TRANSFER = NO_PERMISSION + "transfer ownership of a bank.";
	public static final String NO_PERMISSION_BANK_TRANSFER_OTHER = NO_PERMISSION + "transfer ownership of another player's bank.";
	public static final String NO_PERMISSION_BANK_TRANSFER_ADMIN = NO_PERMISSION + "transfer ownership of an admin bank.";
	public static final String NO_PERMISSION_BANK_TRANSFER_TO_ADMIN = NO_PERMISSION + "transfer ownership of a bank to the admins.";
	public static final String NO_PERMISSION_BANK_SET_OTHER = NO_PERMISSION + "configure someone else's bank.";
	public static final String NO_PERMISSION_BANK_SET_ADMIN = NO_PERMISSION + "configure an admin bank.";
	public static final String NO_PERMISSION_BANK_SELECT = NO_PERMISSION + "select a bank.";
	
	public static final String NO_PERMISSION_RELOAD = NO_PERMISSION + "reload the plugin.";
	public static final String NO_PERMISSION_CONFIG = NO_PERMISSION + "configure the plugin.";
	public static final String NO_PERMISSION_UPDATE = NO_PERMISSION + "update the plugin.";
	public static final String NO_PERMISSION_PAY_INTEREST = NO_PERMISSION + "trigger an interest payment.";


	private static final String USAGE_PREFIX_ACCOUNT = ChatColor.RED + "/" + Config.mainCommandNameAccount + " ";
	private static final String USAGE_PREFIX_BANK = ChatColor.RED + "/" + Config.mainCommandNameBank + " ";
	private static final String USAGE_PREFIX_CONTROL = ChatColor.RED + "/" + Config.mainCommandNameControl + " ";
	public static final String COMMAND_USAGE_ACCOUNT_CREATE = USAGE_PREFIX_ACCOUNT + "create";
	public static final String COMMAND_USAGE_ACCOUNT_REMOVE = USAGE_PREFIX_ACCOUNT + "remove";
	public static final String COMMAND_USAGE_ACCOUNT_INFO = USAGE_PREFIX_ACCOUNT + "info";
	public static final String COMMAND_USAGE_ACCOUNT_LIST = USAGE_PREFIX_ACCOUNT + "list <all>";
	public static final String COMMAND_USAGE_ACCOUNT_LIMITS = USAGE_PREFIX_ACCOUNT + "limits";
	public static final String COMMAND_USAGE_ACCOUNT_SET = USAGE_PREFIX_ACCOUNT + "set [field] [value]";
	public static final String COMMAND_USAGE_ACCOUNT_TRUST = USAGE_PREFIX_ACCOUNT + "trust [playername]";
	public static final String COMMAND_USAGE_ACCOUNT_UNTRUST = USAGE_PREFIX_ACCOUNT + "untrust [playername]";
	public static final String COMMAND_USAGE_ACCOUNT_MIGRATE = USAGE_PREFIX_ACCOUNT + "migrate";
	public static final String COMMAND_USAGE_ACCOUNT_TRANSFER = USAGE_PREFIX_ACCOUNT + "transfer [playername]";
	public static final String COMMAND_USAGE_BANK_CREATE = USAGE_PREFIX_BANK + "create <xyz> <xyz> <admin>";
	public static final String COMMAND_USAGE_BANK_REMOVE = USAGE_PREFIX_BANK + "remove <bankname>";
	public static final String COMMAND_USAGE_BANK_INFO = USAGE_PREFIX_BANK + "info";
	public static final String COMMAND_USAGE_BANK_LIST = USAGE_PREFIX_BANK + "list";
	public static final String COMMAND_USAGE_BANK_LIMITS = USAGE_PREFIX_BANK + "limits";
	public static final String COMMAND_USAGE_BANK_REMOVEALL = USAGE_PREFIX_BANK + "removeall";
	public static final String COMMAND_USAGE_BANK_RESIZE = USAGE_PREFIX_BANK + "resize [bankname] <xyz> <xyz>";
	public static final String COMMAND_USAGE_BANK_SET = USAGE_PREFIX_BANK + "set <bankname> [field] [value]";
	public static final String COMMAND_USAGE_BANK_SELECT = USAGE_PREFIX_BANK + "select";
	public static final String COMMAND_USAGE_BANK_TRANSFER = USAGE_PREFIX_BANK + "transfer <bankname> [playername]";
	public static final String COMMAND_USAGE_VERSION = USAGE_PREFIX_CONTROL + "version";
	public static final String COMMAND_USAGE_RELOAD = USAGE_PREFIX_CONTROL + "reload";
	public static final String COMMAND_USAGE_CONFIG = USAGE_PREFIX_CONTROL + "config [set | add | remove] [field] <newvalue>";
	public static final String COMMAND_USAGE_UPDATE = USAGE_PREFIX_CONTROL + "update";
	public static final String COMMAND_USAGE_PAYINTEREST = USAGE_PREFIX_CONTROL + "payinterest";
	
	public static final String COMMAND_DESC_ACCOUNT = ChatColor.GOLD + "Create, manage, and remove accounts.";
	public static final String COMMAND_DESC_BANK = ChatColor.GOLD + "Create, manage, and remove banks.";
	public static final String COMMAND_DESC_CONTROL = ChatColor.GOLD + "Configure this plugin.";

	public static final String CHANGED_CONFIG_ADDED = ChatColor.GOLD + "Added the config value " + ChatColor.AQUA + "%s" + ChatColor.GOLD + ".";
	public static final String CHANGED_CONFIG_REMOVED = ChatColor.GOLD + "Removed the config value " + ChatColor.AQUA + "%s" + ChatColor.GOLD + ".";
	public static final String CHANGED_CONFIG_SET = ChatColor.GOLD + "Config value " + ChatColor.AQUA + "%s" + ChatColor.GOLD + " has been set to " + ChatColor.GREEN + "%s" + ChatColor.GOLD + ".";

	public static final String OFFLINE_INTEREST_REVENUE = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " in interest while you were offline!";
	public static final String OFFLINE_TRANSACTION_REVENUE = ChatColor.GOLD + "Your account increased in value by " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " while you were offline.";
	public static final String OFFLINE_TRANSACTION_EXPENDITURE = ChatColor.RED + "Your account decreased in value by $%s while you were offline.";
	
}
