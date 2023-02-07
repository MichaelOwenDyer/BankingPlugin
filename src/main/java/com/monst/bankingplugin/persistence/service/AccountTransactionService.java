package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.persistence.repository.AccountTransactionRepository;
import com.monst.bankingplugin.util.Promise;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class AccountTransactionService extends Service {
    
    private final AccountTransactionRepository repo;
    
    public AccountTransactionService(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        super(plugin, connectionSupplier);
        this.repo = new AccountTransactionRepository();
    }
    
    @Override
    public void createTables() {
        execute(repo::createTable);
    }
    
    public void save(AccountTransaction accountTransaction) {
        execute(con -> repo.save(con, accountTransaction));
    }
    
    public int countByAccount(Account account) {
        return query(con -> repo.countByAccountID(con, account.getID())).orElse(0);
    }
    
    public Promise<List<AccountTransaction>> findByAccount(Account account, int offset, int limit) {
        return async(con -> repo.findByAccountID(con, account.getID(), offset, limit));
    }
    
    public void deleteBefore(Instant oldest) {
        transact(con -> repo.deleteBefore(con, Timestamp.from(oldest)));
    }
    
}
