package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.config.Config;
import org.bukkit.ChatColor;

public class Messages {
	
	// ACCOUNT, BANK CREATION
	public static final String BANK_CREATED = ChatColor.GOLD + "Bank created!";
	public static final String ACCOUNT_CREATED = ChatColor.GOLD + "Account created!";
	public static final String ACCOUNT_MIGRATED = ChatColor.GOLD + "Account migrated!";
	public static final String ACCOUNT_RECOVERED = ChatColor.GOLD + "Account recovered!";
	public static final String ACCOUNT_CREATE_FEE_PAID = ChatColor.GOLD + "You have been charged " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to create an account.";
	public static final String ACCOUNT_EXTEND_FEE_PAID = ChatColor.GOLD + "You have been charged " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to extend an account.";
	public static final String ACCOUNT_CREATE_FEE_RECEIVED = ChatColor.GOLD + "%s has paid you " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to create an account at your bank.";
	public static final String ACCOUNT_EXTEND_FEE_RECEIVED = ChatColor.GOLD + "%s has paid you " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " to extend an account at your bank.";
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
	public static final String BANK_FIELD_SET = ChatColor.GOLD + "%s changed " + ChatColor.AQUA + "%s" + ChatColor.GOLD + " from " + ChatColor.GREEN + "%s" + ChatColor.GOLD + " to " + ChatColor.GREEN + "%s" + ChatColor.GOLD + " at bank " + ChatColor.AQUA + "%s" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_REIMBURSEMENT_RECEIVED = ChatColor.GOLD + "You have been reimbursed " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_REIMBURSEMENT_PAID = ChatColor.GOLD + "You reimbursed %s " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + ".";
	public static final String ACCOUNT_BALANCE_NOT_ZERO = ChatColor.RED + "That account is not empty. Are you sure?";
	public static final String ACCOUNTS_REMOVED = ChatColor.GREEN + "%d account%s successfully removed.";
	public static final String BANKS_REMOVED = ChatColor.GREEN + "%d bank%s and %d account%s were removed.";
	
	// NOTIFICATIONS
	public static final String ACCOUNT_OPENED = ChatColor.GOLD + "You opened %s's account chest.";
	public static final String REVENUE_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s " + ChatColor.GOLD + "in revenue from %s!";
	public static final String INTEREST_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s " + ChatColor.GOLD + "in interest on %d account%s!";
	public static final String INTEREST_PAID = ChatColor.RED + "You paid " + ChatColor.GREEN + "$%s " + ChatColor.RED + "in interest on %d account%s!";
	public static final String LOW_BALANCE_FEE_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " in low balance fees on %d account%s.";
	public static final String LOW_BALANCE_FEE_PAID = ChatColor.RED + "You paid " + ChatColor.GREEN + "$%s" + ChatColor.RED + " in low balance fees on %d account%s.";
	public static final String MULTIPLIER_DECREASED = ChatColor.RED + "Your account multiplier has decreased to " + ChatColor.GREEN + "%d.";
	public static final String ACCOUNT_LIMIT = ChatColor.GOLD + "You own %d/%s allowed accounts.";
	public static final String BANK_LIMIT = ChatColor.GOLD + "You own %d/%s allowed banks.";
	public static final String ABOUT_TO_REMOVE_ACCOUNTS = ChatColor.GOLD + "You are about to remove %d account%s.";
	public static final String ABOUT_TO_REMOVE_BANKS = ChatColor.GOLD + "You are about to remove %d bank%s and %d account%s.";
	public static final String ABOUT_TO_TRANSFER = ChatColor.RED + "You are about to transfer ownership of %s to %s.";

	public static final String OFFLINE_INTEREST_EARNED = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " in interest while you were offline!";
	public static final String OFFLINE_BANK_PROFIT = ChatColor.GOLD + "You earned " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " in bank profit while you were offline.";
	public static final String OFFLINE_BANK_LOSS = ChatColor.RED + "You lost $%s in bank losses while you were offline.";

