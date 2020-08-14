package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class NotifyPlayerOnJoinListener implements Listener {

	private final BankingPlugin plugin;

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
							p.sendMessage(String.format(Messages.OFFLINE_BALANCE_INCREASED, Utils.format(result)));
						} else if (result.signum() == -1) {
							p.sendMessage(String.format(Messages.OFFLINE_TRANSACTION_EXPENDITURE, Utils.format(result)));
						}
					}
				});

				plugin.getDatabase().getOfflineInterestRevenue(p, result, new Callback<BigDecimal>(plugin) {
                    @Override
                    public void onResult(BigDecimal result) {
                        if (result.signum() == 1) {
							p.sendMessage(String.format(Messages.OFFLINE_INTEREST_EARNED, Utils.format(result)));
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