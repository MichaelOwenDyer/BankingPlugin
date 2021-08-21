package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.listeners.InterestEventListener;
import org.bukkit.Bukkit;

import java.time.LocalTime;
import java.util.*;

/**
 * Keeps track of the scheduled interest payout events for each bank on the server.
 * Schedules a new {@link org.bukkit.scheduler.BukkitTask} when necessary and cancels
 * tasks that are no longer being used by any bank.
 */
public class InterestEventScheduler {

    private final BankingPlugin plugin;

    // Map times to the banks that pay interest at that time
    private final Map<LocalTime, Set<Bank>> timeBankMap;

    // Map banks to the times at which they pay interest
    private final Map<Bank, Set<LocalTime>> bankTimeMap;

    // Map times to the BukkitTask IDs, so they can be cancelled later
    private final Map<LocalTime, Integer> payoutTaskIds;

    public InterestEventScheduler(BankingPlugin plugin) {
        this.plugin = plugin;
        this.timeBankMap = new HashMap<>();
        this.bankTimeMap = new HashMap<>();
        this.payoutTaskIds = new HashMap<>();
    }

    /**
     * Schedule the {@link InterestEvent}s of all banks on the server.
     */
    public void scheduleAllInterestEvents() {
        if (!plugin.isEnabled())
            return;
        plugin.debug("Scheduling all interest payments...");
        plugin.getBankRepository().getAll().forEach(this::scheduleInterestEvents);
    }

    /**
     * Synchronizes a bank's (new) interest payout times with the {@link InterestEvent}s scheduled on the server.
     * New repeating tasks will be scheduled for any unique payout times at this bank.
     * Old repeating tasks will be unscheduled if they are not being used by any bank anymore.
     *
     * @see #scheduleRepeatingEventAt(LocalTime)
     * @see InterestEvent
     * @see InterestEventListener
     */
    public void scheduleInterestEvents(Bank bank) {
        if (!plugin.isEnabled() || bank == null)
            return;
        plugin.debugf("Scheduling interest payments of bank #%d.", bank.getID());

        Set<LocalTime> bankPayoutTimes = bank.interestPayoutTimes().get(); // Get times at which bank pays out
        bankTimeMap.putIfAbsent(bank, new HashSet<>()); // Ensure the bank has a time set
        bankTimeMap.get(bank).addAll(bankPayoutTimes); // Add all payout times to the bank's time set

        for (LocalTime time : bankPayoutTimes) {
            if (timeBankMap.putIfAbsent(time, new HashSet<>()) == null) // No other bank has a payout scheduled at this time already
                payoutTaskIds.put(time, scheduleRepeatingEventAt(time)); // Therefore, schedule a new payout task
            else
                plugin.debugf("Bank #%d has scheduled an interest payment at %s, task at this time is already scheduled.", bank.getID(), time.toString());
            timeBankMap.get(time).add(bank); // Add the bank to this time's bank set
        }

        Set<LocalTime> removedTimes = new HashSet<>(); // Times at which the bank no longer has a payout
        for (LocalTime time : bankTimeMap.get(bank))
            if (!bankPayoutTimes.contains(time)) // The bank previously had a payout at this time, but no longer
                removedTimes.add(time); // Add it to be removed
        bankTimeMap.get(bank).removeAll(removedTimes); // Remove these times from that bank's time set

        Set<LocalTime> emptyTimes = new HashSet<>(); // Times at which no bank has a payout anymore
        for (LocalTime time : removedTimes) {
            plugin.debugf("Bank #%d no longer has an interest payment scheduled at %s.", bank.getID(), time.toString());
            Set<Bank> set = timeBankMap.get(time); // See which banks are scheduled at this time
            set.remove(bank); // This bank no longer pays out at this time; remove it from the set
            if (set.isEmpty()) // Bank set is empty; time is no longer used by any bank
                emptyTimes.add(time); // Add it to be unscheduled and the entry removed
        }

        emptyTimes.forEach(timeBankMap::remove); // Remove entries for empty times
        if (plugin.isEnabled())
            emptyTimes.forEach(this::unscheduleRepeatingEventAt); // Unschedule empty times' payout tasks

    }

    /**
     * Removes all interest payments associated with the specified bank.
     * Times which are no longer used by any bank will be unscheduled.
     *
     * @param bank bank to remove interest payments from
     */
    public void unscheduleAllInterestEvents(Bank bank) {
        if (bank == null)
            return;
        plugin.debugf("Unscheduling interest payments of bank #%d.", bank.getID());

        bankTimeMap.remove(bank);
        Set<LocalTime> emptyTimes = new HashSet<>();
        timeBankMap.forEach((time, set) -> {
            set.remove(bank);
            if (set.isEmpty())
                emptyTimes.add(time);
        });
        emptyTimes.forEach(timeBankMap::remove);
        if (plugin.isEnabled())
            emptyTimes.forEach(this::unscheduleRepeatingEventAt);
    }

    public void unscheduleAllInterestEvents() {
        payoutTaskIds.values().forEach(Bukkit.getScheduler()::cancelTask);
        payoutTaskIds.clear();
        timeBankMap.clear();
        bankTimeMap.clear();
    }

    /**
     * Performs the necessary arithmetic to schedule an {@link InterestEvent}
     * as a {@link org.bukkit.scheduler.BukkitTask} repeating every 24 hours at the specified time.
     *
     * @param time the time to be scheduled
     * @return the ID of the scheduled task, or -1 if the task was not scheduled
     */
    private int scheduleRepeatingEventAt(LocalTime time) {
        // 24 hours/day * 60 minutes/hour * 60 seconds/minute *  20 ticks/second = 1728000 ticks/day
        final long ticksInADay = 1728000L;

        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTimeInMillis(); // Get current time in milliseconds

        if (LocalTime.now().isAfter(time)) // Time has passed; next instance will be tomorrow
            cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.set(Calendar.SECOND, time.getSecond());
        cal.set(Calendar.MILLISECOND, 0);

        long offset = cal.getTimeInMillis() - currentTime; // Get number of milliseconds between now and next instance
        long ticks = offset / 50L; // Divide by 50 to get number of ticks (assuming 20 ticks per second) until next instance

        // Schedule task starting at next instance, repeating daily, where an InterestEvent is fired with the scheduled banks
        int id = Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(plugin, () -> new InterestEvent(Bukkit.getConsoleSender(), getScheduledBanks(time)).fire(), ticks, ticksInADay);
        plugin.debugf((id != -1 ? "Scheduled" : "Failed to schedule") + " interest payment at %s.", time);
        return id;
    }

    /**
     * Removes a repeating task which is no longer being used by any bank.
     * @param time the time to unschedule
     */
    private void unscheduleRepeatingEventAt(LocalTime time) {
        if (!payoutTaskIds.containsKey(time))
            return;
        Bukkit.getScheduler().cancelTask(payoutTaskIds.get(time));
        payoutTaskIds.remove(time);
        plugin.debugf("Unscheduled interest payment at %s.", time.toString());
    }

    /**
     * Gets all banks with interest payments scheduled at the specified time.
     * @param time the specified time
     * @return a {@link Set} of banks.
     */
    private Set<Bank> getScheduledBanks(LocalTime time) {
        return Optional.ofNullable(timeBankMap.get(time)).orElse(Collections.emptySet());
    }

}
