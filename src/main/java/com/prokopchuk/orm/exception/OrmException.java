package com.prokopchuk.orm.exception;

import lombok.NoArgsConstructor;

public class OrmException extends RuntimeException {
    public OrmException(String message) {
        super(message);
    }
}
