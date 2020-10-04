package com.monst.bankingplugin.lang;

public enum Message {

    ACCOUNT_CREATED ("&6Account created!"),
    ACCOUNT_CREATE_INSUFFICIENT_FUNDS ("&cNot enough money. You need &a%AMOUNT% &6more to create an account."),
    NO_SELF_BANKING ("&cYou are not allowed to create an account at your own bank."),
    ACCOUNT_CREATE_FEE_PAID ("&6You have been charged &a%AMOUNT% &6to create an account at &7%BANK_NAME%&6."),
    ACCOUNT_CREATE_FEE_RECEIVED ("&6%PLAYER% has paid you &a%AMOUNT% &6to create an account at &7%BANK_NAME%&6."),
    ACCOUNT_EXTEND_FEE_PAID ("&6You have been charged &a%AMOUNT% &6to extend an account at &7%BANK_NAME%&6."),
    ACCOUNT_EXTEND_FEE_RECEIVED ("&6%PLAYER% has paid you &a%AMOUNT% &6to extend an account at &7%BANK_NAME%&6."),
    ACCOUNT_REMOVED ("&6Account removed."),
    ACCOUNT_REMOVED_ALL ("&6Successfully removed &b%NUMBER_OF_ACCOUNTS% &6account%s."),
    ACCOUNTS_NOT_FOUND ("&cNo accounts found."),
    ABOUT_TO_REMOVE_ACCOUNTS ("&6You are about to remove &b%NUMBER_OF_ACCOUNTS% &6account(s). Execute again to confirm."),
    ACCOUNT_BALANCE_NOT_ZERO ("&cThat account still has a balance of &a%BALANCE%&c. Are you sure?"),
    ACCOUNT_LIMIT ("&6You own %NUMBER_OF_ACCOUNTS%&a/%LIMIT% allowed accounts."),
    ACCOUNT_LIMIT_REACHED ("&cYou are not allowed to create any more accounts!"),
    ACCOUNT_RENAMED ("&6Account renamed."),
    ACCOUNT_MULTIPLIER_SET ("&6Account multiplier set to &b%MULTIPLIER%&6."),
    ACCOUNT_INTEREST_DELAY_SET ("&6Account interest delay set to &b%NUMBER%&6."),
    ACCOUNT_REMAINING_OFFLINE_SET ("&6Account remaining offline payouts set to &b%NUMBER%&6."),
    ACCOUNT_REMAINING_OFFLINE_RESET_SET ("&6Account remaining offline payouts until multiplier reset set to &b%NUMBER%&6."),
    ACCOUNT_ABOUT_TO_TRANSFER ("&6Click again to confirm transferring ownership of an account to %PLAYER%."),
    ACCOUNT_TRANSFERRED ("&6You transferred %PLAYER% ownership of an account."),
    ACCOUNT_TRANSFERRED_TO_YOU ("&6%OWNER% transferred you ownership of an account."),
    ACCOUNT_MIGRATED ("&6Account migrated!"),
    ACCOUNT_RECOVERED ("&6Account recovered!"),
    ACCOUNT_OPENED ("&6You opened %PLAYER%'s account chest."),
    ACCOUNT_DEPOSIT ("&6You deposited &a%AMOUNT% &6for a new balance of &a%ACCOUNT_BALANCE%&6."),
    ACCOUNT_WITHDRAWAL ("&6You withdrew &a%AMOUNT% &6for a new balance of &a%ACCOUNT_BALANCE%&6."),
    INTEREST_EARNED ("&6You earned &a%AMOUNT% &6in interest on &b%NUMBER_OF_ACCOUNTS% &6accounts."),
    OFFLINE_ACCOUNT_INTEREST ("&6You earned &a%AMOUNT% &6in interest while you were offline."),
    LOW_BALANCE_FEE_PAID ("&cYou paid &a%AMOUNT% &cin low balance fees on &b%NUMBER_OF_ACCOUNTS% &caccounts."),
    MULTIPLIER_DECREASED ("&cAccount multiplier decreased to &b%MULTIPLIER%&c."),
    CHEST_BLOCKED ("&cChest is blocked."),
    CHEST_ALREADY_ACCOUNT ("&cChest is already an account."),
    CANNOT_BREAK_ACCOUNT ("&cYou cannot break that account chest."),
    CHEST_NOT_IN_BANK ("&cChest is not in a bank."),

