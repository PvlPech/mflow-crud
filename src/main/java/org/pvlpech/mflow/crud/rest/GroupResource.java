package org.pvlpech.mflow.crud.rest;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.service.GroupService;

import java.util.List;

@Path("/api/v1/groups")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class GroupResource {

    @Inject
    GroupService groupService;

    @GET
    public Uni<List<Group>> getAll() {
        return groupService.getAllGroups();
    }

    @GET
    @Path("{id}")
    public Uni<Group> get(Long id) {
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
