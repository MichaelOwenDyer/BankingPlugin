package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.exceptions.parse.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Supplier;

public class Parser {

    public static int parseInt(String s) throws IntegerParseException {
        return wrapRuntimeException(() -> Integer.parseInt(s), () -> new IntegerParseException(s));
    }

    public static double parseDouble(String s) throws DoubleParseException {
        return wrapRuntimeException(() -> Double.parseDouble(s), () -> new DoubleParseException(s));
    }

    public static boolean parseBoolean(String s) throws BooleanParseException {
        if (s != null) {
            if (s.equalsIgnoreCase("true"))
                return true;
            if (s.equalsIgnoreCase("false"))
                return false;
        }
        throw new BooleanParseException(s);
    }

    public static LocalTime parseLocalTime(String time) throws TimeParseException {
        return wrapRuntimeException(() -> LocalTime.parse(time), () -> new TimeParseException(time));
    }

    public static World parseWorld(String name) throws WorldParseException {
        return Optional.ofNullable(name).map(Bukkit::getWorld).orElseThrow(() -> new WorldParseException(name));
    }

    public static Material parseMaterial(String name) throws MaterialParseException {
        return Optional.ofNullable(name).map(Material::matchMaterial).orElseThrow(() -> new MaterialParseException(name));
    }

    private static <T, E extends ArgumentParseException> T wrapRuntimeException(Supplier<T> supplier, Supplier<E> exceptionSupplier) throws E {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            throw exceptionSupplier.get();
        }
    }

}
