package back.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Builder
@NoArgsConstructor
@Getter
@ToString
public class OutIngredientDTO {

    private String email;
    private String name;
    private Long count;

    @QueryProjection
    public OutIngredientDTO(String email, String name, Long count) {
        this.email = email;
        this.name = name;
        this.count = count;
    }
}
