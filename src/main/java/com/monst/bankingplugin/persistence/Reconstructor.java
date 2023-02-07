package com.monst.bankingplugin.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface Reconstructor<T> {
    
    T reconstruct(ResultSet resultSet, Connection con) throws SQLException;
    
}
