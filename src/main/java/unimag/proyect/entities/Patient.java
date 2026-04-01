package unimag.proyect.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Patient extends Person {

}