    BANK_CREATED ("&6Bank created!"),
    BANK_CREATE_INSUFFICIENT_FUNDS ("&cNot enough money. You need &a%AMOUNT% &6more to create a bank."),
    BANK_CREATE_FEE_PAID ("&6You have been charged &a%AMOUNT% &6to create a bank."),
    SELECT_WORLDEDIT_REGION ("&cYou must select a WorldEdit region or specify coordinates manually."),
    WORLDEDIT_NOT_ENABLED ("&cWorldEdit is not currently enabled. Please enter coordinates manually."),
    COORDINATE_PARSE_ERROR ("&cCould not parse coordinates in command arguments."),
    BANK_CREATION_DISABLED ("&cBank creation is disabled in this world."),
    SELECTION_TOO_LARGE ("&cThat selection is too large. It exceeds the maximum bank size by &b%NUMBER% &cblocks."),
    SELECTION_TOO_SMALL ("&cThat selection is too small. It falls short of the minimum bank size by &b%NUMBER% &cblocks."),
    SELECTION_OVERLAPS_EXISTING ("&cThat selection overlaps with another bank."),
    SELECTION_CUTS_ACCOUNTS ("&cThat selection does not include all accounts at the bank."),
    BANK_REMOVED ("&6Bank removed."),
    BANK_REMOVED_ALL ("&6Successfully removed &b%NUMBER_OF_BANKS% &6banks and &b%NUMBER_OF_ACCOUNTS% &6accounts."),
    BANK_NOT_FOUND ("&cCould not find bank with the name or ID \"%STRING%\"."),
    BANKS_NOT_FOUND("&cNo banks found."),
    ABOUT_TO_REMOVE_BANKS ("&6You are about to remove &b%NUMBER_OF_BANKS% &6banks and &b%NUMBER_OF_ACCOUNTS% &6accounts. Execute again to confirm."),
    MUST_STAND_IN_BANK ("&cYou must stand in or specify the name or ID of a bank."),
    BANK_LIMIT ("&6You own %NUMBER_OF_BANKS%&a/%LIMIT% allowed banks."),
    BANK_LIMIT_REACHED ("&cYou are not allowed to create any more banks!"),
    BANK_ABOUT_TO_TRANSFER ("&6Click again to confirm transferring ownership of bank &7%BANK_NAME% &6to %PLAYER%."),
    BANK_TRANSFERRED ("&6You transferred %PLAYER% ownership of bank &7%BANK_NAME%&6."),
    BANK_TRANSFERRED_TO_YOU ("&6%OWNER% transferred you ownership of bank &7%BANK_NAME%&6."),
    ALREADY_ADMIN_BANK ("&7%BANK_NAME% &cis alread an admin bank."),
    BANK_RESIZED ("&6Bank resized."),
    BANK_FIELD_SET ("&6Changed &b%PROPERTY% &6at bank &7%BANK_NAME% &6to &b%VALUE%&6."),
    BANK_FIELD_NOT_OVERRIDABLE ("&cThat property is not overridable."),
    NOT_A_NUMBER ("&c\"%STRING%\" is not a number."),
    NOT_AN_INTEGER ("&c\"%STRING%\" is not an integer."),
    NOT_A_FIELD ("&c\"%STRING%\" is not a property."),
    NOT_A_LIST ("&c\"%STRING%\" is not a parsable list."),
    BANK_SELECTED ("&6Bank &7%BANK_NAME% &6was selected with WorldEdit."),
    NAME_NOT_UNIQUE ("&cA bank with that name already exists."),
    NAME_NOT_ALLOWED ("&cName is not allowed."),
    NAME_NOT_CHANGED ("&cThe bank name was not changed."),
    NAME_CHANGED ("&6The bank was successfully renamed to &7%NAME%&6."),
    INTEREST_PAID ("&6You paid account holders &a%AMOUNT% &6in interest."),
    BANK_REVENUE_EARNED ("&6You earned &a%AMOUNT% &6in revenue at bank &7%BANK_NAME%&6."),
    BANK_REVENUE_EARNED_OFFLINE ("&6You earned &a%AMOUNT% &6in bank revenue while you were offline."),
    BANK_LOSS_OFFLINE ("&cYou lost &a%AMOUNT% &cin bank revenue while you were offline."),
    LOW_BALANCE_FEE_RECEIVED ("&6You received &a%AMOUNT% &cin low balance fees on &b%NUMBER_OF_ACCOUNTS% &caccounts."),
    REIMBURSEMENT_RECEIVED ("&6You have been reimbursed &a%AMOUNT%&6."),
    REIMBURSEMENT_PAID ("&6You reimbursed %PLAYER% &a%AMOUNT%&6."),

