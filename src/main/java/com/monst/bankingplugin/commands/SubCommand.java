package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.geo.Vector3D;
import com.monst.bankingplugin.geo.regions.CuboidBankRegion;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.Permission;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final BankingPlugin plugin;
	private final String name;
	private final boolean playerCommand;

    protected SubCommand(BankingPlugin plugin, String name, boolean playerCommand) {
        this.plugin = plugin;
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
     * Executes the subcommand
     * @param sender Sender of the command
     * @param args Arguments of the command
     * @return Whether the command syntax was correct
     */
    protected abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Tab-completes the subcommand
     * @param player Sender of the command
     * @param args Arguments of the command
     * @return A list of tab completions for the subcommand (can be empty)
     */
    protected List<String> getTabCompletions(Player player, String[] args) {
		return Collections.emptyList();
    }

    /**
     * @return the permission node required to see and execute this command.
     */
    protected Permission getPermission() {
        return Permission.NONE;
    }

    /**
     * @return the {@link Message} describing the syntax of this subcommand.
     */
    protected abstract Message getUsageMessage();

    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION;
    }

    /**
     * Gets a message describing how to use this subcommand
     * @param sender Sender to receive the help message
     * @return The help message for this subcommand.
     */
    String getUsageMessage(CommandSender sender, String commandName) {
        if (getPermission().ownedBy(sender))
            return getUsageMessage().with(Placeholder.COMMAND).as(commandName).translate();
        return "";
    }

    public abstract static class BankSubCommand extends SubCommand {

        protected BankSubCommand(BankingPlugin plugin, String name, boolean playerCommand) {
            super(plugin, name, playerCommand);
        }

        protected Bank getBank(CommandSender sender, String[] args) {
            Bank bank = null;
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    bank = plugin.getBankRepository().getAt(p.getLocation().getBlock());
                    if (bank == null) {
                        plugin.debugf("%s wasn't standing in a bank", p.getName());
                        p.sendMessage(Message.MUST_STAND_IN_BANK.translate());
                    }
                } else
                    sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate());
            } else {
                bank = plugin.getBankRepository().getByIdentifier(args[0]);
                if (bank == null) {
                    plugin.debugf("Couldn't find bank with name or ID %s", args[0]);
                    sender.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]).translate());
                }
            }
            return bank;
        }

        protected static int getCoordLookingAt(Player p, int argLength) {
            Block lookingAt = p.getTargetBlock(null, 150);
            int coordinate = 0;
            switch (argLength % 3) {
                case 2: coordinate = lookingAt.getX();
                case 0: coordinate = lookingAt.getY();
                case 1: coordinate = lookingAt.getZ();
            }
            return coordinate;
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
            if (args.length == 4 || args.length == 5) {
                x1 = parseCoordinate(args[1], x2 = loc.getX());
                y1 = parseCoordinate(args[2], y2 = loc.getY());
                z1 = parseCoordinate(args[3], z2 = loc.getZ());
            } else if (args.length >= 7) {
                x1 = parseCoordinate(args[1], loc.getX());
                y1 = parseCoordinate(args[2], loc.getY());
                z1 = parseCoordinate(args[3], loc.getZ());
                x2 = parseCoordinate(args[4], loc.getX());
                y2 = parseCoordinate(args[5], loc.getY());
                z2 = parseCoordinate(args[6], loc.getZ());
            } else
                return null;
            Vector3D loc1 = new Vector3D(x1, y1, z1);
            Vector3D loc2 = new Vector3D(x2, y2, z2);
            return CuboidBankRegion.of(loc.getWorld(), loc1, loc2);
        }

        private static int parseCoordinate(String arg, int relativeTo) throws IntegerParseException {
            return arg.startsWith("~") ? Parser.parseInt(arg.substring(1)) + relativeTo : Parser.parseInt(arg);
        }

    }

}
