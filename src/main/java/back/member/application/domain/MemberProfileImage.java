package back.member.application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberProfileImage {

    PROFILE_IMAGE_ONE("IMG_9709.JPG"),
    PROFILE_IMAGE_TWO("IMG_9706.JPG"),
    PROFILE_IMAGE_THREE("IMG_9707.JPG"),
    PROFILE_IMAGE_FOUR("IMG_9708.JPG"),
    PROFILE_IMAGE_FIVE("IMG_9705.JPG");

    private String name;
}
