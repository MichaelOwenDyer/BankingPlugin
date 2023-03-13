package com.monst.bankingplugin.command;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.entity.geo.region.CuboidBankRegion;
import com.monst.bankingplugin.event.EventCancelledException;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Optional;

public abstract class PlayerSubCommand extends SubCommand {

    protected PlayerSubCommand(BankingPlugin plugin, String name) {
        super(plugin, name, true);
    }

    @Override
    protected final void execute(CommandSender sender, String[] args) throws CommandExecutionException, EventCancelledException {
        execute((Player) sender, args);
    }

    /**
     * Executes the subcommand for the given player.
     *
     * @param player The player who executed the command.
     * @param args The arguments of the command, excluding the subcommand name.
     * @throws CommandExecutionException If an error occurred while executing the command.
     * @throws EventCancelledException If the command event was cancelled.
     */
    protected abstract void execute(Player player, String[] args) throws CommandExecutionException, EventCancelledException;

    @Deprecated
    protected static long getPermissionLimit(Player player, Permissions unlimitedPerm, long defaultLimit) {
        if (unlimitedPerm.ownedBy(player))
            return -1;
        long limit = defaultLimit;
        String permPrefix = unlimitedPerm.toString();
        permPrefix = permPrefix.substring(0, permPrefix.length() - 1);
        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            if (!permInfo.getValue())
                continue;
            String[] split = permInfo.getPermission().split(permPrefix);
            if (split.length <= 1)
                continue;
            try {
                long newLimit = Long.parseLong(split[1]);
                if (newLimit < 0)
                    return -1;
                limit = Math.max(limit, newLimit);
            } catch (NumberFormatException ignored) {
                // Someone has a permission like "bankingplugin.bank.limit.abc"
            }
        }
        return Math.max(limit, -1);
    }
    
    protected Optional<Integer> getAccountLimit(Player player) {
        Optional<Integer> permissionLimit = getPermissionLimit(player, Permissions.ACCOUNT_NO_LIMIT).map(Long::intValue);
        return permissionLimit.isPresent() ? permissionLimit : plugin.config().defaultLimits.account.get();
    }
    
    protected Optional<Integer> getBankLimit(Player player) {
        Optional<Integer> permissionLimit = getPermissionLimit(player, Permissions.BANK_NO_LIMIT).map(Long::intValue);
        return permissionLimit.isPresent() ? permissionLimit : plugin.config().defaultLimits.bank.get();
    }
    
    protected Optional<Long> getBankVolumeLimit(Player player) {
        Optional<Long> permissionLimit = getPermissionLimit(player, Permissions.BANK_VOLUME_NO_LIMIT);
        return permissionLimit.isPresent() ? permissionLimit : plugin.config().bankSizeLimits.maximum.get();
    }
    
    protected static Optional<Long> getPermissionLimit(Player player, Permissions unlimitedPerm) {
        if (unlimitedPerm.ownedBy(player))
            return Optional.empty();
        String permPrefix = unlimitedPerm.toString();
        permPrefix = permPrefix.substring(0, permPrefix.length() - 1); // Remove the trailing "*"
        Optional<Long> limit = Optional.empty();
        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            if (!permInfo.getValue())
                continue;
            String[] split = permInfo.getPermission().split(permPrefix);
            if (split.length <= 1)
                continue;
            try {
                long newLimit = Long.parseLong(split[1]);
                if (newLimit < 0)
                    return Optional.empty();
                if (!limit.isPresent() || newLimit > limit.get())
                    limit = Optional.of(newLimit);
            } catch (NumberFormatException ignored) {
                // Someone has a permission like "bankingplugin.bank.limit.abc"
            }
        }
        return limit;
    }

    protected BankRegion parseBankRegion(Player player, String[] args) throws CommandExecutionException {
        if (args.length <= 3) {
            if (plugin.isWorldEditIntegrated()) {
                BankRegion region = WorldEditReader.getBankRegion(plugin, player).orElse(null);
                if (region != null)
                    return region;
            }
            Block loc = player.getLocation().getBlock();
            Block target = player.getTargetBlock(null, 300);
            return new CuboidBankRegion(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(),
                    target.getX(), target.getY(), target.getZ());
        }
        Block loc = player.getLocation().getBlock();
        Block target = player.getTargetBlock(null, 300);
        int x1, y1, z1, x2, y2, z2;
        if (args.length < 7) {
            x1 = parseRelativeCoordinate(args[1], loc.getX());
            y1 = parseRelativeCoordinate(args[2], loc.getY());
            z1 = parseRelativeCoordinate(args[3], loc.getZ());
            x2 = target.getX();
            y2 = target.getY();
            z2 = target.getZ();
        } else {
            x1 = parseRelativeCoordinate(args[1], loc.getX());
            y1 = parseRelativeCoordinate(args[2], loc.getY());
            z1 = parseRelativeCoordinate(args[3], loc.getZ());
            x2 = parseRelativeCoordinate(args[4], loc.getX());
            y2 = parseRelativeCoordinate(args[5], loc.getY());
            z2 = parseRelativeCoordinate(args[6], loc.getZ());
        }
        return new CuboidBankRegion(loc.getWorld(), x1, y1, z1, x2, y2, z2);
    }

    protected int parseRelativeCoordinate(String arg, int relativeTo) throws CommandExecutionException {
        try {
            boolean relative = arg.startsWith("~");
            if (relative)
                arg = arg.substring(1);
            int parsed = Integer.parseInt(arg);
            if (relative)
                parsed += relativeTo;
            return parsed;
        } catch (NumberFormatException e) {
            throw err(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(arg));
        }
    }

}
