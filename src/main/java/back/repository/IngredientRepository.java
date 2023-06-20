package back.repository;

import back.entity.Ingredient;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("delete from Ingredient i where i.deleted = true")
    void deleteIngredient();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("delete from SuggestedIngredient si where si.name = :name")
    void deleteSuggestedIngredient(@Param("name") String name);
}
