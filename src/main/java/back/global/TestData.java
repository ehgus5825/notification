package back.global;

import back.ingredient.application.domain.Ingredient;
import back.ingredient.application.domain.IngredientStorageType;
import back.ingredient.application.domain.SuggestedIngredient;
import back.member.application.domain.Member;
import back.member.application.domain.MemberProfileImage;
import back.member.application.domain.MemberStatus;
import back.notice.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
