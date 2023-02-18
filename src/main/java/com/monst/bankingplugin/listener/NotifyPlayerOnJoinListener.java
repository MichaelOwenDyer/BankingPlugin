package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Update;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;

public class NotifyPlayerOnJoinListener implements Listener {

    private final BankingPlugin plugin;

	public NotifyPlayerOnJoinListener(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Update update = plugin.getUpdaterService().getUpdateIfAvailable();
        if (update != null && !update.isCompleted() && Permissions.UPDATE.ownedBy(player))
            player.sendMessage(Message.UPDATE_AVAILABLE
                    .with(Placeholder.VERSION).as(update.getVersion())
                    .translate(plugin));

		Instant lastSeenTime = plugin.getLastSeenService().getLastSeenTime(player);

        // Save the last time the player was notified about changes
        plugin.getLastSeenService().updateLastSeenTime(player);

        if (lastSeenTime == null)
            return;

        plugin.getBankIncomeService().findTotalProfitOrLossByPlayerSince(player, lastSeenTime).then(profitOrLoss -> {
            if (profitOrLoss.signum() != 0) {
                Message message = profitOrLoss.signum() > 0 ? Message.BANK_PROFIT_EARNED_OFFLINE : Message.BANK_LOSS_MADE_OFFLINE;
                player.sendMessage(message
                        .with(Placeholder.AMOUNT).as(plugin.getEconomy().format(profitOrLoss.abs().doubleValue()))
                        .translate(plugin));
            }
        });

        plugin.getAccountInterestService().findTotalInterestEarnedByPlayerSince(player, lastSeenTime).then(interest -> {
            if (interest.signum() > 0)
                player.sendMessage(Message.ACCOUNT_INTEREST_EARNED_OFFLINE
                        .with(Placeholder.AMOUNT).as(plugin.getEconomy().format(interest.doubleValue())).translate(plugin));
        });

        plugin.getAccountInterestService().findTotalLowBalanceFeesPaidByPlayerSince(player, lastSeenTime).then(fees -> {
            if (fees.signum() > 0)
                player.sendMessage(Message.ACCOUNT_LOW_BALANCE_FEES_PAID_OFFLINE
                        .with(Placeholder.AMOUNT).as(plugin.getEconomy().format(fees.doubleValue())).translate(plugin));
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.getLastSeenService().updateLastSeenTime(e.getPlayer());
    }

}
