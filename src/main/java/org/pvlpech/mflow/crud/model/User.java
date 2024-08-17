package org.pvlpech.mflow.crud.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.ws.rs.NotFoundException;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@ToString
public class User extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "userSequence", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSequence")
    public Long id;

    @Column(length = 250, nullable = false)
    public String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_groups"
            , joinColumns = @JoinColumn(name = "user_id")
            , inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    public Set<Group> groups = new HashSet<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "owner", fetch = FetchType.EAGER)
    public Set<Group> servedGroups = new HashSet<>();

    public void addGroup(Group group) {
        groups.add(group);
        group.users.add(this);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
        group.users.remove(this);
    }

    public void cleanupAllGroup() {
        Set<Group> groupsCopy = Set.copyOf(this.groups);
        groupsCopy.forEach(this::removeGroup);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Uni<User> merge(User user) {
        Uni<User> merged = Uni.createFrom().item(this);
        if (user == null)
            return merged;

        this.name = user.name == null ? this.name : user.name;
        if (user.groups != null) {
            this.groups.forEach(group -> {
                if (!user.groups.contains(group))
                    removeGroup(group);
            });

            merged = Multi.createFrom().iterable(user.groups)
                    .onItem().transformToUniAndMerge(group -> {
                        if (!this.groups.contains(group))
                            return Group.<Group>findById(group.id)
                                    .onItem().ifNull().failWith(new NotFoundException("Group not found with id: " + group.id))
                                    .invoke(this::addGroup);
                        return Uni.createFrom().voidItem();
                    })
                    .collect().asList()
                    .replaceWith(this);
        }

        return merged;
    }

//    public static Uni<User> findByName(String name) {
//        return find("name", name).firstResult();
//    }
//
//    public static Uni<Long> deleteStefs() {
//        return delete("name", "Stef");
//    }

}
