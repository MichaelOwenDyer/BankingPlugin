package com.monst.bankingplugin.utils;

public interface Nameable {

    /**
     * @return The name stripped of all colors.
     */
    public default String getName() {
        return Utils.stripColor(getRawName());
    }

    /**
     * @return The raw string representing the name, including color codes.
     */
    public String getRawName();

    /**
     * @return The colorized name.
     */
    public default String getColorizedName() {
        return Utils.colorize(getRawName());
    }

    /**
     * Set the name of this object.
     * @param name The new raw name of this object.
     */
    public void setName(String name);

    /**
     * @return Whether the object is currently using its default name.
     */
    public default boolean isDefaultName() {
        return getRawName().contentEquals(getDefaultName());
    }

    /**
     * @return The default name of this object.
     */
    public String getDefaultName();

    /**
     * Set the name of this object to its default.
     * @see this.getDefaultName()
     */
    public void resetName();

}
