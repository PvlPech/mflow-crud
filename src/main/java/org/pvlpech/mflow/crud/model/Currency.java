package org.pvlpech.mflow.crud.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "currencies")
@ToString
public class Currency extends PanacheEntityBase {

    @Id
    public Long id;

    @Column(length = 4, nullable = false)
    public String code;

    @Column(length = 250, nullable = false)
    public String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        return id != null && id.equals(((Currency) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