    ADDED_COOWNER ("&6%PLAYER% added as a co-owner."),
    REMOVED_COOWNER ("&6%PLAYER% removed as a co-owner."),
    ALREADY_COOWNER ("&c%PLAYER% is already a co-owner."),
    NOT_A_COOWNER ("&c%PLAYER% is not a co-owner."),
    ALREADY_OWNER ("&c%OWNER% is already owner."),
    MUST_BE_OWNER ("&cYou must be the owner to do that."),

    ERROR_OCCURRED ("&cAn error occurred: %ERROR%"),

    PLAYER_NOT_FOUND ("&cNo player was found with the name \"%STRING%\"."),
    ACCOUNT_LIMIT_AT_BANK_REACHED ("&cYou are not allowed to create another account at this bank."),
    PLAYER_COMMAND_ONLY ("&cPlayer command only."),
    SAME_ACCOUNT ("&cThat is the same account."),

    CLICK_CHEST_CREATE ("&6Click a chest to create an account."),
    CLICK_ACCOUNT_INFO ("&6Click an account chest to view info."),
    CLICK_ACCOUNT_MIGRATE ("&6Click an account chest to migrate it."),
    CLICK_CHEST_MIGRATE ("&6Click a chest to migrate the account to."),
    CLICK_CHEST_RECOVER ("&6Click a chest to recover the account."),
    CLICK_ACCOUNT_REMOVE ("&6Click an account chest to remove an account."),
    CLICK_ACCOUNT_RENAME ("&6Click an account chest to rename it."),
    CLICK_ACCOUNT_SET ("&6Click an account chest to set it."),
    CLICK_ACCOUNT_TRANSFER ("&6Click an account chest to transfer it to %PLAYER%."),
    CLICK_ACCOUNT_TRUST ("&6Click an account chest to add %PLAYER% as a co-owner."),
    CLICK_ACCOUNT_UNTRUST ("&6Click an account chest to remove %PLAYER% as a co-owner."),

    NO_PERMISSION_ACCOUNT_CREATE ("&cYou do not have permission to create an account."),
    NO_PERMISSION_ACCOUNT_CREATE_PROTECTED ("&cYou do not have permission to create an account on a protected chest."),
    NO_PERMISSION_ACCOUNT_TRUST ("&cYou do not have permission to add a co-owner to an account."),
    NO_PERMISSION_ACCOUNT_TRUST_OTHER ("&cYou do not have permission to add a co-owner to someone else's account."),
    NO_PERMISSION_ACCOUNT_UNTRUST ("&cYou do not have permission to remove a co-owner from an account."),
    NO_PERMISSION_ACCOUNT_UNTRUST_OTHER ("&cYou do not have permission to remove a co-owner from someone else's account."),
    NO_PERMISSION_ACCOUNT_REMOVE_OTHER ("&cYou do not have permission to remove someone else's account."),
    NO_PERMISSION_ACCOUNT_REMOVEALL ("&cYou do not have permission to remove all accounts."),
    NO_PERMISSION_ACCOUNT_VIEW_OTHER ("&cYou do not have permission to view someone else's account."),
    NO_PERMISSION_ACCOUNT_EDIT_OTHER ("&cYou do not have permission to edit someone else's account."),
    NO_PERMISSION_ACCOUNT_MIGRATE ("&cYou do not have permission to migrate an account."),
    NO_PERMISSION_ACCOUNT_MIGRATE_OTHER ("&cYou do not have permission to migrate someone else's account."),
    NO_PERMISSION_ACCOUNT_MIGRATE_BANK ("&cYou do not have permission to migrate an account to another bank."),
    NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED ("&cYou do not have permission to migrate an account to a protected chest."),
    NO_PERMISSION_ACCOUNT_RENAME ("&cYou do not have permission to rename an account."),
    NO_PERMISSION_ACCOUNT_RENAME_OTHER ("&cYou do not have permission to rename someone else's account."),
    NO_PERMISSION_ACCOUNT_TRANSFER ("&cYou do not have permission to transfer ownership of an account."),
    NO_PERMISSION_ACCOUNT_TRANSFER_OTHER ("&cYou do not have permission to transfer ownership of someone else's account."),
    NO_PERMISSION_ACCOUNT_EXTEND_OTHER ("&cYou do not have permission to extend someone else's account."),
    NO_PERMISSION_ACCOUNT_EXTEND_PROTECTED ("&cYou do not have permission to extend a protected account chest."),
    NO_PERMISSION_ACCOUNT_SET ("&cYou do not have permission to set internal account values."),

