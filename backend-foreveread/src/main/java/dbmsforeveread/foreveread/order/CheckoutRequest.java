package dbmsforeveread.foreveread.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutRequest {
    private String shippingAddress;
    private String billingAddress;
}
