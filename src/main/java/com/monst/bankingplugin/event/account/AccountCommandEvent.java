package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.event.PlayerEvent;
import org.bukkit.entity.Player;

import java.util.Arrays;

public abstract class AccountCommandEvent extends AccountEvent implements PlayerEvent {

    private final String[] args;

    public AccountCommandEvent(Player player, String[] args) {
        super(player);
        this.args = Arrays.copyOf(args, args.length);
    }

    public String[] getArgs() {
        return args;
    }

}
