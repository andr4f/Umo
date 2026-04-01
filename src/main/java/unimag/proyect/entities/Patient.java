package unimag.proyect.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "patient")
@PrimaryKeyJoinColumn(name = "id_person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor   
@SuperBuilder
public class Patient extends Person {

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;
}
