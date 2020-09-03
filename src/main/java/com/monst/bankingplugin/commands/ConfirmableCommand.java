package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Confirmable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public interface ConfirmableCommand extends Confirmable<String[]> {
	
	Map<UUID, String[]> unconfirmedCommands = new HashMap<>();
	Map<UUID, BukkitTask> confirmationTimers = new HashMap<>();

	@Override
	default boolean hasEntry(Player p, String[] args) {
		return unconfirmedCommands.containsKey(p.getUniqueId())
				&& Arrays.equals(args, unconfirmedCommands.get(p.getUniqueId()));
	}

	@Override
	default void putEntry(Player p, String[] args) {
		UUID uuid = p.getUniqueId();
		unconfirmedCommands.put(uuid, args);
		Optional.ofNullable(confirmationTimers.get(uuid)).ifPresent(BukkitTask::cancel);
		confirmationTimers.put(uuid, new BukkitRunnable() {
			@Override
			public void run() {
				unconfirmedCommands.remove(uuid);
			}
		}.runTaskLater(BankingPlugin.getInstance(), 300));
	}

	@Override
	default void removeEntry(Player p, String[] args) {
		UUID uuid = p.getUniqueId();
		unconfirmedCommands.remove(uuid);
		Optional.ofNullable(confirmationTimers.get(uuid)).ifPresent(BukkitTask::cancel);
		confirmationTimers.remove(uuid);
	}
	
}
