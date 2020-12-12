package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.commands.account.*;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.ClickType.EClickType;
import com.monst.bankingplugin.utils.ClickType.SetPair;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class AccountInteractListener extends BankingPluginListener {

	public AccountInteractListener(BankingPlugin plugin) {
		super(plugin);
	}

	/**
	 * Checks every inventory interact event for an account action attempt, and
	 * handles the action.
	 */
	@SuppressWarnings({"deprecation","unused"})
	@EventHandler(priority = EventPriority.HIGH)
	public void onAccountInteract(PlayerInteractEvent e) {

		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if (b == null || b.getType() == Material.AIR)
			return;
		Account account = accountRepo.getAt(b.getLocation());
		ClickType<?> clickType = ClickType.getPlayerClickType(p);

		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST))
			return;
		if (clickType == null && account == null)
			return;

		if (clickType != null) {

			if (account == null && !(clickType.getType() == EClickType.CREATE
					|| clickType.getType() == EClickType.MIGRATE
					|| clickType.getType() == EClickType.RECOVER))
				return;
			if (account == null && clickType.getType() == EClickType.MIGRATE && clickType.get() == null)
				return;
			if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK))
				return;

			switch (clickType.getType()) {

				case CREATE:

					if (e.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
						p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
						plugin.debug(p.getName() + " does not have permission to create an account on a protected chest.");
					} else
						AccountCreate.create(p, b);
					ClickType.removePlayerClickType(p);
					break;

				case INFO:

					Objects.requireNonNull(account);
					AccountInfo.info(p, account);
					ClickType.removePlayerClickType(p);
					break;

				case MIGRATE:

					if (clickType.get() == null)
						AccountMigrate.migratePartOne(p, Objects.requireNonNull(account));
					else {
						if (e.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
							p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE_PROTECTED));
							plugin.debug(p.getName() + " does not have permission to migrate an account to a protected chest.");
						} else {
							Account toMigrate = Objects.requireNonNull(clickType.get());
							AccountMigrate.migratePartTwo(p, b, toMigrate);
						}
						ClickType.removePlayerClickType(p);
					}
					break;

				case RECOVER:

					Account toRecover = Objects.requireNonNull(clickType.get());
					AccountRecover.recover(p, b, toRecover);
					ClickType.removePlayerClickType(p);
					break;

				case REMOVE:

					Objects.requireNonNull(account);
					AccountRemove.getInstance().remove(p, account);
					break;

				case RENAME:

					String newName = Objects.requireNonNull(clickType.get());
					Objects.requireNonNull(account);
					AccountRename.rename(p, account, newName);
					ClickType.removePlayerClickType(p);
					break;

				case SET:

					SetPair pair = clickType.get();
					AccountSet.set(p, account, pair.getField(), pair.getValue());
					ClickType.removePlayerClickType(p);
					break;

				case TRANSFER:

					Objects.requireNonNull(account);
					OfflinePlayer newOwner = clickType.get();
					AccountTransfer.getInstance().transfer(p, newOwner, account);
					break;

				case TRUST:

					Objects.requireNonNull(account);
					OfflinePlayer playerToTrust = clickType.get();
					AccountTrust.trust(p, account, playerToTrust);
					ClickType.removePlayerClickType(p);
					break;

				case UNTRUST:

					Objects.requireNonNull(account);
					OfflinePlayer playerToUntrust = clickType.get();
					AccountUntrust.untrust(p, account, playerToUntrust);
					ClickType.removePlayerClickType(p);
					break;
			}
			e.setCancelled(true);
		} else {

			if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
				return;
			if (p.isSneaking() && Utils.hasAxeInHand(p) && e.getAction() == Action.LEFT_CLICK_BLOCK)
				return;

			// Handles account info requests using config info item
			ItemStack infoItem = Config.accountInfoItem;

			if (infoItem != null) {
				ItemStack item = Utils.getItemInMainHand(p);
				if (item != null && infoItem.getType() == item.getType()) {
					e.setCancelled(true);
					AccountInfo.info(p, account);
					return;
				}
				item = Utils.getItemInOffHand(p);
				if (item != null && infoItem.getType() == item.getType()) {
					e.setCancelled(true);
					AccountInfo.info(p, account);
					return;
				}
			}

			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()) {
				if (!account.isTrusted(p) && !account.getBank().isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_VIEW_OTHER)) {
					e.setCancelled(true);
					p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_VIEW_OTHER));
					plugin.debug(p.getName() + " does not have permission to open " + account.getOwner().getName()
							+ "'s account chest.");
					return;
				}

				if (e.isCancelled())
					e.setCancelled(false);
				if (!account.isTrusted(p))
					p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_OPENED,
							new Replacement(Placeholder.PLAYER, () -> account.getOwner().getName())
					));

				plugin.debugf("%s is opening %s account%s (#%d)",
						p.getName(), (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"),
						(account.isCoowner(p) ? " (is co-owner)" : ""), account.getID());
			}
		}
	}
}
