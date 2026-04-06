package unimag.proyect.entities;

import jakarta.persistence.*;
import lombok.*;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_schedule")
@Builder       // ← cambiado de @Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_schedule")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_day", nullable = false, length = 10)
    private WeekDay weekDay;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doctor", nullable = false)
    private Doctor doctor;
}