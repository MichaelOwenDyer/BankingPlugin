package com.monst.bankingplugin.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Converter
public class OfflinePlayerConverter implements AttributeConverter<OfflinePlayer, String> {
    @Override
    public String convertToDatabaseColumn(OfflinePlayer player) {
        if (player == null)
            return null;
        return player.getUniqueId().toString();
    }

    @Override
    public OfflinePlayer convertToEntityAttribute(String uuid) {
        if (uuid == null)
            return null;
        return Bukkit.getOfflinePlayer(UUID.fromString(uuid));
    }
}
