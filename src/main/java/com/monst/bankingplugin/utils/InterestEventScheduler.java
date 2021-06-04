package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
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

    public InterestEventScheduler(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    // Maps times to the banks that pay interest at that time
    private final Map<LocalTime, Set<Bank>> TIME_BANK_MAP = new HashMap<>();

    // Maps banks to the times at which they pay interest
    private final Map<Bank, Set<LocalTime>> BANK_TIME_MAP = new HashMap<>();

    // Maps times to the BukkitTask IDs so they can be cancelled later
    private final Map<LocalTime, Integer> PAYOUT_TASK_IDS = new HashMap<>();

    public Set<Bank> getScheduledBanks(LocalTime time) {
        return Optional.ofNullable(TIME_BANK_MAP.get(time)).orElse(Collections.emptySet());
    }

    public void scheduleAll() {
        if (plugin.isEnabled())
            plugin.getBankRepository().getAll().forEach(this::schedulePayouts);
    }

    public void unschedulePayouts(Bank bank) {
        if (bank == null)
            return;
        if (BANK_TIME_MAP.containsKey(bank))
            BANK_TIME_MAP.get(bank).forEach(this::descheduleTime);
        BANK_TIME_MAP.remove(bank);
        TIME_BANK_MAP.forEach((time, set) -> set.removeIf(bank::equals));
    }

    /**
     * Create Bukkit tasks to trigger interest events at the times specified in the {@link Config}
     *
     * @see #scheduleRepeatAtTime(LocalTime)
     * @see InterestEvent
     * @see InterestEventListener
     */
    public void schedulePayouts(Bank bank) {
        if (!plugin.isEnabled() || bank == null)
            return;

        BANK_TIME_MAP.putIfAbsent(bank, new HashSet<>());

        Set<LocalTime> bankPayoutTimes = bank.getInterestPayoutTimes().get();
        for (LocalTime time : bankPayoutTimes) {
            if (TIME_BANK_MAP.putIfAbsent(time, new HashSet<>()) == null) // If no other bank already has a payout scheduled at this time
                PAYOUT_TASK_IDS.put(time, scheduleRepeatAtTime(time)); // Schedule the payout task
            TIME_BANK_MAP.get(time).add(bank);
            BANK_TIME_MAP.get(bank).add(time);
        }
        for (Iterator<LocalTime> iterator = BANK_TIME_MAP.get(bank).iterator(); iterator.hasNext();) {
            LocalTime time = iterator.next();
            if (bankPayoutTimes.contains(time))
                continue;
            iterator.remove();
            TIME_BANK_MAP.get(time).remove(bank);
            if (TIME_BANK_MAP.get(time).isEmpty()) { // If no more banks have payouts scheduled at this time
                descheduleTime(time); // Remove the payout task
                TIME_BANK_MAP.remove(time);
            }
        }
    }

    /**
     * Perform the necessary arithmetic to schedule a {@link LocalTime} from the {@link Config}
     * as a {@link org.bukkit.scheduler.BukkitTask} repeating every 24 hours.
     *
     * @param time the time to be scheduled
     * @return the ID of the scheduled task, or -1 if the task was not scheduled
     */
    private int scheduleRepeatAtTime(LocalTime time) {
        // 24 hours/day * 60 minutes/hour * 60 seconds/minute *  20 ticks/second = 1728000 ticks/day
        final long ticksInADay = 1728000L;

        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTimeInMillis();

        if (LocalTime.now().isAfter(time))
            cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.set(Calendar.SECOND, time.getSecond());
        cal.set(Calendar.MILLISECOND, 0);

        long offset = cal.getTimeInMillis() - currentTime;
        long ticks = offset / 50L;

        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                () -> Bukkit.getServer().getPluginManager().callEvent(
                        new InterestEvent(plugin, getScheduledBanks(time))), ticks, ticksInADay);
        plugin.debug((id != -1 ? "Scheduled " : "Failed to schedule ") + "daily interest payout at " + time);
        return id;
    }

    private void descheduleTime(LocalTime time) {
        if (!PAYOUT_TASK_IDS.containsKey(time))
            return;
        Bukkit.getScheduler().cancelTask(PAYOUT_TASK_IDS.get(time));
        PAYOUT_TASK_IDS.remove(time);
    }

}
