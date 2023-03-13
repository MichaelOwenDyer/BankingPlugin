package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.event.EventCancelledException;
import com.monst.bankingplugin.command.CommandExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.command.CommandSender;

public class BPDonate extends SubCommand {

    public BPDonate(BankingPlugin plugin) {
        super(plugin, "donate");
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_DONATE;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException, EventCancelledException {
        sender.sendMessage(Message.CLICK_TO_DONATE.with(Placeholder.URL).as("paypal.me/BankingPlugin").translate(plugin));
    }

}
