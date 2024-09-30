package org.pvlpech.mflow.crud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.reactive.mutiny.Mutiny;
import org.pvlpech.mflow.crud.validation.ValidationGroups;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@ToString
@Setter
@Getter
public class Group extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "groupSequence", sequenceName = "groups_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groupSequence")
    private Long id;

    @Column(length = 50, nullable = false)
    @Size(max = 50, message = "Name must not be more than 50 characters")
    @NotBlank(message = "Name must not be blank")
    private String name;

    @ManyToOne()
    @JoinColumn(name = "owner_id", nullable = false)
    /**
     * We need to validate "Owner" existence only in the case of Creation (Post).
     * During entities deletion we set "null" sometimes to support consistency.
     */
    @NotNull(message = "Owner must not be null", groups = ValidationGroups.Post.class)
    private User owner;

    @ManyToMany(mappedBy = "groups")
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "group")
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Category> servedCategories = new HashSet<>();

    public Uni<Set<User>> getUsers() {
        return Mutiny.fetch(this.users);
    }

    public Uni<Set<Category>> getServedCategories() {
        return Mutiny.fetch(servedCategories);
    }

    public Uni<Group> addServedCategory(Category category) {
        return this.getServedCategories()
            .map(cs -> cs.add(category))
            .replaceWith(category)
            .map(c -> {
                if (!this.equals(c.getGroup())) {
                    c.setGroup(this);
                }
                return this;
            });
    }

    public Uni<Group> deleteServedCategory(Category categoryToDelete) {
        return this.getServedCategories()
            .map(servedCategories -> servedCategories.remove(categoryToDelete))
            .replaceWith(categoryToDelete)
            .invoke(category -> category.setGroup(null))
            .replaceWith(this);
    }

    public Uni<Group> deleteAllServedCategories() {
        return this.getServedCategories()
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .invoke(c -> c.setGroup(null))
            .invoke(Category::deleteAllChildrenCategories)
            .call(PanacheEntityBase::delete)
            .collect().asList()
            .replaceWith(this.servedCategories)
            .map(cs -> {
                cs.clear();
                return this;
            });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        return getId() != null && getId().equals(((Group) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
