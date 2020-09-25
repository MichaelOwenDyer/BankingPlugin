package com.monst.bankingplugin.utils;

public interface Nameable {

    /**
     * @return the name of this nameable, stripped of all colors
     * @see Utils#stripColor(String)
     */
    default String getName() {
        return Utils.stripColor(getRawName());
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
