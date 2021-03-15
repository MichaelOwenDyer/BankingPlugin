package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.sql.logging.Receipt;
import com.monst.bankingplugin.utils.Observable;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public abstract class ReceiptGUI<R extends Receipt> extends MultiPageGUI<R> {

    static final Comparator<Receipt> BY_AMOUNT = Comparator.comparing(Receipt::getAmount);
    static final Comparator<Receipt> BY_TIME = Comparator.comparing(Receipt::getTime);

    ReceiptGUI(Supplier<List<R>> source, List<MenuItemFilter<? super R>> menuItemFilters, List<MenuItemSorter<? super R>> menuItemSorters) {
        super(source, menuItemFilters, menuItemSorters);
    }

    @Nullable
    @Override
    Observable getSubject() {
        return null;
    }

    @Override
    GUIType getType() {
        return null;
    }

}
