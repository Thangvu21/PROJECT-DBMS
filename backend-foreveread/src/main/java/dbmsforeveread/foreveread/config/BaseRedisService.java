package dbmsforeveread.foreveread.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface BaseRedisService {

    // lưu căp key -value vào trong redis
    void set(String key, String value);

    // set thời gian trong bộ nhớ cache của 1 cặp key-value
    void setTimeToLive(String key, long timeOutInDays, TimeUnit timeUnit);

    // lưu
    void hashSet(String key, String field, Object value);

    // kiểm tra toonf tại key với field đây ko
    boolean hashExists(String key, String field);

    Object get(String key);

    public Map<String, Object> getField(String key);

    Object hashGet(String key, String field);

    List<Object> hasGetByFieldPrefix(String key, String fieldPrefix);

    Set<String> getFieldPrefixes(String key);

    int numberFieldOfKey (String key);

    void delete(String key);

    void delete(String key, String field);

    void delete(String key, List<String> field);

    boolean isExistKeyWithHash(String key);

}
