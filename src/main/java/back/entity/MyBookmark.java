package back.entity;

import back.common.BaseTimeEntity;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "recipe_bookmark_member")
@NoArgsConstructor
public class MyBookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @Column(name = "member_email")
    private String memberId;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "deleted")
    private Boolean deleted;
}
