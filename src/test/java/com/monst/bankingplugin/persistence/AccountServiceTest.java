package com.monst.bankingplugin.persistence;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.persistence.service.AccountService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.OfflinePlayer;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
public class AccountServiceTest {
    
    private final DataSource dataSource = new HikariDataSource(new HikariConfig("src/test/resources/hikaricp.configurationFile"));
    
    @Mock
    private BankingPlugin plugin;
    
    @Mock
    private Bank bank;
    
    @Mock
    private OfflinePlayer owner;
    
    @Mock
    private AccountLocation loc;
    
    @TestSubject
    AccountService accountService = new AccountService(plugin, dataSource::getConnection);
    
    @Test
    public void testCount() {
        assertEquals(0, accountService.count());
        Account account = new Account(bank, owner, loc);
        EasyMock.replay(plugin, bank, owner, loc);
        accountService.save(account);
        assertEquals(1, accountService.count());
    }
    
}
