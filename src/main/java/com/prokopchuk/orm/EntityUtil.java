package com.prokopchuk.orm;

import com.prokopchuk.orm.annotation.Column;
import com.prokopchuk.orm.annotation.Table;
import com.prokopchuk.orm.exception.OrmException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityUtil {

    public static String resolveTableName(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new OrmException("Table name is not specified for entity class " + entityClass.getSimpleName());
        }

        return entityClass.getAnnotation(Table.class).name();
    }

    public static <T> T extractEntityFromResultSet(Class<T> entityType, ResultSet rs) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        rs.next();

        Field[] declaredFields = entityType.getDeclaredFields();

        final T entity = entityType.getConstructor().newInstance();

        for(Field field : declaredFields) {
            if (!field.isAnnotationPresent(Column.class)) {
                throw new OrmException("Field is not marked with @Column");
            }
            field.setAccessible(true);
            String columnName = field.getAnnotation(Column.class).name();
            Object fieldValue = rs.getObject(columnName);
            field.set(entity, fieldValue);
        }

        return entity;
    }
}
