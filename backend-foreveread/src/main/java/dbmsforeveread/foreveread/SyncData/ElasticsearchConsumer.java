package dbmsforeveread.foreveread.SyncData;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchConsumer {
    private final BookRepository bookRepository;

    @KafkaListener(topics = "book-events", groupId = "better_read")
    public void consumeBookEvent(BookEvent bookEvent) {
        Long bookId = bookEvent.getBookId();
        String eventType = bookEvent.getEventType();

        switch (eventType) {
            case "ADD":
            case "UPDATE":
                Book book = bookRepository.findById(bookId).orElse(null);
                if (book != null) {
                }
                break;
            case "DELETE":
                break;
        }
    }
}
