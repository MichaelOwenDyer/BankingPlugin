package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
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

    private static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

    // Maps times to the banks that pay interest at that time
    private static final Map<LocalTime, Set<Bank>> TIME_BANK_MAP = new HashMap<>();

    // Maps banks to the times at which they pay interest
    private static final Map<Bank, Set<LocalTime>> BANK_TIME_MAP = new HashMap<>();

    // Maps times to the BukkitTask IDs so they can be cancelled later
    private static final Map<LocalTime, Integer> PAYOUT_TASK_IDS = new HashMap<>();

    /**
     * Schedule the {@link InterestEvent}s of all banks on the server.
     */
    public static void scheduleAll() {
        if (!PLUGIN.isEnabled())
            return;
        PLUGIN.debug("Scheduling all interest payments...");
        PLUGIN.getBankRepository().getAll().forEach(InterestEventScheduler::scheduleAll);
    }

    /**
     * Synchronizes a bank's (new) interest payout times with the {@link InterestEvent}s scheduled on the server.
     * New repeating tasks will be scheduled for any unique payout times at this bank.
     * Old repeating tasks will be unscheduled if they are not being used by any bank anymore.
     *
     * @see #scheduleRepeatingPayment(LocalTime)
     * @see InterestEvent
     * @see InterestEventListener
     */
    public static void scheduleAll(Bank bank) {
        if (!PLUGIN.isEnabled() || bank == null)
            return;
        PLUGIN.debugf("Scheduling interest payments of bank #%d.", bank.getID());

        Set<LocalTime> bankPayoutTimes = bank.getInterestPayoutTimes().get(); // Get times at which bank pays out
        BANK_TIME_MAP.putIfAbsent(bank, new HashSet<>()); // Ensure the bank has a time set
        BANK_TIME_MAP.get(bank).addAll(bankPayoutTimes); // Add all payout times to the bank's time set

        for (LocalTime time : bankPayoutTimes) {
            if (TIME_BANK_MAP.putIfAbsent(time, new HashSet<>()) == null) // No other bank has a payout scheduled at this time already
                PAYOUT_TASK_IDS.put(time, scheduleRepeatingPayment(time)); // Therefore, schedule a new payout task
            else
                PLUGIN.debugf("Bank #%d has scheduled an interest payment at %s, task at this time is already scheduled.", bank.getID(), time.toString());
            TIME_BANK_MAP.get(time).add(bank); // Add the bank to this time's bank set
        }

        Set<LocalTime> removedTimes = new HashSet<>(); // Times at which the bank no longer has a payout
        for (LocalTime time : BANK_TIME_MAP.get(bank))
            if (!bankPayoutTimes.contains(time)) // The bank previously had a payout at this time, but no longer
                removedTimes.add(time); // Add it to be removed
        BANK_TIME_MAP.get(bank).removeAll(removedTimes); // Remove these times from that bank's time set

        Set<LocalTime> emptyTimes = new HashSet<>(); // Times at which no bank has a payout anymore
        for (LocalTime time : removedTimes) {
            PLUGIN.debugf("Bank #%d no longer has an interest payment scheduled at %s.", bank.getID(), time.toString());
            Set<Bank> set = TIME_BANK_MAP.get(time); // See which banks are scheduled at this time
            set.remove(bank); // This bank no longer pays out at this time; remove it from the set
            if (set.isEmpty()) // Bank set is empty; time is no longer used by any bank
                emptyTimes.add(time); // Add it to be unscheduled and the entry removed
        }

        emptyTimes.forEach(TIME_BANK_MAP::remove); // Remove entries for empty times
        if (PLUGIN.isEnabled())
            emptyTimes.forEach(InterestEventScheduler::unscheduleRepeatingPayment); // Unschedule empty times' payout tasks

    }

    /**
     * Removes all interest payments associated with the specified bank.
     * Times which are not used by any bank anymore will be unscheduled.
     *
     * @param bank bank to remove interest payments from
     */
    public static void unscheduleAll(Bank bank) {
        if (bank == null)
            return;
        PLUGIN.debugf("Unscheduling interest payments of bank #%d.", bank.getID());

        BANK_TIME_MAP.remove(bank);
        Set<LocalTime> emptyTimes = new HashSet<>();
        TIME_BANK_MAP.forEach((time, set) -> {
            set.remove(bank);
            if (set.isEmpty())
                emptyTimes.add(time);
        });
        emptyTimes.forEach(TIME_BANK_MAP::remove);
        if (PLUGIN.isEnabled())
            emptyTimes.forEach(InterestEventScheduler::unscheduleRepeatingPayment);
    }

    /**
     * Performs the necessary arithmetic to schedule an {@link InterestEvent}
     * as a {@link org.bukkit.scheduler.BukkitTask} repeating every 24 hours at the specified time.
     *
     * @param time the time to be scheduled
     * @return the ID of the scheduled task, or -1 if the task was not scheduled
     */
    private static int scheduleRepeatingPayment(LocalTime time) {
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
                .scheduleSyncRepeatingTask(PLUGIN, new InterestEvent(getScheduledBanks(time))::fire, ticks, ticksInADay);
        PLUGIN.debugf((id != -1 ? "Scheduled" : "Failed to schedule") + " interest payment at %s.", time.toString());
        return id;
    }

    /**
     * Unschedules a repeating task which is no longer being used by any bank.
     * @param time the time to unschedule
     */
    private static void unscheduleRepeatingPayment(LocalTime time) {
        if (!PAYOUT_TASK_IDS.containsKey(time))
            return;
        Bukkit.getScheduler().cancelTask(PAYOUT_TASK_IDS.get(time));
        PAYOUT_TASK_IDS.remove(time);
        PLUGIN.debugf("Unscheduled interest payment at %s.", time.toString());
    }

    /**
     * Gets all banks with interest payments scheduled at the specified time.
     * @param time the specified time
     * @return a {@link Set} of banks.
     */
    private static Set<Bank> getScheduledBanks(LocalTime time) {
        return Optional.ofNullable(TIME_BANK_MAP.get(time)).orElse(Collections.emptySet());
    }

}
