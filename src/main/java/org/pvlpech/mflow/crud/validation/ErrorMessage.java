package org.pvlpech.mflow.crud.validation;

public enum ErrorMessage {

    NO_USER_NAME   ("No user name in the request", 422),
    FILLED_USER_ID ("User id shouldn't be filled", 422),
    NO_USER        ("No user in the request", 422);

    private String message;
    private int status;

    ErrorMessage(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
