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
            IllegalStateException e = new IllegalStateException("Command \"" + Config.commandNameBank + "\" has already been registered!");
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
			if (args.length == 5 || args.length == 6) {

				String argX = args[2];
				String argY = args[3];
				String argZ = args[4];

				int x1, y1, z1, x2, y2, z2;

				x2 = loc.getBlockX();
				y2 = loc.getBlockY();
				z2 = loc.getBlockZ();

				x1 = argX.startsWith("~") ? Integer.parseInt(argX.substring(1)) + x2 : Integer.parseInt(argX);
				y1 = argY.startsWith("~") ? Integer.parseInt(argY.substring(1)) + y2 : Integer.parseInt(argY);
				z1 = argZ.startsWith("~") ? Integer.parseInt(argZ.substring(1)) + z2 : Integer.parseInt(argZ);

				BlockVector3D loc1 = new BlockVector3D(x1, y1, z1);
				BlockVector3D loc2 = new BlockVector3D(x2, y2, z2);
				return CuboidSelection.of(loc.getWorld(), loc1, loc2);

			} else if (args.length >= 8) {

				String argX1 = args[2];
				String argY1 = args[3];
				String argZ1 = args[4];
				String argX2 = args[5];
				String argY2 = args[6];
				String argZ2 = args[7];

				int x1, y1, z1, x2, y2, z2;

				x1 = argX1.startsWith("~") ? Integer.parseInt(argX1.substring(1)) + loc.getBlockX() : Integer.parseInt(argX1);
				y1 = argY1.startsWith("~") ? Integer.parseInt(argY1.substring(1)) + loc.getBlockY() : Integer.parseInt(argY1);
				z1 = argZ1.startsWith("~") ? Integer.parseInt(argZ1.substring(1)) + loc.getBlockZ() : Integer.parseInt(argZ1);
				x2 = argX2.startsWith("~") ? Integer.parseInt(argX2.substring(1)) + loc.getBlockX() : Integer.parseInt(argX2);
				y2 = argY2.startsWith("~") ? Integer.parseInt(argY2.substring(1)) + loc.getBlockY() : Integer.parseInt(argY2);
				z2 = argZ2.startsWith("~") ? Integer.parseInt(argZ2.substring(1)) + loc.getBlockZ() : Integer.parseInt(argZ2);

				BlockVector3D loc1 = new BlockVector3D(x1, y1, z1);
				BlockVector3D loc2 = new BlockVector3D(x2, y2, z2);
				return CuboidSelection.of(loc.getWorld(), loc1, loc2);

			}
			return null;
		}

		static Replacement getReplacement() {
			return new Replacement(Placeholder.COMMAND, Config.commandNameBank);
		}
	}
}
