package com.monst.bankingplugin.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Messages;

interface SchedulableCommand {
	
	static Map<UUID, BukkitTask> playerTimers = new HashMap<>();
	static Map<UUID, String[]> unconfirmed = new HashMap<>();
	static Map<UUID, BukkitTask> scheduled = new HashMap<>();

	void scheduleCommand(Player p, Object request, String[] args, int ticks);
	
	boolean commandConfirmed(Player p, Object request, String[] args);

	default void addUnconfirmedCommand(Player p, String[] args) {
		UUID uuid = p.getUniqueId();
		unconfirmed.put(uuid, args);
		Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());
		playerTimers.put(uuid, new BukkitRunnable() {
			@Override
			public void run() {
				unconfirmed.remove(uuid);
			}
		}.runTaskLater(BankingPlugin.getInstance(), 200));
	}

	default void removeUnconfirmedCommand(OfflinePlayer p) {
		UUID uuid = p.getUniqueId();
		unconfirmed.remove(uuid);
		Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());
		playerTimers.remove(uuid);
	}

	default void unscheduleCommand(Player p) {
		UUID uuid = p.getUniqueId();
		scheduled.remove(uuid);
		Optional.ofNullable(scheduled.get(uuid)).ifPresentOrElse(task -> {
			task.cancel();
			p.sendMessage(Messages.SCHEDULED_COMMAND_CANCELLED);
		}, () -> p.sendMessage(Messages.SCHEDULED_COMMAND_NOT_EXIST));
	}
}
