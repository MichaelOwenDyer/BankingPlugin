package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.control.InterestEvent;
import com.monst.bankingplugin.exception.CancelledException;
import org.bukkit.Bukkit;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulerService {

    private final BankingPlugin plugin;
    private final Set<LocalTime> currentPayoutTimes;
    private final Map<LocalTime, Integer> taskIds;

    public SchedulerService(BankingPlugin plugin) {
        this.plugin = plugin;
        this.currentPayoutTimes = new HashSet<>();
        this.taskIds = new HashMap<>();
    }

    private Set<LocalTime> queryAllDistinctTimes() {
        return plugin.getBankService().findAll().stream()
                .map(plugin.config().interestPayoutTimes::at)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public void scheduleAll() {
        plugin.debug("Scheduling all interest payments...");
        Set<LocalTime> newTimes = queryAllDistinctTimes();
        for (Iterator<LocalTime> iterator = currentPayoutTimes.iterator(); iterator.hasNext();) {
            LocalTime time = iterator.next();
            if (!newTimes.contains(time)) {
                unschedule(time);
                iterator.remove();
            }
        }
        for (LocalTime newTime : newTimes)
            if (currentPayoutTimes.add(newTime))
                taskIds.put(newTime, schedule(newTime));
    }

    private int schedule(LocalTime time) {
        ZonedDateTime nextOccurrence = ZonedDateTime.of(LocalDate.now(), time, ZoneId.systemDefault());
        if (LocalTime.now().isAfter(time)) // Time has passed; next instance will be tomorrow
            nextOccurrence = nextOccurrence.plusDays(1);

        Duration until = Duration.between(Instant.now(), nextOccurrence);
        long ticks = until.getSeconds() * 20; // Get number of ticks between now and next instance

        plugin.debugf("Scheduling interest payment at %s, %d minutes from now", time, until.toMinutes());
        // Schedule task starting at next instance, repeating daily, where an InterestEvent is fired with the scheduled banks
        // 24 hours/day * 60 minutes/hour * 60 seconds/minute * 20 ticks/second = 1728000 ticks/day
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> throwEvent(time), ticks, 1728000L);
    }

    private void unschedule(LocalTime time) {
        plugin.debugf("Unscheduling interest payment at %s...", time);
        Bukkit.getScheduler().cancelTask(taskIds.get(time));
        taskIds.remove(time);
    }

    public void unscheduleAll() {
        plugin.debug("Unscheduling all interest payments...");
        taskIds.values().forEach(Bukkit.getScheduler()::cancelTask);
        taskIds.clear();
    }

    private void throwEvent(LocalTime time) {
        plugin.debugf("Triggering scheduled InterestEvent at %s...", time);
        try {
            new InterestEvent(Bukkit.getConsoleSender(), findBanksAtTime(time)).fire();
        } catch (CancelledException e) {
            plugin.debug("InterestEvent cancelled");
        }
    }

    private Set<Bank> findBanksAtTime(LocalTime time) {
        return plugin.getBankService().findAll().stream()
                .filter(bank -> plugin.config().interestPayoutTimes.at(bank).contains(time))
                .collect(Collectors.toSet());
    }

}