    NO_PERMISSION_BANK_CREATE ("&cYou do not have permission to create a bank."),
    NO_PERMISSION_BANK_CREATE_ADMIN ("&cYou do not have permission to create an admin bank."),
    NO_PERMISSION_BANK_CREATE_PROTECTED ("&cYou do not have permission to create a bank there."),
    NO_PERMISSION_BANK_REMOVE_OTHER ("&cYou do not have permission to remove someone else's bank."),
    NO_PERMISSION_BANK_REMOVE_ADMIN ("&cYou do not have permission to remove an admin bank."),
    NO_PERMISSION_BANK_REMOVEALL ("&cYou do not have permission to remove all banks."),
    NO_PERMISSION_BANK_RESIZE ("&cYou do not have permission to resize a bank."),
    NO_PERMISSION_BANK_RESIZE_OTHER ("&cYou do not have permission to resize someone else's bank."),
    NO_PERMISSION_BANK_RESIZE_ADMIN ("&cYou do not have permission to resize an admin bank."),
    NO_PERMISSION_BANK_TRUST ("&cYou do not have permission to add a co-owner to a bank."),
    NO_PERMISSION_BANK_TRUST_OTHER ("&cYou do not have permission to add a co-owner to someone else's bank."),
    NO_PERMISSION_BANK_TRUST_ADMIN ("&cYou do not have permission to add a co-owner to an admin bank."),
    NO_PERMISSION_BANK_UNTRUST ("&cYou do not have permission to remove a co-owner from a bank."),
    NO_PERMISSION_BANK_UNTRUST_OTHER ("&cYou do not have permission to remove a co-owner from someone else's bank."),
    NO_PERMISSION_BANK_UNTRUST_ADMIN ("&cYou do not have permission to remove a co-owner from an admin bank."),
    NO_PERMISSION_BANK_TRANSFER ("&cYou do not have permission to transfer ownership of a bank."),
    NO_PERMISSION_BANK_TRANSFER_OTHER ("&cYou do not have permission to transfer ownership of someone else's bank."),
    NO_PERMISSION_BANK_TRANSFER_ADMIN ("&cYou do not have permission to transfer ownership of an admin bank."),
    NO_PERMISSION_BANK_SET_OTHER ("&cYou do not have permission to configure someone else's bank."),
    NO_PERMISSION_BANK_SET_ADMIN ("&cYou do not have permission to configure an admin bank."),
    NO_PERMISSION_BANK_SELECT ("&cYou do not have permission to select a bank."),

    NO_PERMISSION_RELOAD ("&cYou do not have permission to reload the plugin."),
    NO_PERMISSION_CONFIG ("&cYou do not have permission to configure the plugin."),
    NO_PERMISSION_UPDATE ("&cYou do not have permission to update the plugin."),
    NO_PERMISSION_PAY_INTEREST ("&cYou do not have permission to trigger an interest payment."),
    
    COMMAND_USAGE_ACCOUNT_CREATE ("&a/%COMMAND% create - Create an account."),
    COMMAND_USAGE_ACCOUNT_REMOVE ("&a/%COMMAND% remove - Remove an account."),
    COMMAND_USAGE_ACCOUNT_INFO ("&a/%COMMAND% info - Open an account GUI."),
    COMMAND_USAGE_ACCOUNT_LIST ("&a/%COMMAND% list <names> - View a list of accounts."),
    COMMAND_USAGE_ACCOUNT_LIMITS ("&a/%COMMAND% limits - View your account limits."),
    COMMAND_USAGE_ACCOUNT_REMOVEALL ("&a/%COMMAND% removeall - Remove all accounts."),
    COMMAND_USAGE_ACCOUNT_SET ("&a/%COMMAND% set [property] [value] - Set an account value."),
    COMMAND_USAGE_ACCOUNT_TRUST ("&a/%COMMAND% trust [name] - Add a co-owner to an account."),
    COMMAND_USAGE_ACCOUNT_UNTRUST ("&a/%COMMAND% untrust [name] - Remove a co-owner from an account."),
    COMMAND_USAGE_ACCOUNT_MIGRATE ("&a/%COMMAND% migrate - Migrate an account to a different chest."),
    COMMAND_USAGE_ACCOUNT_RECOVER ("&a/%COMMAND% recover - Recover a lost account to a new chest."),
    COMMAND_USAGE_ACCOUNT_RENAME ("&a/%COMMAND% rename [name] - Give an account a nickname."),
    COMMAND_USAGE_ACCOUNT_TRANSFER ("&a/%COMMAND% transfer [name] - Transfer ownership of an account."),

