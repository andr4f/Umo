package unimag.proyect.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sys_user")
@PrimaryKeyJoinColumn(name = "id_person")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class SystemUser extends Person {
    @Column (name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role",nullable = false)
        private Role role;
}
