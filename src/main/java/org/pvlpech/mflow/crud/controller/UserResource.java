package org.pvlpech.mflow.crud.controller;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.pvlpech.mflow.crud.model.User;
import org.pvlpech.mflow.crud.service.UserService;

import java.util.List;

@Path("users")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    public Uni<List<User>> get() {
        return userService.getAllUsers();
    }

    @GET
    @Path("{id}")
    public Uni<User> getSingle(Long id) {
        return userService.getUser(id);
    }

    @POST
    public Uni<Response> create(User user) {
        return userService.create(user)
                .map(savedGroup -> Response.status(Response.Status.CREATED).entity(savedGroup).build());
    }

    @PATCH
    @Path("{id}")
    public Uni<Response> update(Long id, User user) {
        return userService.update(id, user)
                .map(updatedUser -> Response.ok(updatedUser).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return userService.deleteUser(id)
                .map(unused -> Response.noContent().build());
    }
}
