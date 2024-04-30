package dbmsforeveread.foreveread.bookCategory;

import dbmsforeveread.foreveread.bookCategory.BookAuthorId;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "book_author")
@IdClass(BookAuthorId.class)
@Data
public class BookAuthor {
    @Id
    @Column(name = "book_id")
    private Long bookId;

    @Id
    @Column(name = "author_id")
    private Long authorId;

}