package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.sql.Database;
import com.monst.bankingplugin.utils.Callback;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NotifyPlayerOnJoinListener extends BankingPluginListener {

	public NotifyPlayerOnJoinListener(BankingPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        Database database = plugin.getDatabase();

		database.getLastLogout(p, Callback.of(logoutTime -> {
		    if (logoutTime < 0) {
                // No logout saved, probably first time joining.
                return;
            }

            database.getBankProfitEarnedByPlayerSince(p, logoutTime, Callback.of(profit -> {
                if (profit.signum() == 0)
                    return;
                Message message = profit.signum() > 0 ? Message.BANK_PROFIT_OFFLINE : Message.BANK_LOSS_OFFLINE;
                p.sendMessage(message.with(Placeholder.AMOUNT).as(profit.abs()).translate());
            }));

            database.getInterestEarnedByPlayerSince(p, logoutTime, Callback.of(interest -> {
                if (interest.signum() > 0)
                    p.sendMessage(Message.OFFLINE_ACCOUNT_INTEREST.with(Placeholder.AMOUNT).as(interest).translate());
            }));

            database.getLowBalanceFeesPaidByPlayerSince(p, logoutTime, Callback.of(fees -> {
                if (fees.signum() > 0)
                    p.sendMessage(Message.OFFLINE_LOW_BALANCE_FEES_PAID.with(Placeholder.AMOUNT).as(fees).translate());
            }));

		}));
        // Player does not actually log off here, this saves the last time the player was notified about changes
        database.logLastSeen(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.getDatabase().logLastSeen(e.getPlayer());
    }

}
