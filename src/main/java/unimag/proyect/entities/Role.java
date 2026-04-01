package unimag.proyect.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    private String id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "Role")
    private List<SystemUser> systemuser; //evaluar si es necesario
}
