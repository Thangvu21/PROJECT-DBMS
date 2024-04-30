package dbmsforeveread.foreveread.inventory;

import dbmsforeveread.foreveread.book.Book;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Inventory {
    @Id
    @Column(name = "book_id")
    private Long bookId;

    @OneToOne
    @JoinColumn(name = "book_id")
    private Book book;

    private int quantity;
}
