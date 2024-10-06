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
            .call(categoryToCreate -> Group.<Group>findById(categoryToCreate.getGroup().getId())
                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + categoryToCreate.getGroup().getId()))
                .flatMap(categoryToCreateGroup -> categoryToCreateGroup.addServedCategory(categoryToCreate)))
            .flatMap(categoryToCreate -> categoryToCreate.persist());
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

    private Uni<Category> swapGroup(Category category, Group group) {
        if (group != null && !group.equals(category.getGroup())) {
            return deleteGroupReference(category)
                .flatMap(currentCategory -> Group.<Group>findById(group.getId()))
                .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + group.getId()))
                .flatMap(targetGroup -> targetGroup.addServedCategory(category))
                .replaceWith(category);
        }
        return Uni.createFrom().item(category);
    }

    private Uni<Category> swapParent(Category category, Category parent) {
        if (parent != null && !parent.equals(category.getParent())) {
            return deleteParentReference(category)
                .flatMap(сhildCategory -> Category.<Category>findById(parent.getId()))
                .onItem().ifNull().failWith(new NotFoundException("Category not found with id: " + parent.getId()))
                .flatMap(parentCategory -> parentCategory.addChildrenCategory(category))
                .replaceWith(category);
        }
        return Uni.createFrom().item(category);
    }

    @WithTransaction
    public Uni<Category> partialUpdate(Category category) {
        return Category.<Category>findById(category.getId())
            .onItem().ifNotNull().transformToUni(categoryInDb -> swapGroup(categoryInDb, category.getGroup()))
            .onItem().ifNotNull().transformToUni(categoryInDb -> swapParent(categoryInDb, category.getParent()))
            .onItem().ifNotNull().transform(categoryInDb -> {
                this.categoryPartialUpdateMapper.mapPartialUpdate(category, categoryInDb);
                return categoryInDb;
            })
            .onItem().ifNotNull().transform(this::validate);
    }

    private Uni<Category> deleteChilds(Category category) {
        return category.getChilds()
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .invoke(categoryChild -> this.delete(categoryChild.getId()))
            .collect().asList()
            .map(categoryChilds -> {
                categoryChilds.clear();
                return category;
            });
    }

    private Uni<Category> deleteGroupReference(Category category) {
        return Uni.createFrom().item(category.getGroup())
            .flatMap(categoryGroup -> categoryGroup.deleteServedCategory(category))
            .replaceWith(category);
    }

    private Uni<Category> deleteParentReference(Category category) {
        Category categoryParent = category.getParent();
        return categoryParent != null
            ? categoryParent.deleteChildrenCategory(category).replaceWith(category)
            : Uni.createFrom().item(category);
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return Category.<Category>findById(id)
            .onItem().ifNull().failWith(new NotFoundException("Category not found with id: " + id))
            .flatMap(this::deleteGroupReference)
            .flatMap(this::deleteParentReference)
            .flatMap(this::deleteChilds)
            .flatMap(PanacheEntityBase::delete);
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
