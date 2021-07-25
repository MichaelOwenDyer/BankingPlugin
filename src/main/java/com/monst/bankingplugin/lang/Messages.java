package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public class Messages {

    private static final EnumMap<Message, String> TRANSLATIONS = new EnumMap<>(Message.class);

    public static void setTranslation(Message message, String translation) {
        TRANSLATIONS.put(message, translation);
    }

    /**
     * @param message      Message which should be translated
     * @param replacements Replacements of placeholders which might be required to be replaced in the message
     * @return Localized Message
     */
    public static String translate(Message message, List<Replacement> replacements) {
        String translation = TRANSLATIONS.get(message);
        if (translation == null)
            return ChatColor.RED + "An error occurred: Translation not found: " + message.toString();

        EnumSet<Placeholder> remainingPlaceholders = message.getAvailablePlaceholders();

        for (Replacement replacement : replacements) {
            Placeholder placeholder = replacement.getPlaceholder();
            if (!remainingPlaceholders.remove(placeholder) || !translation.contains(placeholder.toString()))
                continue;
            translation = translation.replace(placeholder.toString(), replacement.getReplacement());
        }

        return Utils.colorize(translation);
    }

    public static String translate(Message message) {
        String translation = TRANSLATIONS.get(message);
        if (translation == null)
            return ChatColor.RED + "An error occurred: Translation not found: " + message.toString();
        return Utils.colorize(translation);
    }

}

