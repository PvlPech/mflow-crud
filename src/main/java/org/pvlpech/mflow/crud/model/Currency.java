package org.pvlpech.mflow.crud.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "currencies")
@ToString
@Setter
@Getter
public class Currency extends PanacheEntityBase {

    @Id
    private Long id;

    @Column(length = 4, nullable = false)
    @NotBlank(message = "Code must not be blank")
    @Size(max = 4, message= "Code must not be more than 4 characters")
    private String code;

    @Column(length = 250, nullable = false)
    @NotBlank(message = "Name must not be blank")
    @Size(max = 250, message= "Name must not be more than 250 characters")
    private String name;

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
