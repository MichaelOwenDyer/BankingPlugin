package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.log.BankIncome;
import com.monst.bankingplugin.entity.log.QBankIncome;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class BankIncomeRepository extends EntityRepository<BankIncome> {

    static final QBankIncome bankIncome = QBankIncome.bankIncome;

    public BankIncomeRepository(Supplier<EntityManager> emf) {
        super(emf, bankIncome);
    }

    public List<BankIncome> findByBank(Bank bank) {
        return entities().where(bankIncome.bank.eq(bank)).fetch();
    }

    public BigDecimal findTotalProfitOrLossByPlayerSince(OfflinePlayer player, Instant since) {
        return select(bankIncome.netIncome.sum().coalesce(BigDecimal.ZERO))
                .where(bankIncome.recipient.eq(player), bankIncome.timestamp.after(since)).fetchOne();
    }

    public List<Integer> findBefore(Instant oldest) {
        return select(bankIncome.id).where(bankIncome.timestamp.before(oldest)).fetch();
    }

}
