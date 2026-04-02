package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private OfficeRepository officeRepository;
    @Autowired private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired private SpecialityRepository specialityRepository;

    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        officeRepository.deleteAll();
        appointmentTypeRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and find Appointment")
    void shouldFindAppointment() {
        Patient patient = patientRepository.save(Patient.builder()
                .fullName("Pat Test")
                .documentType("CC").documentNumber("PAT1")
                .email("pat@test.com")
                .build());

        Speciality spec = specialityRepository.save(Speciality.builder().name("General Test").build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .fullName("Doc Test")
                .documentType("CC").documentNumber("DOC1")
                .email("doc@test.com")
                .registerNum("REG1")
                .speciality(spec)
                .build());

        Office office = officeRepository.save(Office.builder().code("OFC1").name("Office 1").build());

        AppointmentType type = appointmentTypeRepository.save(AppointmentType.builder().name("Type1").duration(30).build());

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(type)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(30))
                .status("SCHEDULED")
                .build();

        appointmentRepository.save(appointment);

        Optional<Appointment> found = appointmentRepository.findAll().stream()
                .filter(a -> a.getPatient().getIdPerson().equals(patient.getIdPerson()))
                .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("SCHEDULED");
    }
}
