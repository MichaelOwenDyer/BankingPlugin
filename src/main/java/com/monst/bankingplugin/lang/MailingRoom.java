package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Ideal for sending messages en masse to both online and offline players at once
 */
public class MailingRoom {

    private String message;
    private final Set<CommandSender> recipients;
    private final Set<OfflinePlayer> offlineRecipients;

    public MailingRoom(String message) {
        this.message = message;
        this.recipients = new HashSet<>();
        this.offlineRecipients = new HashSet<>();
    }

    public void send() {
        if (message == null)
            return;
        recipients.forEach(p -> p.sendMessage(message));
        offlineRecipients.forEach(p -> Utils.notify(p, message));
    }

    public void newMessage(String message) {
        this.message = message;
    }

    public void addRecipient(CommandSender sender) {
        if (sender != null)
            recipients.add(sender);
    }

    public void addRecipients(Collection<CommandSender> senders) {
        senders.forEach(this::addRecipient);
    }

    public void addOfflineRecipient(OfflinePlayer player) {
        if (player == null)
            return;
        if (player.isOnline())
            recipients.add(player.getPlayer());
        else
            offlineRecipients.add(player);
    }

    public void addOfflineRecipients(Collection<OfflinePlayer> players) {
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
