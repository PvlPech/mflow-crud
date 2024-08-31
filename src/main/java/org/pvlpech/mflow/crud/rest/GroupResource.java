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
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.service.GroupService;

import java.net.URI;
import java.util.List;

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
        @Valid @NotNull(message = "Group must not be blank") Group group,
        @Context UriInfo uriInfo) {
        return this.groupService.create(group)
                .onItem().ifNotNull().transform(g -> {
                    var uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(g.getId())).build();
                    return Response.created(uri).build();
                })
                .onFailure(ConstraintViolationException.class)
                .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

//    @PATCH
//    @Path("{id}")
//    public Uni<Response> update(Long id, Group group) {
//        return groupService.update(id, group)
//                .map(updatedGroup -> Response.ok(updatedGroup).build());
//    }
//
//    @PUT
//    @Path("{id}/users/{userId}")
//    public Uni<Response> addUser(Long id, Long userId) {
//        return groupService.addUser(id, userId)
//                .map(updatedGroup -> Response.ok(updatedGroup).build());
//    }
//
//    @DELETE
//    @Path("{id}")
//    public Uni<Response> delete(Long id) {
//        return groupService.deleteGroup(id)
//                .map(unused -> Response.noContent().build());
//    }
//
//    @DELETE
//    @Path("{id}/users/{userId}")
//    public Uni<Response> deleteUser(Long id, Long userId) {
//        return groupService.deleteUser(id, userId)
//                .map(unused -> Response.noContent().build());
//    }
}
