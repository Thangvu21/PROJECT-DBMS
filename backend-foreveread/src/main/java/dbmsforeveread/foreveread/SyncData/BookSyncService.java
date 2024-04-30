package dbmsforeveread.foreveread.SyncData;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookSyncService {
    private final KafkaTemplate<String, BookEvent> kafkaTemplate;

    public void publishBookEvent(BookDTO bookDTO, String eventType) {
        kafkaTemplate.send("book-events", new BookEvent(null, bookDTO, eventType, LocalDateTime.now()));
    }
}
