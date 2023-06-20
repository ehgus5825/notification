package back.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ingredient")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "capacity", nullable = false, length = 30)
    private Double capacity;

    @Column(name = "capacity_unit", nullable = false, length = 30)
    private String capacityUnit;

    @Column(name = "storage_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private IngredientStorageType storageMethod;

    @Column(name = "image", nullable = false, length = 100)
    private Integer image;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "email", nullable = false)
    private String email;
}
