package com.monst.bankingplugin.lang;

import com.earth2me.essentials.User;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import org.bukkit.OfflinePlayer;

/**
 * A set of static methods designed to easily send messages to players whether or not they are online.
 */
public abstract class Mailman {

    static final BankingPlugin plugin = BankingPlugin.getInstance();

    public static void notify(OfflinePlayer player, String message) {
        notify(player, message, false);
    }

    public static void notify(OfflinePlayer player, String message, boolean mailIfOffline) {
        if (player == null || message == null)
            return;
        if (player.isOnline())
            player.getPlayer().sendMessage(message);
        else if (mailIfOffline)
            mail(player, message);
    }

    public static void mail(OfflinePlayer player, String message) {
        if (!Config.enableMail || player == null || message == null)
            return;
        User user = plugin.getEssentials().getUserMap().getUser(player.getUniqueId());
        if (user != null)
            user.addMail(message);
    }
}
