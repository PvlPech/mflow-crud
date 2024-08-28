package org.pvlpech.mflow.crud.rest;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.service.GroupService;

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

//    @POST
//    public Uni<Response> create(Group group) {
//        return groupService.create(group)
//                .map(savedGroup -> Response.status(Response.Status.CREATED).entity(savedGroup).build());
//    }
//
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
