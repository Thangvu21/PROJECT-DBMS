package dbmsforeveread.foreveread.bookCategory;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@Data
@EqualsAndHashCode
public class BookCategoryId implements Serializable {
    private Long bookId;
    private Long categoryId;
}
