package com.monst.bankingplugin.events.account;

import org.bukkit.command.CommandSender;

import java.util.Arrays;

public abstract class AccountCommandEvent extends AccountEvent {

    private final String[] args;

    public AccountCommandEvent(CommandSender executor, String[] args) {
        super(executor);
        this.args = Arrays.copyOf(args, args.length);
    }

    public String[] getArgs() {
        return args;
    }

}
