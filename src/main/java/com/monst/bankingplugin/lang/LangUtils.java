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
            messages.put(message, langConfig.getString(message.getPath(), message.getDefaultMessage()));
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
        for (Replacement replacement : replacements)
            finalMessage = finalMessage.replace(replacement.getPlaceholder().toString(), replacement.getReplacement());
        return Utils.colorize(finalMessage);
    }

}

