package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.persistence.repository.AccountInterestRepository;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AccountInterestService extends Service {
    
    private final AccountInterestRepository repo;
    
    public AccountInterestService(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        super(plugin, connectionSupplier);
        this.repo = new AccountInterestRepository();
    }
    
    @Override
    public void createTables() {
        execute(repo::createTable);
    }
    
    public void saveAll(Collection<AccountInterest> accountInterests) {
        transact(con -> repo.saveAll(con, accountInterests));
    }
    
    public int countByAccount(Account account) {
        return query(con -> repo.countByAccountID(con, account.getID())).orElse(0);
    }
    
    public Promise<List<AccountInterest>> findByAccount(Account account, int offset, int limit) {
        return async(con -> repo.findByAccountID(con, account.getID(), offset, limit));
    }
    
    public Promise<BigDecimal> findTotalInterestEarnedByPlayerSince(OfflinePlayer player, Instant lastSeen) {
        return async(con -> repo.getTotalInterestEarnedByPlayerSince(con, player.getUniqueId(), Timestamp.from(lastSeen)));
    }
    
    public Promise<BigDecimal> findTotalLowBalanceFeesPaidByPlayerSince(OfflinePlayer player, Instant lastSeen) {
        return async(con -> repo.getTotalLowBalanceFeesPaidByPlayerSince(con, player.getUniqueId(), Timestamp.from(lastSeen)));
    }
    
    public void deleteBefore(Instant oldest) {
        transact(con -> repo.deleteBefore(con, Timestamp.from(oldest)));
    }
    
}
