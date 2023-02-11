package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.gui.AccountGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public class AccountInteractListener implements Listener {

	private final BankingPlugin plugin;

	public AccountInteractListener(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Checks every block interact event for an account action attempt, and handles the action.
	 */
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.HIGH)
	public void onAccountInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();
		Action action = e.getAction();
		
		if (block == null)
			return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)
			return;
		if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
			return;
		// Player has left-clicked or right-clicked a chest
		
		ClickAction clickAction = ClickAction.get(player);
		if (clickAction != null) {
			// Player has a pending click action
			
			if (action == Action.LEFT_CLICK_BLOCK)
				return;
			// Player has a pending click action and has right-clicked a chest
			
			try {
				if (clickAction.isBlockAction()) {
					// Chest is not meant to be an account (yet)
					clickAction.onClick(block);
				} else {
					// Chest should already be an account; if it isn't, return.
					Account account = plugin.getAccountService().findAtChest(block);
					if (account == null)
						return; // Chest is not an account; do not cancel the event and allow it to be opened
					clickAction.onClick(account);
				}
			} catch (CommandExecutionException ex) {
				player.sendMessage(ex.getLocalizedMessage());
				plugin.debug(ex.getMessage());
			} catch (EventCancelledException ex) {
				plugin.debug("Interact event cancelled: " + ex);
			}
			
			e.setCancelled(true); // Do not open the chest, even if the interaction fails
			return;
		}
		// At this point, the player has clicked a chest, and has no pending click action
		
		Account account = plugin.getAccountService().findAtChest(block);
		if (account == null)
			return;
		// Player has clicked an account with no pending click action
		
		if (action == Action.LEFT_CLICK_BLOCK && player.isSneaking()) {
			Set<Material> axes = EnumSet.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
					Material.GOLDEN_AXE, Material.DIAMOND_AXE);
			boolean hasAxe = Stream.of(player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand())
					.filter(item -> item != null)
					.anyMatch(item -> axes.contains(item.getType()));
			if (hasAxe) // Player is destroying the account chest
				return;
		}
		// Player has clicked an account chest without the intention of destroying it.

		if (plugin.config().accountInfoItem.isHeldBy(player)) {
			// Player has clicked an account chest while holding the account info item.
			e.setCancelled(true); // Do not open the chest, show the account info GUI.
			new AccountGUI(plugin, player, account).open();
			return;
		}
		// Player has clicked an account chest without the intention of destroying it and is not holding the account info item.

		if (action != Action.RIGHT_CLICK_BLOCK || player.isSneaking())
			return;
		// Player has right-clicked an account chest with the intention of opening it.

		if (!account.isTrusted(player)) {
			// Player is not owner or co-owner of the account.
			if (!account.getBank().isOwner(player) && Permissions.ACCOUNT_VIEW_OTHER.notOwnedBy(player)) {
				// Player is not owner of the bank either, and does not have permission to open other players' accounts.
				e.setCancelled(true); // Do not open the chest
				player.sendMessage(Message.NO_PERMISSION_ACCOUNT_VIEW_OTHER.translate(plugin)); // Show the no permission message
				plugin.debug("%s does not have permission to open %s's account chest.", player.getName(), account.getOwner().getName());
				return;
			}
			// Player has permission to open the account despite not being trusted on the account.
			// Send them a message telling them whose account they are opening.
			player.sendMessage(Message.ACCOUNT_CHEST_OPENED.with(Placeholder.PLAYER).as(account.getOwner().getName()).translate(plugin));
		}

		e.setCancelled(false); // Ensure that the chest is opened.
		plugin.debug("%s is opening account %s", player.getName(), account);

	}
}
