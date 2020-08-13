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
     * Set the raw name of this nameable.
     * @param name The new raw name of this nameable
     */
    void setName(String name);

    /**
     * @return whether the nameable is currently using its default name
     */
    default boolean isDefaultName() {
        return getRawName().contentEquals(getDefaultName());
    }

    /**
     * @return the default name of this nameable
     */
    String getDefaultName();

    /**
     * Set the name of this nameable to its default.
     * @see #getDefaultName()
     */
    void setToDefaultName();

    /**
     * Ensure that the current name is valid and currently being reflected everywhere it should be.
     * If it is null, make it the default name.
     */
    default void updateName() {
        setName(getRawName() != null ? getRawName() : getDefaultName());
    }

}
