package dbmsforeveread.foreveread.book;

import com.fasterxml.jackson.annotation.JsonFormat;
import dbmsforeveread.foreveread.author.Author;
import dbmsforeveread.foreveread.author.AuthorDTO;
import dbmsforeveread.foreveread.category.Category;
import dbmsforeveread.foreveread.category.CategoryDTO;
import dbmsforeveread.foreveread.inventory.Inventory;
import dbmsforeveread.foreveread.inventory.InventoryDTO;
import dbmsforeveread.foreveread.publisher.Publisher;
import dbmsforeveread.foreveread.publisher.PublisherDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class BookDTO {
    private Long id;
    private String title;
    private String isbn;
    private PublisherDTO publisher;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;
    private String language;
    private Integer pages;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private List<Long> authorIds;
    private Set<CategoryDTO> category;
    private InventoryDTO inventory;
    private Integer quantity;
    private List<Long> categoryIds;
    private Long publisherId;

}