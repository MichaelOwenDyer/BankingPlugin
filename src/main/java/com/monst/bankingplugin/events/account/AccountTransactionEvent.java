package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * This event is fired when the contents of an account chest are updated in a way that the balance changes.
 */
public class AccountTransactionEvent extends SingleAccountEvent {

	private final BigDecimal amount;
	private final BigDecimal newBalance;

	public AccountTransactionEvent(@Nonnull Player player, @Nonnull Account account,
								   @Nonnull BigDecimal amount, @Nonnull BigDecimal newBalance) {
		super(player, account);
		this.amount = amount;
		this.newBalance = newBalance;
	}

	@Nonnull
	public BigDecimal getAmount() {
		return amount;
	}

	@Nonnull
	public BigDecimal getNewBalance() {
		return newBalance;
	}

}
