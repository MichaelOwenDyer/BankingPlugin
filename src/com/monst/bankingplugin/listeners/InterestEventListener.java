package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.interest.InterestEvent;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Messages;

import net.milkbowl.vault.economy.EconomyResponse;

public class InterestEventListener implements Listener {
	
	private BankingPlugin plugin;
	private AccountUtils accountUtils;
	private boolean multi = Config.enableInterestMultipliers;
	
	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	@EventHandler
	public void onInterestEvent(InterestEvent e) {
		
		for (Account account : accountUtils.getAccounts()) {
			
			OfflinePlayer owner = account.getOwner();
			AccountStatus status = account.getStatus();
			
			BigDecimal interest = account.getBalance().multiply(BigDecimal.valueOf(Config.baselineInterestRate));
			interest = interest.setScale(2, RoundingMode.HALF_EVEN);
			BigDecimal baseInterest = interest;
			
			if (status.allowNextPayout(owner.isOnline())) {
				int multiplier = 1;
				if (multi) {
					multiplier = status.getRealMultiplier();
					interest.multiply(BigDecimal.valueOf(multiplier));
				}

				status.incrementMultiplier(owner.isOnline());
				
				EconomyResponse r = plugin.getEconomy().depositPlayer(owner, account.getLocation().getWorld().getName(),
						interest.doubleValue());
	            if (!r.transactionSuccess()) {
	                plugin.debug("Economy transaction failed: " + r.errorMessage);
	                if (owner.isOnline())
	                	owner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
				} else {
					if (Config.enableInterestLog) {
						plugin.getDatabase().logInterest(account, baseInterest, multiplier, interest, null);
					}
	            }
	            
			}
			
		}
		
	}
	
}
