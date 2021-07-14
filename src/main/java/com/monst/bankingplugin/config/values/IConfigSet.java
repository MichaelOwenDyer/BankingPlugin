package com.monst.bankingplugin.config.values;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface IConfigSet<T> extends IConfigCollection<T, Set<T>> {

    @Override
    default Set<T> getEmptyCollection() {
        return new HashSet<>();
    }

    @Override
    default Object convertToSettableType(Set<T> set) {
        return set.stream().map(String::valueOf).collect(Collectors.toList()); // must convert to List<String> in order to set
    }

}
