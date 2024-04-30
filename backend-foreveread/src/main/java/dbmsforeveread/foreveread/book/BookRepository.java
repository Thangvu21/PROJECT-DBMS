package dbmsforeveread.foreveread.book;

import dbmsforeveread.foreveread.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    @Query("SELECT b FROM Book b WHERE " +
            "(:query IS NULL OR b.title LIKE %:query%) AND " +
            "(:minPrice IS NULL OR :maxPrice IS NULL OR b.price BETWEEN :minPrice AND :maxPrice)")
            Page<Book> searchBooks(
            @Param("query") String query,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    public void deleteById(Long id);

}