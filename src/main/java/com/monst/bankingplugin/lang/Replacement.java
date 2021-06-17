package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.utils.HasOrCanGet;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.Utils;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class Replacement extends Pair<Placeholder, HasOrCanGet<Object>> {

    public Replacement(@Nonnull Placeholder placeholder, @Nonnull Object replacement) {
        super(placeholder, new HasOrCanGet<>(replacement));
    }

    public Replacement(@Nonnull Placeholder placeholder, @Nonnull Supplier<Object> replacement) {
        super(placeholder, new HasOrCanGet<>(replacement));
    }

    /**
     * @return String which will replace the placeholder
     */
    public String getReplacement() {
        if (getFirst().isMoney())
            return Utils.format(Double.parseDouble(String.valueOf(getSecond().get())));
        return String.valueOf(getSecond().get());
    }

    /**
     * @return Placeholder that will be replaced
     */
    public Placeholder getPlaceholder() {
        return getFirst();
    }

}
