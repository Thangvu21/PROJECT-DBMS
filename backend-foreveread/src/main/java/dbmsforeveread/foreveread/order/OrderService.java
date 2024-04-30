package dbmsforeveread.foreveread.order;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookService;
import dbmsforeveread.foreveread.cart.Cart;
import dbmsforeveread.foreveread.cart.CartService;
import dbmsforeveread.foreveread.cartItem.CartItem;
import dbmsforeveread.foreveread.orderItem.OrderItem;
import dbmsforeveread.foreveread.user.User;
import dbmsforeveread.foreveread.user.UserProfile;
import dbmsforeveread.foreveread.user.UserRepository;
import dbmsforeveread.foreveread.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private BookService bookService;
    @Transactional
    public Order createOrder(User user, List<CartItem> cartItems, CheckoutRequest checkoutRequest) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setBillingAddress(checkoutRequest.getBillingAddress());

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Book book = cartItem.getBook();
                    int quantity = cartItem.getQuantity();

                    bookService.updateInventory(book.getId(), -quantity);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setBook(book);
                    orderItem.setQuantity(quantity);
                    orderItem.setPrice(book.getPrice());

                    return orderItem;
                })
                .collect(Collectors.toList());

        BigDecimal totalAmount = orderItems.stream()
                .map(orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }
}
