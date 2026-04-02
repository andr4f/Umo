package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Patient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PatientRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void clean() {
        patientRepository.deleteAll();
    }

    private Patient savedPatient(String docNum, String email) {
        return patientRepository.save(Patient.builder()
                .fullName("Test Patient")
                .documentType("CC")
                .documentNumber(docNum)
                .email(email)
                .phone("1234567890")
                .build());
    }

    @Test
    @DisplayName("Save and find Patient by document number")
    void shouldFindPatientByDocumentNumber() {
        savedPatient("DOC123", "patient1@test.com");
        
        Optional<Patient> found = patientRepository.findAll().stream()
                .filter(p -> p.getDocumentNumber().equals("DOC123"))
                .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("patient1@test.com");
    }
}
