package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.entity.log.QAccountInterest;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class AccountInterestRepository extends EntityRepository<AccountInterest> {

    static final QAccountInterest accountInterest = QAccountInterest.accountInterest;

    public AccountInterestRepository(Supplier<EntityManager> emf) {
        super(emf, accountInterest);
    }

    public List<AccountInterest> findByAccount(Account account) {
        return entities().where(accountInterest.account.eq(account)).fetch();
    }

    public BigDecimal findTotalInterestEarnedByPlayerSince(OfflinePlayer player, Instant since) {
        return select(accountInterest.interest.sum().coalesce(BigDecimal.ZERO))
                .where(accountInterest.recipient.eq(player), accountInterest.timestamp.after(since)).fetchOne();
    }

    public BigDecimal findTotalLowBalanceFeesPaidByPlayerSince(OfflinePlayer player, Instant since) {
        return select(accountInterest.lowBalanceFee.sum().coalesce(BigDecimal.ZERO))
                .where(accountInterest.recipient.eq(player), accountInterest.timestamp.after(since)).fetchOne();
    }

    public List<Integer> findOlderThan(Instant oldest) {
        return select(accountInterest.id).where(accountInterest.timestamp.before(oldest)).fetch();
    }

}
