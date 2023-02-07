package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A configuration value of the type {@link Path}.
 */
public abstract class PathConfigurationValue extends ConfigurationValue<Path> {

    public PathConfigurationValue(BankingPlugin plugin, String path, Path defaultValue) {
        super(plugin, path, defaultValue);
    }

    @Override
    protected Path parse(String input) throws ArgumentParseException {
        try {
            return Paths.get(input);
        } catch (InvalidPathException e) {
            throw new ArgumentParseException(Message.NOT_A_FILENAME.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    protected Object convertToYamlType(Path path) {
        return path.toString();
    }

}
