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
public class GroupService {

//    @WithTransaction
//    public Uni<Group> create(Group group) {
//        return User.<User>findById(group.getOwner().getId())
//                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + group.getOwner().getId()))
//                .flatMap(owner -> (new Group()).merge(group))
//                .flatMap(newGroup -> newGroup.persist());
//    }
//
//    @WithTransaction
//    public Uni<Group> update(Long id, Group updatedGroup) {
//        return Group.<Group>findById(id)
//                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
//                .flatMap(existingGroup -> existingGroup.merge(updatedGroup))
//                .flatMap(existingGroup -> existingGroup.persist());
//    }
//
//    @WithTransaction
//    public Uni<Void> deleteGroup(Long id) {
//        return Group.<Group>findById(id)
//                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
//                .call(Group::cleanupUsers)
//                .call(groupToBeDeleted -> groupToBeDeleted.setOwner(null))
//                .flatMap(groupToBeDeleted -> groupToBeDeleted.delete());
//    }

    @WithTransaction
    public Uni<List<Group>> getAllGroups() {
        return Group.listAll(Sort.by("name"));
    }

    @WithTransaction
    public Uni<Group> getGroup(Long id) {
        return Group.findById(id);
    }

//    @WithTransaction
//    public Uni<Group> addUser(Long id, Long userId) {
//        return Group.<Group>findById(id)
//                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
//                .flatMap(affectedGroup -> User.<User>findById(userId)
//                        .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + userId))
//                        .flatMap(affectedUser -> affectedUser.addGroup(affectedGroup))
//                        .replaceWith(affectedGroup));
//    }
//
//    @WithTransaction
//    public Uni<Object> deleteUser(Long id, Long userId) {
//        return Group.<Group>findById(id)
//                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
//                .flatMap(affectedGroup -> User.<User>findById(userId)
//                        .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + userId))
//                        .call(affectedUser -> affectedUser.removeGroup(affectedGroup))
//                        .replaceWith(affectedGroup));
//    }
}
