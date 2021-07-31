package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.utils.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A non-empty list of positive integers. Defaults to { 1 }
 */
public class Multipliers extends OverridableValue<List<String>, List<Integer>> implements ConfigCollection<Integer, List<Integer>> {

    public Multipliers(BankingPlugin plugin) {
        super(plugin, "interest-multipliers", Collections.singletonList(1));
    }

    @Override
    public List<Integer> getEmptyCollection() {
        return new ArrayList<>();
    }

    @Override
    public void ensureValid(List<Integer> multipliers) throws InvalidValueException {
        if (multipliers.isEmpty())
            throw new InvalidValueException(Collections.singletonList(1));
    }

    @Override
    public Integer parseSingle(String input) throws IntegerParseException {
        return Parser.parseInt(input);
    }

    @Override
    public void ensureValidSingle(Integer i) throws InvalidValueException {
        if (i < 0)
            throw new InvalidValueException(Math.abs(i));
    }

    @Override
    public Object convertToStorableType(List<Integer> multipliers) {
        return multipliers; // No conversion necessary to save to file
    }

}
