package unimag.proyect.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.engine.profile.Fetch;

import java.util.List;


@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Doctor extends Person {


    @Column(name = "register_num")
    private String registerNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "id_specility", nullable = false)
    private Speciality speciality;

    @OneToMany(mappedBy = "doctor")
    private List<DoctorSchedule> schedules;




}
