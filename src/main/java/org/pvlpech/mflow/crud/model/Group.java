package org.pvlpech.mflow.crud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.ws.rs.NotFoundException;
import lombok.*;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String name;

    @ManyToOne()
    @JoinColumn(name = "owner_id", nullable = false)
    @Setter(AccessLevel.NONE)
    private User owner;

//    @ManyToMany(mappedBy = "groups")
//    @JsonIgnore
//    @Getter(AccessLevel.NONE)
//    @Setter(AccessLevel.NONE)
//    private Set<User> users = new HashSet<>();
//
//    public Uni<Set<User>> getUsers() {
//        return Mutiny.fetch(this.users);
//    }
//
//    public Uni<Void> addUser(User user) {
//        return this.getUsers()
//                .map(users -> users.add(user))
//                .replaceWithVoid();
//    }
//
//    public Uni<Void> setOwner(User newOwner) {
//        Uni<Void> result = Uni.createFrom().voidItem();
//        if (this.getOwner() != null && !this.getOwner().equals(newOwner)) {
//            result = this.getOwner().removeServedGroup(this);
//        }
//        if (newOwner != null && !newOwner.equals(this.getOwner())) {
//            result = newOwner.addServedGroup(this)
//                    .replaceWith(newOwner)
//                    .flatMap(owner -> owner.addGroup(this));
//        }
//        return result.replaceWith(newOwner).map(owner -> this.setOwner(owner)).replaceWithVoid();
//    }
//
//    public Uni<Void> swapOwner() {
//        return this.getUsers().flatMap(users -> {
//            if (users.size() == 1)
//                return Uni.createFrom().voidItem();
//            User newOwner = users.stream()
//                    .filter(user -> !user.equals(this.getOwner()))
//                    .findFirst()
//                    .orElse(this.getOwner());
//            return this.setOwner(newOwner);
//        });
//    }
//
//    public Uni<Void> cleanupUsers() {
//        return this.getUsers().flatMap(users -> Uni.combine().all().unis(
//                users.stream()
//                        .map(user -> user.removeGroup(this))
//                        .collect(Collectors.toList())
//        ).discardItems());
//    }
//
//    public Uni<Group> merge(Group group) {
//        return Uni.createFrom().item(this)
//                .map(currentGroup -> {
//                    if (group == null) return currentGroup;
//                    currentGroup.setName(group.getName() == null ? currentGroup.getName() : group.getName());
//                    return currentGroup;
//                })
//                .flatMap(currentGroup -> {
//                    if (group.getOwner() != null) {
//                        return User.<User>findById(group.getOwner().getId())
//                                .onItem().ifNull().failWith(new NotFoundException("User not found with id: " + group.getOwner().getId()))
//                                .flatMap(newOwner -> {
//                                    this.setOwner(newOwner);
//                                    return Uni.createFrom().item(this);
//                                });
//                    }
//                    return Uni.createFrom().item(this);
//                });
//    }

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
