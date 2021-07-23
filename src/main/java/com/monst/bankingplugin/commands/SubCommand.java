package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.regions.CuboidBankRegion;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.repository.BankRepository;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

	private final String name;
	private final boolean playerCommand;

    protected SubCommand(String name, boolean playerCommand) {
    	this.name = name;
        this.playerCommand = playerCommand;
    }

    String getName() {
        return name;
    }

    /**
     * @return Whether the command can only be used by players, not by the console
     */
    boolean isPlayerCommand() {
        return playerCommand;
    }

    /**
     * Execute the sub command
     * @param sender Sender of the command
     * @param args Arguments of the command ({@code args[0]} is the sub command's name)
     * @return Whether the command syntax was correct
     */
    protected abstract boolean execute(CommandSender sender, String[] args);

    /**
     * @param sender Sender of the command
     * @param args Arguments of the command ({@code args[0]} is the sub command's name)
     * @return A list of tab completions for the sub command (may be an empty list)
     */
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
		return Collections.emptyList();
    }

    /**
     * @return the permission node required to see and execute this command.
     */
    protected String getPermission() {
        return null;
    }

    /**
     * @return the {@link Message} describing the syntax of this subcommand.
     */
    protected abstract Message getUsageMessage();

    /**
     * Sends a message to the command sender describing how to use this subcommand
     * @param sender Sender to receive the help message
     * @return The help message for the command.
     */
    String getHelpMessage(CommandSender sender, String commandName) {
        if (hasPermission(sender, getPermission()))
            return Messages.get(getUsageMessage(), new Replacement(Placeholder.COMMAND, commandName));
        return "";
    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (permission == null || sender == null || permission.isEmpty() || sender.hasPermission(permission))
            return true;
        for (PermissionAttachmentInfo permInfo : sender.getEffectivePermissions())
            if (Utils.startsWithIgnoreCase(permInfo.getPermission(), permission) && permInfo.getValue())
                return true;
        return false;
    }

    public abstract static class AccountSubCommand extends SubCommand {

        protected static final AccountRepository accountRepo = PLUGIN.getAccountRepository();

        protected AccountSubCommand(String name, boolean playerCommand) {
            super(name, playerCommand);
        }

    }

    public abstract static class BankSubCommand extends SubCommand {

        protected static final BankRepository bankRepo = PLUGIN.getBankRepository();

        protected BankSubCommand(String name, boolean playerCommand) {
            super(name, playerCommand);
        }

        protected static Bank getBank(CommandSender sender, String[] args) {
            Bank bank = null;
            if (args.length == 1) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    bank = bankRepo.getAt(p.getLocation().getBlock());
                    if (bank == null) {
                        PLUGIN.debug(p.getName() + " wasn't standing in a bank");
                        p.sendMessage(Messages.get(Message.MUST_STAND_IN_BANK));
                    }
                } else {
                    sender.sendMessage(Messages.get(Message.PLAYER_COMMAND_ONLY));
                }
            } else {
                bank = bankRepo.getByIdentifier(args[1]);
                if (bank == null) {
                    PLUGIN.debugf("Couldn't find bank with name or ID %s", args[1]);
                    sender.sendMessage(Messages.get(Message.BANK_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
                }
            }
            return bank;
        }

        protected static String getCoordLookingAt(Player p, int argLength) {
            Location loc = p.getTargetBlock(null, 150).getLocation();
            switch (argLength % 3) {
                case 0: return "" + loc.getBlockY();
                case 1: return "" + loc.getBlockZ();
                case 2: return "" + loc.getBlockX();
            }
            return "";
        }

        /**
         * Parses coordinates for a new bank region from command arguments
         * @param args the arguments to parse
         * @param loc the location of the player sending the command
         * @return a {@link CuboidBankRegion} described by the command arguments
         * @throws NumberFormatException if the coordinates could not be parsed
         */
        protected static CuboidBankRegion parseCoordinates(String[] args, Block loc) throws IntegerParseException {
            int x1, y1, z1, x2, y2, z2;
            if (args.length == 5 || args.length == 6) {
                x1 = parseCoordinate(args[2], x2 = loc.getX());
                y1 = parseCoordinate(args[3], y2 = loc.getY());
                z1 = parseCoordinate(args[4], z2 = loc.getZ());
            } else if (args.length >= 8) {
                x1 = parseCoordinate(args[2], loc.getX());
                y1 = parseCoordinate(args[3], loc.getY());
                z1 = parseCoordinate(args[4], loc.getZ());
                x2 = parseCoordinate(args[5], loc.getX());
                y2 = parseCoordinate(args[6], loc.getY());
                z2 = parseCoordinate(args[7], loc.getZ());
            } else
                return null;
            BlockVector3D loc1 = new BlockVector3D(x1, y1, z1);
            BlockVector3D loc2 = new BlockVector3D(x2, y2, z2);
            return CuboidBankRegion.of(loc.getWorld(), loc1, loc2);
        }

        private static int parseCoordinate(String arg, int relativeTo) throws IntegerParseException {
            return arg.startsWith("~") ? Parser.parseInt(arg.substring(1)) + relativeTo : Parser.parseInt(arg);
        }

    }

    public abstract static class ControlSubCommand extends SubCommand {

        protected ControlSubCommand(String name, boolean playerCommand) {
            super(name, playerCommand);
        }

    }

}
