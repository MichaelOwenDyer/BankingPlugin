package com.monst.bankingplugin.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Ownable;

interface Confirmable<T extends Ownable> {
	
	Map<UUID, String[]> unconfirmedCommands = new HashMap<>();
	Map<UUID, BukkitTask> confirmationTimers = new HashMap<>();
	
	default boolean commandConfirmed(Player p, String[] args) {
		if (unconfirmedCommands.containsKey(p.getUniqueId()) && Arrays.equals(unconfirmedCommands.get(p.getUniqueId()), args)) {
			UUID uuid = p.getUniqueId();
			unconfirmedCommands.remove(uuid);
			Optional.ofNullable(confirmationTimers.get(uuid)).ifPresent(task -> task.cancel());
			confirmationTimers.remove(uuid);
			return true;
		} else {
			UUID uuid = p.getUniqueId();
			unconfirmedCommands.put(uuid, args);
			Optional.ofNullable(confirmationTimers.get(uuid)).ifPresent(task -> task.cancel());
			confirmationTimers.put(uuid, new BukkitRunnable() {
				@Override
				public void run() {
					unconfirmedCommands.remove(uuid);
				}
			}.runTaskLater(BankingPlugin.getInstance(), 300));
			return false;
		}
	}

}
