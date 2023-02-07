package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.type.ConfigurationCollection;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A non-empty list of positive integers. Defaults to [1]
 */
public class InterestMultipliers extends ConfigurationCollection<Integer, List<Integer>> implements BankPolicy<List<Integer>> {

    private final AllowOverride allowOverride;

    public InterestMultipliers(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("interest-multipliers"), Collections.singletonList(1));
        this.allowOverride = new AllowOverride(plugin, "interest-multipliers");
    }

    @Override
    public List<Integer> createCollection() {
        return new ArrayList<>();
    }

    @Override
    public Integer parseElement(String input) throws ArgumentParseException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(input));
        }
    }

    @Override
    protected Integer convertElement(Object o) throws ValueOutOfBoundsException, UnreadableValueException {
        if (o instanceof Integer)
            return (Integer) o;
        if (o instanceof Number)
            throw new ValueOutOfBoundsException(((Number) o).intValue());
        try {
            throw new ValueOutOfBoundsException(Integer.parseInt(o.toString()));
        } catch (NumberFormatException e) {
            throw new UnreadableValueException();
        }
    }

    @Override
    protected Bound<List<Integer>> getBound() {
        return Bound.disallowing(List::isEmpty, list -> Collections.singletonList(1));
    }

    @Override
    protected Bound<Integer> getElementBound() {
        return Bound.requiring(i -> i >= 0, Math::abs);
    }

    @Override
    public List<Integer> at(Bank bank) {
        if (bank.getInterestMultipliers() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setInterestMultipliers(get());
            return get();
        }
        return allowOverride.get() ? bank.getInterestMultipliers() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setInterestMultipliers(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setInterestMultipliers(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getInterestMultipliers()).orElseGet(this));
    }
    
    @Override
    public AllowOverride getAllowOverride() {
        return allowOverride;
    }
    
}
