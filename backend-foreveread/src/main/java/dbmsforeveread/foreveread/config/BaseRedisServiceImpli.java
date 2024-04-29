package dbmsforeveread.foreveread.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class BaseRedisServiceImpli implements BaseRedisService {
    private final RedisTemplate<String, Object> redisTemplate;


    // thao tác vowis thằng lưu 3 trươờng
    private final HashOperations<String, String, Object> hashOperations;

    public BaseRedisServiceImpli(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }


    @Override
    public void set(String key, String value) {
        // nhuw set qua terminal thoi
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setTimeToLive(String key, long timeOutInDays, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeOutInDays, timeUnit);
    }

    @Override
    public void hashSet(String key, String field, Object value) {
        hashOperations.put(key, field, value);
    }

    @Override
    public boolean hashExists(String key, String field) {
        return hashOperations.hasKey(key, field);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<String, Object> getField(String key) {
        return hashOperations.entries(key);
    }

    @Override
    public Object hashGet(String key, String field) {
        return hashOperations.get(key, field);
    }

    @Override
    public List<Object> hasGetByFieldPrefix(String key, String fieldPrefix) {
        List<Object> objects = new ArrayList<>();

        Map<String, Object> hasEntries = hashOperations.entries(key);
        for (Map.Entry<String, Object> entry : hasEntries.entrySet()) {
            if (entry.getKey().startsWith(fieldPrefix)) {
                objects.add(entry.getValue());
            }
        }
        return objects;
    }

    @Override
    public Set<String> getFieldPrefixes(String key) {
        // Trong trường hợp này, các khóa đó là các trường (fields) trong hash.
        return hashOperations.entries(key).keySet();
    }

    @Override
    public int numberFieldOfKey(String key) {
        Map<String, Object> entries = hashOperations.entries(key);
        return entries.size();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(String key, String field) {
        hashOperations.delete(key, field);
    }

    @Override
    public void delete(String key, List<String> fields) {
        for (String field : fields) {
            hashOperations.delete(key, field);
        }
    }

    public boolean isExistKeyWithHash(String key) {
        return hashOperations.getOperations().hasKey(key);
    }
}
