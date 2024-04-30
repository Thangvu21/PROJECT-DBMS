package dbmsforeveread.foreveread.SyncData;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookEvent {
    private Long bookId;
    private BookDTO bookDTO;
    private String eventType;
    private LocalDateTime timestamp;
}
