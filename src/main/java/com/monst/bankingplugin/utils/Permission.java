package com.monst.bankingplugin.utils;

import org.bukkit.permissions.Permissible;

public enum Permission {

    ACCOUNT_CREATE ("bankingplugin.account.create"),
    ACCOUNT_CREATE_PROTECTED ("bankingplugin.account.create.protected"),
    ACCOUNT_TRUST ("bankingplugin.account.trust"),
    ACCOUNT_TRUST_OTHER ("bankingplugin.account.trust.other"),
    ACCOUNT_REMOVE_OTHER ("bankingplugin.account.remove.other"),
    ACCOUNT_REMOVE_PROTECTED ("bankingplugin.account.remove.protected"),
    ACCOUNT_REMOVEALL ("bankingplugin.account.removeall"),
    ACCOUNT_INFO_OTHER ("bankingplugin.account.info.other"),
    ACCOUNT_LIST_OTHER ("bankingplugin.account.list.other"),
    ACCOUNT_VIEW_OTHER ("bankingplugin.account.view.other"),
    ACCOUNT_EDIT_OTHER ("bankingplugin.account.edit.other"),
    ACCOUNT_MIGRATE ("bankingplugin.account.migrate"),
    ACCOUNT_MIGRATE_OTHER ("bankingplugin.account.migrate.other"),
    ACCOUNT_MIGRATE_BANK ("bankingplugin.account.migrate.bank"),
    ACCOUNT_RECOVER ("bankingplugin.account.recover"),
    ACCOUNT_RENAME ("bankingplugin.account.rename"),
    ACCOUNT_RENAME_OTHER ("bankingplugin.account.rename.other"),
    ACCOUNT_CONFIGURE ("bankingplugin.account.configure"),
    ACCOUNT_TRANSFER ("bankingplugin.account.transfer"),
    ACCOUNT_TRANSFER_OTHER ("bankingplugin.account.transfer.other"),
    ACCOUNT_EXTEND_OTHER ("bankingplugin.account.extend.other"),
    ACCOUNT_NO_LIMIT ("bankingplugin.account.limit.*"),

    BANK_CREATE ("bankingplugin.bank.create"),
    BANK_CREATE_ADMIN ("bankingplugin.bank.create.admin"),
    BANK_REMOVE_OTHER ("bankingplugin.bank.remove.other"),
    BANK_REMOVE_ADMIN ("bankingplugin.bank.remove.admin"),
    BANK_REMOVEALL ("bankingplugin.bank.removeall"),
    BANK_NO_LIMIT ("bankingplugin.bank.limit.*"),
    BANK_NO_SIZE_LIMIT ("bankingplugin.bank.size.*"),
    BANK_RESIZE ("bankingplugin.bank.resize"),
    BANK_RESIZE_OTHER ("bankingplugin.bank.resize.other"),
    BANK_RESIZE_ADMIN ("bankingplugin.bank.resize.admin"),
    BANK_TRUST ("bankingplugin.bank.trust"),
    BANK_TRUST_OTHER ("bankingplugin.bank.trust.other"),
    BANK_TRUST_ADMIN ("bankingplugin.bank.trust.admin"),
    BANK_TRANSFER ("bankingplugin.bank.transfer"),
    BANK_TRANSFER_OTHER ("bankingplugin.bank.transfer.other"),
    BANK_TRANSFER_ADMIN ("bankingplugin.bank.transfer.admin"),
    BANK_SET_OTHER ("bankingplugin.bank.configure.other"),
    BANK_SET_ADMIN ("bankingplugin.bank.configure.admin"),
    BANK_SELECT ("worldedit.selection.pos"),

    BYPASS_EXTERNAL_PLUGINS ("bankingplugin.external-bypass"),
    CONFIG ("bankingplugin.config"),
    RELOAD ("bankingplugin.reload"),
    UPDATE ("bankingplugin.update"),
    PAY_INTEREST ("bankingplugin.payinterest"),

    NONE ("") {
        @Override
        public boolean ownedBy(Permissible permissible) {
            return true;
        }
    };

    private final String perm;

    Permission(String perm) {
        this.perm = perm;
    }

    public boolean ownedBy(Permissible permissible) {
        return permissible.hasPermission(perm);
    }

    public boolean notOwnedBy(Permissible permissible) {
        return !ownedBy(permissible);
    }

    @Override
    public String toString() {
        return perm;
    }

}
