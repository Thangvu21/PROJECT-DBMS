package dbmsforeveread.foreveread.inventory;

import dbmsforeveread.foreveread.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByBookId(Long bookId);
    void deleteByBookId(Long bookId);
}
