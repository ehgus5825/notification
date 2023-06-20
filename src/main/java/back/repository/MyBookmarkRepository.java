package back.repository;

import back.entity.MyBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MyBookmarkRepository extends JpaRepository<MyBookmark, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("delete from MyBookmark mk where mk.deleted = true")
    void deleteMyBookmark();
}
