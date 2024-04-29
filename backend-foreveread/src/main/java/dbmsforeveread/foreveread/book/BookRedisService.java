
package dbmsforeveread.foreveread.book;

import dbmsforeveread.foreveread.config.BaseRedisService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface BookRedisService  {
    // clear cache
    void addBookToRedis (BookDTO book);
    void deleteBookToRedis (String id);
    BookDTO getBookFromRedis (String id);
    void setTTLForKey (String key, Long time, TimeUnit timeUnit);

}
