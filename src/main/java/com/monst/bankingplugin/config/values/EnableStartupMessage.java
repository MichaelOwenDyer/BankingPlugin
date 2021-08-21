package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class EnableStartupMessage extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    private final String[] STARTUP_MESSAGE = new String[] {
            ChatColor.GREEN + "   __ " + ChatColor.DARK_GREEN + "  __",
            ChatColor.GREEN + "  |__)" + ChatColor.DARK_GREEN + " |__)" + ChatColor.DARK_GREEN + "   BankingPlugin" + ChatColor.AQUA + " v" + plugin.getDescription().getVersion(),
            ChatColor.GREEN + "  |__)" + ChatColor.DARK_GREEN + " |   " + ChatColor.DARK_GRAY  + "        by monst",
            ""
    };

    public EnableStartupMessage(BankingPlugin plugin) {
        super(plugin, "enable-startup-message", true);
    }

    public void printIfEnabled() {
        if (get())
            print();
    }

    public void print() {
        Bukkit.getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);
    }

}
