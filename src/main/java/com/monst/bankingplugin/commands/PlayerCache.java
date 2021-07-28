package com.monst.bankingplugin.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class PlayerCache {

	static final Cache<UUID, Object> PLAYER_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(15, TimeUnit.SECONDS)
			.build();

	private PlayerCache() {

	}

	/**
	 * Clear all click types, cancel timers
	 */
	public static void clear() {
		PLAYER_CACHE.invalidateAll();
	}

	public static boolean put(Player sender, Object object) {
		if (PLAYER_CACHE.asMap().remove(sender.getUniqueId(), object))
			return true;
		PLAYER_CACHE.put(sender.getUniqueId(), object);
		return false;
	}

}
