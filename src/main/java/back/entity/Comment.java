package back.entity;

import back.common.BaseTimeEntityWithModify;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "recipe_comment")
@Getter
@NoArgsConstructor
public class Comment extends BaseTimeEntityWithModify {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_comment_id")
    private Long commentID;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeID;

    @Column(name = "member_email", nullable = false)
    private String memberID;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "modified_state", nullable = false)
    private Boolean modifiedState;

    @Column(name = "deleted_state")
    private Boolean deletedState;
}
