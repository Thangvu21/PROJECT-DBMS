package dbmsforeveread.foreveread.book;

import co.elastic.clients.elasticsearch.nodes.Http;
import dbmsforeveread.foreveread.bookCategory.BookAuthorRepository;
import dbmsforeveread.foreveread.bookCategory.BookCategoryRepository;
import dbmsforeveread.foreveread.SearchEngine.domain.Product;
import dbmsforeveread.foreveread.SearchEngine.service.ProductService;
import dbmsforeveread.foreveread.category.Category;
import dbmsforeveread.foreveread.inventory.InventoryRepository;
import dbmsforeveread.foreveread.orderItem.OrderItemRepository;
import jakarta.servlet.annotation.WebFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@WebFilter
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final ProductService productService;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final BookAuthorRepository bookAuthorRepository;
    @Autowired
    private BookRedisService bookRedisServiceImpli;

    public static final long DEFAULT_TTL = 5 * 60;

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookDetails(@PathVariable Long id) {
        BookDTO book = bookRedisServiceImpli.getBookFromRedis(id.toString());
        if (book == null) {
            book = (bookService.getBookDetails(id) != null)
                        ? bookService.getBookDetails(id)
                        : new BookDTO();
            // trả về 1 bookDTO trống set thời gian sốsosongs luôn giải quyết được cái Cache Aside
            bookRedisServiceImpli.setTTLForKey(id.toString(), DEFAULT_TTL, TimeUnit.SECONDS);
            bookRedisServiceImpli.addBookToRedis(book);
        }
        return ResponseEntity.ok(book);
//        return ResponseEntity.ok(bookService.getBookDetails(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        // update cache khi có sự thay đổi trong nhưững quyển trong cache
        // nếu không tìm thâấy thì thôi không cần update
        // trả lời câu hỏi tại sao cần set db trước xoá cache sau để đống bộ dữ liệu
        BookDTO bookDTO = bookRedisServiceImpli.getBookFromRedis(id.toString());
        if (bookDTO != null) {
            bookRedisServiceImpli.deleteBookToRedis(id.toString());
            BookDTO bookDTO1 = bookService.getBookDetails(id);
            bookRedisServiceImpli.addBookToRedis(bookDTO1);
        }
//        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {

        bookService.deleteBook(id);
        BookDTO bookDTO = bookRedisServiceImpli.getBookFromRedis(id.toString());
        if (bookDTO != null) {
            bookRedisServiceImpli.deleteBookToRedis(id.toString());
        }
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/search")
//    public Page<BookSearchResultDTO> searchBooks(BookSearchRequest request) {
////        return bookService.searchBooks(request);
//        return productService.searchProductsByName(request);
//    }

//    @GetMapping("/search")
//    public ResponseEntity<Page<Product>> searchBooks(@RequestParam(required = false) String title,
//                                                     @RequestParam(defaultValue = "0") int page,
//                                                     @RequestParam(defaultValue = "10") int size) {
//        try {
//            Page<Product> products = productService.searchProductsByName(title, PageRequest.of(page, size));
//            return ResponseEntity.ok(products);
//        } catch (Exception e) {
//            log.error("Error occurred while searching books", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @GetMapping("/{bookId}/categories")
    public ResponseEntity<Set<Category>> getBookCategories(@PathVariable Long bookId) {
        Set<Category> categories = bookService.getBookCategories(bookId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{bookId}/recommendations")
    public ResponseEntity<List<BookRecommendedResponse>> getRecommendedBooks(@PathVariable Long bookId) {
        List<BookRecommendedResponse> recommendedBooks = bookService.getRecommendedBooks(bookId);
        return ResponseEntity.ok(recommendedBooks);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "") String category,
            @RequestParam(required = false, defaultValue = "") String publisher,
            @RequestParam(required = false, defaultValue = "") String minPrice,
            @RequestParam(required = false, defaultValue = "") String maxPrice,
            Pageable pageable
    ) throws IOException {
        Map<String, Object> filters = new HashMap<>();
        if (!category.isEmpty()) {
            filters.put("category", category);
        }
        if (!publisher.isEmpty()) {
            filters.put("publisher", publisher);
        }
        Double minPriceValue = null;
        if (!minPrice.isEmpty() && !minPrice.equalsIgnoreCase("null")) {
            minPriceValue = Double.parseDouble(minPrice);
        }
        Double maxPriceValue = null;
        if (!maxPrice.isEmpty() && !maxPrice.equalsIgnoreCase("null") && !maxPrice.equalsIgnoreCase("undefined")) {
            maxPriceValue = Double.parseDouble(maxPrice);
        }

        Page<Product> products = productService.searchProducts(title, category,publisher, minPriceValue, maxPriceValue, pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody BookDTO book) {
        try {
//            Book savedBook = bookService.addBookEvent(book);
            bookService.addBookEvent(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(book);
        } catch (Exception e) {
            // Handle any exceptions and return appropriate error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
