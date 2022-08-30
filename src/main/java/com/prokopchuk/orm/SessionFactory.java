package com.prokopchuk.orm;

import lombok.AllArgsConstructor;

import javax.sql.DataSource;

@AllArgsConstructor
public class SessionFactory {
    private final DataSource dataSource;

    public Session creatSession() {
        return new Session(dataSource);
    }
}
