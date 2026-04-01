package unimag.proyect.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "appointment_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_appointment_type")
    private UUID idAppointmentType;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer duration; // duration in minutes

    @OneToMany(mappedBy = "appointmentType")
    private List<Appointment> appointments;

}