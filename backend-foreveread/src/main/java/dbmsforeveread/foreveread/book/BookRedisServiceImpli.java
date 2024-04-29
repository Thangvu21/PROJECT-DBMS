package dbmsforeveread.foreveread.book;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbmsforeveread.foreveread.config.BaseRedisService;
import dbmsforeveread.foreveread.config.BaseRedisServiceImpli;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v2/redis")
@Service
@Component
@Slf4j
public class BookRedisServiceImpli extends BaseRedisServiceImpli implements BookRedisService {

    private final ObjectMapper redisObjectMapper;

    @Autowired
    public BookRedisServiceImpli(RedisTemplate<String, Object> redisTemplate, ObjectMapper redisObjectMapper) {
        super(redisTemplate);
        this.redisObjectMapper = redisObjectMapper;
    }

//    final String key = "Books";

    @Override
    public void addBookToRedis(BookDTO book) {
//        if (book == null) {
//            this.hashSet();
//        }
//        String field = key + book.getId();
        String key = book.getId().toString();
        try {
            String jsonString = redisObjectMapper.writeValueAsString(book);
            this.set(key , jsonString);
            this.setTimeToLive(key, 30, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to add book to Redis", e);
        }
    }


    @Override
    public void deleteBookToRedis(String id) {
//        String field = key + id;
        String json = (String) this.get(id);
        if (json != null) {
            this.delete(id);
        }
    }
    // hoặc ây tuwf t đã cái delete vơới update t từ h kiểu thêm vào redis đã

    @Override
    public BookDTO getBookFromRedis(String id) {
//        String field = key + id;
//        long startTime = System.currentTimeMillis();
        // check xem co ko
        String json = (String) this.get(id);
        BookDTO BookResponse = null;

        try {
            if (json != null) {
                BookResponse = redisObjectMapper.readValue(json, BookDTO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse book from Redis", e);
        }

//        long endTime = System.currentTimeMillis();
//        long searchTime = endTime - startTime;
//        log.info("Time to search bookId{} in Redis: {} ms", id, searchTime);
        return BookResponse;
    }

    @Override
    public void setTTLForKey(String key, Long time, TimeUnit timeUnit) {
        this.setTimeToLive(key, time, timeUnit);
    }
}