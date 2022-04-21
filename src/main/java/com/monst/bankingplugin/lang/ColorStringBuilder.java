package com.monst.bankingplugin.lang;

import org.bukkit.ChatColor;

import static org.bukkit.ChatColor.*;

public class ColorStringBuilder {

    private final StringBuilder builder;

    public ColorStringBuilder() {
        this.builder = new StringBuilder(32);
    }

    public ColorStringBuilder(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    public ColorStringBuilder black(Object... objects) {
        append(BLACK, objects);
        return this;
    }

    public ColorStringBuilder darkBlue(Object... objects) {
        append(DARK_BLUE, objects);
        return this;
    }

    public ColorStringBuilder darkGreen(Object... objects) {
        append(DARK_GREEN, objects);
        return this;
    }

    public ColorStringBuilder darkAqua(Object... objects) {
        append(DARK_AQUA, objects);
        return this;
    }

    public ColorStringBuilder darkRed(Object... objects) {
        append(DARK_RED, objects);
        return this;
    }

    public ColorStringBuilder darkPurple(Object... objects) {
        append(DARK_PURPLE, objects);
        return this;
    }

    public ColorStringBuilder gold(Object... objects) {
        append(GOLD, objects);
        return this;
    }

    public ColorStringBuilder gray(Object... objects) {
        append(GRAY, objects);
        return this;
    }

    public ColorStringBuilder darkGray(Object... objects) {
        append(DARK_GRAY, objects);
        return this;
    }

    public ColorStringBuilder blue(Object... objects) {
        append(BLUE, objects);
        return this;
    }

    public ColorStringBuilder green(Object... objects) {
        append(GREEN, objects);
        return this;
    }

    public ColorStringBuilder aqua(Object... objects) {
        append(AQUA, objects);
        return this;
    }

    public ColorStringBuilder red(Object... objects) {
        append(RED, objects);
        return this;
    }

    public ColorStringBuilder lightPurple(Object... objects) {
        append(LIGHT_PURPLE, objects);
        return this;
    }

    public ColorStringBuilder yellow(Object... objects) {
        append(YELLOW, objects);
        return this;
    }

    public ColorStringBuilder white(Object... objects) {
        append(WHITE, objects);
        return this;
    }

    public ColorStringBuilder magic(Object... objects) {
        append(MAGIC, objects);
        return this;
    }

    public ColorStringBuilder bold(Object... objects) {
        append(BOLD, objects);
        return this;
    }

    public ColorStringBuilder strikethrough(Object... objects) {
        append(STRIKETHROUGH, objects);
        return this;
    }

    public ColorStringBuilder underline(Object... objects) {
        append(UNDERLINE, objects);
        return this;
    }

    public ColorStringBuilder italic(Object... objects) {
        append(ITALIC, objects);
        return this;
    }

    public ColorStringBuilder reset(Object... objects) {
        append(RESET, objects);
        return this;
    }

    private void append(ChatColor color, Object... objects) {
        if (objects.length == 0)
            builder.append(color);
        for (Object obj : objects)
            builder.append(color).append(obj);
    }

    public ColorStringBuilder append(Object... objects) {
        for (Object obj : objects)
            builder.append(obj);
        return this;
    }

    public String toString() {
        return builder.toString();
    }

}
