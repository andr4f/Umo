package unimag.proyect.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_schedule")
    private String id;

    @Column(name = "week_day")
    private String weekDay;

    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @ManyToOne
    @JoinColumn(name = "id_doctor")
    private Doctor doctor;
}