    COMMAND_USAGE_BANK_CREATE ("&a/%COMMAND% create - Create a bank."),
    COMMAND_USAGE_BANK_REMOVE ("&a/%COMMAND% remove - Remove a bank."),
    COMMAND_USAGE_BANK_INFO ("&a/%COMMAND% info - Open a bank GUI."),
    COMMAND_USAGE_BANK_LIST ("&a/%COMMAND% list - View a list of banks."),
    COMMAND_USAGE_BANK_LIMITS ("&a/%COMMAND% limits - View your bank limits."),
    COMMAND_USAGE_BANK_REMOVEALL ("&a/%COMMAND% removeall - Remove all banks."),
    COMMAND_USAGE_BANK_RESIZE ("&a/%COMMAND% resize - Resize a bank."),
    COMMAND_USAGE_BANK_TRUST ("&a/%COMMAND% trust - Add a co-owner to a bank."),
    COMMAND_USAGE_BANK_UNTRUST ("&a/%COMMAND% untrust - Remove a co-owner from a bank."),
    COMMAND_USAGE_BANK_RENAME ("&a/%COMMAND% rename - Rename a bank."),
    COMMAND_USAGE_BANK_SET ("&a/%COMMAND% set - Configure a bank."),
    COMMAND_USAGE_BANK_SELECT ("&a/%COMMAND% select - Select a bank with WorldEdit."),
    COMMAND_USAGE_BANK_TRANSFER ("&a/%COMMAND% transfer - Transfer ownership of a bank."),

    COMMAND_USAGE_VERSION ("&a/%COMMAND% version - View the version of BankingPlugin."),
    COMMAND_USAGE_RELOAD ("&a/%COMMAND% reload - Reload BankingPlugin."),
    COMMAND_USAGE_CONFIG ("&a/%COMMAND% config [set|add|remove] [property] [value] - Configure BankingPlugin."),
    COMMAND_USAGE_UPDATE ("&a/%COMMAND% update - Update BankingPlugin."),
    COMMAND_USAGE_PAY_INTEREST ("&a/%COMMAND% payinterest <banks> - Trigger an interest payment."),

    ACCOUNT_COMMAND_DESC ("Create, manage, and remove accounts."),
    BANK_COMMAND_DESC ("Create, manage, and remove banks."),
    CONTROL_COMMAND_DESC ("Manage the plugin."),

    CONFIG_VALUE_ADDED ("&6Added &a%VALUE% &6to &a%PROPERTY%&6."),
    CONFIG_VALUE_REMOVED ("&6Removed &a%VALUE% &6from &a%PROPERTY%&6."),
    CONFIG_VALUE_SET ("&6Changed &a%PROPERTY% &6from &a%PREVIOUS_VALUE% &6to &a%VALUE%&6."),
    INTEREST_PAYOUT_TRIGGERED ("&6Triggered an interest payout at &b%NUMBER_OF_BANKS% &6banks."),
    RELOADED_PLUGIN ("&6Successfully reloaded &b%NUMBER_OF_BANKS% &6banks and &b%NUMBER_OF_ACCOUNTS% &6accounts."),

    UPDATE_AVAILABLE ("&6&lVersion &c%VERSION% &6of &aBanking&7Plugin &6is available &chere&6."),
    UPDATE_CLICK_TO_DOWNLOAD ("Click to download."),
    UPDATE_NO_UPDATE ("&6&lNo new update available."),
    UPDATE_CHECKING ("&6&lChecking for updates..."),
    UPDATE_ERROR ("&c&lError while checking for updates."),

    COMMAND_DESC_HEADER (""),
    COMMAND_DESC_FOOTER ("");

    private final String defaultMessage;
    private final Type type;

    Message(String defaultMessage) {
        this(Type.DEFAULT, defaultMessage);
    }

    Message(Type type, String defaultMessage) {
        this.type = type;
        this.defaultMessage = defaultMessage;
    }

    String getDefaultMessage() {
        return defaultMessage;
    }

    String getPath() {
        return "message." + type.section + toString().toLowerCase().replace("_", "-");
    }

    private enum Type {

        DEFAULT(""),
        COMMAND_DESCRIPTION("commandDescription."),
        NO_PERMISSION("noPermission."),
        CONFIG("config."),
        UPDATE("update.");

        private final String section;

        Type(String section) {
            this.section = section;
        }
    }
}
