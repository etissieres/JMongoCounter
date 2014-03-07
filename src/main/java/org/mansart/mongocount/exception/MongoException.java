package org.mansart.mongocount.exception;

public final class MongoException extends Exception {

    public MongoException(String message) {
        super(message);
    }

    public MongoException(String message, Throwable cause) {
        super(message, cause);
    }
}
