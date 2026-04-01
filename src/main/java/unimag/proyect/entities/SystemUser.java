package unimag.proyect.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class SystemUser extends Person {
@Column (name = "username", nullable = false)
private String username;

@Column(name = "password", nullable = false)
private String password;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_role",nullable = false)
    private Role role;


}
