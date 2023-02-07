package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.control.InterestEvent;
import com.monst.bankingplugin.exception.EventCancelledException;
import org.bukkit.Bukkit;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulerService {

    private final BankingPlugin plugin;
    private final Map<LocalTime, Integer> payoutTimeTaskIDs;

    public SchedulerService(BankingPlugin plugin) {
        this.plugin = plugin;
        this.payoutTimeTaskIDs = new HashMap<>();
        scheduleAll();
    }

    private Set<LocalTime> queryAllDistinctTimes() {
        return plugin.getBankService().findAll().stream()
                .map(plugin.config().interestPayoutTimes::at)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public void scheduleAll() {
        plugin.debug("Scheduling all interest payments...");
        Set<LocalTime> currentDistinctTimes = queryAllDistinctTimes();
        for (Iterator<LocalTime> iterator = payoutTimeTaskIDs.keySet().iterator(); iterator.hasNext();) {
            LocalTime time = iterator.next();
            if (!currentDistinctTimes.contains(time)) {
                unschedule(time);
                iterator.remove();
            }
        }
        for (LocalTime time : currentDistinctTimes)
            payoutTimeTaskIDs.computeIfAbsent(time, this::schedule);
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
        Integer taskID = payoutTimeTaskIDs.remove(time);
        if (taskID != null)
            Bukkit.getScheduler().cancelTask(taskID);
    }

    public void unscheduleAll() {
        plugin.debug("Unscheduling all interest payments...");
        payoutTimeTaskIDs.values().forEach(Bukkit.getScheduler()::cancelTask);
        payoutTimeTaskIDs.clear();
    }

    private void throwEvent(LocalTime time) {
        plugin.debugf("Triggering scheduled InterestEvent at %s...", time);
        try {
            new InterestEvent(Bukkit.getConsoleSender(), findBanksAtTime(time)).fire();
        } catch (EventCancelledException e) {
            plugin.debug("InterestEvent cancelled");
        }
    }

    private Set<Bank> findBanksAtTime(LocalTime time) {
        return plugin.getBankService().findAll().stream()
                .filter(bank -> plugin.config().interestPayoutTimes.at(bank).contains(time))
                .collect(Collectors.toSet());
    }

}
