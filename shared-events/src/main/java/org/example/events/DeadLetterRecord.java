package org.example.events;

public class DeadLetterRecord<T> {
    private T payload;
    private String errorType;
    private String errorMessage;
    private String stackTrace; // можно оставить пустым если не нужен полный трейс

    public DeadLetterRecord() {
    }

    public DeadLetterRecord(T payload, String errorType, String errorMessage, String stackTrace) {
        this.payload = payload;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {
        return "DeadLetterRecord{" +
                "payload=" + payload +
                ", errorType='" + errorType + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                '}';
    }
}