	public static final String PLAYER_REMOVED_BANK = ChatColor.RED + "%s has removed bank %s.";

	
	// SUCCESS NOTIFICATIONS
	public static final String INTEREST_PAYOUT_TRIGGERED = ChatColor.GOLD + "Interest payout event triggered at %d bank%s.";
	public static final String ACCOUNT_RENAMED = ChatColor.GOLD + "Account has been renamed.";
	public static final String MULTIPLIER_SET = ChatColor.GOLD + "The account multiplier has been set to %d.";
	public static final String INTEREST_DELAY_SET = ChatColor.GOLD + "The delay until next payout has been set to %d.";
	public static final String REMAINING_OFFLINE_PAYOUTS_SET = ChatColor.GOLD + "The number of remaining offline payouts has been set to %d.";
	public static final String REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET_SET = ChatColor.GOLD + "The number of remaining offline payouts until multiplier reset has been set to %d.";
	public static final String ADDED_COOWNER = ChatColor.GOLD + "%s added as a co-owner.";
	public static final String REMOVED_COOWNER = ChatColor.GOLD + "%s removed as a co-owner.";
	public static final String RELOADED_PLUGIN = ChatColor.GOLD + "Successfully reloaded BankingPlugin.";
	public static final String ACCOUNT_DEPOSIT = ChatColor.GOLD + "You have deposited " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " into %s account.";
	public static final String ACCOUNT_WITHDRAWAL = ChatColor.GOLD + "You have withdrawn " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + " from %s account.";
	public static final String ACCOUNT_NEW_BALANCE = ChatColor.GOLD + "Your new balance is " + ChatColor.GREEN + "$%s" + ChatColor.GOLD + ".";
	public static final String BANK_SELECTED = ChatColor.GOLD + "Bank region selected as %s.";
	public static final String OWNERSHIP_TRANSFERRED = ChatColor.GOLD + "%s transferred %s ownership of %s" + ChatColor.GOLD + ".";
	public static final String NAME_CHANGED = ChatColor.GOLD + "Bank name was successfully changed.";
	
	// DENY NOTIFICATIONS
	public static final String MUST_BE_OWNER = ChatColor.RED + "You must be the owner of the account to do that.";
	public static final String CANNOT_BREAK_ACCOUNT = ChatColor.RED + "You cannot break that account chest.";
	public static final String CHEST_NOT_IN_BANK = ChatColor.RED + "That chest is not located within a bank.";
	public static final String NOT_STANDING_IN_BANK = ChatColor.RED + "You must stand in or specify the name of a bank.";
	public static final String ACCOUNT_CREATE_INSUFFICIENT_FUNDS = ChatColor.RED + "You do not have sufficient funds to create an account at this bank.";
	public static final String BANK_NOT_FOUND = ChatColor.RED + "No bank was found with the name or ID \"%s\".";
	public static final String NONE_FOUND = ChatColor.RED + "Found no %s to %s.";
	public static final String SELECT_WORLDEDIT_REGION = ChatColor.RED + "Select a WorldEdit region first.";
	public static final String COORDINATES_PARSE_ERROR = ChatColor.RED + "Invalid coordinates.";
	public static final String WORLD_DISABLED = ChatColor.RED + "BankingPlugin has been disabled in this world.";
	public static final String SELECTION_OVERLAPS_EXISTING = ChatColor.RED + "Your selection overlaps with another bank.";
	public static final String SELECTION_CUTS_ACCOUNTS = ChatColor.RED + "Your selection does not contain all accounts at this bank.";
	public static final String WORLDEDIT_NOT_ENABLED = ChatColor.RED + "WorldEdit is not currently enabled. Please enter coordinates manually.";
	public static final String ALREADY_ADMIN_BANK = ChatColor.RED + "That bank is already an admin bank.";
	public static final String ALREADY_OWNER = ChatColor.RED + "%s already owner of that %s.";
	public static final String ALREADY_COOWNER = ChatColor.RED + "%s already a co-owner of that %s.";
	public static final String NOT_A_COOWNER = ChatColor.RED + "%s not a co-owner of that %s.";
	public static final String BANK_CREATE_INSUFFICIENT_FUNDS = ChatColor.RED + "You do not have sufficient funds to create a bank.";
	public static final String FIELD_NOT_OVERRIDABLE = ChatColor.RED + "That field cannot be overridden!";
	public static final String NOT_A_NUMBER = ChatColor.RED + "\"%s\" is not a number!";
	public static final String NOT_AN_INTEGER = ChatColor.RED + "\"%s\" is not an integer!";
	public static final String NOT_A_FIELD = ChatColor.RED + "\"%s\" is not a valid field!";
	public static final String NOT_A_LIST = ChatColor.RED + "\"%s\" could not be parsed!";
	public static final String ERROR_OCCURRED = ChatColor.RED + "An error occurred! ";
	public static final String PLAYER_NOT_FOUND = ChatColor.RED + "No player was found under the name \"%s\".";
	public static final String BANK_LIMIT_REACHED = "You are not allowed to create any more banks!";
	public static final String ACCOUNT_LIMIT_REACHED = ChatColor.RED + "You are not allowed to create any more accounts!";
	public static final String NO_SELF_BANKING = ChatColor.RED + "You are not allowed to create an account at your own bank.";
	public static final String PER_BANK_ACCOUNT_LIMIT_REACHED = ChatColor.RED + "You are not allowed to create another account at this bank.";
	public static final String PLAYER_COMMAND_ONLY = ChatColor.RED + "Only players can use that command.";
	public static final String SAME_ACCOUNT = ChatColor.RED + "Nothing happened. That is the same account.";
	public static final String SELECTION_TOO_LARGE = ChatColor.RED + "You are not allowed to create a bank of that size, as it would exceed the volume limit of %d blocks by %d.";
	public static final String SELECTION_TOO_SMALL = ChatColor.RED + "You are not allowed to create a bank of that size, as it would fall short of the minimum volume of %d blocks by %d.";
	public static final String SELECTION_TOO_LARGE_RESIZE = ChatColor.RED + "You are not allowed to resize a bank to that size, as it would exceed the volume limit of %d blocks by %d.";
	public static final String SELECTION_TOO_SMALL_RESIZE = ChatColor.RED + "You are not allowed to resize a bank to that size, as it would fall short of the minimum volume of %d blocks by %d.";
	
