package com.monst.bankingplugin.persistence;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Query {
    
    final String sql;
    
    Query(String sql) {
        this.sql = sql;
    }
    
    public static Query of(String sql) {
        return new Query(sql);
    }
    
    public boolean execute(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            return stmt.execute(sql);
        }
    }
    
    public int executeUpdate(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
    
    public ResultSet executeQuery(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            return stmt.executeQuery(sql);
        }
    }
    
    public <T> T asOne(Connection con, Reconstructor<T> reconstructor) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            if (!resultSet.next())
                return null;
            return reconstructor.reconstruct(resultSet, con);
        }
    }
    
    public <T> T asOne(Connection con, Class<T> clazz) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            if (!resultSet.next())
                return null;
            return resultSet.getObject(1, clazz);
        }
    }
    
    public <T> List<T> asList(Connection con, Reconstructor<T> reconstructor) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            List<T> list = new ArrayList<>();
            while (resultSet.next())
                list.add(reconstructor.reconstruct(resultSet, con));
            return list;
        }
    }
    
    public <T> List<T> asList(Connection con, Class<T> clazz) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            List<T> list = new ArrayList<>();
            while (resultSet.next())
                list.add(resultSet.getObject(1, clazz));
            return list;
        }
    }
    
    public <T, R> List<R> asList(Connection con, Class<T> clazz, Function<T, R> mapper) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            List<R> list = new ArrayList<>();
            while (resultSet.next())
                list.add(mapper.apply(resultSet.getObject(1, clazz)));
            return list;
        }
    }
    
    public <T> Set<T> asSet(Connection con, Reconstructor<T> reconstructor) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            Set<T> set = new HashSet<>();
            while (resultSet.next())
                set.add(reconstructor.reconstruct(resultSet, con));
            return set;
        }
    }
    
    public <T> Set<T> asSet(Connection con, Class<T> clazz) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            Set<T> set = new HashSet<>();
            while (resultSet.next())
                set.add(resultSet.getObject(1, clazz));
            return set;
        }
    }
    
    public <T, R> Set<R> asSet(Connection con, Class<T> clazz, Function<T, R> mapper) throws SQLException {
        try (ResultSet resultSet = executeQuery(con)) {
            Set<R> set = new HashSet<>();
            while (resultSet.next())
                set.add(mapper.apply(resultSet.getObject(1, clazz)));
            return set;
        }
    }
    
    public ParametrizedQuery with(Object param) {
        return new ParametrizedQuery(sql).and(param);
    }
    
    public ParametrizedQuery with(Object... params) {
        return new ParametrizedQuery(sql).and(params);
    }
    
    public ParametrizedQuery with(Iterable<?> params) {
        return new ParametrizedQuery(sql).and(params);
    }
    
    public ParametrizedQuery in(Collection<?> params) {
        String formattedSQL = String.format(sql, params.stream().map(o -> "?").collect(Collectors.joining(",")));
        return new ParametrizedQuery(formattedSQL).and(params);
    }
    
    public <T> ParametrizedQuery in(Collection<T> params, Function<T, Object> valueExtractor) {
        String formattedSQL = String.format(sql, params.stream().map(o -> "?").collect(Collectors.joining(",")));
        return new ParametrizedQuery(formattedSQL).and(params.stream().map(valueExtractor).collect(Collectors.toList()));
    }
    
    public <T> BatchBuilder<T> batch(Collection<T> elements) {
        return new BatchBuilder<>(elements);
    }
    
    public class BatchBuilder<T> {
        
        private final Batch<T> batch;
        
        private BatchBuilder(Collection<T> elements) {
            this.batch = new Batch<>(elements);
        }
        
        public Batch<T> with(Function<T, List<Object>> deconstructor) {
            batch.deconstructors.add(deconstructor);
            return batch;
        }
        
    }
    
    public class Batch<T> {
        
        private final Collection<T> elements;
        private final List<Function<T, List<Object>>> deconstructors;
        
        private Batch(Collection<T> elements) {
            this.elements = elements;
            this.deconstructors = new ArrayList<>(2);
        }
        
        public Batch<T> and(Function<T, Object> paramExtractor) {
            deconstructors.add(element -> Collections.singletonList(paramExtractor.apply(element)));
            return this;
        }
        
        public int executeUpdate(Connection con) throws SQLException {
            try (PreparedStatement stmt = prepare(con)) {
                return stmt.executeBatch().length;
            }
        }
        
        private PreparedStatement prepare(Connection con) throws SQLException {
            PreparedStatement stmt = con.prepareStatement(sql);
            for (T element : elements) {
                int paramIndex = 1;
                for (Function<T, List<Object>> deconstructor : deconstructors) {
                    for (Object param : deconstructor.apply(element))
                        stmt.setObject(paramIndex++, param);
                }
                stmt.addBatch();
            }
            return stmt;
        }
        
    }
    
}
