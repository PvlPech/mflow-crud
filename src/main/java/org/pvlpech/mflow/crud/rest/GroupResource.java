package org.pvlpech.mflow.crud.rest;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
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
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.model.User;
import org.pvlpech.mflow.crud.service.GroupService;
import org.pvlpech.mflow.crud.validation.ValidationGroups;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/v1/groups")
@ApplicationScoped
@Produces(APPLICATION_JSON)
@Tag(name = "groups")
public class GroupResource {

    @Inject
    GroupService groupService;

    @GET
    @Operation(summary = "Returns all the groups from the database")
    @APIResponse(
        responseCode = "200",
        description = "Gets all groups",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = Group.class, type = SchemaType.ARRAY),
            examples = @ExampleObject(name = "groups", value = Examples.VALID_EXAMPLE_GROUP_LIST)
        )
    )
    public Uni<List<Group>> getAll() {
        return groupService.getAllGroups();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Returns a group for a given identifier")
    @APIResponse(
        responseCode = "200",
        description = "Gets a group for a given id",
        content = @Content(
            mediaType = APPLICATION_JSON, schema = @Schema(implementation = Group.class),
            examples = @ExampleObject(name = "group", value = Examples.VALID_EXAMPLE_GROUP)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "The group is not found for a given identifier"
    )
    public Uni<Group> get(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return groupService.getGroup(id);
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Creates a group")
    @APIResponse(
        responseCode = "201",
        description = "The URI of the created group",
        headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid group passed in (or no request body found)"
    )
    @APIResponse(
        responseCode = "404",
        description = "No user found"
    )
    public Uni<Response> create(
        @RequestBody(
            name = "group",
            required = true,
            content = @Content(
                mediaType = APPLICATION_JSON,
                schema = @Schema(implementation = Group.class),
                examples = @ExampleObject(name = "valid_group", value = Examples.VALID_EXAMPLE_GROUP_TO_CREATE)
            )
        )
        @Valid @ConvertGroup(to = ValidationGroups.Post.class) @NotNull(message = "Group must not be blank") Group group,
        @Context UriInfo uriInfo) {
        return this.groupService.create(group)
                .onItem().ifNotNull().transform(g -> {
                    var uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(g.getId())).build();
                    return Response.created(uri).build();
                })
                .onFailure(ConstraintViolationException.class)
                .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

    @PATCH
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Partially updates an exiting group")
    @APIResponse(
        responseCode = "200",
        description = "Updated the group",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = Group.class),
            examples = @ExampleObject(name = "user", value = Examples.VALID_EXAMPLE_GROUP)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Null group passed in (or no request body found)"
    )
    @APIResponse(
        responseCode = "404",
        description = "No group/user found"
    )
    public Uni<Response> update(@Parameter(name = "id", required = true) @PathParam("id") Long id,
                                @RequestBody(
                                    name = "valid_group",
                                    required = true,
                                    content = @Content(
                                        schema = @Schema(implementation = User.class),
                                        examples = @ExampleObject(name = "valid_group", value = Examples.VALID_EXAMPLE_GROUP)
                                    )
                                )
                                @NotNull Group group) {
        if (group.getId() == null) {
            group.setId(id);
        }

        return this.groupService.partialUpdate(group)
            .onItem().ifNotNull().transform(g -> Response.ok(g).build())
            .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build())
            .onFailure(ConstraintViolationException.class)
            .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

    @PUT
    @Path("/{id}/users/{userId}")
    @Operation(summary = "Add the User to the Group")
    @APIResponse(
        responseCode = "204",
        description = "Added the User to the Group"
    )
    @APIResponse(
        responseCode = "404",
        description = "No group/user found"
    )
    public Uni<Response> addUser(@Parameter(name = "id", required = true) @PathParam("id") Long id,
                                 @Parameter(name = "userId", required = true) @PathParam("userId") Long userId) {
        return groupService.addUser(id, userId)
                .map(unused -> Response.noContent().build());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes an exiting group")
    @APIResponse(
        responseCode = "204",
        description = "Deletes a group"
    )
    @APIResponse(
        responseCode = "404",
        description = "No group found"
    )
    public Uni<Response> delete(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return groupService.deleteGroup(id)
                .map(unused -> Response.noContent().build());
    }

    @DELETE
    @Path("/{id}/users/{userId}")
    @Operation(summary = "Delete the User from the Group")
    @APIResponse(
        responseCode = "204",
        description = "Deleted the User to the Group"
    )
    @APIResponse(
        responseCode = "404",
        description = "No group/user found"
    )
    public Uni<Response> deleteUser(Long id, Long userId) {
        return groupService.deleteUser(id, userId)
                .map(unused -> Response.noContent().build());
    }

    @GET
    @Path("/{id}/users")
    @Operation(summary = "Returns a users for a group with given identifier")
    @APIResponse(
        responseCode = "200",
        description = "Gets a users for a group with given id",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = User.class, type = SchemaType.ARRAY),
            examples = @ExampleObject(name = "users", value = Examples.VALID_EXAMPLE_USER_LIST)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "The group is not found for a given identifier"
    )
    public Uni<Set<User>> getUsers(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return groupService.getUsers(id);
    }
}
