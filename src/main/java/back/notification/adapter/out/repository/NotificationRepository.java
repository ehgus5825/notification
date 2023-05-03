package back.notification.adapter.out.repository;

import back.notification.application.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Transactional
    @Query("delete from Notifications n where n.type = 1 and n.createTime < :date")
    void deleteNotification(@Param("date") LocalDateTime date);

    @Modifying
    @Transactional
    @Query("delete from Notifications n where n.type != 1 and n.createTime < :date")
    void deleteDeadlineNotification(@Param("date") LocalDateTime date);

    /*
    @Query("select mn from MemberNotification mn where mn.email = :email")
    MemberNotifications findMemberNotification(@Param("email") String email);
     */
}
