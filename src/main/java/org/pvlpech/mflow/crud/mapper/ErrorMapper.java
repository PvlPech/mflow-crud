package org.pvlpech.mflow.crud.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.CompositeException;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.quarkus.logging.Log;

/**
 * Create a HTTP response from an exception.
 *
 * Response Example:
 *
 * <pre>
 * HTTP/1.1 422 Unprocessable Entity
 * Content-Length: 111
 * Content-Type: application/json
 *
 * {
 *     "code": 422,
 *     "error": "Person name was not set on request.",
 *     "exceptionType": "jakarta.ws.rs.WebApplicationException"
 * }
 * </pre>
 */

@Provider
public class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
        Log.error("Failed to handle request", exception);

        Throwable throwable = exception;

        int code = 500;
        if (throwable instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        }

        // This is a Mutiny exception and it happens, for example, when we try to insert a new
        // person but the name is already in the database
        if (throwable instanceof CompositeException) {
            throwable = ((CompositeException) throwable).getCause();
        }

        ObjectNode exceptionJson = objectMapper.createObjectNode();
        exceptionJson.put("exceptionType", throwable.getClass().getName());
        exceptionJson.put("code", code);

        if (exception.getMessage() != null) {
            exceptionJson.put("error", throwable.getMessage());
        }

        return Response.status(code)
                .entity(exceptionJson)
                .build();
    }

}