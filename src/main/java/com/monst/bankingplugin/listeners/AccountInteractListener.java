package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.account.AccountInfo;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AccountInteractListener extends BankingPluginListener {

	public AccountInteractListener(BankingPlugin plugin) {
		super(plugin);
	}

	/**
	 * Checks every block interact event for an account action attempt, and
	 * handles the action.
	 */
	@SuppressWarnings({"unused"})
	@EventHandler(priority = EventPriority.HIGH)
	public void onAccountInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block block = e.getClickedBlock();
		Action action = e.getAction();

		if (block == null)
			return;
		if (!Utils.isChest(block))
			return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)
			return;

		ClickType clickType = ClickType.getPlayerClickType(p);
		Account account = accountRepo.getAt(block);
		if (account == null && clickType == null)
			return;

		if (clickType != null) {

			if (action == Action.LEFT_CLICK_BLOCK)
				return;
			if (account == null && !clickType.canClickedBlockNotBeAccount())
				return;

			e.setCancelled(true);

			switch (clickType.getType()) {
				case CREATE:
				case RECOVER:
					clickType.execute(p, block); // Operations on a block
					break;
				case MIGRATE_SELECT_NEW_CHEST:
					clickType.execute(p, account, block); // Operations on an account and a block
					break;
				case INFO:
				case RENAME:
				case REMOVE:
				case TRUST:
				case UNTRUST:
				case MIGRATE_SELECT_ACCOUNT:
				case TRANSFER:
				case CONFIGURE:
					clickType.execute(p, account); // Operations on an account
			}

		} else {

			if (action == Action.LEFT_CLICK_BLOCK && p.isSneaking() && Utils.hasAxeInHand(p))
				return;

			// The account info item will allow account info requests with no ClickType
			ItemStack infoItem = Config.accountInfoItem.get();
			if (infoItem != null)
				for (ItemStack item : new ItemStack[] { Utils.getItemInMainHand(p), Utils.getItemInOffHand(p) })
					if (item != null && infoItem.getType() == item.getType()) {
						e.setCancelled(true);
						AccountInfo.info(p, account);
						return;
					}

			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()) {
				if (!account.isTrusted(p) && !account.getBank().isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_VIEW_OTHER)) {
					e.setCancelled(true);
					p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_VIEW_OTHER));
					plugin.debug(p.getName() + " does not have permission to open " + account.getOwner().getName()
							+ "'s account chest.");
					return;
				}

				e.setCancelled(false);
				if (!account.isTrusted(p))
					p.sendMessage(Messages.get(Message.ACCOUNT_OPENED,
							new Replacement(Placeholder.PLAYER, account.getOwnerDisplayName())
					));

				plugin.debugf("%s is opening %s account%s (#%d)",
						p.getName(), (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"),
						(account.isCoOwner(p) ? " (is co-owner)" : ""), account.getID());
			}
		}
	}
}
