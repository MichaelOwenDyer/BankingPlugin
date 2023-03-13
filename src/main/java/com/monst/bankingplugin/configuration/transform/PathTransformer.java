package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTransformer implements Transformer<Path> {
    
    @Override
    public Path parse(String input) throws ArgumentParseException {
        try {
            return Paths.get(input);
        } catch (InvalidPathException e) {
            throw new ArgumentParseException(Message.NOT_A_FILENAME.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    public Object toYaml(Path value) {
        return value.toString();
    }
    
    public Transformer<Path> fileType(String extension) {
        return bounded(Bound.requiring(path -> path.toString().endsWith(extension),
                path -> path.resolveSibling(path.getFileName() + extension)));
    }
    
}
