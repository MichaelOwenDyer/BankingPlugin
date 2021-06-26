package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;

public interface Nameable {

    /**
     * @return the name of this nameable, stripped of all colors
     * @see ChatColor#stripColor(String)
     * @see ChatColor#stripColor(String)
     */
    default String getName() {
        return ChatColor.stripColor(Utils.colorize(getRawName()));
    }

    /**
     * @return the name of this nameable, including color codes
     */
    String getRawName();

    /**
     * @return the colorized name of this nameable
     * @see Utils#colorize(String)
     */
    default String getColorizedName() {
        return Utils.colorize(getRawName());
    }

    /**
     * Sets the raw name of this nameable.
     *
     * @param name The new raw name of this nameable
     */
    void setName(String name);

}
