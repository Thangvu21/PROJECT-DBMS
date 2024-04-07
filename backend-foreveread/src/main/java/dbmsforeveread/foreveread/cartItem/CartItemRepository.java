package dbmsforeveread.foreveread.cartItem;

import dbmsforeveread.foreveread.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);

    void deleteByCartId(Long id);

    List<CartItem> findByCartId(Long id);
}

