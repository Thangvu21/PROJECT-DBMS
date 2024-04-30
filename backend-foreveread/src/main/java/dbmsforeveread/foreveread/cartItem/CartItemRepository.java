package dbmsforeveread.foreveread.cartItem;

import dbmsforeveread.foreveread.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);

    @Query("SELECT ci FROM CartItem ci LEFT JOIN FETCH ci.book b LEFT JOIN FETCH b.inventory WHERE ci.cart.id = :cartId AND ci.book.id = :bookId")
    Optional<CartItem> findByCartIdAndBookIdWithInventory(@Param("cartId") Long cartId, @Param("bookId") Long bookId);

    @Query("SELECT ci FROM CartItem ci LEFT JOIN FETCH ci.book b LEFT JOIN FETCH b.inventory WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithInventory(@Param("cartId") Long cartId);

    void deleteByCartId(Long cartId);
    void deleteByBookId(Long bookId);
}