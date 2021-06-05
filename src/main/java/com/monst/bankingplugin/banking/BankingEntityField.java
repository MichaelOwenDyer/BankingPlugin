package com.monst.bankingplugin.banking;

public interface BankingEntityField<Entity extends BankingEntity> {

    Object getFrom(Entity entity);

}
