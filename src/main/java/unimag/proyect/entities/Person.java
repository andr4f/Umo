package unimag.proyect.entities;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public abstract class Person {   // ← solo esto cambia

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_person")
    private UUID idPerson;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PersonStatus status = PersonStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;
}
