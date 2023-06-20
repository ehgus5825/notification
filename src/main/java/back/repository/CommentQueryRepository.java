package back.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static back.entity.QCommentHeart.*;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager em;

    @Transactional
    public void deleteCommentHeart(List<Long> ids) {

        jpaQueryFactory.delete(commentHeart)
                .where(commentHeart.commentId.in(ids))
                .execute();

        em.flush();
        em.clear();
    }

    @Transactional
    public void deleteCommentHeartPeople(Long id) {

    }
}
