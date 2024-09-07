package org.pvlpech.mflow.crud.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import org.pvlpech.mflow.crud.mapper.GroupPartialUpdateMapper;
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.model.User;

import java.util.List;

@ApplicationScoped
public class GroupService {

    @Inject
    Validator validator;

    @Inject
    GroupPartialUpdateMapper groupPartialUpdateMapper;

    @WithTransaction
    public Uni<Group> create(Group group) {
        return Uni.createFrom().item(this.validate(group))
            .flatMap(g -> User.<User>findById(g.getOwner().getId())
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + g.getOwner().getId()))
                .flatMap(u -> u.addServedGroup(g))
                .flatMap(u -> g.persist())
            );
    }

    /**
     * Validates a {@link Group} for partial update according to annotation validation rules on the {@link Group} object.
     *
     * @param group The {@link Group}
     * @return The same {@link Group} that was passed in, assuming it passes validation. The return is used as a convenience so the method can be called in a functional pipeline.
     * @throws ConstraintViolationException If validation fails
     */
    private Group validate(Group group) {
        var violations = this.validator.validate(group);

        if ((violations != null) && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return group;
    }
    @WithTransaction
    public Uni<Group> partialUpdate(Group group) {
        return Group.<Group>findById(group.getId())
            .onItem().ifNotNull().transform(g -> {
                // change the owner case start
                if (group.getOwner() != null && !group.getOwner().equals(g.getOwner())) {
                    g.getOwner().deleteServedGroup(g)
                        .flatMap(u -> User.<User>findById(group.getOwner().getId()))
                        .flatMap(u -> u.addServedGroup(g));
                }
                // change the owner case end
                return g;
            })
            .onItem().ifNotNull().transform(g -> {
                this.groupPartialUpdateMapper.mapPartialUpdate(group, g);
                return g;
            })
            .onItem().ifNotNull().transform(this::validate);
    }

    @WithTransaction
    public Uni<Void> deleteGroup(Long id) {
        return Group.<Group>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
            .flatMap(g -> Uni.createFrom().item(g.getOwner())
                .flatMap(u -> u.deleteServedGroup(g))
                .flatMap(u -> u.deleteGroup(g))
                .flatMap(u -> Group.deleteById(id)))
            .replaceWithVoid();
    }

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
