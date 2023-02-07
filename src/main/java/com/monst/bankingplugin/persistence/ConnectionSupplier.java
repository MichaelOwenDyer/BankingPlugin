package com.monst.bankingplugin.persistence;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionSupplier {
    
    Connection get() throws SQLException;
    
}
