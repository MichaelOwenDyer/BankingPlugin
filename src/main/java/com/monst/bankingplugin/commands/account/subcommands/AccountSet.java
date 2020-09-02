package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountSet extends AccountSubCommand {

    public AccountSet() {
        super("set", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_SET) ? Messages.COMMAND_USAGE_ACCOUNT_SET : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to configure an account");
        if (args.length < 2)
            return false;

        switch (args[1].toLowerCase()) {

            case "nickname":
                if (!p.hasPermission(Permissions.ACCOUNT_CREATE)) {
                    p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME);
                    return true;
                }
                StringBuilder sb = new StringBuilder(32);
                if (args.length >= 3)
                    sb.append(args[2]);
                for (int i = 3; i < args.length; i++)
                    sb.append(" ").append(args[i]);
                String nickname = sb.toString();

                if (!nickname.trim().isEmpty() && !Utils.isAllowedName(nickname)) {
                    plugin.debug("Name \"" + nickname + "\" is not allowed");
                    p.sendMessage(Messages.NAME_NOT_ALLOWED);
                    return true;
                }
                ClickType.setPlayerClickType(p,
                        new ClickType.SetClickType(ClickType.SetClickType.SetField.NICKNAME, nickname));
                p.sendMessage(Messages.CLICK_CHEST_SET);
                break;

            case "multiplier":
                if (args.length < 3)
                    return false;
                if (!p.hasPermission(Permissions.ACCOUNT_SET_MULTIPLIER)) {
                    p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
                    return true;
                }

                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
                    return true;
                }

                ClickType.setPlayerClickType(p,
                        new ClickType.SetClickType(ClickType.SetClickType.SetField.MULTIPLIER, args[2]));
                p.sendMessage(Messages.CLICK_CHEST_SET);
                break;

            case "interest-delay":
                if (args.length < 3)
                    return false;
                if (p.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
                    p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
                    return true;
                }

                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
                }

                ClickType.setPlayerClickType(p,
                        new ClickType.SetClickType(ClickType.SetClickType.SetField.DELAY, args[2]));
                p.sendMessage(Messages.CLICK_CHEST_SET);
                break;

            default:
                p.sendMessage(String.format(Messages.NOT_A_FIELD, args[1]));
        }
        return true;
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> fields = Arrays.asList("nickname", "multiplier", "interest-delay");
        if (args.length == 2)
            return Utils.filter(fields, field -> field.startsWith(args[1].toLowerCase()));
        return Collections.emptyList();
    }

}
