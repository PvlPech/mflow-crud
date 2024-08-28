package org.pvlpech.mflow.crud.rest;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.pvlpech.mflow.crud.model.User;
import org.pvlpech.mflow.crud.service.UserService;

import java.net.URI;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/v1/users")
@ApplicationScoped
@Produces(APPLICATION_JSON)
@Tag(name = "users")
public class UserResource {

    @Inject
    UserService userService;

    @POST
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Creates a user")
    @APIResponse(
        responseCode = "201",
        description = "The URI of the created user",
        headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid user passed in (or no request body found)"
    )
    public Uni<Response> create(
        @RequestBody(
            name = "user",
            required = true,
            content = @Content(
                mediaType = APPLICATION_JSON,
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(name = "valid_user", value = Examples.VALID_EXAMPLE_USER_TO_CREATE)
            )
        )
        @Valid @NotNull(message = "User must not be blank") User user,
        @Context UriInfo uriInfo) {
        return this.userService.create(user)
                .onItem().ifNotNull().transform(u -> {
                    var uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(u.getId())).build();
                    return Response.created(uri).build();
                })
                .onFailure(ConstraintViolationException.class)
                .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

    @GET
    @Operation(summary = "Returns all the users from the database")
    @APIResponse(
        responseCode = "200",
        description = "Gets all users",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = User.class, type = SchemaType.ARRAY),
            examples = @ExampleObject(name = "heroes", value = Examples.VALID_EXAMPLE_USER_LIST)
        )
    )
    public Uni<List<User>> getAll() {
        return userService.getAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Returns a user for a given identifier")
    @APIResponse(
        responseCode = "200",
        description = "Gets a user for a given id",
        content = @Content(
            mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class),
            examples = @ExampleObject(name = "user", value = Examples.VALID_EXAMPLE_USER)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "The user is not found for a given identifier"
    )
    public Uni<Response> get(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return this.userService.getById(id)
                .onItem().ifNotNull().transform(u -> Response.ok(u).build())
                .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Partially updates an exiting user")
    @APIResponse(
        responseCode = "200",
        description = "Updated the user",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = User.class),
            examples = @ExampleObject(name = "user", value = Examples.VALID_EXAMPLE_USER)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Null user passed in (or no request body found)"
    )
    @APIResponse(
        responseCode = "404",
        description = "No user found"
    )
    public Uni<Response> update(@Parameter(name = "id", required = true) @PathParam("id") Long id,
                                @RequestBody(
                                    name = "valid_hero",
                                    required = true,
                                    content = @Content(
                                        schema = @Schema(implementation = User.class),
                                        examples = @ExampleObject(name = "valid_hero", value = Examples.VALID_EXAMPLE_USER)
                                    )
                                )
                                @NotNull User user) {
        if (user.getId() == null) {
            user.setId(id);
        }

        return this.userService.partialUpdate(user)
                .onItem().ifNotNull().transform(u -> Response.ok(u).build())
                .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build())
                .onFailure(ConstraintViolationException.class)
                .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes an exiting user")
    @APIResponse(
        responseCode = "204",
        description = "Deletes a user"
    )
    public Uni<Response> delete(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return userService.delete(id)
                .map(unused -> Response.noContent().build());
    }
}
