package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
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

		database.getLastLogout(p, Callback.of(plugin, logoutTime -> {
		    if (logoutTime < 0) {
                // No logout saved, probably first time joining.
                return;
            }

            database.getTotalBankProfitSinceLogout(p, logoutTime, Callback.of(plugin, profit -> {
                if (profit.signum() == 0)
                    return;
                p.sendMessage(LangUtils.getMessage(profit.signum() > 0 ? Message.BANK_REVENUE_EARNED_OFFLINE : Message.BANK_LOSS_OFFLINE,
                        new Replacement(Placeholder.AMOUNT, profit)
                ));
            }));

            database.getTotalAccountInterestSinceLogout(p, logoutTime, Callback.of(plugin, interest -> {
                if (interest.signum() > 0)
                    p.sendMessage(LangUtils.getMessage(Message.OFFLINE_ACCOUNT_INTEREST,
                            new Replacement(Placeholder.AMOUNT, interest)
                    ));
            }));

            database.getTotalLowBalanceFeesPaidSinceLogout(p, logoutTime, Callback.of(plugin, fees -> {
                if (fees.signum() > 0)
                    p.sendMessage(LangUtils.getMessage(Message.OFFLINE_LOW_BALANCE_FEES_PAID,
                            new Replacement(Placeholder.AMOUNT, fees)
                    ));
            }));

            // Player does not actually log off here, this saves the last time the player was notified about changes
            database.logLastSeen(p, null);
		}));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.getDatabase().logLastSeen(e.getPlayer(), null);
    }

}
