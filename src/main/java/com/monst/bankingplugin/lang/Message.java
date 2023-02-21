package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.BankingPlugin;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.monst.bankingplugin.lang.Placeholder.*;

/**
 * Contains every message that this plugin can send to players.
 * Every message is accompanied by a sample scenario in which it would be sent, a default english message text,
 * and a list of placeholder variables that are available for use within the text.
 */
public enum Message implements Translatable {

    ACCOUNT_OPENED(
            "A player opens an account at a bank.",
            gold("Account opened!"),
            BANK_NAME
    ),
    ACCOUNT_CREATE_INSUFFICIENT_FUNDS(
            "A player cannot afford to open an account.",
            red("Not enough money. You need ").green(AMOUNT_REMAINING).red(" more to open an account."),
            PRICE, PLAYER_BALANCE, AMOUNT_REMAINING
    ),
    NO_SELF_BANKING(
            "A player tries and is not allowed to open an account at their own bank.",
            red("You are not allowed to open an account at your own bank."),
            BANK_NAME
    ),
    ACCOUNT_CREATE_FEE_PAID(
            "A player is charged money to open an account at a bank.",
            gold("You have been charged ").green(PRICE).gold(" to open an account at ").gray(BANK_NAME).gold("."),
            PRICE, BANK_NAME
    ),
    ACCOUNT_CREATE_FEE_RECEIVED(
            "A bank owner receives payment from a player who created an account.", // TODO: Test
            gold(PLAYER, " has paid you ").green(AMOUNT).gold(" to open an account at ").gray(BANK_NAME).gold("."),
            PLAYER, AMOUNT, BANK_NAME
    ),
    ACCOUNT_EXTEND_INSUFFICIENT_FUNDS(
            "A player cannot afford to extend an account.",
            red("Not enough money. You need ").green(AMOUNT_REMAINING).red(" more to extend an account."),
            PRICE, PLAYER_BALANCE, AMOUNT_REMAINING
    ),
    ACCOUNT_EXTEND_FEE_PAID(
            "A player is charged money to extend their account chest into a double chest.",
            gold("You have been charged ").green(PRICE).gold(" to extend an account at ").gray(BANK_NAME).gold("."),
            PRICE, BANK_NAME
    ),
    ACCOUNT_EXTEND_FEE_RECEIVED(
            "A bank owner receives payment from a player who extended their account.", // TODO: Test
            gold(PLAYER, " has paid you ").green(AMOUNT).gold(" to extend an account at ").gray(BANK_NAME).gold("."),
            PLAYER, AMOUNT, BANK_NAME
    ),
    ABOUT_TO_CLOSE_ACCOUNT(
            "A player is about to close an account.",
            gold("You are about to close account ").gray(ACCOUNT_ID).gold(" at bank ").gray(BANK_NAME).gold("."),
            BANK_NAME, ACCOUNT_ID
    ),
    ACCOUNT_CLOSED(
            "A player closes an account at a bank.",
            gold("Account closed."),
            BANK_NAME
    ),
    ABOUT_TO_CLOSE_ALL_ACCOUNTS(
            "An admin must confirm closing multiple accounts at once.",
            gold("You are about to close ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            NUMBER_OF_ACCOUNTS
    ),
    ALL_ACCOUNTS_CLOSED(
            "An admin removes multiple accounts at once.",
            gold("Successfully closed ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            NUMBER_OF_ACCOUNTS
    ),
    ACCOUNTS_NOT_FOUND(
            "A player attempts to remove all accounts but there are no accounts to remove.",
            red("No accounts found.")
    ),
    ACCOUNT_BALANCE_NOT_ZERO(
            "A player tries to remove an account that still has items in it and must click again to confirm.",
            red("That account still has a balance of ").green(ACCOUNT_BALANCE).red(". Are you sure?"),
            ACCOUNT_BALANCE
    ),
    ACCOUNT_LIMIT(
            "A player executes the /account limits command.",
            gold("You own ").green(NUMBER_OF_ACCOUNTS + "/" + LIMIT).gold(" allowed account(s)."),
            NUMBER_OF_ACCOUNTS, LIMIT
    ),
    ACCOUNT_LIMIT_REACHED(
            "A player attempts to open an account but has reached their account limit.",
            red("You are not allowed to open any more accounts!"),
            LIMIT
    ),
    ACCOUNT_RENAMED(
            "A player sets an account nickname.",
            gold("Account nicknamed ").gray(NAME).gold("."),
            NAME
    ),
    ACCOUNT_SET_INTEREST_MULTIPLIER(
            "An admin sets the interest multiplier of an account.", // TODO: Test
            gold("Account interest multiplier set to ").green(INTEREST_MULTIPLIER + "x").gold("."),
            INTEREST_MULTIPLIER
    ),
    ACCOUNT_SET_REMAINING_OFFLINE(
            "A player sets the remaining offline payouts at an account.",
            gold("Account remaining offline payouts set to ").green(VALUE).gold("."),
            VALUE
    ),
    ACCOUNT_ABOUT_TO_TRANSFER(
            "A player clicks an account to transfer it to another player and must click again to confirm.",
            gold("You are about to transfer ownership of account ").gray("#" + ACCOUNT_ID).gold(" to " + PLAYER, "."),
            PLAYER, ACCOUNT_ID
    ),
    ACCOUNT_TRANSFERRED(
            "A player transfers ownership of an account to another player.",
            gold("You transferred " + PLAYER, " ownership of an account."),
            PLAYER
    ),
    ACCOUNT_TRANSFERRED_TO_YOU(
            "A player is transferred ownership of an account by another player.", // TODO: Test
            gold(PLAYER, " transferred you ownership of an account."),
            PLAYER
    ),
    ACCOUNT_MIGRATED(
            "A player clicks a chest to migrate an account into it.",
            gold("Account migrated!")
    ),
    ACCOUNT_RECOVERED(
            "A player clicks a chest to recover a lost account into it.",
            gold("Account recovered!")
    ),
    ACCOUNT_CHEST_OPENED(
            "A player opens someone else's account chest using the account.view-other permission.",
            gold("You opened " + PLAYER, "'s account chest."),
            PLAYER
    ),
    ACCOUNT_DEPOSIT(
            "A player puts items into an account chest and closes the chest.", // TODO: Test
            gold("You deposited ").green(AMOUNT).gold(" for a new balance of ").green(ACCOUNT_BALANCE).gold("."),
            AMOUNT, ACCOUNT_BALANCE
    ),
    ACCOUNT_WITHDRAWAL(
            "A player takes items out of an account chest and closes the chest.", // TODO: Test
            gold("You withdrew ").green(AMOUNT).gold(" for a new balance of ").green(ACCOUNT_BALANCE).gold("."),
            AMOUNT, ACCOUNT_BALANCE
    ),
    ACCOUNT_INTEREST_EARNED(
            "A player earns interest on at least one account.",
            gold("You earned ").green(AMOUNT).gold(" in interest on ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            AMOUNT, NUMBER_OF_ACCOUNTS
    ),
    ACCOUNT_INTEREST_EARNED_OFFLINE(
            "A player logs onto the server after having earned interest offline.",
            gold("You earned ").green(AMOUNT).gold(" in interest while you were offline."),
            AMOUNT
    ),
    ACCOUNT_LOW_BALANCE_FEES_PAID(
            "A player must pay low balance fees on at least one account.",
            red("You paid ").green(AMOUNT).red(" in low balance fees on ").green(NUMBER_OF_ACCOUNTS).red(" account(s)."),
            AMOUNT, NUMBER_OF_ACCOUNTS
    ),
    ACCOUNT_LOW_BALANCE_FEES_PAID_OFFLINE(
            "A player logs into the server after having paid low balance fees offline.",
            red("You paid ").green(AMOUNT).red(" in low balance fees while you were offline."),
            AMOUNT
    ),
    ACCOUNT_INTEREST_MULTIPLIER_DECREASED(
            "A player removes too much worth from their account chest and causes the interest multiplier to decrease.",
            red("Account interest multiplier decreased to ").green(INTEREST_MULTIPLIER, "x").red("."),
            INTEREST_MULTIPLIER
    ),
    CHEST_BLOCKED(
            "A player attempts to open an account on a chest that cannot be opened due to an obstruction.",
            red("Chest is blocked.")
    ),
    CHEST_ALREADY_ACCOUNT(
            "A player attempts to open an account on a chest that is already an account.",
            red("Chest is already an account.")
    ),
    CANNOT_BREAK_ACCOUNT_CHEST(
            "A player attempts to break an account chest.",
            red("You cannot break that account chest.")
    ),
    CHEST_NOT_IN_BANK(
            "A player attempts to open an account at a chest not located in a bank.",
            red("Chest is not in a bank.")
    ),
    BANK_CREATED(
            "A player creates a bank.",
            gold("Bank created!"),
            BANK_NAME
    ),
    BANK_CREATE_INSUFFICIENT_FUNDS(
            "A player cannot afford to create a bank.",
            red("Not enough money. You need ").green(AMOUNT_REMAINING).red(" more to create a bank."),
            PRICE, AMOUNT_REMAINING, PLAYER_BALANCE
    ),
    BANK_CREATE_FEE_PAID(
            "A player is charged money to create a bank.",
            gold("You have been charged ").green(PRICE).gold(" to create a bank."),
            PRICE
    ),
    CANT_SELECT_BANK(
            "A player attempts to select a bank, but neither WorldEdit nor GriefPrevention is enabled.",
            red("WorldEdit must be enabled to select a bank.")
    ),
    WORLD_DISABLED(
            "A player attempts to create a bank in world that is disabled in the config.",
            red("Bank creation is disabled in this world.")
    ),
    BANK_SELECTION_TOO_LARGE(
            "A player attempts to create a bank and a size that is greater than the maximum defined in the config.",
            red("That selection is too large. It exceeds the maximum allowed bank volume by ").green(DIFFERENCE).red(" blocks."),
            BANK_SIZE, MAXIMUM, DIFFERENCE
    ),
    BANK_SELECTION_TOO_SMALL(
            "A player attempts to create a bank and a size that is smaller than the minimum defined in the config.",
            red("That selection is too small. It falls short of the minimum allowed bank volume by ").green(DIFFERENCE).red(" blocks."),
            BANK_SIZE, MINIMUM, DIFFERENCE
    ),
    BANK_SELECTION_OVERLAPS_EXISTING(
            "A player attempts to create a bank that overlaps and at least one existing bank.",
            red("That selection overlaps with ").green(NUMBER_OF_BANKS).red(" existing bank(s)."),
            NUMBER_OF_BANKS
    ),
    BANK_SELECTION_CUTS_ACCOUNTS(
            "A player attempts to resize a bank to a selection that does not encompass all accounts at the bank.",
            red("That selection does not include all accounts at the bank."),
            NUMBER_OF_ACCOUNTS
    ),
    BANK_ABOUT_TO_REMOVE(
            "An admin must confirm removing one bank or more.",
            gold("You are about to remove bank ").gray(BANK_NAME).gold(" and its ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            BANK_NAME, NUMBER_OF_ACCOUNTS
    ),
    BANK_REMOVED(
            "A player removes a bank and all its accounts.",
            gold("Bank ").gray(BANK_NAME).gold(" and ").green(NUMBER_OF_ACCOUNTS).gold(" account(s) were removed."),
            BANK_NAME, NUMBER_OF_ACCOUNTS
    ),
    ABOUT_TO_REMOVE_ALL_BANKS(
            "An admin must confirm removing multiple banks.",
            gold("You are about to remove ").green(NUMBER_OF_BANKS).gold(" bank(s) and their ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            NUMBER_OF_BANKS, NUMBER_OF_ACCOUNTS
    ),
    ALL_BANKS_REMOVED(
            "A player removes multiple banks at once.",
            gold("Successfully removed ").green(NUMBER_OF_BANKS).gold(" bank(s) and ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            NUMBER_OF_BANKS, NUMBER_OF_ACCOUNTS
    ),
    BANK_NOT_FOUND(
            "A player attempts to open a GUI of a bank with a specified name, but no bank could be found.",
            red("Could not find bank with the name \"" + INPUT + "\"."),
            INPUT
    ),
    BANKS_NOT_FOUND(
            "A player attempts to remove all banks, but there are no banks on the server.",
            red("No banks found.")
    ),
    MUST_STAND_IN_OR_SPECIFY_BANK(
            "A player attempts to open a GUI of a bank, but is not standing in a bank.",
            red("You must stand in or specify the name of a bank.")
    ),
    BANK_LIMIT(
            "A player executes the /bank limits command.",
            gold("You own " + NUMBER_OF_BANKS).green("/" + LIMIT).gold(" allowed bank(s) of up to " + BANK_SIZE + " blocks in size."),
            NUMBER_OF_BANKS, LIMIT, BANK_SIZE
    ),
    BANK_LIMIT_REACHED(
            "A player attempts to create a bank, but has reached their bank limit.",
            red("You are not allowed to create any more banks!"),
            LIMIT
    ),
    ABOUT_TO_TRANSFER_BANK(
            "A player executes a command to transfer a bank to another player and must execute it again to confirm.",
            gold("You are about to transfer ownership of bank ").gray(BANK_NAME).gold(" to " + PLAYER, "."),
            PLAYER, BANK_NAME
    ),
    BANK_TRANSFERRED_TO_ADMIN(
            "A player transfers ownership of a bank to nobody, making it an admin bank.",
            gold("You converted bank ").gray(BANK_NAME).gold(" into an admin bank."),
            BANK_NAME
    ),
    BANK_TRANSFERRED(
            "A player transfers ownership of a bank to another player.",
            gold("You transferred " + PLAYER, " ownership of bank ").gray(BANK_NAME).gold("."),
            PLAYER, BANK_NAME
    ),
    BANK_TRANSFERRED_TO_YOU(
            "A player is transferred ownership of a bank by another player.",
            gold(PLAYER, " transferred you ownership of bank ").gray(BANK_NAME).gold("."),
            PLAYER, BANK_NAME
    ),
    BANK_ALREADY_ADMIN(
            "A player attempts to transfer a bank to an admin bank, but the bank is already an admin bank.",
            red("Bank ").gray(BANK_NAME).red(" is already an admin bank."),
            BANK_NAME
    ),
    BANK_RESIZED(
            "A player resizes a bank to a certain number of blocks.",
            gold("Bank resized."),
            BANK_SIZE
    ),
    BANK_POLICY_SET(
            "A player configures a policy at a bank to a new value.",
            gold("The policy ").green(POLICY).gold(" at bank ").gray(BANK_NAME).gold(" has been changed from ").green(PREVIOUS_VALUE).gold(" to ").green(VALUE).gold("."),
            POLICY, BANK_NAME, PREVIOUS_VALUE, VALUE
    ),
    BANK_POLICY_NOT_OVERRIDABLE(
            "A player attempts to configure a bank policy that is not overridable.",
            red("That policy is not currently overridable. Your changes will be saved, but will not take effect."),
            POLICY
    ),
    BANK_SELECTED(
            "A player selects a bank with WorldEdit and/or WorldGuard.",
            gold("Bank ").gray(BANK_NAME).gold(" was selected."),
            BANK_NAME
    ),
    NOT_A_NUMBER(
            "A player attempts to set a configuration property to a number, but provided a string that was not a number.",
            red("\"" + INPUT + "\" is not a number."),
            INPUT
    ),
    NOT_AN_INTEGER(
            "A player attempts to set a configuration property to an integer, but provided a string that was not an integer.",
            red("\"" + INPUT + "\" is not an integer."),
            INPUT
    ),
    NOT_A_BOOLEAN(
            "A player attempts to set a configuration property to a boolean, but provided a string that was not \"true\" or \"false\".",
            red("\"" + INPUT + "\" is not a boolean."),
            INPUT
    ),
    NOT_A_MATERIAL(
            "A player attempts to set a configuration property to a material, but provided a string that was not a material.",
            red("\"" + INPUT + "\" is not a material."),
            INPUT
    ),
    NOT_A_REGULAR_EXPRESSION(
            "A player attempts to set a configuration property to a regular expression, but provided a string that was not a valid pattern.",
            red("\"" + INPUT + "\" is not a valid regular expression."),
            INPUT
    ),
    NOT_A_FILENAME(
            "A player attempts to set a configuration property to a filename, but provided a string that was not a valid filename.",
            red("\"" + INPUT + "\" is not a valid filename."),
            INPUT
    ),
    NOT_AN_EXPRESSION(
            "A player attempts to set a configuration property to a mathematical expression, but provided a string that was not a valid expression.",
            red("\"" + INPUT + "\" is not a valid expression."),
            INPUT
    ),
    NOT_A_TIME(
            "A player attempts to set a configuration property to a time, but provided a string that was not a time.",
            red("\"" + INPUT + "\" is not a time."),
            INPUT
    ),
    NOT_A_WORLD(
            "A player attempts to set a configuration property to a world, but provided a string that was not the name of a world.",
            red("\"" + INPUT + "\" is not a world."),
            INPUT
    ),
    NOT_A_BANK_POLICY(
            "A player attempts to configure a bank policy, but provided a string that was not a policy.",
            red("\"" + INPUT + "\" is not a bank policy."),
            INPUT
    ),
    NOT_A_PROPERTY(
            "A player attempts to set a configuration property, but provided a string that was not a property.",
            red("\"" + INPUT + "\" is not a property."),
            INPUT
    ),
    NAME_NOT_UNIQUE(
            "A player attempts to create or rename a bank with a name already in use by another bank.",
            red("A bank with that name already exists."),
            BANK_NAME
    ),
    NAME_NOT_ALLOWED(
            "A player attempts to create or rename a bank or account with a name that does not match the name-regex pattern in the config.",
            red("That name is not allowed."),
            NAME, PATTERN
    ),
    NAME_CHANGED(
            "A player renames a bank to a different name.",
            gold("The bank was successfully renamed to ").gray(BANK_NAME).gold("."),
            BANK_NAME // TODO: Allow previous bank name?
    ),
    ACCOUNT_INTEREST_PAID(
            "A bank owner pays interest to the account holders at their bank.",
            gold("You paid account holders ").green(AMOUNT).gold(" in interest."),
            AMOUNT, NUMBER_OF_ACCOUNTS
    ),
    ACCOUNT_LOW_BALANCE_FEES_RECEIVED(
            "A bank owner receives low balance fee payments from account holders.",
            gold("You received ").green(AMOUNT).gold(" in low balance fees from ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            AMOUNT, NUMBER_OF_ACCOUNTS
    ),
    BANK_REVENUE_EARNED(
            "A bank owner receives some bank revenue.",
            gold("You received ").green(AMOUNT).gold(" in bank revenue from ").green(NUMBER_OF_BANKS).gold(" bank(s)."),
            AMOUNT, NUMBER_OF_BANKS
    ),
    BANK_PROFIT_MADE(
            "A bank owner makes a profit from their bank.",
            gold("You made a profit of ").green(AMOUNT).gold(" at bank ").gray(BANK_NAME).gold("."),
            AMOUNT, BANK_NAME
    ),
    BANK_PROFIT_EARNED_OFFLINE(
            "A bank owner logs onto the server after having made a profit while offline.",
            gold("You made ").green(AMOUNT).gold(" in bank profit while you were offline."),
            AMOUNT
    ),
    BANK_LOSS_MADE_OFFLINE(
            "A bank owner logs onto the server after having made a loss while offline.",
            red("You made ").green(AMOUNT).red(" in bank losses while you were offline."),
            AMOUNT
    ),
    ACCOUNT_REIMBURSEMENT_RECEIVED(
            "A player is reimbursed by the bank owner after removing an account.",
            gold("You have been reimbursed ").green(AMOUNT).gold("."),
            AMOUNT
    ),
    ACCOUNT_REIMBURSEMENT_PAID(
            "A bank owner reimburses a player who removed an account at their bank.", // TODO: Test
            gold("You reimbursed " + PLAYER + " ").green(AMOUNT).gold("."),
            PLAYER, AMOUNT
    ),
    ADDED_CO_OWNER(
            "A player adds another player as a co-owner of an account or a bank.",
            gold("Added " + PLAYER, " as a co-owner."),
            PLAYER
    ),
    REMOVED_CO_OWNER(
            "A player removes another player as a co-owner of an account or a bank.",
            gold("Removed " + PLAYER, " as a co-owner."),
            PLAYER
    ),
    ALREADY_CO_OWNER(
            "A player attempts to add another player as a co-owner of an account or a bank, but that player is already a co-owner.",
            red(PLAYER, " is already a co-owner."),
            PLAYER
    ),
    NOT_A_CO_OWNER(
            "A player attempts to remove another player as a co-owner of an account or a bank, but that player is not a co-owner.",
            red(PLAYER, " is not a co-owner."),
            PLAYER
    ),
    ALREADY_OWNER(
            "A player attempts to transfer ownership of an account or bank to the player who already owns it.",
            red(PLAYER, " is already owner."),
            PLAYER
    ),
    MUST_BE_OWNER(
            "A co-owner attempts to perform an action on a bank or account that only the owner can perform.",
            red("You must be the owner to do that.")
    ),
    PLAYER_NOT_FOUND(
            "A player references another player in command arguments who either does not exist or who has never played on the server before.",
            red("No player was found with the name \"", INPUT, "\"."),
            INPUT
    ),
    ACCOUNT_LIMIT_AT_BANK_REACHED(
            "A player attempts to open an account at a bank, but has reached their limit for that specific bank.",
            red("You are not allowed to open another account at this bank."),
            BANK_NAME, LIMIT
    ),
    PLAYER_COMMAND_ONLY(
            "An admin attempts to execute a player-only command from the console.",
            red("You must be a player to do that.")
    ),
    SAME_CHEST(
            "A player attempts to migrate an account to the same chest.",
            red("That is the same chest.")
    ),
    CLICK_CHEST_OPEN(
            "A player runs the /account open command and may click a chest.",
            gold("Click a chest to open an account.")
    ),
    CLICK_ACCOUNT_INFO(
            "A player runs the /account info command and may click an account chest.",
            gold("Click an account chest to view info.")
    ),
    CLICK_ACCOUNT_MIGRATE(
            "A player runs the /account migrate command and may click an account.",
            gold("Click an account chest to migrate it.")
    ),
    CLICK_CHEST_MIGRATE(
            "A player is migrating an account and and may click another chest to migrate to.",
            gold("Click a chest to migrate the account to.")
    ),
    CLICK_CHEST_RECOVER(
            "A player runs the /account recover command, selects a lost account, and may click a chest.",
            gold("Click a chest to recover the account.")
    ),
    CLICK_ACCOUNT_CLOSE(
            "A player runs the /account close command and may click an account chest.",
            gold("Click an account chest to close the account.")
    ),
    CLICK_ACCOUNT_RENAME(
            "A player runs the /account rename command and may click an account chest.",
            gold("Click an account chest to rename it.")
    ),
    CLICK_ACCOUNT_CONFIGURE(
            "A player runs the /account configure command and may click an account chest.",
            gold("Click an account chest to set ").green(PROPERTY).gold(" to ").green(VALUE).gold("."),
            PROPERTY, VALUE
    ),
    CLICK_ACCOUNT_TRANSFER(
            "A player runs the /account transfer command and may click an account chest.",
            gold("Click an account chest to transfer it to " + PLAYER, "."),
            PLAYER
    ),
    CLICK_ACCOUNT_TRUST(
            "A player runs the /account trust command and may click an account chest.",
            gold("Click an account chest to add " + PLAYER, " as a co-owner."),
            PLAYER
    ),
    CLICK_ACCOUNT_UNTRUST(
            "A player runs the /account untrust command and may click an account chest.",
            gold("Click an account chest to remove " + PLAYER, " as a co-owner."),
            PLAYER
    ),
    CLICK_AGAIN_TO_CONFIRM(
            "A player must click a chest for a second time to confirm an action.",
            gold("Click again to confirm.")
    ),
    EXECUTE_AGAIN_TO_CONFIRM(
            "A player must execute a command for a second time to confirm.",
            gold("Execute again to confirm.")
    ),
    NO_PERMISSION(
            "A player attempts to execute a command without permission.",
            red("You do not have permission to do that.")
    ),
    NO_PERMISSION_ACCOUNT_OPEN(
            "A player attempts to use the /account open command without permission.",
            red("You do not have permission to open an account.")
    ),
    NO_PERMISSION_ACCOUNT_OPEN_PROTECTED(
            "A player attempts to open an account on a protected chest without permission.",
            red("You do not have permission to open an account on a protected chest.")
    ),
    NO_PERMISSION_ACCOUNT_TRUST(
            "A player attempts to use the /account trust command without permission.",
            red("You do not have permission to add a co-owner to an account.")
    ),
    NO_PERMISSION_ACCOUNT_TRUST_OTHER(
            "A player attempts to add another player as a co-owner of someone else's account without permission.",
            red("You do not have permission to add a co-owner to someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_UNTRUST(
            "A player attempts to use the /account untrust command without permission.",
            red("You do not have permission to remove a co-owner from an account.")
    ),
    NO_PERMISSION_ACCOUNT_UNTRUST_OTHER(
            "A player attempts to remove another player as a co-owner of someone else's account without permission.",
            red("You do not have permission to remove a co-owner from someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_CLOSE(
            "A player attempts to use the /account close command without permission.",
            red("You do not have permission to remove an account.")
    ),
    NO_PERMISSION_ACCOUNT_CLOSE_OTHER(
            "A player attempts to close someone else's account without permission.",
            red("You do not have permission to close someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_CLOSE_ALL(
            "A player attempts to use the /account closeall command without permission.",
            red("You do not have permission to close all accounts.")
    ),
    NO_PERMISSION_ACCOUNT_VIEW_OTHER(
            "A player attempts to open someone else's account chest without permission.",
            red("You do not have permission to view someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_EDIT_OTHER(
            "A player attempts to edit the contents of someone else's account chest without permission.",
            red("You do not have permission to edit someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE(
            "A player attempts to use the /account migrate command without permission.",
            red("You do not have permission to migrate an account.")
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE_OTHER(
            "A player attempts to migrate someone else's account without permission.",
            red("You do not have permission to migrate someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE_BANK(
            "A player attempts to migrate an account to another bank without permission.",
            red("You do not have permission to migrate an account to another bank.")
    ),
    NO_PERMISSION_ACCOUNT_RECOVER(
            "A player attempts to use the /account recover command without permission.",
            red("You do not have permission to recover an account.")
    ),
    NO_PERMISSION_ACCOUNT_RENAME(
            "A player attempts to use the /account rename command without permission.",
            red("You do not have permission to rename an account.")
    ),
    NO_PERMISSION_ACCOUNT_RENAME_OTHER(
            "A player attempts to rename someone else's account without permission.",
            red("You do not have permission to rename someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_TRANSFER(
            "A player attempts to use the /account transfer command without permission.",
            red("You do not have permission to transfer ownership of an account.")
    ),
    NO_PERMISSION_ACCOUNT_TRANSFER_OTHER(
            "A player attempts to transfer ownership of someone else's account without permission.",
            red("You do not have permission to transfer ownership of someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_EXTEND_OTHER(
            "A player attempts to extend someone else's account chest without permission.",
            red("You do not have permission to extend someone else's account.")
    ),
    NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED( // TODO: Test
            "A player attempts to extend a protected account chest without permission.",
            red("You do not have permission to extend a protected account chest.")
    ),
    NO_PERMISSION_ACCOUNT_CONFIGURE(
            "A player attempts to use the /account configure command without permission.",
            red("You do not have permission to set internal account values.")
    ),
    NO_PERMISSION_BANK_CREATE(
            "A player attempts to use the /bank create command without permission.",
            red("You do not have permission to create a bank.")
    ),
    NO_PERMISSION_BANK_CREATE_ADMIN(
            "A player attempts to create an admin bank without permission.",
            red("You do not have permission to create an admin bank.")
    ),
    NO_PERMISSION_BANK_CREATE_PROTECTED(
            "A player attempts to create a bank in a region that lacks the WorldGuard flag \"create-bank\" without permission.",
            red("You do not have permission to create a bank there.")
    ),
    NO_PERMISSION_BANK_REMOVE(
            "A player attempts to use the /bank remove command without permission.",
            red("You do not have permission to remove a bank.")
    ),
    NO_PERMISSION_BANK_REMOVE_OTHER(
            "A player attempts to remove someone else's bank without permission.",
            red("You do not have permission to remove someone else's bank.")
    ),
    NO_PERMISSION_BANK_REMOVE_ADMIN(
            "A player attempts to remove an admin bank without permission.",
            red("You do not have permission to remove an admin bank.")
    ),
    NO_PERMISSION_BANK_REMOVE_ALL(
            "A player attempts to use the /bank removeall command without permission.",
            red("You do not have permission to remove all banks.")
    ),
    NO_PERMISSION_BANK_RESIZE( // TODO: Test
            "A player attempts to use the /bank resize command without permission.",
            red("You do not have permission to resize a bank.")
    ),
    NO_PERMISSION_BANK_RESIZE_OTHER(
            "A player attempts to resize someone else's bank without permission.",
            red("You do not have permission to resize someone else's bank.")
    ),
    NO_PERMISSION_BANK_RESIZE_ADMIN(
            "A player attempts to resize an admin bank without permission.",
            red("You do not have permission to resize an admin bank.")
    ),
    NO_PERMISSION_BANK_RENAME(
            "A player attempts to use the /bank rename command without permission",
            red("You do not have permission to rename a bank.")
    ),
    NO_PERMISSION_BANK_RENAME_OTHER(
            "A player attempts to rename someone else's bank without permission",
            red("You do not have permission to rename someone else's bank.")
    ),
    NO_PERMISSION_BANK_RENAME_ADMIN(
            "A player attempts to rename an admin bank without permission",
            red("You do not have permission to rename an admin bank.")
    ),
    NO_PERMISSION_BANK_TRUST(
            "A player attempts to use the /bank trust command without permission.",
            red("You do not have permission to add a co-owner to a bank.")
    ),
    NO_PERMISSION_BANK_TRUST_OTHER(
            "A player attempts to add another player as a co-owner of someone else's bank without permission.",
            red("You do not have permission to add a co-owner to someone else's bank.")
    ),
    NO_PERMISSION_BANK_TRUST_ADMIN(
            "A player attempts to add another player as a co-owner of an admin bank without permission.",
            red("You do not have permission to add a co-owner to an admin bank.")
    ),
    NO_PERMISSION_BANK_UNTRUST(
            "A player attempts to use the /bank untrust command without permission.",
            red("You do not have permission to remove a co-owner from a bank.")
    ),
    NO_PERMISSION_BANK_UNTRUST_OTHER(
            "A player attempts to remove another player as a co-owner of someone else's bank without permission.",
            red("You do not have permission to remove a co-owner from someone else's bank.")
    ),
    NO_PERMISSION_BANK_UNTRUST_ADMIN(
            "A player attempts to remove another player as a co-owner of an admin bank without permission.",
            red("You do not have permission to remove a co-owner from an admin bank.")
    ),
    NO_PERMISSION_BANK_TRANSFER(
            "A player attempts to use the /bank transfer command without permission.",
            red("You do not have permission to transfer ownership of a bank.")
    ),
    NO_PERMISSION_BANK_TRANSFER_OTHER(
            "A player attempts to transfer ownership of someone else's bank without permission.",
            red("You do not have permission to transfer ownership of someone else's bank.")
    ),
    NO_PERMISSION_BANK_TRANSFER_ADMIN(
            "A player attempts to transfer ownership of an admin bank without permission.",
            red("You do not have permission to transfer ownership of an admin bank.")
    ),
    NO_PERMISSION_BANK_CONFIGURE(
            "A player attempts to use the /bank configure command without permission.",
            red("You do not have permission to configure a bank.")
    ),
    NO_PERMISSION_BANK_CONFIGURE_OTHER(
            "A player attempts to configure someone else's bank without permission.",
            red("You do not have permission to configure someone else's bank.")
    ),
    NO_PERMISSION_BANK_CONFIGURE_ADMIN(
            "A player attempts to configure an admin bank without permission.",
            red("You do not have permission to configure an admin bank.")
    ),
    NO_PERMISSION_BANK_SELECT(
            "A player attempts to select a bank without the WorldEdit permission worldedit.selection.pos.",
            red("You do not have permission to select a bank.")
    ),
    NO_PERMISSION_RELOAD(
            "A player attempts to reload the plugin without permission.",
            red("You do not have permission to reload the plugin.")
    ),
    NO_PERMISSION_CONFIGURE(
            "A player attempts to configure the plugin without permission.",
            red("You do not have permission to configure the plugin.")
    ),
    NO_PERMISSION_UPDATE(
            "A player attempts to update the plugin without permission.",
            red("You do not have permission to update the plugin.")
    ),
    NO_PERMISSION_PAY_INTEREST(
            "A player attempts to trigger an interest payment without permission.",
            red("You do not have permission to trigger an interest payment.")
    ),
    COMMAND_USAGE_ACCOUNT_OPEN(
            "",
            green("/" + COMMAND).gold(" - Open an account."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_CLOSE(
            "",
            green("/" + COMMAND).gold(" - Close an account."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_INFO(
            "",
            green("/" + COMMAND).gold(" - Open an account GUI."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_LIST(
            "",
            green("/" + COMMAND + " <names>").gold(" - View a list of accounts."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_LIMITS(
            "",
            green("/" + COMMAND).gold(" - View your account limits."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_CLOSE_ALL(
            "",
            green("/" + COMMAND).gold(" - Close all accounts."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_CONFIGURE(
            "",
            green("/" + COMMAND + " [property] [value]").gold(" - Configure an account value."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_TRUST(
            "",
            green("/" + COMMAND + " [name]").gold(" - Add a co-owner to an account."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_UNTRUST(
            "",
            green("/" + COMMAND + " [name]").gold(" - Remove a co-owner from an account."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_MIGRATE(
            "",
            green("/" + COMMAND).gold(" - Migrate an account to a different chest."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_RECOVER(
            "",
            green("/" + COMMAND).gold(" - Recover a lost account to a new chest."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_RENAME(
            "",
            green("/" + COMMAND + " [name]").gold(" - Give an account a nickname."),
            COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_TRANSFER(
            "",
            green("/" + COMMAND + " [name]").gold(" - Transfer ownership of an account."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_CREATE(
            "",
            green("/" + COMMAND).gold(" - Create a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_REMOVE(
            "",
            green("/" + COMMAND).gold(" - Remove a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_INFO(
            "",
            green("/" + COMMAND).gold(" - Open a bank GUI."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_LIST(
            "",
            green("/" + COMMAND).gold(" - View a list of banks."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_LIMITS(
            "",
            green("/" + COMMAND).gold(" - View your bank limits."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_REMOVE_ALL(
            "",
            green("/" + COMMAND).gold(" - Remove all banks."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_RESIZE(
            "",
            green("/" + COMMAND).gold(" - Resize a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_TRUST(
            "",
            green("/" + COMMAND).gold(" - Add a co-owner to a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_UNTRUST(
            "",
            green("/" + COMMAND).gold(" - Remove a co-owner from a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_RENAME(
            "",
            green("/" + COMMAND).gold(" - Rename a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_CONFIGURE(
            "",
            green("/" + COMMAND).gold(" - Configure a bank."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_SELECT(
            "",
            green("/" + COMMAND).gold(" - Select a bank and WorldEdit."),
            COMMAND
    ),
    COMMAND_USAGE_BANK_TRANSFER(
            "",
            green("/" + COMMAND).gold(" - Transfer ownership of a bank."),
            COMMAND
    ),
    COMMAND_USAGE_VERSION(
            "",
            green("/" + COMMAND).gold(" - View the version of BankingPlugin."),
            COMMAND
    ),
    COMMAND_USAGE_RELOAD(
            "",
            green("/" + COMMAND).gold(" - Reload BankingPlugin."),
            COMMAND
    ),
    COMMAND_USAGE_CONFIGURE(
            "",
            green("/" + COMMAND + " [property] [value]").gold(" - Configure BankingPlugin."),
            COMMAND
    ),
    COMMAND_USAGE_UPDATE(
            "",
            green("/" + COMMAND).gold(" - Update BankingPlugin."),
            COMMAND
    ),
    COMMAND_USAGE_PAY_INTEREST(
            "",
            green("/" + COMMAND + " <banks>").gold(" - Trigger an interest payment."),
            COMMAND
    ),
    COMMAND_USAGE_DONATE(
            "",
            green("/" + COMMAND).gold(" - Donate to support the development of BankingPlugin."),
            COMMAND
    ),
    ACCOUNT_COMMAND_DESC(
            "A description of the account command.",
            green("Open, close, and manage accounts.")
    ),
    BANK_COMMAND_DESC(
            "A description of the bank command.",
            green("Create, remove, and manage banks.")
    ),
    CONTROL_COMMAND_DESC(
            "A description of the control command.",
            green("Manage the plugin.")
    ),
    NOT_A_CONFIGURATION_VALUE(
            "An admin tries to configure a property in the config that does not exist.",
            red("\"" + INPUT + "\" is not a configuration property."),
            INPUT
    ),
    CONFIGURATION_VALUE_SET(
            "An admin sets the value of a property in the config.",
            gold("Changed ").green(PROPERTY).gold(" from ").green(PREVIOUS_VALUE).gold(" to ").green(VALUE).gold("."),
            PROPERTY, PREVIOUS_VALUE, VALUE
    ),
    RESTART_REQUIRED(
            "An admin sets the value of a property in the config, but a restart is required before the changes can take effect.",
            red("A restart is required before changes to that property can take effect."),
            PROPERTY
    ),
    INTEREST_PAYOUT_TRIGGERED(
            "An admin triggers an interest payout.",
            gold("Triggered an interest payout at ").green(NUMBER_OF_BANKS).gold(" bank(s)."),
            NUMBER_OF_BANKS
    ),
    RELOADED_PLUGIN(
            "An admin reloads all banks and accounts on the server.",
            gold("Successfully reloaded ").green(NUMBER_OF_BANKS).gold(" bank(s) and ").green(NUMBER_OF_ACCOUNTS).gold(" account(s)."),
            NUMBER_OF_BANKS, NUMBER_OF_ACCOUNTS
    ),
    UPDATE_CHECKING(
            "An admin is notified that the plugin is checking for updates.",
            gold().bold("Checking for updates...")
    ),
    UPDATE_AVAILABLE(
            "An admin is notified that an update to BankingPlugin is available.",
            gold().bold("Version ").red(VERSION).gold(" of ").green("Banking").darkGreen("Plugin").gold(" is available."),
            VERSION
    ),
    NO_UPDATE_AVAILABLE(
            "An admin is notified that no new updates are available.",
            gold().bold("No new version available.")
    ),
    UPDATE_CHECK_ERROR(
            "An admin is notified that an error occurred while checking for updates.",
            red().bold("Error while checking for updates.")
    ),
    CLICK_TO_DONATE(
            "A player executes the donate command and they are shown the donation link.",
            gold().bold("Thank you! Click to donate: ").green(URL),
            URL
    );

    private final String path;
    private final String scenario;
    private final String defaultMessage;
    private final Set<Placeholder> availablePlaceholders = EnumSet.noneOf(Placeholder.class);

    Message(String scenario, ColorStringBuilder defaultMessage, Placeholder... availablePlaceholders) {
        this.path = "message." + toString().toLowerCase().replace("_", "-");
        this.scenario = scenario;
        this.defaultMessage = defaultMessage.toString();
        this.availablePlaceholders.addAll(Arrays.asList(availablePlaceholders));
    }

    public String getPath() {
        return path;
    }

    public String getExampleScenario() {
        return scenario;
    }

    public String getAvailablePlaceholders() {
        return availablePlaceholders.stream().map(Placeholder::toString).collect(Collectors.joining(", "));
    }

    public ValuedMessage.ReplacementBuilder with(Placeholder placeholder) {
        return new ValuedMessage().and(placeholder);
    }

    public class ValuedMessage implements Translatable {

        private final List<Replacement> replacements = new ArrayList<>(4);

        public ReplacementBuilder and(Placeholder placeholder) {
            return new ReplacementBuilder(placeholder);
        }

        public class ReplacementBuilder {

            private final Placeholder placeholder;

            private ReplacementBuilder(Placeholder placeholder) {
                this.placeholder = placeholder;
            }

            public ValuedMessage as(Object replacement) {
                ValuedMessage.this.replacements.add(new Replacement(placeholder, replacement));
                return ValuedMessage.this;
            }
        }

        @Override
        public String translate(BankingPlugin plugin) {
            if (plugin == null)
                return inEnglish();
            String translation = plugin.config().languageFile.getTranslation(Message.this, defaultMessage);
            Set<Placeholder> remainingPlaceholders = EnumSet.copyOf(availablePlaceholders);
            for (Replacement replacement : replacements) {
                Placeholder placeholder = replacement.getPlaceholder();
                if (remainingPlaceholders.remove(placeholder))
                    translation = translation.replace(placeholder.toString(), replacement.getReplacement());
            }
            if (!remainingPlaceholders.isEmpty()) {
                plugin.log(Level.WARNING, "Notify author! Message " + path + " has unfilled placeholders!");
                plugin.debug(remainingPlaceholders.toString());
            }
            return translation;
        }

        @Override
        public String inEnglish() {
            String translation = defaultMessage;
            Set<Placeholder> remainingPlaceholders = EnumSet.copyOf(availablePlaceholders);
            for (Replacement replacement : replacements) {
                Placeholder placeholder = replacement.getPlaceholder();
                if (!remainingPlaceholders.remove(placeholder))
                    continue;
                translation = translation.replace(placeholder.toString(), replacement.getReplacement());
            }
            return translation;
        }

    }

    @Override
    public String translate(BankingPlugin plugin) {
        if (!availablePlaceholders.isEmpty()) {
            plugin.log(Level.WARNING, "Notify author! Message " + path + " has unfilled placeholders!");
            plugin.debug(availablePlaceholders.toString());
        }
        return plugin.config().languageFile.getTranslation(this, defaultMessage);
    }

    @Override
    public String inEnglish() {
        return defaultMessage;
    }

    private static class Replacement {

        private final Placeholder placeholder;
        private final Object replacement;

        private Replacement(Placeholder placeholder, Object replacement) {
            this.placeholder = placeholder;
            this.replacement = replacement;
        }

        /**
         * @return Placeholder that will be replaced
         */
        private Placeholder getPlaceholder() {
            return placeholder;
        }

        /**
         * @return String which will replace the placeholder
         */
        private String getReplacement() {
            return String.valueOf(replacement);
        }

    }

    private static ColorStringBuilder gold(Object... objects) {
        return new ColorStringBuilder(64).gold(objects);
    }

    private static ColorStringBuilder green(Object... objects) {
        return new ColorStringBuilder(48).green(objects);
    }

    private static ColorStringBuilder red(Object... objects) {
        return new ColorStringBuilder(96).red(objects);
    }

}
