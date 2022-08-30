package com.prokopchuk.orm;

import com.prokopchuk.orm.annotation.Table;
import com.prokopchuk.orm.demo.entity.Product;
import com.prokopchuk.orm.exception.OrmException;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Session implements Closeable {

    private final Connection connection;

    private Map<EntityKey, Object> cache = new HashMap<>();

    @SneakyThrows
    public Session(DataSource dataSource) {
        connection = dataSource.getConnection();
    }

    @SneakyThrows
    public <T> T find(Class<T> entityType, Object id) {
        if (connection.isClosed()) {
            throw new OrmException("Session is already closed");
        }

        EntityKey entityKey = new EntityKey(entityType, id);

        if (cache.containsKey(entityKey)) {
            return (T) cache.get(entityKey);
        }

        var entity = loadFromDb(entityType, id);

        cache.put(entityKey, entity);

        return entity;
    }

    private <T> T loadFromDb(Class<T> entityType, Object id) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var tableName = EntityUtil.resolveTableName(entityType);

        var query = String.format("SELECT *  FROM %s WHERE id = ?", tableName);

        var ps = connection.prepareStatement(query);
        System.out.println(query);

        ps.setObject(1, id);
        var rs = ps.executeQuery();

        return EntityUtil.extractEntityFromResultSet(entityType, rs);
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private record EntityKey(Class<?> type, Object id) {

    }
}
