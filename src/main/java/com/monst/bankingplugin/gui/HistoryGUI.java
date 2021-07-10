package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.sql.Database;
import com.monst.bankingplugin.sql.logging.Receipt;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Observable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class HistoryGUI<R extends Receipt> extends MultiPageGUI<R> {

    private static final Comparator<Receipt> BY_TIME = Comparator.comparing(Receipt::getTime);
    private static final List<MenuItemSorter<Receipt>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Newest", BY_TIME.reversed()),
            MenuItemSorter.of("Oldest", BY_TIME)
    );
    static final Database DATABASE = BankingPlugin.getInstance().getDatabase();

    HistoryGUI(Consumer<Callback<Collection<R>>> source,
               List<MenuItemFilter<? super R>> filters, List<MenuItemSorter<? super R>> sorters) {
        super(source, filters, Stream.of(SORTERS, sorters).flatMap(List::stream).collect(Collectors.toList()));
    }

    @Nullable
    @Override
    Observable getSubject() {
        return null;
    }

}
