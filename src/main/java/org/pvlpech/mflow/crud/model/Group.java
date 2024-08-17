package org.pvlpech.mflow.crud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.ws.rs.NotFoundException;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@ToString
public class Group extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "groupSequence", sequenceName = "groups_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groupSequence")
    public Long id;

    @Column(length = 50, nullable = false)
    public String name;

    @ManyToOne()
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public User owner;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.EAGER)
    @JsonIgnore
    public Set<User> users = new HashSet<>();

    public void specifyOwner(User owner) {
        if (this.owner != null && !this.owner.equals(owner)) {
            this.owner.servedGroups.remove(this);
        }
        if (owner != null && !owner.equals(this.owner)) {
            owner.servedGroups.add(this);
        }
        this.owner = owner;
        owner.addGroup(this);
    }

    public void swapOwner() {
        if (users.size() == 1)
            return;
        User newOwner = users.stream().filter(user -> !user.equals(this.owner)).findFirst().orElse(this.owner);
        specifyOwner(newOwner);
    }

    public void cleanupUsers() {
        users.forEach(user -> user.removeGroup(this));
    }

    public Uni<Group> merge(Group group) {
        Uni<Group> merged = Uni.createFrom().item(this);
        if (group == null)
            return merged;

        this.name = group.name == null ? this.name : group.name;
        if (group.owner != null) {
            merged = User.<User>findById(group.owner.id)
                    .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + group.owner.id))
                    .flatMap(newOwner -> {
                        specifyOwner(newOwner);
                        return Uni.createFrom().item(this);
                    });
        }

        return merged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        return id != null && id.equals(((Group) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
