package org.pvlpech.mflow.crud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@ToString
@Setter
@Getter
public class User extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "userSequence", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSequence")
    private Long id;

    @Column(length = 250, nullable = false)
    @Size(max = 250, message = "Name must not be more than 250 characters")
    @NotBlank(message = "Name must not be blank")
    private String name;

//    @ManyToMany()
//    @JoinTable(name = "users_groups"
//        , joinColumns = @JoinColumn(name = "user_id")
//        , inverseJoinColumns = @JoinColumn(name = "group_id")
//    )
//    @JsonIgnore
//    @Getter(AccessLevel.NONE)
//    @Setter(AccessLevel.NONE)
//    private Set<Group> groups = new HashSet<>();
//
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "owner")
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Group> servedGroups = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return getId() != null && id.equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

//    public Uni<Set<Group>> getGroups() {
//        return Mutiny.fetch(groups);
//    }
//
//    public Uni<User> addGroup(Group group) {
//        return this.getGroups()
//                .map(gs -> gs.add(group))
//                .replaceWith(group)
//                .flatMap(Group::getUsers)
//                .map(us -> us.add(this))
//                .replaceWith(this);
//    }
//
//    public Uni<Void> deleteGroup(Group group) {
//        return this.getGroups()
//                .map(gs -> gs.remove(group))
//                .replaceWith(group)
//                .flatMap(Group::getUsers)
//                .map(us -> us.remove(this))
//                .replaceWithVoid();
//    }
//
    public Uni<Set<Group>> getServedGroups() {
        return Mutiny.fetch(servedGroups);
    }

    public Uni<User> addServedGroup(Group group) {
        return this.getServedGroups()
                .map(gs -> gs.add(group))
                .replaceWith(this);
    }

//    public Uni<Void> removeAllGroup() {
//        return this.getGroups()
//                .map(Set::copyOf)
//                .flatMap(groupsCopy -> Uni.combine().all().unis(
//                                groupsCopy.stream()
//                                        .map(this::removeGroup)
//                                        .collect(Collectors.toList())
//                        ).discardItems()
//                );
//    }
//
//    public Uni<User> merge(User user) {
//        return Uni.createFrom().item(this)
//                .map(currentUser -> {
//                    if (user == null) return currentUser;
//                    currentUser.setName(user.getName() == null ? currentUser.getName() : user.getName());
//                    return currentUser;
//                });
//    }

//    public static Uni<User> findByName(String name) {
//        return find("name", name).firstResult();
//    }
//
//    public static Uni<Long> deleteStefs() {
//        return delete("name", "Stef");
//    }

}
