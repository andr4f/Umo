package unimag.proyect.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;


@Entity
@Table(name = "doctor")
@PrimaryKeyJoinColumn(name = "id_person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Doctor extends Person {


    @Column(name = "register_num", nullable = false, unique = true)
    private String registerNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "id_speciality", nullable = false, unique = true)
    private Speciality speciality;

    @OneToMany(mappedBy = "doctor")
    private List<DoctorSchedule> schedules;

}
