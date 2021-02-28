package com.monst.bankingplugin.banking;

public interface Persistable<ID> {

    /**
     * @return whether or not this persistable entity has an ID.
     */
    boolean hasID();

    /**
     * Sets the ID of this persistable entity, if it does not have one already.
     */
    void setID(ID id);

    /**
     * @return the ID of this persistable entity, or a placeholder value if it does not exist.
     */
    ID getID();

}
