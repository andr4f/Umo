package unimag.proyect.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter


public class Speciality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_speciality")
    private Integer idSpeciality;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "speciality")
    private List<Doctor> doctors;
}
