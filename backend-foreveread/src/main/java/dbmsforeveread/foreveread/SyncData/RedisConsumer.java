//package dbmsforeveread.foreveread.SyncData;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dbmsforeveread.foreveread.book.Book;
//import dbmsforeveread.foreveread.book.BookRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class RedisConsumer {
//    private final BookRepository bookRepository;
//    private final RedisTemplate<String, String> redisTemplate;
//    private final ObjectMapper objectMapper;
//
//    @KafkaListener(topics = "book-events")
//    public void consumeBookEvent(BookEvent bookEvent) {
//        Long bookId = bookEvent.getBookId();
//        String eventType = bookEvent.getEventType();
//
//        switch (eventType) {
//            case "UPDATE":
//                Book book = bookRepository.findById(bookId).orElse(null);
//                if (book != null) {
//                    try {
//                        String bookJson = objectMapper.writeValueAsString(book);
//                        redisTemplate.opsForValue().set("book:" + bookId, bookJson);
//                    } catch (JsonProcessingException e) {
//                    }
//                }
//                break;
//            case "DELETE":
//                redisTemplate.delete("book:" + bookId);
//                break;
//        }
//    }
//}