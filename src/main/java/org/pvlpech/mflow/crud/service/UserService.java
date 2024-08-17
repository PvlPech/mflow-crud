package org.pvlpech.mflow.crud.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.model.User;

import java.util.List;

@ApplicationScoped
public class UserService {

    @WithTransaction
    public Uni<User> create(User user) {
        return user.persist();
    }

    @WithTransaction
    public Uni<User> update(Long id, User updatedUser) {
        return User.<User>findById(id)
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + id))
                .flatMap(existingUser -> existingUser.merge(updatedUser))
                .flatMap(existingUser -> existingUser.persist());
    }

    @WithTransaction
    public Uni<Void> deleteUser(Long id) {
        return User.<User>findById(id)
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + id))
                .flatMap(userToBeDeleted -> {
                    userToBeDeleted.servedGroups.forEach(Group::swapOwner);
                    userToBeDeleted.cleanupAllGroup();
                    return userToBeDeleted.delete();
                });
    }

    @WithTransaction
    public Uni<List<User>> getAllUsers() {
        return User.listAll(Sort.by("name"));
    }

    @WithTransaction
    public Uni<User> getUser(Long id) {
        return User.findById(id);
    }
}
