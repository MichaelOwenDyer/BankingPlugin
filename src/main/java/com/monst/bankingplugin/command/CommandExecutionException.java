package com.monst.bankingplugin.command;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Translatable;

public class CommandExecutionException extends Exception {

    private final String translatedMessage;

    public CommandExecutionException(BankingPlugin plugin, Translatable message) {
        super(message.inEnglish());
        this.translatedMessage = message.translate(plugin);
    }

    @Override
    public String getLocalizedMessage() {
        return translatedMessage;
    }

}
