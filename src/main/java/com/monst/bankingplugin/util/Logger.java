package com.monst.bankingplugin.util;

import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public interface Logger {
    
    void log(String message);
    
    void log(Throwable throwable);
    
    default void close() {}
    
    Logger NO_OP = new Logger() {
        @Override
        public void log(String message) {}
        
        @Override
        public void log(Throwable throwable) {}
    };
    
    static Logger printingTo(PrintWriter out) {
        return new Logger() {
            @Override
            public void log(String message) {
                out.printf("[%s] %s%n", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString(), message);
            }
            
            @Override
            public void log(Throwable throwable) {
                throwable.printStackTrace(out);
                out.flush();
            }
    
            @Override
            public void close() {
                out.close();
            }
        };
    }
    
}
