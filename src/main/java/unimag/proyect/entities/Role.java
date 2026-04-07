package unimag.proyect.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_role")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
