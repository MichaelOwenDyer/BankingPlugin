package com.monst.bankingplugin.config.values.simple;

public class LanguageFile extends ConfigString {

    public LanguageFile() {
        super("language-file", "en_US");
    }

    @Override
    protected void afterSet() {
        PLUGIN.getLanguageConfig().reload();
    }

}
