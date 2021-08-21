package com.monst.bankingplugin.banking;

public interface Persistable {

    /**
     * @return whether this persistable entity has an ID.
     */
    boolean hasID();

    /**
     * Sets the ID of this persistable entity, if it does not have one already.
     */
    void setID(int id);

    /**
     * @return the ID of this persistable entity, or a placeholder value if it does not exist.
     */
    int getID();

}
