package com.monst.bankingplugin.utils;

import org.bukkit.entity.Player;

import java.util.Arrays;

public class Permissions {

	public static final String ACCOUNT_CREATE = "bankingplugin.account.create";
	public static final String ACCOUNT_CREATE_PROTECTED = "bankingplugin.account.create.protected";
	public static final String ACCOUNT_TRUST = "bankingplugin.account.trust";
	public static final String ACCOUNT_TRUST_OTHER = "bankingplugin.account.trust.other";
	public static final String ACCOUNT_REMOVE_OTHER = "bankingplugin.account.remove.other";
	public static final String ACCOUNT_REMOVE_PROTECTED = "bankingplugin.account.remove.protected";
	public static final String ACCOUNT_REMOVEALL = "bankingplugin.account.removeall";
	public static final String ACCOUNT_INFO_OTHER = "bankingplugin.account.info.other";
	public static final String ACCOUNT_LIST_OTHER = "bankingplugin.account.list.other";
	public static final String ACCOUNT_VIEW_OTHER = "bankingplugin.account.view.other";
	public static final String ACCOUNT_EDIT_OTHER = "bankingplugin.account.edit.other";
	public static final String ACCOUNT_MIGRATE = "bankingplugin.account.migrate";
	public static final String ACCOUNT_MIGRATE_OTHER = "bankingplugin.account.migrate.other";
	public static final String ACCOUNT_MIGRATE_BANK = "bankingplugin.account.migrate.bank";
	public static final String ACCOUNT_RECOVER = "bankingplugin.account.recover";
	public static final String ACCOUNT_RENAME = "bankingplugin.account.rename";
	public static final String ACCOUNT_RENAME_OTHER = "bankingplugin.account.rename.other";
	public static final String ACCOUNT_SET = "bankingplugin.account.set";
	public static final String ACCOUNT_TRANSFER = "bankingplugin.account.transfer";
	public static final String ACCOUNT_TRANSFER_OTHER = "bankingplugin.account.transfer.other";
	public static final String ACCOUNT_EXTEND_OTHER = "bankingplugin.account.extend.other";
	public static final String ACCOUNT_NO_LIMIT = "bankingplugin.account.limit.*";

	public static final String BANK_CREATE = "bankingplugin.bank.create";
	public static final String BANK_CREATE_ADMIN = "bankingplugin.bank.create.admin";
	public static final String BANK_REMOVE_OTHER = "bankingplugin.bank.remove.other";
	public static final String BANK_REMOVE_ADMIN = "bankingplugin.bank.remove.admin";
	public static final String BANK_REMOVEALL = "bankingplugin.bank.removeall";
	public static final String BANK_NO_LIMIT = "bankingplugin.bank.limit.*";
	public static final String BANK_NO_SIZE_LIMIT = "bankingplugin.bank.size.*";
	public static final String BANK_RESIZE = "bankingplugin.bank.resize";
	public static final String BANK_RESIZE_OTHER = "bankingplugin.bank.resize.other";
	public static final String BANK_RESIZE_ADMIN = "bankingplugin.bank.resize.admin";
	public static final String BANK_TRUST = "bankingplugin.bank.trust";
	public static final String BANK_TRUST_OTHER = "bankingplugin.bank.trust.other";
	public static final String BANK_TRUST_ADMIN = "bankingplugin.bank.trust.admin";
	public static final String BANK_TRANSFER = "bankingplugin.bank.transfer";
	public static final String BANK_TRANSFER_OTHER = "bankingplugin.bank.transfer.other";
	public static final String BANK_TRANSFER_ADMIN = "bankingplugin.bank.transfer.admin";
	public static final String BANK_SET_OTHER = "bankingplugin.bank.configure.other";
	public static final String BANK_SET_ADMIN = "bankingplugin.bank.configure.admin";
	public static final String BANK_SELECT = "worldedit.selection.pos";

	public static final String BYPASS_EXTERNAL_PLUGINS = "bankingplugin.external-bypass";
	public static final String CONFIG = "bankingplugin.config";
	public static final String RELOAD = "bankingplugin.reload";
	public static final String UPDATE = "bankingplugin.update";
	public static final String PAY_INTEREST = "bankingplugin.payinterest";

	public static boolean hasAny(Player player, String... permissions) {
		return Arrays.stream(permissions).anyMatch(player::hasPermission);
	}

	public static boolean hasAll(Player player, String... permissions) {
		return Arrays.stream(permissions).allMatch(player::hasPermission);
	}

}
