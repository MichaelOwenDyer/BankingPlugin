package com.monst.bankingplugin.exception;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;

public class ExecutionException extends Exception {

    private final String translatedMessage;

    public ExecutionException(String message) {
        super(message);
        this.translatedMessage = message;
    }

    public ExecutionException(BankingPlugin plugin, Message message) {
        super(message.inEnglish());
        this.translatedMessage = message.translate(plugin);
    }

    public ExecutionException(BankingPlugin plugin, Message.ValuedMessage message) {
        super(message.inEnglish());
        this.translatedMessage = message.translate(plugin);
    }

    @Override
    public String getLocalizedMessage() {
        return translatedMessage;
    }

}
