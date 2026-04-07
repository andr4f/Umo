package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.PersonStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialityRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private SpecialityRepository specialityRepository;

    @Autowired
    private DoctorRepository doctorRepository; // necesario para el último test

    @BeforeEach
    void clean() {
        doctorRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    // ── helpers ──────────────────────────────────────────────
    private Speciality savedSpeciality(String name) {
        return specialityRepository.save(Speciality.builder().name(name).build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByName ───────────────────────────────────────────

    @Test
    @DisplayName("Encuentra especialidad por nombre exacto")
    void shouldFindSpecialityByExactName() {
        // Arrange
        savedSpeciality("Cardiology");

        // Act
        Optional<Speciality> result = specialityRepository.findByName("Cardiology");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("Retorna vacío si el nombre exacto no existe")
    void shouldReturnEmptyWhenNameNotFound() {
        // Arrange — BD vacía (BeforeEach limpia)

        // Act
        Optional<Speciality> result = specialityRepository.findByName("Neurology");

        // Assert
        assertThat(result).isEmpty();
    }

    // ── findByNameContainingIgnoreCase ───────────────────────

    @Test
    @DisplayName("Encuentra especialidades por nombre parcial")
    void shouldFindSpecialitiesByPartialName() {
        // Arrange
        savedSpeciality("Cardiology");
        savedSpeciality("Cardiovascular Surgery");
        savedSpeciality("Neurology");

        // Act
        List<Speciality> result = specialityRepository.findByNameContainingIgnoreCase("cardio");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getName().toLowerCase().contains("cardio"));
    }

    @Test
    @DisplayName("Búsqueda parcial es case-insensitive")
    void shouldFindSpecialitiesCaseInsensitive() {
        // Arrange
        savedSpeciality("Cardiology");

        // Act
        List<Speciality> result = specialityRepository.findByNameContainingIgnoreCase("CARDIO");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("Retorna lista vacía si no hay coincidencias parciales")
    void shouldReturnEmptyListWhenNoPartialMatch() {
        // Arrange
        savedSpeciality("Cardiology");

        // Act
        List<Speciality> result = specialityRepository.findByNameContainingIgnoreCase("xyz");

        // Assert
        assertThat(result).isEmpty();
    }

    // ── existsByName ─────────────────────────────────────────

    @Test
    @DisplayName("Retorna true si la especialidad existe por nombre")
    void shouldReturnTrueWhenSpecialityExists() {
        // Arrange
        savedSpeciality("Pediatrics");

        // Act
        boolean exists = specialityRepository.existsByName("Pediatrics");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Retorna false si la especialidad no existe")
    void shouldReturnFalseWhenSpecialityNotExists() {
        // Arrange — BD vacía

        // Act
        boolean exists = specialityRepository.existsByName("Dermatology");

        // Assert
        assertThat(exists).isFalse();
    }

    // ── findSpecialitiesWithActiveDoctors ────────────────────
// ── findSpecialitiesWithActiveDoctors ────────────────────

    @Test
    @DisplayName("Retorna especialidades que tienen al menos un doctor activo")
    void shouldReturnSpecialitiesWithActiveDoctors() {
        // Arrange
        Speciality cardiology = savedSpeciality("Cardiology");
        Speciality neurology  = savedSpeciality("Neurology");

        doctorRepository.save(Doctor.builder()
                .fullName("Doc Activo")              // ← faltaba
                .documentType("CC")                  // ← faltaba (NOT NULL)
                .documentNumber("DOC-001")           // ← faltaba (NOT NULL)
                .email("active@test.com")            // ← faltaba
                .registerNum("REG-001")              // ← faltaba
                .speciality(cardiology)
                .status(PersonStatus.ACTIVE)
                .build());

        doctorRepository.save(Doctor.builder()
                .fullName("Doc Inactivo")            // ← faltaba
                .documentType("CC")                  // ← faltaba (NOT NULL)
                .documentNumber("DOC-002")           // ← faltaba (NOT NULL)
                .email("inactive@test.com")          // ← faltaba
                .registerNum("REG-002")              // ← faltaba
                .speciality(neurology)
                .status(PersonStatus.INACTIVE)
                .build());

        // Act
        List<Speciality> result = specialityRepository.findSpecialitiesWithActiveDoctors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("No retorna especialidades sin doctores activos")
    void shouldReturnEmptyWhenNoActiveDoctors() {
        // Arrange
        Speciality cardiology = savedSpeciality("Cardiology");

        doctorRepository.save(Doctor.builder()
                .fullName("Doc Inactivo")            // ← faltaba
                .documentType("CC")                  // ← faltaba (NOT NULL)
                .documentNumber("DOC-003")           // ← faltaba (NOT NULL)
                .email("inactive2@test.com")         // ← faltaba
                .registerNum("REG-003")              // ← faltaba
                .speciality(cardiology)
                .status(PersonStatus.INACTIVE)
                .build());

        // Act
        List<Speciality> result = specialityRepository.findSpecialitiesWithActiveDoctors();

        // Assert
        assertThat(result).isEmpty();
    }
}