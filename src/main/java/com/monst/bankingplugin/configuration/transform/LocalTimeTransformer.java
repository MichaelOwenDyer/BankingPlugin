package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class LocalTimeTransformer implements Transformer<LocalTime> {
    
    @Override
    public LocalTime parse(String input) throws ArgumentParseException {
        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            throw new ArgumentParseException(Message.NOT_A_TIME.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    public Object toYaml(LocalTime value) {
        return value.toString();
    }
    
}
