package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface IConfigSet<T> extends IConfigCollection<T, Set<T>> {

    @Override
    default Set<T> getEmptyCollection() {
        return new HashSet<>();
    }

    @Override
    default Object convertToConfigType(Set<T> set) {
        return Utils.map(set, String::valueOf, Collectors.toList()); // must convert to List<String> in order to set
    }

}
