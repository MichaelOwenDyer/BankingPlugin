package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.Utils;

public class LocalizedMessage {

    private final Message message;
    private final String localizedString;

    public LocalizedMessage(Message message, String localizedString) {
        this.message = message;
        this.localizedString = Utils.colorize(localizedString);
    }

    /**
     * @return the {@link Message} enum that represents this object
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @return the localized content of the message
     */
    public String getLocalizedString() {
        return localizedString;
    }

}