	// CLICK PROMPTS
	public static final String CLICK_CHEST = ChatColor.GOLD + "Click a chest to %s.";
	public static final String CLICK_ACCOUNT_CHEST = ChatColor.GOLD + "Click an account chest to %s.";
	public static final String CLICK_TO_CONFIRM = ChatColor.GOLD + "Click chest again to confirm.";
	public static final String EXECUTE_AGAIN_TO_CONFIRM = ChatColor.GOLD + "Execute command again to confirm.";
	
	
	private static final String NO_PERMISSION = ChatColor.RED + "You do not have permission to ";
	public static final String NO_PERMISSION_ACCOUNT_CREATE = NO_PERMISSION + "create an account.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE_PROTECTED = NO_PERMISSION + "create an account on a protected chest.";
	public static final String NO_PERMISSION_ACCOUNT_TRUST = NO_PERMISSION + "add a co-owner to an account.";
	public static final String NO_PERMISSION_ACCOUNT_TRUST_OTHER = NO_PERMISSION + "add a co-owner to someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_UNTRUST = NO_PERMISSION + "remove a co-owner from an account.";
	public static final String NO_PERMISSION_ACCOUNT_UNTRUST_OTHER = NO_PERMISSION + "remove a co-owner from someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_REMOVE_OTHER = NO_PERMISSION + "remove someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_REMOVEALL = NO_PERMISSION + "remove all accounts.";
	public static final String NO_PERMISSION_ACCOUNT_LIST_OTHER = NO_PERMISSION + "view a list of others' accounts.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_VIEW = NO_PERMISSION + "view someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_OTHER_EDIT = NO_PERMISSION + "edit someone else's account contents.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE = NO_PERMISSION + "migrate an account.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE_OTHER = NO_PERMISSION + "migrate someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE_BANK = NO_PERMISSION + "migrate an account to another bank.";
	public static final String NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED = NO_PERMISSION + "migrate an account to a protected chest.";
	public static final String NO_PERMISSION_ACCOUNT_RENAME = NO_PERMISSION + "rename an account.";
	public static final String NO_PERMISSION_ACCOUNT_RENAME_OTHER = NO_PERMISSION + "rename someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_TRANSFER = NO_PERMISSION + "transfer ownership of an account.";
	public static final String NO_PERMISSION_ACCOUNT_TRANSFER_OTHER = NO_PERMISSION + "transfer ownership of another player's account.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_OTHER = NO_PERMISSION + "extend someone else's account.";
	public static final String NO_PERMISSION_ACCOUNT_SET = NO_PERMISSION + "configure internal account values.";
	public static final String NO_PERMISSION_ACCOUNT_CREATE_OTHER = NO_PERMISSION + "create an account in someone else's name.";
	public static final String NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED = NO_PERMISSION + "extend a protected account chest.";
	
