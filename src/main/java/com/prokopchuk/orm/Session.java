package com.prokopchuk.orm;

import com.prokopchuk.orm.annotation.Column;
import com.prokopchuk.orm.annotation.Id;
import com.prokopchuk.orm.exception.OrmException;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Session implements Closeable {

    private final Connection connection;

    private final Map<EntityKey, Object> cache = new HashMap<>();

    private final Map<EntityKey, Object[]> snapshots = new HashMap<>();

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

        snapshots.put(entityKey, EntityUtil.extractFieldValuesFromEntity(entity));

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
    public void close() {
        try {
            for (var cacheEntry : cache.entrySet()) {
                var snapshot = snapshots.get(cacheEntry.getKey());
                if (!EntityUtil.compareEntityWithSnapshot(cacheEntry.getValue(), snapshot)) {
                    updateEntity(cacheEntry.getValue());
                }
            }
            connection.close();
        } catch (SQLException e) {
            throw new OrmException(e.getMessage());
        }
    }

    public void updateEntity(Object entity) throws SQLException {
        String query = String.format("update %s set %s where %s",
                EntityUtil.resolveTableName(entity.getClass()), getFieldsUpdateQueryPart(entity), getIdWhereStatementForUpdateQuery(entity));
        var statement = connection.createStatement();
        statement.executeUpdate(query);
        System.out.println(query);
    }

    @SneakyThrows
    private String getIdWhereStatementForUpdateQuery(Object entity) {
        var fields = Arrays.stream(entity.getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(Id.class)).toList();
        if (fields.size() > 1) {
            throw new OrmException("One entity field should be marked with Id");
        }

        var idField = fields.get(0);

        return createQueryPartForField(entity, idField);
    }

    @SneakyThrows
    private String createQueryPartForField(Object entity, Field field) {
        if (!field.isAnnotationPresent(Column.class)) {
            throw new OrmException("Entity field %s should be marked with Column");
        }
        field.setAccessible(true);
        var fieldValue = field.get(entity);
        var fieldName = field.getAnnotation(Column.class).name();

        fieldValue = fieldValue instanceof Number
                ? fieldValue
                : "'" + fieldValue + "'";

        return String.format("%s=%s", fieldName, fieldValue);
    }

    private String getFieldsUpdateQueryPart(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .map(f -> createQueryPartForField(entity, f))
                .collect(Collectors.joining(", "));
    }

    private record EntityKey(Class<?> type, Object id) {

    }
}
