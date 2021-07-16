package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.utils.Parser;

import java.util.Collections;

/**
 * A non-empty list of positive integers. Defaults to { 1 }
 */
public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("interest-multipliers", Collections.singletonList(1));
    }

    @Override
    public Integer parseSingle(String input) throws IntegerParseException {
        return Math.abs(Parser.parseInt(input));
    }

}
