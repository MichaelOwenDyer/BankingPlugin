package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.exceptions.parse.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Parser {

    public static int parseInt(String s) throws IntegerParseException {
        return wrapRuntimeException(() -> Integer.parseInt(s), () -> new IntegerParseException(s));
    }

    private static final NumberFormat LOCAL_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());
    public static double parseDouble(String input) throws DoubleParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number number = LOCAL_FORMAT.parse(input, parsePosition);
        if (parsePosition.getIndex() != input.length()) {
            throw new DoubleParseException(input);
        }
        return number.doubleValue();
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

    public static Pattern parsePattern(String regex) throws PatternParseException {
        return wrapRuntimeException(() -> Pattern.compile(regex), () -> new PatternParseException(regex));
    }

    public static Path parsePath(String name, String extension) throws PathParseException {
        String fileName = name.endsWith(extension) ? name : name + extension;
        return wrapRuntimeException(() -> Paths.get(fileName), () -> new PathParseException(name));
    }

    private static <T, E extends ArgumentParseException> T wrapRuntimeException(Supplier<T> supplier, Supplier<E> exceptionSupplier) throws E {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            throw exceptionSupplier.get();
        }
    }

}
