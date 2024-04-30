package dbmsforeveread.foreveread.SyncData;

import dbmsforeveread.foreveread.review.ReviewDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDBConsumer {
    private final ReviewDocumentRepository reviewDocumentRepository;

    @KafkaListener(topics = "book-events", groupId = "better_read")
    public void consumeBookEvent(BookEvent bookEvent) {
        Long bookId = bookEvent.getBookId();
        String eventType = bookEvent.getEventType();

        switch (eventType) {
            case "DELETE":
                reviewDocumentRepository.deleteAllByBookId(bookId);
                break;
        }
    }
}
