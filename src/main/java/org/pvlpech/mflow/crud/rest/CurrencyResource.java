package org.pvlpech.mflow.crud.rest;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.pvlpech.mflow.crud.model.Currency;
import org.pvlpech.mflow.crud.service.CurrencyService;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/v1/currencies")
@ApplicationScoped
@Tag(name = "currencies")
@Produces(APPLICATION_JSON)
public class CurrencyResource {

    @Inject
    CurrencyService currencyService;

    @GET
    @Operation(summary = "Returns all the currencies from the database")
    @APIResponse(
        responseCode = "200",
        description = "Gets all currencies",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = Currency.class, type = SchemaType.ARRAY),
            examples = @ExampleObject(name = "currencies", value = Examples.VALID_EXAMPLE_CURRENCY_LIST)
        )
    )
    public Uni<List<Currency>> getAll() {
        return currencyService.getAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Returns a currency for a given identifier")
    @APIResponse(
        responseCode = "200",
        description = "Gets a currency for a given id",
        content = @Content(
            mediaType = APPLICATION_JSON, schema = @Schema(implementation = Currency.class),
            examples = @ExampleObject(name = "currency", value = Examples.VALID_EXAMPLE_CURRENCY)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "The currency is not found for a given identifier"
    )
    public Uni<Response> get(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return currencyService.get(id)
            .onItem().ifNotNull().transform(currency -> Response.ok(currency).build())
            .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

}
