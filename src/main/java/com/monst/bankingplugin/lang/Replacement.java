package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.Utils;

class Replacement {

    private final Placeholder placeholder;
    private final Object replacement;

    Replacement(Placeholder placeholder, Object replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    /**
     * @return Placeholder that will be replaced
     */
    public Placeholder getPlaceholder() {
        return placeholder;
    }

    /**
     * @return String which will replace the placeholder
     */
    public String getReplacement() {
        if (placeholder.isMoney())
            return Utils.format(Double.parseDouble(getFormatted()));
        return getFormatted();
    }

    private String getFormatted() {
        return String.valueOf(replacement);
    }

}
