package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.LanguageConfig;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;

import java.util.EnumMap;
import java.util.EnumSet;

public class LangUtils {

    private static final BankingPlugin plugin = BankingPlugin.getInstance();
    private static final EnumMap<Message, String> messages = new EnumMap<>(Message.class);

    public static void reload() {
        LanguageConfig langConfig = plugin.getPluginConfig().getLanguageConfig();
        for (Message message : Message.values())
            messages.put(message, langConfig.getString(message));
    }

    /**
     * @param message      Message which should be translated
     * @param replacements Replacements of placeholders which might be required to be replaced in the message
     * @return Localized Message
     */
    public static String getMessage(Message message, Replacement... replacements) {
        String finalMessage = messages.get(message);
        if (finalMessage == null)
            return ChatColor.RED + "An error occurred: Message not found: " + message.toString();

        EnumSet<Placeholder> allowedPlaceholders = message.getAvailablePlaceholders().clone();

        for (Replacement replacement : replacements) {
            Placeholder placeholder = replacement.getPlaceholder();
            if (!allowedPlaceholders.contains(placeholder) || !finalMessage.contains(placeholder.toString()))
                continue;
            finalMessage = finalMessage.replace(placeholder.toString(), replacement.getReplacement());
            allowedPlaceholders.remove(placeholder); // Should be empty by end of loop
        }

        for (Placeholder placeholder : allowedPlaceholders)
            plugin.debugf("Placeholder missing from message call! Message: %s, Placeholder: %s", message.toString(), placeholder.toString());

        return Utils.colorize(finalMessage);
    }

}

