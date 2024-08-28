package org.pvlpech.mflow.crud.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.pvlpech.mflow.crud.mapper.UserPartialUpdateMapper;
import org.pvlpech.mflow.crud.model.User;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    Validator validator;

    @Inject
    UserPartialUpdateMapper userPartialUpdateMapper;

    @WithTransaction
    public Uni<User> create(User user) {
        return Uni.createFrom().item(this.validate(user)).flatMap(u -> u.persist());
    }

    @WithTransaction
    public Uni<List<User>> getAll() {
        return User.listAll(Sort.by("name"));
    }

    @WithTransaction
    public Uni<User> getById(Long id) {
        return User.findById(id);
    }

    @WithTransaction
    public Uni<User> partialUpdate(User user) {
        return User.<User>findById(user.getId())
                .onItem().ifNotNull().transform(u -> {
                    this.userPartialUpdateMapper.mapPartialUpdate(user, u);
                    return u;
                })
                .onItem().ifNotNull().transform(this::validate);
    }

    /**
     * Validates a {@link User} for partial update according to annotation validation rules on the {@link User} object.
     * @param user The {@link User}
     * @return The same {@link User} that was passed in, assuming it passes validation. The return is used as a convenience so the method can be called in a functional pipeline.
     * @throws ConstraintViolationException If validation fails
     */
    private User validate(User user) {
        var violations = this.validator.validate(user);

        if ((violations != null) && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return user;
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return User.deleteById(id).replaceWithVoid();
    }
}