	public static final String NO_PERMISSION_BANK_CREATE = NO_PERMISSION + "create a bank.";
	public static final String NO_PERMISSION_BANK_CREATE_ADMIN = NO_PERMISSION + "create an admin bank.";
	public static final String NO_PERMISSION_BANK_CREATE_PROTECTED = NO_PERMISSION + "create a bank there.";
	public static final String NO_PERMISSION_BANK_REMOVE_OTHER = NO_PERMISSION + "remove someone else's bank.";
	public static final String NO_PERMISSION_BANK_REMOVE_ADMIN = NO_PERMISSION + "remove an admin bank.";
	public static final String NO_PERMISSION_BANK_REMOVEALL = NO_PERMISSION + "remove all banks.";
	public static final String NO_PERMISSION_BANK_RESIZE = NO_PERMISSION + "resize a bank.";
	public static final String NO_PERMISSION_BANK_RESIZE_OTHER = NO_PERMISSION + "resize someone else's bank.";
	public static final String NO_PERMISSION_BANK_RESIZE_ADMIN = NO_PERMISSION + "resize an admin bank.";
	public static final String NO_PERMISSION_BANK_TRUST = NO_PERMISSION + "add a co-owner to a bank.";
	public static final String NO_PERMISSION_BANK_TRUST_OTHER = NO_PERMISSION + "add a co-owner to someone else's bank.";
	public static final String NO_PERMISSION_BANK_TRUST_ADMIN = NO_PERMISSION + "add a co-owner to an admin bank.";
	public static final String NO_PERMISSION_BANK_UNTRUST = NO_PERMISSION + "remove a co-owner from a bank.";
	public static final String NO_PERMISSION_BANK_UNTRUST_OTHER = NO_PERMISSION + "remove a co-owner from someone else's bank.";
	public static final String NO_PERMISSION_BANK_UNTRUST_ADMIN = NO_PERMISSION + "remove a co-owner from an admin bank.";
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


	private static final String ACCOUNT_USAGE_PREFIX = ChatColor.RED + "/" + Config.mainCommandNameAccount + " ";
	private static final String BANK_USAGE_PREFIX = ChatColor.RED + "/" + Config.mainCommandNameBank + " ";
	private static final String CONTROL_USAGE_PREFIX = ChatColor.RED + "/" + Config.mainCommandNameControl + " ";

	public static final String COMMAND_USAGE_ACCOUNT_CREATE = ACCOUNT_USAGE_PREFIX + "create";
	public static final String COMMAND_USAGE_ACCOUNT_REMOVE = ACCOUNT_USAGE_PREFIX + "remove";
	public static final String COMMAND_USAGE_ACCOUNT_INFO = ACCOUNT_USAGE_PREFIX + "info";
	public static final String COMMAND_USAGE_ACCOUNT_LIST = ACCOUNT_USAGE_PREFIX + "list <all>";
	public static final String COMMAND_USAGE_ACCOUNT_LIMITS = ACCOUNT_USAGE_PREFIX + "limits";
	public static final String COMMAND_USAGE_ACCOUNT_REMOVEALL = ACCOUNT_USAGE_PREFIX + "removeall";
	public static final String COMMAND_USAGE_ACCOUNT_SET = ACCOUNT_USAGE_PREFIX + "set [field] [value]";
	public static final String COMMAND_USAGE_ACCOUNT_TRUST = ACCOUNT_USAGE_PREFIX + "trust [playername]";
	public static final String COMMAND_USAGE_ACCOUNT_UNTRUST = ACCOUNT_USAGE_PREFIX + "untrust [playername]";
	public static final String COMMAND_USAGE_ACCOUNT_MIGRATE = ACCOUNT_USAGE_PREFIX + "migrate";
	public static final String COMMAND_USAGE_ACCOUNT_RECOVER = ACCOUNT_USAGE_PREFIX + "recover";
	public static final String COMMAND_USAGE_ACCOUNT_RENAME = ACCOUNT_USAGE_PREFIX + "rename [accountname]";
	public static final String COMMAND_USAGE_ACCOUNT_TRANSFER = ACCOUNT_USAGE_PREFIX + "transfer [playername]";

