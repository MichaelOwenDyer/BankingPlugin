package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public interface Confirmable {
	
	Map<UUID, String[]> unconfirmedCommands = new HashMap<>();
	Map<UUID, BukkitTask> confirmationTimers = new HashMap<>();
	
	default boolean needsConfirmation(Player p, String[] args) {
		if (unconfirmedCommands.containsKey(p.getUniqueId())
				&& Arrays.equals(args, unconfirmedCommands.get(p.getUniqueId()))) {
			UUID uuid = p.getUniqueId();
			unconfirmedCommands.remove(uuid);
			Optional.ofNullable(confirmationTimers.get(uuid)).ifPresent(BukkitTask::cancel);
			confirmationTimers.remove(uuid);
			return false;
		} else {
			UUID uuid = p.getUniqueId();
			unconfirmedCommands.put(uuid, args);
			Optional.ofNullable(confirmationTimers.get(uuid)).ifPresent(BukkitTask::cancel);
			confirmationTimers.put(uuid, new BukkitRunnable() {
				@Override
				public void run() {
					unconfirmedCommands.remove(uuid);
				}
			}.runTaskLater(BankingPlugin.getInstance(), 300));
			return true;
		}
	}
}
