package back;

import back.entity.Ingredient;
import back.entity.IngredientStorageType;
import back.entity.SuggestedIngredient;
import back.entity.Member;
import back.entity.MemberProfileImage;
import back.entity.MemberStatus;
import back.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TestData {

    public Member createMember(String memberId){
        Member member = Member.builder()
                .email(memberId)
                .password("password123!")
                .nickname("닉네임뿅")
                .profile(MemberProfileImage.PROFILE_IMAGE_FIVE)
                .memberStatus(MemberStatus.STEADY_STATUS)
                .build();

        return member;
    }

    public Ingredient createIngredient(String ingredientName, String memberId, Integer days){
        Ingredient ingredient = Ingredient.builder()
                .email(memberId)
                .image(1)
                .name(ingredientName)
                .capacityUnit("g")
                .storageMethod(IngredientStorageType.FRIDGE)
                .registrationDate(LocalDate.now())
                .deleted(false)
                .capacity(70.0)
                .expirationDate(LocalDate.now().plusDays(days))
                .build();

        return ingredient;
    }

    public Notice createNotice(String title) {
        Notice notice = Notice.builder()
                .title(title)
                .content("어쩌고 저쩌고 웅성웅성")
                .build();

        return notice;
    }

    public SuggestedIngredient createSuggestedIngredient(String email, String name) {
        SuggestedIngredient ingredient = SuggestedIngredient.builder()
                .email(email)
                .unit("g")
                .name(name)
                .build();

        return ingredient;
    }
}
