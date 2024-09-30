package org.pvlpech.mflow.crud.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import org.pvlpech.mflow.crud.mapper.CategoryPartialUpdateMapper;
import org.pvlpech.mflow.crud.model.Category;
import org.pvlpech.mflow.crud.model.Group;

import java.util.List;

@ApplicationScoped
public class CategoryService {

    @Inject
    Validator validator;

    @Inject
    CategoryPartialUpdateMapper categoryPartialUpdateMapper;

    @WithTransaction
    public Uni<Category> create(Category category) {
        return Uni.createFrom().item(this.validate(category))
            .flatMap(c -> Group.<Group>findById(c.getGroup().getId())
                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + c.getGroup().getId()))
                .flatMap(g -> g.addServedCategory(c))
                .flatMap(g -> c.persist())
            );
    }

    /**
     * Validates a {@link Category} for partial update according to annotation validation rules on the {@link Category} object.
     *
     * @param category The {@link Category}
     * @return The same {@link Category} that was passed in, assuming it passes validation. The return is used as a convenience so the method can be called in a functional pipeline.
     * @throws ConstraintViolationException If validation fails
     */
    private Category validate(Category category) {
        var violations = this.validator.validate(category);

        if ((violations != null) && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return category;
    }
    @WithTransaction
    public Uni<Category> partialUpdate(Category category) {
        return Category.<Category>findById(category.getId())
            .onItem().ifNotNull().transformToUni(categoryInDb -> {
                // change the group case start
                if (category.getGroup() != null && !category.getGroup().equals(categoryInDb.getGroup())) {
                    return categoryInDb.getGroup().deleteServedCategory(categoryInDb)
                        .flatMap(g -> Group.<Group>findById(category.getGroup().getId()))
                        .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + category.getGroup().getId()))
                        .flatMap(g -> g.addServedCategory(categoryInDb))
                        .replaceWith(categoryInDb);
                // change the group case end
                } else {
                    return Uni.createFrom().item(categoryInDb);
                }
            })
            .onItem().ifNotNull().transformToUni(categoryInDb -> {
                // change the parent case start
                if (category.getParent() != null && !category.getParent().equals(categoryInDb.getParent())) {
                    return categoryInDb.getParent().deleteChildrenCategory(categoryInDb)
                        .flatMap(с -> Category.<Category>findById(category.getParent().getId()))
                        .onItem().ifNull().failWith(new NotFoundException("Category not found with id: " + category.getParent().getId()))
                        .flatMap(c -> c.addChildrenCategory(categoryInDb))
                        .replaceWith(categoryInDb);
                    // change the parent case end
                } else {
                    return Uni.createFrom().item(categoryInDb);
                }
            })
            .onItem().ifNotNull().transform(c -> {
                this.categoryPartialUpdateMapper.mapPartialUpdate(category, c);
                return c;
            })
            .onItem().ifNotNull().transform(this::validate);
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return Category.<Category>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Category not found with id: " + id))
            .flatMap(c -> Uni.createFrom().item(c.getGroup())
                .flatMap(g -> g.deleteServedCategory(c))
                .flatMap(g -> c.deleteAllChildrenCategories())
                .flatMap(g -> Category.deleteById(id)))
            .replaceWithVoid();
    }

    @WithTransaction
    public Uni<List<Category>> getAll() {
        return Category.listAll(Sort.by("name"));
    }

    @WithTransaction
    public Uni<Category> get(Long id) {
        return Category.findById(id);
    }

}
