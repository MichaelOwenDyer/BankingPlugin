package com.monst.bankingplugin.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class CommandCache {

	private static final Cache<UUID, Integer> PLAYER_COMMAND_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(15, TimeUnit.SECONDS)
			.build();

	private CommandCache() {}

	public static void clear() {
		PLAYER_COMMAND_CACHE.invalidateAll();
	}

	public static boolean isFirstUsage(Player sender, int commandHash) {
		if (PLAYER_COMMAND_CACHE.asMap().remove(sender.getUniqueId(), commandHash))
			return false;
		PLAYER_COMMAND_CACHE.put(sender.getUniqueId(), commandHash);
		return true;
	}

}
