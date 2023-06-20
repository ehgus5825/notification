package back.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "comment_heart")
@Getter
@NoArgsConstructor
public class CommentHeart {

    @Id
    @Column(name = "comment_id")
    Long commentId;

    @Column(name = "count")
    Integer count;

    @Column(name = "delete_status")
    Boolean deleteStatus;
}
