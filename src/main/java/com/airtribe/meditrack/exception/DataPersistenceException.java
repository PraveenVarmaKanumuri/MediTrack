package com.airtribe.meditrack.exception;

public class DataPersistenceException extends Exception {

    private final String operation;

    public DataPersistenceException(String operation, String message) {
        super("Data operation '" + operation + "' failed: " + message);
        this.operation = operation;
    }

    public DataPersistenceException(String operation, String message, Throwable cause) {
        super("Data operation '" + operation + "' failed: " + message, cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}