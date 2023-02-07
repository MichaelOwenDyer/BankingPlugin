package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.persistence.Query;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AccountTransactionRepository {
    
    public void createTable(Connection con) throws SQLException {
        Query.of("CREATE TABLE IF NOT EXISTS ACCOUNT_TRANSACTION("
                        + "transaction_id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"
                        + "account_id INTEGER NOT NULL REFERENCES ACCOUNT(account_id),"
                        + "bank_id INTEGER NOT NULL REFERENCES BANK(bank_id),"
                        + "executor_uuid UUID NOT NULL,"
                        + "previous_balance NUMERIC(16,2) NOT NULL,"
                        + "amount NUMERIC(16,2) NOT NULL,"
                        + "new_balance NUMERIC(16,2) NOT NULL,"
                        + "timestamp TIMESTAMP NOT NULL)")
                .executeUpdate(con);
    }
    
    public void save(Connection con, AccountTransaction accountTransaction) throws SQLException {
        Query.of("INSERT INTO ACCOUNT_TRANSACTION "
                        + "(account_id, bank_id, executor_uuid, previous_balance, amount, new_balance, timestamp) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")
                .with(deconstruct(accountTransaction))
                .executeUpdate(con);
    }
    
    public int countByAccountID(Connection con, int accountID) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM ACCOUNT_TRANSACTION WHERE account_id = ?")
                .with(accountID)
                .asOne(con, Integer.class);
    }
    
    // findByAccountID returns promise
    public List<AccountTransaction> findByAccountID(Connection con, int accountID, int offset, int limit) throws SQLException {
        return Query.of("SELECT * FROM ACCOUNT_TRANSACTION WHERE account_id = ? ORDER BY transaction_id OFFSET ? ROWS LIMIT ?")
                .with(accountID, offset, limit)
                .asList(con, this::reconstruct);
    }
    
    public void deleteBefore(Connection con, Timestamp oldest) throws SQLException {
        Query.of("DELETE FROM ACCOUNT_TRANSACTION WHERE timestamp < ?")
                .with(oldest)
                .executeUpdate(con);
    }
    
    private List<Object> deconstruct(AccountTransaction transaction) {
        return Arrays.asList(
                transaction.getAccountID(),
                transaction.getBankID(),
                transaction.getExecutor().getUniqueId(),
                transaction.getPreviousBalance(),
                transaction.getAmount(),
                transaction.getNewBalance(),
                Timestamp.from(transaction.getInstant())
        );
    }
    
    private AccountTransaction reconstruct(ResultSet rs, Connection con) throws SQLException {
        return new AccountTransaction(
                rs.getInt("transaction_id"),
                rs.getTimestamp("timestamp").toInstant(),
                rs.getInt("account_id"),
                rs.getInt("bank_id"),
                Bukkit.getOfflinePlayer(rs.getObject("executor_uuid", UUID.class)),
                rs.getBigDecimal("new_balance"),
                rs.getBigDecimal("previous_balance"),
                rs.getBigDecimal("amount")
        );
    }
    
}
