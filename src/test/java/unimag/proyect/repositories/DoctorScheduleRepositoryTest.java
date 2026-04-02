package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.entities.Speciality;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorScheduleRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @BeforeEach
    void clean() {
        doctorScheduleRepository.deleteAll();
        doctorRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    private Doctor savedDoctor() {
        Speciality spec = specialityRepository.save(Speciality.builder().name("Neurology").build());
        return doctorRepository.save(Doctor.builder()
                .fullName("Dr. Neuro")
                .documentType("CC")
                .documentNumber("DOC789")
                .email("neuro@test.com")
                .registerNum("REG456")
                .speciality(spec)
                .build());
    }

    @Test
    @DisplayName("Save and find DoctorSchedule")
    void shouldFindDoctorSchedule() {
        Doctor doc = savedDoctor();
        DoctorSchedule schedule = DoctorSchedule.builder()
                .weekDay("MONDAY")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .doctor(doc)
                .build();
        doctorScheduleRepository.save(schedule);

        Optional<DoctorSchedule> found = doctorScheduleRepository.findAll().stream()
                .filter(s -> s.getDoctor().getIdPerson().equals(doc.getIdPerson()))
                .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getWeekDay()).isEqualTo("MONDAY");
    }
}
