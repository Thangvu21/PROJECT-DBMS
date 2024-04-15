package dbmsforeveread.foreveread.book;

import dbmsforeveread.foreveread.category.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRedisService bookRedisServiceImpli;
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookDetails(@PathVariable Long id) {
        BookDTO book = bookRedisServiceImpli.getBookFromRedis(id.toString());
        if (book == null) {
            book = bookService.getBookDetails(id);
            bookRedisServiceImpli.addBookToRedis(book);
        }
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public Page<BookSearchResultDTO> searchBooks(BookSearchRequest request) {
        return bookService.searchBooks(request);
    }

    @GetMapping("/{bookId}/categories")
    public ResponseEntity<Set<Category>> getBookCategories(@PathVariable Long bookId) {
        Set<Category> categories = bookService.getBookCategories(bookId);
        return ResponseEntity.ok(categories);
    }
}
