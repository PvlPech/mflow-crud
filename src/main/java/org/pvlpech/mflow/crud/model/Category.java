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
@Table(name = "categories")
@ToString
@Setter
@Getter
public class Category extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "categorySequence", sequenceName = "categories_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categorySequence")
    private Long id;

    @Column(length = 100, nullable = false)
    @Size(max = 100, message = "Name must not be more than 100 characters")
    @NotBlank(message = "Name must not be blank")
    private String name;

    @ManyToOne()
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy="parent", cascade = CascadeType.ALL)
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Category> childs = new HashSet<>();

    @ManyToOne()
    @JoinColumn(name = "group_id", nullable = false)
    /**
     * We need to validate "Group" existence only in the case of Creation (Post).
     * During entities deletion we set "null" sometimes to support consistency.
     */
    @NotNull(message = "Group must not be null", groups = ValidationGroups.Post.class)
    private Group group;

    public Uni<Set<Category>> getChilds() {
        return Mutiny.fetch(this.childs);
    }

    public Uni<Category> addChildrenCategory(Category category) {
        return this.getChilds()
            .map(cs -> cs.add(category))
            .replaceWith(category)
            .map(c -> {
                if (!this.equals(c.getParent())) {
                    c.setParent(this);
                }
                return this;
            });
    }

    public Uni<Category> deleteChildrenCategory(Category category) {
        return this.getChilds()
            .map(cs -> cs.remove(category))
            .replaceWith(category)
            .map(c -> {
                c.setParent(null);
                return this;
            });
    }

    public Uni<Category> deleteAllChildrenCategories() {
        return this.getChilds()
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .invoke(c -> Uni.createFrom().item(c.getGroup())
                .chain(g -> g.deleteServedCategory(c))
                .chain(g -> c.deleteAllChildrenCategories()))
            .invoke(c -> c.setParent(null))
            .call(PanacheEntityBase::delete)
            .collect().asList()
            .replaceWith(this.childs)
            .map(cs -> {
                cs.clear();
                return this;
            });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        return getId() != null && getId().equals(((Category) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
