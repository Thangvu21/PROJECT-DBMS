package dbmsforeveread.foreveread.bookCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    void deleteAllByBookId(Long bookId);
}
