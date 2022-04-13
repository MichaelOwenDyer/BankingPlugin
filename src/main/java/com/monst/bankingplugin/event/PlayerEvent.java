package com.monst.bankingplugin.event;

import org.bukkit.entity.Player;

public interface PlayerEvent extends CommandSenderEvent {

    default Player getPlayer() {
        return (Player) getExecutor();
    }

}
