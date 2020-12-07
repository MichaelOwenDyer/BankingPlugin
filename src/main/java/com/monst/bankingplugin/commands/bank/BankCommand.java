package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.selections.BlockVector3D;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.utils.BankUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand extends BankingPluginCommand<BankCommand.SubCommand> {

	private static boolean commandCreated = false;

    public BankCommand(final BankingPlugin plugin) {

        super(plugin);

        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + name + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }

        this.name = Config.commandNameBank;
        this.desc = LangUtils.getMessage(Message.BANK_COMMAND_DESC);
		this.pluginCommand = super.createPluginCommand();

		addSubCommand(new BankCreate());
		addSubCommand(new BankInfo());
		addSubCommand(new BankLimits());
		addSubCommand(new BankList());
		addSubCommand(new BankRemove());
		addSubCommand(new BankRemoveall());
		addSubCommand(new BankRename());
		addSubCommand(new BankResize());
		addSubCommand(new BankSelect());
		addSubCommand(new BankSet());
		addSubCommand(new BankTransfer());
		addSubCommand(new BankTrust());
		addSubCommand(new BankUntrust());

        register();
        commandCreated = true;

    }

    abstract static class SubCommand extends BankingPluginSubCommand {

		static final BankUtils bankUtils = plugin.getBankUtils();

		SubCommand(String name, boolean playerCommand) {
			super(name, playerCommand);
		}

		static Bank getBank(CommandSender sender, String[] args) {
			Bank bank = null;
			if (args.length == 1) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					bank = bankUtils.getBank(p.getLocation());
					if (bank == null) {
						plugin.debug(p.getName() + " wasn't standing in a bank");
						p.sendMessage(LangUtils.getMessage(Message.MUST_STAND_IN_BANK));
					}
				} else {
					sender.sendMessage(LangUtils.getMessage(Message.PLAYER_COMMAND_ONLY));
				}
			} else {
				bank = bankUtils.getBank(args[1]);
				if (bank == null) {
					plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
					sender.sendMessage(LangUtils.getMessage(Message.BANK_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
				}
			}
			return bank;
		}

		static String getCoordLookingAt(Player p, int argLength) {
			Location loc = p.getTargetBlock(null, 150).getLocation();
			switch (argLength % 3) {
				case 0: return "" + loc.getBlockX();
				case 1: return "" + loc.getBlockY();
				case 2: return "" + loc.getBlockZ();
			}
			return "";
		}

		/**
		 * Parses coordinates for a new bank selection from command arguments
		 * @param args the arguments to parse
		 * @param loc the location of the player sending the command
		 * @return a {@link CuboidSelection} described by the command arguments
		 * @throws NumberFormatException if the coordinates could not be parsed
		 */
		static CuboidSelection parseCoordinates(String[] args, Location loc) throws NumberFormatException {
			int x1, y1, z1, x2, y2, z2;
			if (args.length == 5 || args.length == 6) {
				x1 = parseCoordinate(args[2], x2 = loc.getBlockX());
				y1 = parseCoordinate(args[3], y2 = loc.getBlockY());
				z1 = parseCoordinate(args[4], z2 = loc.getBlockZ());

			} else if (args.length >= 8) {
				x1 = parseCoordinate(args[2], loc.getBlockX());
				y1 = parseCoordinate(args[3], loc.getBlockY());
				z1 = parseCoordinate(args[4], loc.getBlockZ());
				x2 = parseCoordinate(args[5], loc.getBlockX());
				y2 = parseCoordinate(args[6], loc.getBlockY());
				z2 = parseCoordinate(args[7], loc.getBlockZ());

			} else
				return null;
			BlockVector3D loc1 = new BlockVector3D(x1, y1, z1);
			BlockVector3D loc2 = new BlockVector3D(x2, y2, z2);
			return CuboidSelection.of(loc.getWorld(), loc1, loc2);
		}

		private static int parseCoordinate(String arg, int relativeTo) throws NumberFormatException {
			return arg.startsWith("~") ? Integer.parseInt(arg.substring(1)) + relativeTo : Integer.parseInt(arg);
		}

	}
}
