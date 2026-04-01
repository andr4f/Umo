package unimag.proyect.entities;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Table(name = "office")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_office")
    private UUID idOffice;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String location;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
}