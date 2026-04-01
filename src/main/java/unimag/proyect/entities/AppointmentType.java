package unimag.proyect.entities;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_appointment_type")
    private Integer idAppointmentType;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer duration;
}