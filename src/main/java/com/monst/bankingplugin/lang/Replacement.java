package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.BankingPlugin;

public class Replacement {

    private final Placeholder placeholder;
    private final String replacement;

    public Replacement(Placeholder placeholder, Object replacement) {
        this.placeholder = placeholder;
        this.replacement = String.valueOf(replacement);
    }

    /**
     * @return String which will replace the placeholder
     */
    public String getReplacement() {
        if (placeholder.isMoney())
            return BankingPlugin.getInstance().getEconomy().format(Double.parseDouble(replacement));
        return replacement;
    }

    /**
     * @return Placeholder that will be replaced
     */
    public Placeholder getPlaceholder() {
        return placeholder;
    }

}