	public static final String COMMAND_USAGE_BANK_CREATE = BANK_USAGE_PREFIX + "create [bankname] <xyz> <xyz> <admin>";
	public static final String COMMAND_USAGE_BANK_REMOVE = BANK_USAGE_PREFIX + "remove <bankname>";
	public static final String COMMAND_USAGE_BANK_INFO = BANK_USAGE_PREFIX + "info";
	public static final String COMMAND_USAGE_BANK_LIST = BANK_USAGE_PREFIX + "list";
	public static final String COMMAND_USAGE_BANK_LIMITS = BANK_USAGE_PREFIX + "limits";
	public static final String COMMAND_USAGE_BANK_REMOVEALL = BANK_USAGE_PREFIX + "removeall";
	public static final String COMMAND_USAGE_BANK_RESIZE = BANK_USAGE_PREFIX + "resize [bankname] <xyz> <xyz>";
	public static final String COMMAND_USAGE_BANK_TRUST = BANK_USAGE_PREFIX + "trust [bankname] [playername]";
	public static final String COMMAND_USAGE_BANK_UNTRUST = BANK_USAGE_PREFIX + "untrust [bankname] [playername]";
	public static final String COMMAND_USAGE_BANK_RENAME = BANK_USAGE_PREFIX + "rename [bankname] [bankname]";
	public static final String COMMAND_USAGE_BANK_SET = BANK_USAGE_PREFIX + "set [bankname] [field] [value]";
	public static final String COMMAND_USAGE_BANK_SELECT = BANK_USAGE_PREFIX + "select";
	public static final String COMMAND_USAGE_BANK_TRANSFER = BANK_USAGE_PREFIX + "transfer [bankname] [playername]";

	public static final String COMMAND_USAGE_VERSION = CONTROL_USAGE_PREFIX + "version";
	public static final String COMMAND_USAGE_RELOAD = CONTROL_USAGE_PREFIX + "reload";
	public static final String COMMAND_USAGE_CONFIG = CONTROL_USAGE_PREFIX + "config [set | add | remove] [field] <newvalue>";
	public static final String COMMAND_USAGE_UPDATE = CONTROL_USAGE_PREFIX + "update";
	public static final String COMMAND_USAGE_PAY_INTEREST = CONTROL_USAGE_PREFIX + "payinterest";
	
	public static final String ACCOUNT_COMMAND_DESC = ChatColor.GOLD + "Create, manage, and remove accounts.";
	public static final String BANK_COMMAND_DESC = ChatColor.GOLD + "Create, manage, and remove banks.";
	public static final String CONTROL_COMMAND_DESC = ChatColor.GOLD + "Configure this plugin.";

	public static final String CONFIG_VALUE_ADDED = ChatColor.GOLD + "Added the config value " + ChatColor.AQUA + "%s" + ChatColor.GOLD + ".";
	public static final String CONFIG_VALUE_REMOVED = ChatColor.GOLD + "Removed the config value " + ChatColor.AQUA + "%s" + ChatColor.GOLD + ".";
	public static final String CONFIG_VALUE_SET = ChatColor.GOLD + "Config value " + ChatColor.AQUA + "%s" + ChatColor.GOLD + " has been set to " + ChatColor.GREEN + "%s" + ChatColor.GOLD + ".";

}
