package back.persistence;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationByMemberAdapter {

    private final RedisTemplate<String, Boolean> repository;

    public NotificationByMemberAdapter(
            @Qualifier("notificationRedisTemplate") RedisTemplate<String, Boolean> repository) {
        this.repository = repository;
    }

    public void update(String memberId) {
        repository.opsForValue().set(memberId, true);
    }
}