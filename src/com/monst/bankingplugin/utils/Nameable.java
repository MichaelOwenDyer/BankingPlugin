package com.monst.bankingplugin.utils;

public interface Nameable {

    /**
     * @return The name stripped of all colors.
     */
    default String getName() {
        return Utils.stripColor(getRawName());
    }

    /**
     * @return The raw string representing the name, including color codes.
     */
    String getRawName();

    /**
     * @return The colorized name.
     */
    default String getColorizedName() {
        return Utils.colorize(getRawName());
    }

    /**
     * Set the name of this object.
     * @param name The new raw name of this object.
     */
    void setName(String name);

    /**
     * @return Whether the object is currently using its default name.
     */
    default boolean isDefaultName() {
        return getRawName().contentEquals(getDefaultName());
    }

    /**
     * @return The default name of this object.
     */
    String getDefaultName();

    /**
     * Set the name of this object to its default.
     * @see this.getDefaultName()
     */
    void setToDefaultName();

}
