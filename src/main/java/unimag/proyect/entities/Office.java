package unimag.proyect.entities;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_office")
    private Integer idOffice;

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