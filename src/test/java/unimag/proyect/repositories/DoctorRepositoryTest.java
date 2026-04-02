package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Speciality;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @BeforeEach
    void clean() {
        doctorRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    private Speciality savedSpeciality() {
        return specialityRepository.save(Speciality.builder().name("Pediatrics").build());
    }

    private Doctor savedDoctor(String docNum, String registerNum, Speciality spec) {
        return doctorRepository.save(Doctor.builder()
                .fullName("Test Doctor")
                .documentType("CC")
                .documentNumber(docNum)
                .email("doctor@test.com")
                .registerNum(registerNum)
                .speciality(spec)
                .build());
    }

    @Test
    @DisplayName("Save and find Doctor by register number")
    void shouldFindDoctorByRegisterNum() {
        Speciality spec = savedSpeciality();
        savedDoctor("DOC456", "REG123", spec);
        
        Optional<Doctor> found = doctorRepository.findAll().stream()
                .filter(d -> d.getRegisterNum().equals("REG123"))
                .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getDocumentNumber()).isEqualTo("DOC456");
    }
}
