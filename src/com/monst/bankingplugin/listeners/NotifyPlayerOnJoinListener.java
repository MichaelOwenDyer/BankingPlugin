package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Utils;

public class NotifyPlayerOnJoinListener implements Listener {

	private BankingPlugin plugin;

	public NotifyPlayerOnJoinListener(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

		plugin.getDatabase().getLastLogout(p, new Callback<Long>(plugin) {
            @Override
            public void onResult(Long result) {
                if (result < 0) {
                    // No logout saved, probably first time joining.
                    return;
                }

				plugin.getDatabase().getOfflineTransactionRevenue(p, result, new Callback<BigDecimal>(plugin) {
					@Override
					public void onResult(BigDecimal result) {
						if (result.signum() == 1) {
							p.sendMessage(String.format(Messages.OFFLINE_TRANSACTION_REVENUE, Utils.formatNumber(result)));
						} else if (result.signum() == -1) {
							p.sendMessage(String.format(Messages.OFFLINE_TRANSACTION_EXPENDITURE, Utils.formatNumber(result)));
						}
					}
				});

				plugin.getDatabase().getOfflineInterestRevenue(p, result, new Callback<BigDecimal>(plugin) {
                    @Override
                    public void onResult(BigDecimal result) {
                        if (result.signum() == 1) {
							p.sendMessage(String.format(Messages.OFFLINE_INTEREST_REVENUE, Utils.formatNumber(result)));
                        }
                    }
                });

				// Player does not actually log off here
				plugin.getDatabase().logLogout(p, null);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.getDatabase().logLogout(e.getPlayer(), null);
    }

}
