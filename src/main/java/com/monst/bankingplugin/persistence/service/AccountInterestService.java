package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.persistence.repository.AccountInterestRepository;
import com.monst.bankingplugin.util.Callback;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Supplier;

public class AccountInterestService extends EntityService<AccountInterest, AccountInterestRepository> {

    public AccountInterestService(BankingPlugin plugin, Supplier<EntityManager> emf) {
        super(plugin, emf, new AccountInterestRepository(emf));
    }

    public void findByAccount(Account account, Callback<Collection<AccountInterest>> callback) {
        async(() -> repo.findByAccount(account), callback);
    }

    public void findTotalInterestEarnedByPlayerSince(OfflinePlayer player, Instant since, Callback<BigDecimal> callback) {
        async(() -> repo.findTotalInterestEarnedByPlayerSince(player, since), callback);
    }

    public void findTotalLowBalanceFeesPaidByPlayerSince(OfflinePlayer player, Instant since, Callback<BigDecimal> callback) {
        async(() -> repo.findTotalLowBalanceFeesPaidByPlayerSince(player, since), callback);
    }

    public void deleteBefore(Instant oldest) {
        async(() -> repo.findOlderThan(oldest), Callback.of(plugin, this::removeAll));
    }

    private void removeAll(Collection<Integer> ids) {
        transaction(em -> ids.stream().map(id -> em.getReference(AccountInterest.class, id)).forEach(em::remove));
    }

}
