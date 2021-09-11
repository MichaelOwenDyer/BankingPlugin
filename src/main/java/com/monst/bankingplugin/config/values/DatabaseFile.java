package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.PathParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Callback;
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

public class DatabaseFile extends ConfigValue<String, Path> implements NonNativeString<Path> {

    public DatabaseFile(BankingPlugin plugin) {
        super(plugin, "database-file", Paths.get("banking.db"));
    }

    @Override
    public Path parse(String input) throws PathParseException {
        return Parser.parsePath(input, ".db");
    }

    @Override
    public void afterSet(CommandSender executor) {
        plugin.reloadEntities(Callback.onResult(banksAndAccounts -> {
            int numberOfBanks = banksAndAccounts.size();
            int numberOfAccounts = banksAndAccounts.values().size();
            executor.sendMessage(Message.RELOADED_PLUGIN
                    .with(Placeholder.NUMBER_OF_BANKS).as(numberOfBanks)
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(numberOfAccounts)
                    .translate());
        }));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        Stream.Builder<String> tabCompletions = Stream.builder();
        tabCompletions.accept(getFormatted());
        tabCompletions.accept("banking.db");
        Path databaseFolder = plugin.getDataFolder().toPath().resolve("database");
        try {
            Files.walk(databaseFolder)
                    .filter(Files::isRegularFile)
                    .map(databaseFolder::relativize)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".db"))
                    .forEach(tabCompletions);
        } catch (IOException ignored) {}
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

}
