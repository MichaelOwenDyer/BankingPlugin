package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains every message that this plugin can send to players.
 * Every message is accompanied by a sample scenario in which it would be sent, a default english message text,
 * and a list of placeholder variables that are available for use within the text.
 */
public enum Message {

    ACCOUNT_CREATED (
            "A player creates an account at a bank.",
            "&6Account created!",
            Placeholder.BANK_NAME
    ),
    ACCOUNT_CREATE_INSUFFICIENT_FUNDS (
            "A player cannot afford to create an account.",
            "&cNot enough money. You need &a%AMOUNT_REMAINING% &cmore to create an account.",
            Placeholder.PRICE, Placeholder.PLAYER_BALANCE, Placeholder.AMOUNT_REMAINING
    ),
    NO_SELF_BANKING (
            "A player tries and is not allowed to create an account at their own bank.",
            "&cYou are not allowed to create an account at your own bank.",
            Placeholder.BANK_NAME
    ),
    ACCOUNT_CREATE_FEE_PAID (
            "A player is charged money to create an account at a bank.",
            "&6You have been charged &a%PRICE% &6to create an account at &7%BANK_NAME%&6.",
            Placeholder.PRICE, Placeholder.BANK_NAME
    ),
    ACCOUNT_CREATE_FEE_RECEIVED (
            "A bank owner receives payment from a player who created an account.", // TODO: Test
            "&6%PLAYER% has paid you &a%AMOUNT% &6to create an account at &7%BANK_NAME%&6.",
            Placeholder.PLAYER, Placeholder.AMOUNT, Placeholder.BANK_NAME
    ),
    ACCOUNT_EXTEND_INSUFFICIENT_FUNDS (
            "A player cannot afford to extend an account.",
            "&cNot enough money. You need &a%AMOUNT_REMAINING% &cmore to extend an account.",
            Placeholder.PRICE, Placeholder.PLAYER_BALANCE, Placeholder.AMOUNT_REMAINING
    ),
    ACCOUNT_EXTEND_FEE_PAID (
            "A player is charged money to extend their account chest into a double chest.",
            "&6You have been charged &a%PRICE% &6to extend an account at &7%BANK_NAME%&6.",
            Placeholder.PRICE, Placeholder.BANK_NAME
    ),
    ACCOUNT_EXTEND_FEE_RECEIVED (
            "A bank owner receives payment from a player who extended their account.", // TODO: Test
            "&6%PLAYER% has paid you &a%AMOUNT% &6to extend an account at &7%BANK_NAME%&6.",
            Placeholder.PLAYER, Placeholder.AMOUNT, Placeholder.BANK_NAME
    ),
    ACCOUNT_REMOVED (
            "A player removes an account at a bank.",
            "&6Account removed.",
            Placeholder.BANK_NAME
    ),
    ACCOUNT_CONFIRM_REMOVE_ALL (
            "An admin must confirm removing multiple accounts at once.",
            "&6You are about to remove &b%NUMBER_OF_ACCOUNTS% &6account(s).",
            Placeholder.NUMBER_OF_ACCOUNTS
    ),
    ALL_ACCOUNTS_REMOVED (
            "An admin removes multiple accounts at once.",
            "&6Successfully removed &b%NUMBER_OF_ACCOUNTS% &6account(s).",
            Placeholder.NUMBER_OF_ACCOUNTS
    ),
    ACCOUNT_NOT_FOUND (
            "A player attempts to open a GUI of an account and a specific ID, but there is no account and that ID.",
            "&cCould not find account and ID &b%INPUT%&c.",
            Placeholder.INPUT
    ),
    ACCOUNTS_NOT_FOUND (
            "A player attempts to remove all accounts but there are no accounts to remove.",
            "&cNo accounts found."
    ),
    ACCOUNT_BALANCE_NOT_ZERO (
            "A player tries to remove an account that still has items in it and must click again to confirm.",
            "&cThat account still has a balance of &a%ACCOUNT_BALANCE%&c. Are you sure?",
            Placeholder.ACCOUNT_BALANCE
    ),
    ACCOUNT_LIMIT (
            "A player executes the /account limits command.",
            "&6You own &b%NUMBER_OF_ACCOUNTS%&6/&b%LIMIT% &6allowed account(s).",
            Placeholder.NUMBER_OF_ACCOUNTS, Placeholder.LIMIT
    ),
    ACCOUNT_LIMIT_REACHED (
            "A player attempts to create an account but has reached their account limit.",
            "&cYou are not allowed to create any more accounts!",
            Placeholder.LIMIT
    ),
    ACCOUNT_RENAMED (
            "A player sets an account nickname.",
            "&6Account renamed to &7%ACCOUNT_NAME%&6.",
            Placeholder.ACCOUNT_NAME
    ),
    ACCOUNT_SET_MULTIPLIER (
            "An admin sets the multiplier of an account.", // TODO: Test
            "&6Account multiplier set to &b%MULTIPLIER%x&6.",
            Placeholder.MULTIPLIER, Placeholder.MULTIPLIER_STAGE
    ),
    ACCOUNT_SET_INTEREST_DELAY (
            "An admin sets the interest delay of an account.",
            "&6Account interest delay set to &b%NUMBER%&6.",
            Placeholder.NUMBER
    ),
    ACCOUNT_SET_REMAINING_OFFLINE (
            "A player sets the remaining offline payouts at an account.",
            "&6Account remaining offline payouts set to &b%NUMBER%&6.",
            Placeholder.NUMBER
    ),
    ACCOUNT_CONFIRM_TRANSFER (
            "A player clicks an account to transfer it to another player and must click again to confirm.",
            "&6Click again to confirm transferring ownership of an account to %PLAYER%.",
            Placeholder.PLAYER
    ),
    ACCOUNT_TRANSFERRED (
            "A player transfers ownership of an account to another player.",
            "&6You transferred %PLAYER% ownership of an account.",
            Placeholder.PLAYER
    ),
    ACCOUNT_TRANSFERRED_TO_YOU (
            "A player is transferred ownership of an account by another player.", // TODO: Test
            "&6%PLAYER% transferred you ownership of an account.",
            Placeholder.PLAYER
    ),
    ACCOUNT_MIGRATED (
            "A player clicks a chest to migrate an account into it.",
            "&6Account migrated!"
    ),
    ACCOUNT_RECOVERED (
            "A player clicks a chest to recover a lost account into it.",
            "&6Account recovered!"
    ),
    ACCOUNT_OPENED (
            "A player opens someone else's account chest using the account.view-other permission.",
            "&6You opened %PLAYER%'s account chest.",
            Placeholder.PLAYER
    ),
    ACCOUNT_DEPOSIT (
            "A player puts items into an account chest and closes the chest.", // TODO: Test
            "&6You deposited &a%AMOUNT% &6for a new balance of &a%ACCOUNT_BALANCE%&6.",
            Placeholder.AMOUNT, Placeholder.ACCOUNT_BALANCE
    ),
    ACCOUNT_WITHDRAWAL (
            "A player takes items out of an account chest and closes the chest.", // TODO: Test
            "&6You withdrew &a%AMOUNT% &6for a new balance of &a%ACCOUNT_BALANCE%&6.",
            Placeholder.AMOUNT, Placeholder.ACCOUNT_BALANCE
    ),
    INTEREST_EARNED (
            "A player earns interest on at least one account.",
            "&6You earned &a%AMOUNT% &6in interest on &b%NUMBER_OF_ACCOUNTS% &6account(s).",
            Placeholder.AMOUNT, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    OFFLINE_ACCOUNT_INTEREST (
            "A player logs onto the server after having earned interest offline.",
            "&6You earned &a%AMOUNT% &6in interest while you were offline.",
            Placeholder.AMOUNT
    ),
    OFFLINE_LOW_BALANCE_FEES_PAID (
            "A player logs into the server after having paid low balance fees offline.",
            "&cYou paid &a%AMOUNT% &cin low balance fees while you were offline.",
            Placeholder.AMOUNT
    ),
    LOW_BALANCE_FEE_PAID (
            "A player must pay low balance fees on at least one account.",
            "&cYou paid &a%AMOUNT% &cin low balance fees on &b%NUMBER_OF_ACCOUNTS% &caccount(s).",
            Placeholder.AMOUNT, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    MULTIPLIER_DECREASED (
            "A player removes too much worth from their account chest and causes the multiplier to decrease.",
            "&cAccount multiplier decreased to &b%NUMBER%&c.",
            Placeholder.NUMBER
    ),
    CHEST_BLOCKED (
            "A player attempts to create an account on a chest that cannot be opened due to an obstruction.",
            "&cChest is blocked."
    ),
    CHEST_ALREADY_ACCOUNT (
            "A player attempts to create an account on a chest that is already an account.",
            "&cChest is already an account."
    ),
    CANNOT_BREAK_ACCOUNT (
            "A player attempts to break an account chest.",
            "&cYou cannot break that account chest."
    ),
    CHEST_NOT_IN_BANK (
            "A player attempts to create an account at a chest not located in a bank.",
            "&cChest is not in a bank."
    ),
    BANK_CREATED (
            "A player creates a bank.",
            "&6Bank created!",
            Placeholder.BANK_NAME
    ),
    BANK_CREATE_INSUFFICIENT_FUNDS (
            "A player cannot afford to create a bank.",
            "&cNot enough money. You need &a%AMOUNT_REMAINING% &cmore to create a bank.",
            Placeholder.PRICE, Placeholder.AMOUNT_REMAINING, Placeholder.PLAYER_BALANCE
    ),
    BANK_CREATE_FEE_PAID (
            "A player is charged money to create a bank.",
            "&6You have been charged &a%PRICE% &6to create a bank.",
            Placeholder.PRICE
    ),
    BANK_SELECT_REGION (
            "A player attempts to create a bank without a WorldEdit selection or specifying coordinates in the command.",
            "&cYou must select a WorldEdit region or specify coordinates manually."
    ),
    CANT_SELECT_BANK (
            "A player attempts to select a bank, but neither WorldEdit nor GriefPrevention is enabled.",
            "&cWorldEdit must be enabled to select a bank."
    ),
    WORLDEDIT_NOT_ENABLED (
            "A player attempts to create a bank without specifying coordinates in the command and WorldEdit is not enabled.",
            "&cWorldEdit is not currently enabled. Please enter coordinates manually."
    ),
    WORLD_DISABLED (
            "A player attempts to create a bank in world that is disabled in the config.",
            "&cBank creation is disabled in this world.",
            Placeholder.WORLD
    ),
    BANK_SELECTION_TOO_LARGE (
            "A player attempts to create a bank and a size that is greater than the maximum defined in the config.",
            "&cThat selection is too large. It exceeds the maximum bank size by &b%DIFFERENCE% &cblocks.",
            Placeholder.BANK_SIZE, Placeholder.MAXIMUM, Placeholder.DIFFERENCE
    ),
    BANK_SELECTION_TOO_SMALL (
            "A player attempts to create a bank and a size that is smaller than the minimum defined in the config.",
            "&cThat selection is too small. It falls short of the minimum bank size by &b%DIFFERENCE% &cblocks.",
            Placeholder.BANK_SIZE, Placeholder.MINIMUM, Placeholder.DIFFERENCE
    ),
    BANK_SELECTION_OVERLAPS_EXISTING (
            "A player attempts to create a bank that overlaps and at least one existing bank.",
            "&cThat selection overlaps and &b%NUMBER_OF_BANKS% &cexisting bank(s).",
            Placeholder.NUMBER_OF_BANKS
    ),
    BANK_SELECTION_CUTS_ACCOUNTS (
            "A player attempts to resize a bank to a selection that does not encompass all accounts at the bank.",
            "&cThat selection does not include all accounts at the bank.",
            Placeholder.NUMBER_OF_ACCOUNTS
    ),
    BANK_REMOVED (
            "A player removes a bank and all its accounts.",
            "&6Bank &b%BANK_NAME% &6and &b%NUMBER_OF_ACCOUNTS% &6account(s) were removed.",
            Placeholder.BANK_NAME, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    ALL_BANKS_REMOVED (
            "A player removes multiple banks at once.",
            "&6Successfully removed &b%NUMBER_OF_BANKS% &6bank(s) and &b%NUMBER_OF_ACCOUNTS% &6account(s).",
            Placeholder.NUMBER_OF_BANKS, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    BANK_NOT_FOUND (
            "A player attempts to open a GUI of a bank and a specified name or ID, but no bank could be found.",
            "&cCould not find bank and the name or ID \"%INPUT%\".",
            Placeholder.INPUT
    ),
    BANKS_NOT_FOUND (
            "A player attempts to remove all banks, but there are no banks on the server.",
            "&cNo banks found."
    ),
    BANK_CONFIRM_REMOVE (
            "An admin must confirm removing one bank or more.",
            "&6Execute command again to remove &b%NUMBER_OF_BANKS% &6bank(s) and &b%NUMBER_OF_ACCOUNTS% &6account(s).",
            Placeholder.NUMBER_OF_BANKS, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    MUST_STAND_IN_BANK (
            "A player attempts to open a GUI of a bank, but is not standing in a bank.",
            "&cYou must stand in or specify the name or ID of a bank."
    ),
    BANK_LIMIT (
            "A player executes the /bank limits command.",
            "&6You own %NUMBER_OF_BANKS%&a/%LIMIT% allowed bank(s) of up to %BANK_SIZE% blocks in size each.",
            Placeholder.NUMBER_OF_BANKS, Placeholder.LIMIT, Placeholder.BANK_SIZE
    ),
    BANK_LIMIT_REACHED (
            "A player attempts to create a bank, but has reached their bank limit.",
            "&cYou are not allowed to create any more banks!",
            Placeholder.LIMIT
    ),
    BANK_CONFIRM_TRANSFER (
            "A player executes a command to transfer a bank to another player and must execute it again to confirm.",
            "&6Execute command again to confirm transferring ownership of bank &7%BANK_NAME% &6to %PLAYER%.",
            Placeholder.PLAYER, Placeholder.BANK_NAME
    ),
    BANK_TRANSFERRED (
            "A player transfers ownership of a bank to another player.",
            "&6You transferred %PLAYER% ownership of bank &7%BANK_NAME%&6.",
            Placeholder.PLAYER, Placeholder.BANK_NAME
    ),
    BANK_TRANSFERRED_TO_YOU (
            "A player is transferred ownership of a bank by another player.",
            "&6%PLAYER% transferred you ownership of bank &7%BANK_NAME%&6.",
            Placeholder.PLAYER, Placeholder.BANK_NAME
    ),
    BANK_ALREADY_ADMIN (
            "A player attempts to transfer a bank to an admin bank, but the bank is already an admin bank.",
            "&7%BANK_NAME% &cis already an admin bank.",
            Placeholder.BANK_NAME
    ),
    BANK_RESIZED (
            "A player resizes a bank to a certain number of blocks.",
            "&6Bank resized.",
            Placeholder.BANK_SIZE
    ),
    BANK_PROPERTY_SET (
            "A player sets a property at a bank to a new value.",
            "&6Changed &b%PROPERTY% &6at bank &7%BANK_NAME% &6from &b%PREVIOUS_VALUE% &6to &b%VALUE%&6.",
            Placeholder.PROPERTY, Placeholder.BANK_NAME, Placeholder.PREVIOUS_VALUE, Placeholder.VALUE
    ),
    BANK_PROPERTY_NOT_OVERRIDABLE (
            "A player attempts to set a bank property that is configured as not overridable.",
            "&cThat property is not currently overridable. Your changes will be saved, but will not take effect.",
            Placeholder.PROPERTY
    ),
    BANK_SELECTED (
            "A player selects a bank and WorldEdit.",
            "&6Bank &7%BANK_NAME% &6was selected and WorldEdit.",
            Placeholder.BANK_NAME
    ),
    NOT_A_NUMBER (
            "A player attempts to set a configuration property to a number, but provided a string that was not a number.",
            "&c\"%INPUT%\" is not a number.",
            Placeholder.INPUT
    ),
    NOT_AN_INTEGER (
            "A player attempts to set a configuration property to an integer, but provided a string that was not an integer.",
            "&c\"%INPUT%\" is not an integer.",
            Placeholder.INPUT
    ),
    NOT_A_BOOLEAN (
            "A player attempts to set a configuration property to a boolean, but provided a string that was not \"true\" or \"false\".",
            "&c\"%INPUT%\" is not a boolean.",
            Placeholder.INPUT
    ),
    NOT_A_MATERIAL (
            "A player attempts to set a configuration property to a material, but provided a string that was not a material.",
            "&c\"%INPUT%\" is not a material.",
            Placeholder.INPUT
    ),
    NOT_A_PATTERN (
            "A player attempts to set a configuration property to a regular expression, but provided a string that was not a valid pattern.",
            "&c\"%INPUT%\" is not a valid regular expression.",
            Placeholder.INPUT
    ),
    NOT_A_FILENAME (
            "A player attempts to set a configuration property to a filename, but provided a string that was not a valid filename.",
            "&c\"%INPUT%\" is not a valid filename.",
            Placeholder.INPUT
    ),
    NOT_A_FUNCTION (
            "A player attempts to set a configuration property to a mathematical function, but provided a string that was not a valid function.",
            "&c\"%INPUT%\" is not a valid function.",
            Placeholder.INPUT
    ),
    NOT_A_TIME (
            "A player attempts to set a configuration property to a time, but provided a string that was not a time.",
            "&c\"%INPUT%\" is not a time.",
            Placeholder.INPUT
    ),
    NOT_A_WORLD (
            "A player attempts to set a configuration property to a world, but provided a string that was not the name of a world.",
            "&c\"%INPUT%\" is not a world.",
            Placeholder.INPUT
    ),
    NOT_A_PROPERTY (
            "A player attempts to set a configuration property, but provided a string that was not a property.",
            "&c\"%INPUT%\" is not a property.",
            Placeholder.INPUT
    ),
    NAME_NOT_UNIQUE (
            "A player attempts to create or rename a bank and a name already in use by another bank.",
            "&cA bank and that name already exists.",
            Placeholder.NAME
    ),
    NAME_NOT_ALLOWED (
            "A player attempts to create or rename a bank or account and a name that does not match the name-regex pattern in the config.",
            "&cThat name is not allowed.",
            Placeholder.NAME, Placeholder.PATTERN
    ),
    NAME_NOT_CHANGED (
            "A player renames a bank to the same name.",
            "&cThe name was not changed.",
            Placeholder.NAME
    ),
    NAME_CHANGED (
            "A player renames a bank to a different name.",
            "&6The bank was successfully renamed to &7%BANK_NAME%&6.",
            Placeholder.BANK_NAME // TODO: Allow previous bank name?
    ),
    INTEREST_PAID (
            "A bank owner pays interest to the account holders at their bank.",
            "&6You paid account holders &a%AMOUNT% &6in interest.",
            Placeholder.AMOUNT, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    LOW_BALANCE_FEE_RECEIVED (
            "A bank owner receives low balance fee payments from account holders.",
            "&6You received &a%AMOUNT% &cin low balance fees from &b%NUMBER_OF_ACCOUNTS% &caccount(s).",
            Placeholder.AMOUNT, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    BANK_REVENUE (
            "A bank owner receives some bank revenue.",
            "&6You received &a%AMOUNT% &6in bank revenue from &b%NUMBER_OF_BANKS% &6bank(s).",
            Placeholder.AMOUNT, Placeholder.NUMBER_OF_BANKS
    ),
    BANK_PROFIT (
            "A bank owner makes a profit from their bank.",
            "&6You made a profit of &a%AMOUNT% &6at bank &7%BANK_NAME%&6.",
            Placeholder.AMOUNT, Placeholder.BANK_NAME
    ),
    BANK_PROFIT_OFFLINE (
            "A bank owner logs onto the server after having made a profit while offline.",
            "&6You made &a%AMOUNT% &6in bank profit while you were offline.",
            Placeholder.AMOUNT
    ),
    BANK_LOSS_OFFLINE (
            "A bank owner logs onto the server after having made a loss while offline.",
            "&cYou made &a%AMOUNT% &cin bank losses while you were offline.",
            Placeholder.AMOUNT
    ),
    REIMBURSEMENT_RECEIVED (
            "A player is reimbursed by the bank owner after removing an account.",
            "&6You have been reimbursed &a%AMOUNT%&6.",
            Placeholder.AMOUNT
    ),
    REIMBURSEMENT_PAID (
            "A bank owner reimburses a player who removed an account at their bank.", // TODO: Test
            "&6You reimbursed %PLAYER% &a%AMOUNT%&6.",
            Placeholder.PLAYER, Placeholder.AMOUNT
    ),
    ADDED_COOWNER (
            "A player adds another player as a co-owner of an account or a bank.",
            "&6Added %PLAYER% as a co-owner.",
            Placeholder.PLAYER
    ),
    REMOVED_COOWNER (
            "A player removes another player as a co-owner of an account or a bank.",
            "&6Removed %PLAYER% as a co-owner.",
            Placeholder.PLAYER
    ),
    ALREADY_COOWNER (
            "A player attempts to add another player as a co-owner of an account or a bank, but that player is already a co-owner.",
            "&c%PLAYER% is already a co-owner.",
            Placeholder.PLAYER
    ),
    NOT_A_COOWNER (
            "A player attempts to remove another player as a co-owner of an account or a bank, but that player is not a co-owner.",
            "&c%PLAYER% is not a co-owner.",
            Placeholder.PLAYER
    ),
    ALREADY_OWNER (
            "A player attempts to transfer ownership of an account or bank to the player who already owns it.",
            "&c%PLAYER% is already owner.",
            Placeholder.PLAYER
    ),
    MUST_BE_OWNER (
            "A co-owner attempts to perform an action on a bank or account that only the owner can perfom.",
            "&cYou must be the owner to do that."
    ),
    ERROR_OCCURRED (
            "An error occurs, usually during transactions when a loan was not permitted.",
            "&cAn error occurred: %ERROR%",
            Placeholder.ERROR
    ),
    PLAYER_NOT_FOUND (
            "A player references another player in command arguments who either does not exist or who has never played on the server before.",
            "&cNo player was found and the name \"%INPUT%\".",
            Placeholder.INPUT
    ),
    ACCOUNT_LIMIT_AT_BANK_REACHED (
            "A player attempts to create an account at a bank, but has reached their limit for that specific bank.",
            "&cYou are not allowed to create another account at this bank.",
            Placeholder.BANK_NAME, Placeholder.LIMIT
    ),
    PLAYER_COMMAND_ONLY (
            "An admin attempts to execute a player-only command from the console.",
            "&cYou must be a player to do that."
    ),
    SAME_CHEST (
            "A player attempts to migrate an account to the same chest.",
            "&cThat is the same chest."
    ),
    CLICK_CHEST_CREATE (
            "A player runs the /account create command and must click a chest.",
            "&6Click a chest to create an account."
    ),
    CLICK_ACCOUNT_INFO (
            "A player runs the /account info command and must click an account chest.",
            "&6Click an account chest to view info."
    ),
    CLICK_ACCOUNT_MIGRATE (
            "A player runs the /account migrate command and must click an account.",
            "&6Click an account chest to migrate it."
    ),
    CLICK_CHEST_MIGRATE (
            "A player is migrating an account and and must click another chest.",
            "&6Click a chest to migrate the account to."
    ),
    CLICK_CHEST_RECOVER (
            "A player runs the /account recover command, selects a lost account, and must click a chest.",
            "&6Click a chest to recover the account."
    ),
    CLICK_ACCOUNT_REMOVE (
            "A player runs the /account remove command and must click an account chest.",
            "&6Click an account chest to remove an account."
    ),
    CLICK_ACCOUNT_RENAME (
            "A player runs the /account rename command and must click an account chest.",
            "&6Click an account chest to rename it."
    ),
    CLICK_ACCOUNT_CONFIGURE (
            "A player runs the /account configure command and must click an account chest.",
            "&6Click an account chest to set &b%PROPERTY%&6 to &b%VALUE%&6.",
            Placeholder.PROPERTY, Placeholder.VALUE
    ),
    CLICK_ACCOUNT_TRANSFER (
            "A player runs the /account transfer command and must click an account chest.",
            "&6Click an account chest to transfer it to %PLAYER%.",
            Placeholder.PLAYER
    ),
    CLICK_ACCOUNT_TRUST (
            "A player runs the /account trust command and must click an account chest.",
            "&6Click an account chest to add %PLAYER% as a co-owner.",
            Placeholder.PLAYER
    ),
    CLICK_ACCOUNT_UNTRUST (
            "A player runs the /account untrust command and must click an account chest.",
            "&6Click an account chest to remove %PLAYER% as a co-owner.",
            Placeholder.PLAYER
    ),
    CLICK_AGAIN_TO_CONFIRM (
            "A player must click a chest for a second time to confirm an action.",
            "&6Click again to confirm."
    ),
    EXECUTE_AGAIN_TO_CONFIRM (
            "A player must execute a command for a second time to confirm.",
            "&6Execute again to confirm."
    ),
    NO_PERMISSION (
            "A player attempts to execute a command without permission.",
            "&cYou do not have permission to do that."
    ),
    NO_PERMISSION_ACCOUNT_CREATE (
            "A player attempts to use the /account create command without permission.",
            "&cYou do not have permission to create an account."
    ),
    NO_PERMISSION_ACCOUNT_CREATE_PROTECTED (
            "A player attempts to create an account on a protected chest without permission.",
            "&cYou do not have permission to create an account on a protected chest."
    ),
    NO_PERMISSION_ACCOUNT_TRUST (
            "A player attempts to use the /account trust command without permission.",
            "&cYou do not have permission to add a co-owner to an account."
    ),
    NO_PERMISSION_ACCOUNT_TRUST_OTHER (
            "A player attempts to add another player as a co-owner of someone else's account without permission.",
            "&cYou do not have permission to add a co-owner to someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_UNTRUST (
            "A player attempts to use the /account untrust command without permission.",
            "&cYou do not have permission to remove a co-owner from an account."
    ),
    NO_PERMISSION_ACCOUNT_UNTRUST_OTHER (
            "A player attempts to remove another player as a co-owner of someone else's account without permission.",
            "&cYou do not have permission to remove a co-owner from someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_REMOVE (
            "A player attempts to use the /account remove command without permission.",
            "&cYou do not have permission to remove an account."
    ),
    NO_PERMISSION_ACCOUNT_REMOVE_OTHER (
            "A player attempts to remove someone else's account without permission.",
            "&cYou do not have permission to remove someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_REMOVE_PROTECTED (
            "A player attempts to remove a protected account without permission.",
            "&cYou do not have permission to remove a protected account."
    ),
    NO_PERMISSION_ACCOUNT_REMOVEALL (
            "A player attempts to use the /account removeall command without permission.",
            "&cYou do not have permission to remove all accounts."
    ),
    NO_PERMISSION_ACCOUNT_VIEW_OTHER (
            "A player attempts to open someone else's account chest without permission.",
            "&cYou do not have permission to view someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_EDIT_OTHER (
            "A player attempts to edit the contents of someone else's account chest without permission.",
            "&cYou do not have permission to edit someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE (
            "A player attempts to use the /account migrate command without permission.",
            "&cYou do not have permission to migrate an account."
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE_OTHER (
            "A player attempts to migrate someone else's account without permission.",
            "&cYou do not have permission to migrate someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE_BANK (
            "A player attempts to migrate an account to another bank without permission.",
            "&cYou do not have permission to migrate an account to another bank."
    ),
    NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED (
            "A player attempts to migrate an account to a protected chest without permission.",
            "&cYou do not have permission to migrate an account to a protected chest."
    ),
    NO_PERMISSION_ACCOUNT_RECOVER (
            "A player attempts to use the /account recover command without permission.",
            "&cYou do not have permission to recover an account."
    ),
    NO_PERMISSION_ACCOUNT_RENAME (
            "A player attempts to use the /account rename command without permission.",
            "&cYou do not have permission to rename an account."
    ),
    NO_PERMISSION_ACCOUNT_RENAME_OTHER (
            "A player attempts to rename someone else's account without permission.",
            "&cYou do not have permission to rename someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_TRANSFER (
            "A player attempts to use the /account transfer command without permission.",
            "&cYou do not have permission to transfer ownership of an account."
    ),
    NO_PERMISSION_ACCOUNT_TRANSFER_OTHER (
            "A player attempts to transfer ownership of someone else's account without permission.",
            "&cYou do not have permission to transfer ownership of someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_EXTEND_OTHER (
            "A player attempts to extend someone else's account chest without permission.",
            "&cYou do not have permission to extend someone else's account."
    ),
    NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED ( // TODO: Test
            "A player attempts to extend a protected account chest without permission.",
            "&cYou do not have permission to extend a protected account chest."
    ),
    NO_PERMISSION_ACCOUNT_CONTRACT_PROTECTED ( // TODO: Test
            "A player attempts to contract a protected account chest without permission.",
            "&cYou do not have permission to contract a protected account chest."
    ),
    NO_PERMISSION_ACCOUNT_CONFIGURE (
            "A player attempts to use the /account configure command without permission.",
            "&cYou do not have permission to set internal account values."
    ),
    NO_PERMISSION_BANK_CREATE (
            "A player attempts to use the /bank create command without permission.",
            "&cYou do not have permission to create a bank."
    ),
    NO_PERMISSION_BANK_CREATE_ADMIN (
            "A player attempts to create an admin bank without permission.",
            "&cYou do not have permission to create an admin bank."
    ),
    NO_PERMISSION_BANK_CREATE_PROTECTED (
            "A player attempts to create a bank in a region that lacks the WorldGuard flag \"create-bank\" without permission.",
            "&cYou do not have permission to create a bank there."
    ),
    NO_PERMISSION_BANK_REMOVE (
            "A player attempts to use the /bank remove command without permission.",
            "&cYou do not have permission to remove a bank."
    ),
    NO_PERMISSION_BANK_REMOVE_OTHER (
            "A player attempts to remove someone else's bank without permission.",
            "&cYou do not have permission to remove someone else's bank."
    ),
    NO_PERMISSION_BANK_REMOVE_ADMIN (
            "A player attempts to remove an admin bank without permission.",
            "&cYou do not have permission to remove an admin bank."
    ),
    NO_PERMISSION_BANK_REMOVEALL (
            "A player attempts to use the /bank removeall command without permission.",
            "&cYou do not have permission to remove all banks."
    ),
    NO_PERMISSION_BANK_RESIZE ( // TODO: Test
            "A player attempts to use the /bank resize command without permission.",
            "&cYou do not have permission to resize a bank."
    ),
    NO_PERMISSION_BANK_RESIZE_OTHER (
            "A player attempts to resize someone else's bank without permission.",
            "&cYou do not have permission to resize someone else's bank."
    ),
    NO_PERMISSION_BANK_RESIZE_ADMIN (
            "A player attempts to resize an admin bank without permission.",
            "&cYou do not have permission to resize an admin bank."
    ),
    NO_PERMISSION_BANK_RENAME (
            "A player attempts to use the /bank rename command without permission",
            "&cYou do not have permission to rename a bank."
    ),
    NO_PERMISSION_BANK_RENAME_OTHER (
            "A player attempts to rename someone else's bank without permission",
            "&cYou do not have permission to rename someone else's bank."
    ),
    NO_PERMISSION_BANK_RENAME_ADMIN (
            "A player attempts to rename an admin bank without permission",
            "&cYou do not have permission to rename an admin bank."
    ),
    NO_PERMISSION_BANK_TRUST (
            "A player attempts to use the /bank trust command without permission.",
            "&cYou do not have permission to add a co-owner to a bank."
    ),
    NO_PERMISSION_BANK_TRUST_OTHER (
            "A player attempts to add another player as a co-owner of someone else's bank without permission.",
            "&cYou do not have permission to add a co-owner to someone else's bank."
    ),
    NO_PERMISSION_BANK_TRUST_ADMIN (
            "A player attempts to add another player as a co-owner of an admin bank without permission.",
            "&cYou do not have permission to add a co-owner to an admin bank."
    ),
    NO_PERMISSION_BANK_UNTRUST (
            "A player attempts to use the /bank untrust command without permission.",
            "&cYou do not have permission to remove a co-owner from a bank."
    ),
    NO_PERMISSION_BANK_UNTRUST_OTHER (
            "A player attempts to remove another player as a co-owner of someone else's bank without permission.",
            "&cYou do not have permission to remove a co-owner from someone else's bank."
    ),
    NO_PERMISSION_BANK_UNTRUST_ADMIN (
            "A player attempts to remove another player as a co-owner of an admin bank without permission.",
            "&cYou do not have permission to remove a co-owner from an admin bank."
    ),
    NO_PERMISSION_BANK_TRANSFER (
            "A player attempts to use the /bank transfer command without permission.",
            "&cYou do not have permission to transfer ownership of a bank."
    ),
    NO_PERMISSION_BANK_TRANSFER_OTHER (
            "A player attempts to transfer ownership of someone else's bank without permission.",
            "&cYou do not have permission to transfer ownership of someone else's bank."
    ),
    NO_PERMISSION_BANK_TRANSFER_ADMIN (
            "A player attempts to transfer ownership of an admin bank without permission.",
            "&cYou do not have permission to transfer ownership of an admin bank."
    ),
    NO_PERMISSION_BANK_CONFIGURE (
            "A player attempts to use the /bank configure command without permission.",
            "&cYou do not have permission to configure a bank."
    ),
    NO_PERMISSION_BANK_CONFIGURE_OTHER (
            "A player attempts to configure someone else's bank without permission.",
            "&cYou do not have permission to configure someone else's bank."
    ),
    NO_PERMISSION_BANK_CONFIGURE_ADMIN (
            "A player attempts to configure an admin bank without permission.",
            "&cYou do not have permission to configure an admin bank."
    ),
    NO_PERMISSION_BANK_SELECT (
            "A player attempts to select a bank without the WorldEdit permission worldedit.selection.pos.",
            "&cYou do not have permission to select a bank."
    ),
    NO_PERMISSION_RELOAD (
            "A player attempts to reload the plugin without permission.",
            "&cYou do not have permission to reload the plugin."
    ),
    NO_PERMISSION_CONFIG (
            "A player attempts to configure the plugin without permission.",
            "&cYou do not have permission to configure the plugin."
    ),
    NO_PERMISSION_UPDATE (
            "A player attempts to update the plugin without permission.",
            "&cYou do not have permission to update the plugin."
    ),
    NO_PERMISSION_PAY_INTEREST (
            "A player attempts to trigger an interest payment without permission.",
            "&cYou do not have permission to trigger an interest payment."
    ),
    COMMAND_USAGE_ACCOUNT_CREATE (
            "",
            "&a/%COMMAND% create &6- Create an account.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_REMOVE (
            "",
            "&a/%COMMAND% remove &6- Remove an account.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_INFO (
            "",
            "&a/%COMMAND% info &6- Open an account GUI.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_LIST (
            "",
            "&a/%COMMAND% list <names> &6- View a list of accounts.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_LIMITS (
            "",
            "&a/%COMMAND% limits &6- View your account limits.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_REMOVE_ALL (
            "",
            "&a/%COMMAND% removeall &6- Remove all accounts.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_CONFIGURE (
            "",
            "&a/%COMMAND% configure [property] [value] &6- Configure an account value.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_TRUST (
            "",
            "&a/%COMMAND% trust [name] &6- Add a co-owner to an account.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_UNTRUST (
            "",
            "&a/%COMMAND% untrust [name] &6- Remove a co-owner from an account.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_MIGRATE (
            "",
            "&a/%COMMAND% migrate &6- Migrate an account to a different chest.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_RECOVER (
            "",
            "&a/%COMMAND% recover &6- Recover a lost account to a new chest.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_RENAME (
            "",
            "&a/%COMMAND% rename [name] &6- Give an account a nickname.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_ACCOUNT_TRANSFER (
            "",
            "&a/%COMMAND% transfer [name] &6- Transfer ownership of an account.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_CREATE (
            "",
            "&a/%COMMAND% create &6- Create a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_REMOVE (
            "",
            "&a/%COMMAND% remove &6- Remove a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_INFO (
            "",
            "&a/%COMMAND% info &6- Open a bank GUI.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_LIST (
            "",
            "&a/%COMMAND% list &6- View a list of banks.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_LIMITS (
            "",
            "&a/%COMMAND% limits &6- View your bank limits.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_REMOVEALL (
            "",
            "&a/%COMMAND% removeall &6- Remove all banks.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_RESIZE (
            "",
            "&a/%COMMAND% resize &6- Resize a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_TRUST (
            "",
            "&a/%COMMAND% trust &6- Add a co-owner to a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_UNTRUST (
            "",
            "&a/%COMMAND% untrust &6- Remove a co-owner from a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_RENAME (
            "",
            "&a/%COMMAND% rename &6- Rename a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_CONFIGURE (
            "",
            "&a/%COMMAND% configure &6- Configure a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_SELECT (
            "",
            "&a/%COMMAND% select &6- Select a bank and WorldEdit.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_BANK_TRANSFER (
            "",
            "&a/%COMMAND% transfer &6- Transfer ownership of a bank.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_VERSION (
            "",
            "&a/%COMMAND% version &6- View the version of BankingPlugin.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_RELOAD (
            "",
            "&a/%COMMAND% reload &6- Reload BankingPlugin.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_CONFIG (
            "",
            "&a/%COMMAND% configure [property] [value] &6- Configure BankingPlugin.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_UPDATE (
            "",
            "&a/%COMMAND% update &6- Update BankingPlugin.",
            Placeholder.COMMAND
    ),
    COMMAND_USAGE_PAY_INTEREST (
            "",
            "&a/%COMMAND% payinterest <banks> &6- Trigger an interest payment.",
            Placeholder.COMMAND
    ),
    ACCOUNT_COMMAND_DESC (
            "",
            "Create, manage, and remove accounts."
    ),
    BANK_COMMAND_DESC (
            "",
            "Create, manage, and remove banks."
    ),
    CONTROL_COMMAND_DESC (
            "",
            "Manage the plugin."
    ),
    NOT_A_CONFIG_VALUE (
            "An admin tries to configure a property in the config that does not exist.",
            "&c\"%INPUT%\" is not a configuration property.",
            Placeholder.INPUT
    ),
    CONFIG_VALUE_ADDED (
            "An admin adds a value to a property in the config.",
            "&6Added &a%VALUE% &6to &a%PROPERTY%&6.",
            Placeholder.VALUE, Placeholder.PROPERTY
    ),
    CONFIG_VALUE_REMOVED (
            "An admin removes a value from a property in the config.",
            "&6Removed &a%VALUE% &6from &a%PROPERTY%&6.",
            Placeholder.VALUE, Placeholder.PROPERTY
    ),
    CONFIG_VALUE_SET (
            "An admin sets the value of a property in the config.",
            "&6Changed &a%PROPERTY% &6from &a%PREVIOUS_VALUE% &6to &a%VALUE%&6.",
            Placeholder.PROPERTY, Placeholder.PREVIOUS_VALUE, Placeholder.VALUE
    ),
    RESTART_REQUIRED (
            "An admin sets the value of a property in the config, but a restart is required before the changes can take effect.",
            "&cA restart is required before changes to that property can take effect.",
            Placeholder.PROPERTY
    ),
    INTEREST_PAYOUT_TRIGGERED (
            "An admin triggers an interest payout.",
            "&6Triggered an interest payout at &b%NUMBER_OF_BANKS% &6bank(s).",
            Placeholder.NUMBER_OF_BANKS
    ),
    RELOADED_PLUGIN (
            "An admin reloads all banks and accounts on the server.",
            "&6Successfully reloaded &b%NUMBER_OF_BANKS% &6bank(s) and &b%NUMBER_OF_ACCOUNTS% &6account(s).",
            Placeholder.NUMBER_OF_BANKS, Placeholder.NUMBER_OF_ACCOUNTS
    ),
    UPDATE_AVAILABLE (
            "An admin is notified that an update to BankingPlugin is available.",
            "&6&lVersion &c%VERSION% &6of &aBanking&2Plugin &6is available.",
            Placeholder.VERSION
    ),
    UPDATE_CLICK_TO_DOWNLOAD (
            "An admin is notified of the download location for the update.",
            "&6Click to download: %LINK%",
            Placeholder.LINK
    ),
    UPDATE_NO_UPDATE (
            "An admin is notified that no new updates are available.",
            "&6&lNo new version available."
    ),
    UPDATE_CHECKING (
            "An admin is notified that the plugin is checking for updates.",
            "&6&lChecking for updates..."
    ),
    UPDATE_ERROR (
            "An admin is notified that an error occurred while checking for updates.",
            "&c&lError while checking for updates."
    );

    private final String path;
    private final String scenario;
    private final String defaultMessage;
    private final EnumSet<Placeholder> placeholders = EnumSet.noneOf(Placeholder.class);
    private String translation = null;

    Message(String scenario, String defaultMessage, Placeholder... placeholders) {
        this.path = "message." + toString().toLowerCase().replace("_", "-");
        this.scenario = scenario;
        this.defaultMessage = defaultMessage;
        this.placeholders.addAll(Arrays.asList(placeholders));
    }

    public String getPath() {
        return path;
    }

    public String getExampleScenario() {
        return scenario;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getFormattedPlaceholdersList() {
        return placeholders.stream().map(Placeholder::toString).collect(Collectors.joining(", "));
    }

    public String getTranslation() {
        if (translation != null)
            return translation;
        return defaultMessage;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public Builder.ReplacementBuilder with(Placeholder placeholder) {
        return new Builder().and(placeholder);
    }

    public String translate() {
        if (!placeholders.isEmpty())
            throw new IllegalStateException("Missing replacement(s)! " + placeholders);
        return Utils.colorize(getTranslation());
    }

    public class Builder {

        private final List<Replacement> replacements;

        private Builder() {
            replacements = new ArrayList<>();
        }

        public ReplacementBuilder and(Placeholder placeholder) {
            return new ReplacementBuilder(placeholder);
        }

        public String translate() {
            EnumSet<Placeholder> remainingPlaceholders = placeholders.clone();
            String translation = getTranslation();
            for (Replacement replacement : replacements) {
                Placeholder placeholder = replacement.getPlaceholder();
                if (!remainingPlaceholders.remove(placeholder))
                    continue;
                translation = translation.replace(placeholder.toString(), replacement.getReplacement());
            }
            if (!remainingPlaceholders.isEmpty())
                throw new IllegalStateException("Missing replacement(s)! " + remainingPlaceholders);
            return Utils.colorize(translation);
        }

        public class ReplacementBuilder {

            private final Placeholder placeholder;

            private ReplacementBuilder(Placeholder placeholder) {
                this.placeholder = placeholder;
            }

            public Builder as(Object replacement) {
                replacements.add(new Replacement(placeholder, replacement));
                return Builder.this;
            }
        }
    }
}
