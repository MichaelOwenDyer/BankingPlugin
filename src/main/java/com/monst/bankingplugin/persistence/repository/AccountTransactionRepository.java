package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.entity.log.QAccountTransaction;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class AccountTransactionRepository extends EntityRepository<AccountTransaction> {

    static final QAccountTransaction accountTransaction = QAccountTransaction.accountTransaction;

    public AccountTransactionRepository(Supplier<EntityManager> emf) {
        super(emf, accountTransaction);
    }

    public List<AccountTransaction> findByAccount(Account account) {
        return entities().where(accountTransaction.account.eq(account)).fetch();
    }

    public List<Integer> findBefore(Instant oldest) {
        return select(accountTransaction.id).where(accountTransaction.timestamp.before(oldest)).fetch();
    }

}
