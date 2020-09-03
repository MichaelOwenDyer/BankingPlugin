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

@SuppressWarnings("unused")
public class NotifyPlayerOnJoinListener implements Listener {

	private final BankingPlugin plugin;

	public NotifyPlayerOnJoinListener(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

		plugin.getDatabase().getLastLogout(p, Callback.of(plugin, result -> {
		    if (result < 0) {
                // No logout saved, probably first time joining.
                return;
            }

            plugin.getDatabase().getOfflineTransactionRevenue(p, result, Callback.of(plugin, bigDecimal -> {
                if (bigDecimal.signum() == 1)
                    p.sendMessage(String.format(Messages.OFFLINE_BALANCE_INCREASED, Utils.format(bigDecimal)));
                else if (bigDecimal.signum() == -1)
                    p.sendMessage(String.format(Messages.OFFLINE_TRANSACTION_EXPENDITURE, Utils.format(bigDecimal)));
            }));

            plugin.getDatabase().getOfflineInterestRevenue(p, result, Callback.of(plugin, bigDecimal -> {
                if (bigDecimal.signum() == 1)
                    p.sendMessage(String.format(Messages.OFFLINE_INTEREST_EARNED, Utils.format(bigDecimal)));
            }));

            // Player does not actually log off here, this saves the last time the player was notified about changes
            plugin.getDatabase().logLogout(p, null);
		}));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.getDatabase().logLogout(e.getPlayer(), null);
    }

}
