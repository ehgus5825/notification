package back.repository;

import back.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("delete from Comment c where c.deletedState = true")
    void deleteComment();
}
