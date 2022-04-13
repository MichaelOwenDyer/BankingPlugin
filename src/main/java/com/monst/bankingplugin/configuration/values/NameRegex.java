package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.pluginconfiguration.ConfigurationValue;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NameRegex extends ConfigurationValue<Pattern> {

    private final BankingPlugin plugin;

    public NameRegex(BankingPlugin plugin) {
        super(plugin, "name-regex", Pattern.compile(".*"));
        this.plugin = plugin;
    }

    @Override
    public Pattern parse(String input) throws ArgumentParseException {
        try {
            return Pattern.compile(input);
        } catch (PatternSyntaxException e) {
            throw new ArgumentParseException(Message.NOT_A_REGULAR_EXPRESSION.with(Placeholder.INPUT).as(input).translate(plugin));
        }
    }

    public boolean doesNotMatch(String name) {
        return !get().matcher(name).matches();
    }

}
