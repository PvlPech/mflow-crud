package org.pvlpech.mflow.crud.service;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import org.pvlpech.mflow.crud.mapper.GroupPartialUpdateMapper;
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GroupService {

    @Inject
    Validator validator;

    @Inject
    GroupPartialUpdateMapper groupPartialUpdateMapper;

    @Inject
    CategoryService categoryService;

    @WithTransaction
    public Uni<Group> create(Group group) {
        return Uni.createFrom().item(this.validate(group))
            .call(groupToCreate -> User.<User>findById(groupToCreate.getOwner().getId())
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + groupToCreate.getOwner().getId()))
                .flatMap(groupToCreateUser -> groupToCreateUser.addServedGroup(groupToCreate)))
            .flatMap(groupToCreate -> groupToCreate.persist());
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

    private Uni<Group> swapOwner(Group group, User owner) {
        if (owner != null && !owner.equals(group.getOwner())) {
            return deleteOwnerReference(group)
                .flatMap(currentGroup -> User.<User>findById(owner.getId()))
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + owner.getId()))
                .flatMap(targetOwner -> targetOwner.addServedGroup(group))
                .replaceWith(group);
        }
        return Uni.createFrom().item(group);
    }

    @WithTransaction
    public Uni<Group> partialUpdate(Group group) {
        return Group.<Group>findById(group.getId())
            .onItem().ifNotNull().transformToUni(groupInDb -> swapOwner(groupInDb, group.getOwner()))
            .onItem().ifNotNull().transform(groupInDb -> {
                this.groupPartialUpdateMapper.mapPartialUpdate(group, groupInDb);
                return groupInDb;
            })
            .onItem().ifNotNull().transform(this::validate);
    }

    private Uni<Group> deleteServedCategories(Group group) {
        return group.getServedCategories()
            .map(HashSet::new) //copy of the servedCategories set to avoid concurrent modifications
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .filter(servedCategory -> servedCategory.getParent() != null) //to delete only parent categories, the rest of categories will be deleted by hierarchy
            .call(servedCategoryToDelete -> categoryService.delete(servedCategoryToDelete.getId()))
            .collect().asList()
            .replaceWith(group);
    }

    private Uni<Group> deleteOwnerReference(Group group) {
        return Uni.createFrom().item(group.getOwner())
            .flatMap(groupOwner -> groupOwner.deleteServedGroup(group))
            .replaceWith(group);
    }

    private Uni<Group> deleteUserReferences(Group group) {
        return group.getUsers()
            .map(HashSet::new) //copy of the users set to avoid concurrent modifications
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .call(groupUser -> groupUser.deleteGroup(group))
            .collect().asList()
            .replaceWith(group);
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return Group.<Group>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
            .flatMap(this::deleteServedCategories)
            .flatMap(this::deleteOwnerReference)
            .flatMap(this::deleteUserReferences)
            .flatMap(PanacheEntityBase::delete);
    }

    @WithTransaction
    public Uni<List<Group>> getAll() {
        return Group.listAll(Sort.by("name"));
    }

    @WithTransaction
    public Uni<Group> get(Long id) {
        return Group.findById(id);
    }

    @WithTransaction
    public Uni<Void> addUser(Long id, Long userId) {
        return Group.<Group>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
            .flatMap(group -> User.<User>findById(userId)
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + userId))
                .flatMap(user -> user.addGroup(group))
                .replaceWithVoid());
    }

    @WithTransaction
    public Uni<Void> deleteUser(Long id, Long userId) {
        return Group.<Group>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
            .flatMap(group -> User.<User>findById(userId)
                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + userId))
                .flatMap(user -> user.deleteGroup(group))
                .replaceWithVoid());
    }

    @WithTransaction
    public Uni<Set<User>> getUsers(Long id) {
        return Group.<Group>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + id))
            .flatMap(Group::getUsers);
    }
}
