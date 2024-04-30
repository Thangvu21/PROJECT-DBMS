package dbmsforeveread.foreveread.cart;

import dbmsforeveread.foreveread.book.BookService;
import dbmsforeveread.foreveread.cartItem.CartItem;
import dbmsforeveread.foreveread.cartItem.CartItemDTO;
import dbmsforeveread.foreveread.exceptions.InsufficientInventoryException;
import dbmsforeveread.foreveread.order.Order;
import dbmsforeveread.foreveread.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final BookService bookService;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@RequestParam Long userId) {
        CartDTO cartDTO = cartService.getCartDTOByUserId(userId);
        return ResponseEntity.ok(cartDTO);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            @RequestParam Long bookId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        try {
            CartDTO cartDTO = cartService.addToCart(bookId, userId, quantity);
            return ResponseEntity.ok(cartDTO);
        } catch (InsufficientInventoryException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/items/{bookId}")
    public ResponseEntity<CartDTO> updateQuantity(
            @PathVariable Long bookId,
            @RequestParam Long userId,
            @RequestParam int quantity
    ) {
        try {
            CartDTO cartDTO = cartService.updateQuantity(bookId, userId, quantity);
            return ResponseEntity.ok(cartDTO);
        } catch (InsufficientInventoryException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Long bookId,
            @RequestParam Long userId
    ) {
        CartDTO cartDTO = cartService.removeFromCart(bookId, userId);
        return ResponseEntity.ok(cartDTO);
    }
//
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam Long userId) {
        Order order = cartService.checkout(userId);
        return ResponseEntity.ok(order);
    }
}