package dbmsforeveread.foreveread.cart;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookService;
import dbmsforeveread.foreveread.cartItem.CartItem;
import dbmsforeveread.foreveread.cartItem.CartItemDTO;
import dbmsforeveread.foreveread.cartItem.CartItemRepository;
import dbmsforeveread.foreveread.exceptions.BookNotFoundException;
import dbmsforeveread.foreveread.exceptions.InsufficientInventoryException;
import dbmsforeveread.foreveread.inventory.Inventory;
import dbmsforeveread.foreveread.inventory.InventoryRepository;
import dbmsforeveread.foreveread.order.CheckoutRequest;
import dbmsforeveread.foreveread.order.Order;
import dbmsforeveread.foreveread.order.OrderService;
import dbmsforeveread.foreveread.user.User;
import dbmsforeveread.foreveread.user.UserProfile;
import dbmsforeveread.foreveread.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final BookService bookService;
    private final UserRepository userRepository;
    private final OrderService orderService;
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BookNotFoundException("Cart not found for user: " + userId));
    }

    @Transactional
    public Order checkout(Long userId) {
        Cart cart = getCartByUserId(userId);
        List<CartItem> cartItems = cart.getCartItems();

        validateCartItems(cartItems);

        User user = cart.getUser();
        CheckoutRequest checkoutRequest = createCheckoutRequest(user);

        Order order = orderService.createOrder(user, cartItems, checkoutRequest);
        clearCart(userId);

        return order;
    }

    private void validateCartItems(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            int quantity = cartItem.getQuantity();

            if (book.getInventory().getQuantity() < quantity) {
                throw new InsufficientInventoryException("Insufficient inventory for book: " + book.getTitle());
            }
        }
    }

    private CheckoutRequest createCheckoutRequest(User user) {
        UserProfile userProfile = user.getUserProfile();
        return CheckoutRequest.builder()
                .shippingAddress(userProfile.getAddress())
                .billingAddress(userProfile.getAddress())
                .build();
    }

    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }
    @Transactional(readOnly = true)
    public CartDTO getCartDTOByUserId(Long userId) {
        Cart cart = getCartByUserId(userId);
        return mapCartToDTO(cart);
    }

    private CartDTO mapCartToDTO(Cart cart) {
        return CartDTO.builder()
                .id(cart.getId())
//                .user((cart.getUser()))
//                .createdAt(cart.getCreatedAt())
                .cartItems(mapCartItemsToDTO(cart.getCartItems()))
                .totalItems(calculateTotalItems(cart))
                .totalPrice(calculateCartTotal(cart))
                .build();
    }

    private List<CartItemDTO> mapCartItemsToDTO(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> CartItemDTO.builder()
                        .bookId(item.getBook().getId())
                        .bookImageUrl(item.getBook().getImageUrl())
                        .bookTitle(item.getBook().getTitle())
                        .price(item.getBook().getPrice())
                        .quantity(item.getQuantity())
                        .total(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();
    }

    private int calculateTotalItems(Cart cart) {
        return cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal calculateCartTotal(Cart cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional()
    public Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BookNotFoundException("User not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Transactional()
    public CartDTO addToCart(Long bookId, Long userId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Optional<CartItem> optionalCartItem = cartItemRepository.findByCartIdAndBookIdWithInventory(cart.getId(), bookId);
        CartItem cartItem;

        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            validateInventoryAvailability(cartItem.getBook().getInventory(), newQuantity);
            cartItem.setQuantity(newQuantity);
        } else {
            Book book = bookService.getBookById(bookId);
            validateInventoryAvailability(book.getInventory(), quantity);
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setBook(book);
            cartItem.setQuantity(quantity);
            cart.addCartItem(cartItem);
        }

        cartItemRepository.save(cartItem);
        updateInventoryQuantity(cartItem.getBook().getInventory(), -quantity);

        return mapCartToDTO(cart);
    }

    @Transactional()
    public CartDTO updateQuantity(Long bookId, Long userId, int quantity) {
        Cart cart = getCartByUserId(userId);
        CartItem cartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId)
                .orElseThrow(() -> new BookNotFoundException("Cart item not found"));

        Inventory inventory = cartItem.getBook().getInventory();
        int quantityDifference = quantity - cartItem.getQuantity();
        validateInventoryAvailability(inventory, quantityDifference);

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        updateInventoryQuantity(inventory, -quantityDifference);

        return mapCartToDTO(cart);
    }

    @Transactional()
    public CartDTO removeFromCart(Long bookId, Long userId) {
        Cart cart = getCartByUserId(userId);
        CartItem cartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId)
                .orElseThrow(() -> new BookNotFoundException("Cart item not found"));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        updateInventoryQuantity(cartItem.getBook().getInventory(), cartItem.getQuantity());

        return mapCartToDTO(cart);
    }

    @Transactional()
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        List<CartItem> cartItems = cartItemRepository.findByCartIdWithInventory(cart.getId());

        for (CartItem cartItem : cartItems) {
            updateInventoryQuantity(cartItem.getBook().getInventory(), cartItem.getQuantity());
        }

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getCartItems().clear();
    }

    private void validateInventoryAvailability(Inventory inventory, int requestedQuantity) {
        if (requestedQuantity > inventory.getQuantity()) {
            throw new InsufficientInventoryException("Insufficient inventory for the requested quantity");
        }
    }

    private void updateInventoryQuantity(Inventory inventory, int quantityChange) {
        inventory.setQuantity(inventory.getQuantity() + quantityChange);
        inventoryRepository.save(inventory);
    }
}