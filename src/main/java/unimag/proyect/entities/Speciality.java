package unimag.proyect.entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "speciality")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter


public class Speciality {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_speciality")
    private UUID idSpeciality;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "speciality")
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();
}
