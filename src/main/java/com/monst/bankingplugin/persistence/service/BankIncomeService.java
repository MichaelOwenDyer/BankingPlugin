package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.log.BankIncome;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.persistence.repository.BankIncomeRepository;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class BankIncomeService extends Service {
    
    private final BankIncomeRepository repo;
    
    public BankIncomeService(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        super(plugin, connectionSupplier);
        this.repo = new BankIncomeRepository();
    }
    
    @Override
    public void createTables() {
        execute(repo::createTable);
    }
    
    public void saveAll(Collection<BankIncome> bankIncomes) {
        execute(con -> repo.saveAll(con, bankIncomes));
    }
    
    public int countByBank(Bank bank) {
        return query(con -> repo.countByBankID(con, bank.getID())).orElse(0);
    }
    
    public Promise<List<BankIncome>> findByBank(Bank bank, int offset, int limit) {
        return async(con -> repo.findByBankID(con, bank.getID(), offset, limit));
    }
    
    public Promise<BigDecimal> findTotalProfitOrLossByPlayerSince(OfflinePlayer player, Instant lastSeen) {
        return async(con -> repo.getTotalProfitOrLossByPlayerSince(con, player.getUniqueId(), Timestamp.from(lastSeen)));
    }
    
    public Promise<BigDecimal> findTotalProfitOrLossAtBankSince(Bank bank, Instant lastSeen) {
        return async(con -> repo.getTotalProfitOrLossAtBankSince(con, bank.getID(), Timestamp.from(lastSeen)));
    }
    
    public void deleteBefore(Instant oldest) {
        transact(con -> repo.deleteBefore(con, Timestamp.from(oldest)));
    }
    
}
