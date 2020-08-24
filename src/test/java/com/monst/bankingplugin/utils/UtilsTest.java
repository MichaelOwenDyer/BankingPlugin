package com.monst.bankingplugin.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class UtilsTest {

    private final List<String> list = Arrays.asList("Hello", "nice", "to", "meet", "you");

    @Test
    void filter() {
        List<String> filtered = Utils.filter(list, s -> s.contains("i") || s.contains("e"));
        System.out.println(filtered);
    }

    @Test
    void map() {
        List<Integer> mapped = Utils.map(list, String::length);
        System.out.println(mapped);
    }
}