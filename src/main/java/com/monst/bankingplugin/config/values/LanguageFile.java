package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.PathParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageFile extends ConfigValue<String, Path> implements NonNativeString<Path> {

    public LanguageFile(BankingPlugin plugin) {
        super(plugin, "language-file", Paths.get("en_US.lang"));
    }

    @Override
    public Path parse(String input) throws PathParseException {
        return Parser.parsePath(input, ".lang");
    }

    @Override
    public void afterSet(CommandSender executor) {
        plugin.reloadLanguageConfig();
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        Stream.Builder<String> tabCompletions = Stream.builder();
        tabCompletions.accept(getFormatted());
        tabCompletions.accept("en_US.lang");
        Path langFolder = plugin.getDataFolder().toPath().resolve("lang");
        try {
            Files.walk(langFolder)
                    .filter(Files::isRegularFile)
                    .map(langFolder::relativize)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".lang"))
                    .forEach(tabCompletions);
        } catch (IOException ignored) {}
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

}
