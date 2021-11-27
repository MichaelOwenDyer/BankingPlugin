package com.monst.bankingplugin.events;

import org.bukkit.entity.Player;

public interface PlayerEvent extends CommandSenderEvent {

    default Player getPlayer() {
        return (Player) getExecutor();
    }

}
