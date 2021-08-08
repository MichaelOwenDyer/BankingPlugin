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
        if (placeholder.isMoney() && replacement instanceof Number)
            return Utils.format(((Number) replacement).doubleValue());
        return String.valueOf(replacement);
    }

}
