package com.monst.bankingplugin.utils;

import com.earth2me.essentials.User;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Messenger {

    private static final BankingPlugin plugin = BankingPlugin.getInstance();

    private String message;
    private final Set<CommandSender> recipients;
    private final Set<OfflinePlayer> offlineRecipients;

    public Messenger(String message) {
        this.message = message;
        this.recipients = new HashSet<>();
        this.offlineRecipients = new HashSet<>();
    }

    public void send() {
        send(false);
    }

    public void send(boolean mailIfOffline) {
        if (message == null) {
            plugin.debug(new NullPointerException("Message undefined!"));
            return;
        }
        recipients.forEach(p -> p.sendMessage(message));
        offlineRecipients.forEach(p -> notify(p, message, mailIfOffline));
    }

    public static void notify(OfflinePlayer player, String message) {
        notify(player, message, false);
    }

    public static void notify(OfflinePlayer player, String message, boolean mailIfOffline) {
        if (player.isOnline())
            message(player.getPlayer(), message);
        else if (mailIfOffline)
            mail(player, message);
    }

    public static void message(CommandSender sender, String message) {
        if (sender != null && message != null)
            sender.sendMessage(message);
    }

    public static void mail(OfflinePlayer player, String message) {
        if (!Config.enableMail || player == null || message == null)
            return;
        User user = plugin.getEssentials().getUserMap().getUser(player.getUniqueId());
        if (user != null)
            user.addMail(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void addRecipient(CommandSender sender) {
        if (sender != null)
            recipients.add(sender);
    }

    public void addRecipient(Collection<CommandSender> senders) {
        senders.forEach(this::addRecipient);
    }

    public void addOfflineRecipient(OfflinePlayer player) {
        if (player == null)
            return;
        if (player.isOnline())
            addRecipient(player.getPlayer());
        else
            offlineRecipients.add(player);
    }

    public void addOfflineRecipient(Collection<OfflinePlayer> players) {
        players.forEach(this::addOfflineRecipient);
    }

    public void removeRecipient(CommandSender sender) {
        if (sender != null)
            recipients.remove(sender);
    }

    public void removeOfflineRecipient(OfflinePlayer player) {
        if (player == null)
            return;
        offlineRecipients.remove(player);
        if (player.isOnline())
            removeRecipient(player.getPlayer());
    }

}
