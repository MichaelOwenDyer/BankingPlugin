package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.LanguageConfig;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;

import java.util.EnumMap;

public class LangUtils {

    private static final BankingPlugin plugin = BankingPlugin.getInstance();

    private static final EnumMap<Message, String> messages = new EnumMap<>(Message.class);

    private static void reload() {
        LanguageConfig langConfig = plugin.getPluginConfig().getLanguageConfig();

        for (Message message : Message.values())
            messages.put(message, langConfig.getString(message.toString()));

        // Add BankingPlugin Messages
        messages.put(Message.ACCOUNT_CREATED, langConfig.getString("message.account-created", "&6You were withdrawn &c%CREATION-PRICE% &6to create an account."));
        messages.put(Message.BANK_CREATED, langConfig.getString("message.bank-created", "&6You were withdrawn &c%CREATION-PRICE% &6to create a bank."));
        messages.put(Message.BANK_TRANSFERRED, langConfig.getString("message.bank-transferred", "&6You were withdrawn &c%CREATION-PRICE% &6to create a bank."));
        messages.put(Message.ACCOUNT_TRANSFERRED, langConfig.getString("message.bank-transferred", "&6You were withdrawn &c%CREATION-PRICE% &6to create a bank."));
        messages.put(Message.ACCOUNT_MIGRATED, langConfig.getString("message.bank-transferred", "&6You were withdrawn &c%CREATION-PRICE% &6to create a bank."));
        messages.put(Message.ACCOUNT_RECOVERED, langConfig.getString("message.bank-transferred", "&6You were withdrawn &c%CREATION-PRICE% &6to create a bank."));
        messages.put(Message.CHEST_ALREADY_ACCOUNT, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.ACCOUNT_CREATE_FEE_PAID, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.BANK_CREATE_FEE_PAID, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.ACCOUNT_CREATE_FEE_RECEIVED, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.ACCOUNT_EXTEND_FEE_PAID, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.ACCOUNT_EXTEND_FEE_RECEIVED, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.NAME_NOT_UNIQUE, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.NAME_NOT_ALLOWED, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.NAME_NOT_CHANGED, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.NAME_CHANGED, langConfig.getString("message.chest-already-account", "&cChest already account."));
        messages.put(Message.CHEST_BLOCKED, langConfig.getString("message.chest-blocked", "&cThere must not be a block above the chest."));
        messages.put(Message.DOUBLE_CHEST_BLOCKED, langConfig.getString("message.double-chest-blocked", "&cThere must not be a block above the chest."));
        messages.put(Message.ACCOUNT_REMOVED, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.ACCOUNTS_REMOVED, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.BANK_REMOVED, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.BANKS_REMOVED, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.BANK_RESIZED, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.BANK_FIELD_SET, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.BANK_SELECTED, langConfig.getString("message.account-removed", "&6Shop removed."));
        messages.put(Message.REVENUE_WHILE_OFFLINE, langConfig.getString("message.revenue-while-offline", "&6While you were offline, your accounts have made a revenue of &c%REVENUE%&6."));
        messages.put(Message.NOT_ENOUGH_MONEY, langConfig.getString("message.not-enough-money", "&cNot enough money."));
        messages.put(Message.ERROR_OCCURRED, langConfig.getString("message.error-occurred", "&cAn error occurred: %ERROR%"));
        messages.put(Message.CLICK_CHEST_CREATE, langConfig.getString("message.click-chest-to-create-account", "&aClick a chest within 15 seconds to create an account."));
        messages.put(Message.CLICK_CHEST_REMOVE, langConfig.getString("message.click-chest-to-remove-account", "&aClick an account within 15 seconds to remove it."));
        messages.put(Message.CLICK_CHEST_INFO, langConfig.getString("message.click-chest-for-info", "&aClick an account within 15 seconds to retrieve information."));
        messages.put(Message.CLICK_TO_CONFIRM, langConfig.getString("message.click-to-confirm", "&aClick again to confirm."));
        messages.put(Message.CANNOT_BREAK_ACCOUNT, langConfig.getString("message.cannot-break-account", "&cYou can't break an account."));
        messages.put(Message.ACCOUNT_LIMIT_REACHED, langConfig.getString("message.account-limit-reached", "&cYou reached your limit of &6%LIMIT% &caccount/s."));
        messages.put(Message.CREATION_CANCELLED, langConfig.getString("message.creation-cancelled", "&cShop creation has been cancelled."));
        messages.put(Message.UPDATE_AVAILABLE, langConfig.getString("message.update.update-available", "&6&lVersion &c%VERSION% &6of &cShopChest &6is available &chere."));
        messages.put(Message.UPDATE_CLICK_TO_DOWNLOAD, langConfig.getString("message.update.click-to-download", "Click to download"));
        messages.put(Message.UPDATE_NO_UPDATE, langConfig.getString("message.update.no-update", "&6&lNo new update available."));
        messages.put(Message.UPDATE_CHECKING, langConfig.getString("message.update.checking", "&6&lChecking for updates..."));
        messages.put(Message.UPDATE_ERROR, langConfig.getString("message.update.error", "&c&lError while checking for updates."));
        messages.put(Message.NO_PERMISSION_RELOAD, langConfig.getString("message.noPermission.reload", "&cYou don't have permission to reload the accounts."));
        messages.put(Message.NO_PERMISSION_UPDATE, langConfig.getString("message.noPermission.update", "&cYou don't have permission to check for updates."));
        messages.put(Message.NO_PERMISSION_CONFIG, langConfig.getString("message.noPermission.config", "&cYou don't have permission to change configuration values."));
        messages.put(Message.COMMAND_DESC_HEADER, langConfig.getString("message.commandDescription.header", "&6==== &c/%COMMAND% &6Help"));
        messages.put(Message.COMMAND_DESC_FOOTER, langConfig.getString("message.commandDescription.footer", "&6==== End"));
        messages.put(Message.COMMAND_DESC_CREATE, langConfig.getString("message.commandDescription.create", "&a/%COMMAND% create <amount> <buy-price> <sell-price> - Create an account."));
        messages.put(Message.COMMAND_DESC_CREATE_ADMIN, langConfig.getString("message.commandDescription.create-admin", "&a/%COMMAND% create <amount> <buy-price> <sell-price> [normal|admin] - Create an account."));
        messages.put(Message.COMMAND_DESC_REMOVE, langConfig.getString("message.commandDescription.remove", "&a/%COMMAND% remove - Remove an account."));
        messages.put(Message.COMMAND_DESC_INFO, langConfig.getString("message.commandDescription.info", "&a/%COMMAND% info - Retrieve account information."));
        messages.put(Message.COMMAND_DESC_REMOVEALL, langConfig.getString("message.commandDescription.removeall", "&a/%COMMAND% removeall - Remove all accounts of a player."));
        messages.put(Message.COMMAND_DESC_RELOAD, langConfig.getString("message.commandDescription.reload", "&a/%COMMAND% reload - Reload accounts."));
        messages.put(Message.COMMAND_DESC_UPDATE, langConfig.getString("message.commandDescription.update", "&a/%COMMAND% update - Check for Updates."));
        messages.put(Message.COMMAND_DESC_LIMITS, langConfig.getString("message.commandDescription.limits", "&a/%COMMAND% limits - View account limits."));
        messages.put(Message.COMMAND_DESC_OPEN, langConfig.getString("message.commandDescription.open", "&a/%COMMAND% open - Open an account."));
        messages.put(Message.COMMAND_DESC_CONFIG, langConfig.getString("message.commandDescription.config", "&a/%COMMAND% config <set|add|remove> <property> <value> - Change configuration values."));
        messages.put(Message.CHANGED_CONFIG_SET, langConfig.getString("message.config.set", "&6Changed &a%PROPERTY% &6to &a%VALUE%&6."));
        messages.put(Message.CHANGED_CONFIG_REMOVED, langConfig.getString("message.config.removed", "&6Removed &a%VALUE% &6from &a%PROPERTY%&6."));
        messages.put(Message.CHANGED_CONFIG_ADDED, langConfig.getString("message.config.puted", "&6Added &a%VALUE% &6to &a%PROPERTY%&6."));

    }

    /**
     * @param message      Message which should be translated
     * @param replacements Replacements of placeholders which might be required to be replaced in the message
     * @return Localized Message
     */
    public static String getMessage(Message message, Replacement... replacements) {
        String finalMessage = messages.get(message);

        for (Replacement replacement : replacements) {
            Placeholder placeholder = replacement.getPlaceholder();
            String toReplace = replacement.getReplacement();

            if (placeholder.isMoney())
                toReplace = plugin.getEconomy().format(Double.parseDouble(toReplace));

            finalMessage = finalMessage.replace(placeholder.toString(), toReplace);
        }

        return Utils.nonNull(Utils.colorize(finalMessage), () -> ChatColor.RED + "An error occurred: Message not found: " + message.toString());
    }

}

