package back.notification.adapter.out.persistence;

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

    public void create(String memberId) {
        repository.opsForValue().set(memberId, false);
    }

    public void modify(String memberId, boolean value) {
        repository.opsForValue().set(memberId, value);
    }

    public Boolean getSign(String memberId) {
        return repository.opsForValue().get(memberId);
    }
}