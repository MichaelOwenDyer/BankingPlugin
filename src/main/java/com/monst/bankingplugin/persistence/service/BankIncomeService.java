package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.log.BankIncome;
import com.monst.bankingplugin.persistence.repository.BankIncomeRepository;
import com.monst.bankingplugin.util.Callback;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Supplier;

public class BankIncomeService extends EntityService<BankIncome, BankIncomeRepository> {

    public BankIncomeService(BankingPlugin plugin, Supplier<EntityManager> emf) {
        super(plugin, emf, new BankIncomeRepository(emf));
    }

    public void findByBank(Bank bank, Callback<Collection<BankIncome>> callback) {
        async(() -> repo.findByBank(bank), callback);
    }

    public void findTotalProfitOrLossByPlayerSince(OfflinePlayer player, Instant since, Callback<BigDecimal> callback) {
        async(() -> repo.findTotalProfitOrLossByPlayerSince(player, since), callback);
    }

    public void deleteBefore(Instant oldest) {
        async(() -> repo.findBefore(oldest), Callback.of(plugin, this::removeAll));
    }

    private void removeAll(Collection<Integer> ids) {
        transaction(em -> ids.stream().map(id -> em.getReference(BankIncome.class, id)).forEach(em::remove));
    }

}
