package dbmsforeveread.foreveread.bookCategory;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.category.Category;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "book_category")
@IdClass(BookCategoryId.class)
public class BookCategory {

    @Id
    @Column(name = "book_id")
    private Long bookId;

    @Id
    @Column(name = "category_id")
    private Long categoryId;
}