package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.Utils;

import java.util.function.Supplier;

public class Replacement {

    private final Placeholder placeholder;
    private final Supplier<Object> replacement;

    public Replacement(Placeholder placeholder, Object replacement) {
        this.placeholder = placeholder;
        this.replacement = () -> replacement;
    }

    public Replacement(Placeholder placeholder, Supplier<Object> replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    /**
     * @return String which will replace the placeholder
     */
    public String getReplacement() {
        if (placeholder.isMoney())
            return Utils.format(Double.parseDouble(String.valueOf(replacement.get())));
        return String.valueOf(replacement.get());
    }

    /**
     * @return Placeholder that will be replaced
     */
    public Placeholder getPlaceholder() {
        return placeholder;
    }

}
