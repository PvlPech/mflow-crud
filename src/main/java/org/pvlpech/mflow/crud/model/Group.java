package org.pvlpech.mflow.crud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
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

    public Uni<Set<User>> getUsers() {
        return Mutiny.fetch(this.users);
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
