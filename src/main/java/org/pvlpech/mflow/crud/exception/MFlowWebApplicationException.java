package org.pvlpech.mflow.crud.exception;

import jakarta.ws.rs.WebApplicationException;

public class MFlowWebApplicationException extends WebApplicationException {

    public MFlowWebApplicationException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage(), errorMessage.getStatus());
    }
}
