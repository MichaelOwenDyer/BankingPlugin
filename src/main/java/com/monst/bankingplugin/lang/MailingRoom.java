package com.monst.bankingplugin.lang;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Ideal for sending messages en masse to many players at once
 */
public class MailingRoom {

    public static Builder draft(String message) {
        return new MailingRoom(message).new Builder();
    }

    private final String message;
    private final Set<CommandSender> recipients;

    private MailingRoom(String message) {
        this.message = Objects.requireNonNull(message);
        this.recipients = new HashSet<>();
    }

    public MailingRoom and(CommandSender sender) {
        if (sender != null)
            recipients.add(sender);
        return this;
    }

    public MailingRoom and(Player player) {
        if (player != null)
            recipients.add(player);
        return this;
    }

    public MailingRoom and(OfflinePlayer player) {
        if (player != null && player.isOnline())
            recipients.add(player.getPlayer());
        return this;
    }

    public MailingRoom and(Collection<OfflinePlayer> players) {
        for (OfflinePlayer player : players)
            if (player != null && player.isOnline())
                recipients.add(player.getPlayer());
        return this;
    }

    public MailingRoom butNotTo(CommandSender sender) {
        if (sender != null)
            recipients.remove(sender);
        return this;
    }

    public MailingRoom butNotTo(Player player) {
        if (player != null)
            recipients.remove(player);
        return this;
    }

    public MailingRoom butNotTo(OfflinePlayer player) {
        if (player != null && player.isOnline())
            recipients.remove(player.getPlayer());
        return this;
    }

    public MailingRoom butNotTo(Collection<OfflinePlayer> players) {
        for (OfflinePlayer player : players)
            if (player != null && player.isOnline())
                recipients.remove(player.getPlayer());
        return this;
    }

    public class Builder {

        public MailingRoom to(CommandSender sender) {
            return and(sender);
        }

        public MailingRoom to(Player player) {
            return and(player);
        }

        public MailingRoom to(OfflinePlayer player) {
            return and(player);
        }

        public MailingRoom to(Collection<OfflinePlayer> players) {
            return and(players);
        }

    }

    public void send() {
        for (CommandSender recipient : recipients)
            recipient.sendMessage(message);
    }

}
