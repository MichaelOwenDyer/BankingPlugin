package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.LastSeen;
import com.monst.bankingplugin.entity.QAccount;
import com.monst.bankingplugin.entity.QBank;
import com.monst.bankingplugin.entity.QLastSeen;
import com.querydsl.jpa.JPAExpressions;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class LastSeenRepository extends EntityRepository<LastSeen> {

    static final QLastSeen lastSeen = QLastSeen.lastSeen;

    public LastSeenRepository(Supplier<EntityManager> emf) {
        super(emf, lastSeen);
    }

    public Instant getLastSeenTime(OfflinePlayer player) {
        return select(lastSeen.time).where(lastSeen.player.eq(player)).fetchOne();
    }

    public List<OfflinePlayer> findUnused() {
        QBank bank = QBank.bank;
        QAccount account = QAccount.account;
        return select(lastSeen.player).where(lastSeen.player.notIn(
                JPAExpressions.selectDistinct(bank.owner).from(bank).where(bank.owner.isNotNull())
        ), lastSeen.player.notIn(
                JPAExpressions.selectDistinct(account.owner).from(account)
        )).fetch();
    }

}
