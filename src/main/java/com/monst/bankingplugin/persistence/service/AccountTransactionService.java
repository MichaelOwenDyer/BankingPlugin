package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.persistence.repository.AccountTransactionRepository;
import com.monst.bankingplugin.util.Callback;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.Collection;
import java.util.function.Supplier;

public class AccountTransactionService extends EntityService<AccountTransaction, AccountTransactionRepository> {

    public AccountTransactionService(BankingPlugin plugin, Supplier<EntityManager> emf) {
        super(plugin, emf, new AccountTransactionRepository(emf));
    }

    public void findByAccount(Account account, Callback<Collection<AccountTransaction>> callback) {
        async(() -> repo.findByAccount(account), callback);
    }

    public void deleteBefore(Instant oldest) {
        async(() -> repo.findBefore(oldest), Callback.of(plugin, this::removeAll));
    }

    private void removeAll(Collection<Integer> ids) {
        transaction(em -> ids.stream().map(id -> em.getReference(AccountTransaction.class, id)).forEach(em::remove));
    }

}
