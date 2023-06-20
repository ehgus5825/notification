package back.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;;

@Getter
@AllArgsConstructor
public enum IngredientStorageType {

    FREEZER("냉동"),
    FRIDGE("냉장"),
    SEASON("조미료"),
    ROOM("실온");

    private String type;

}
