package dbmsforeveread.foreveread.book;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dbmsforeveread.foreveread.author.Author;
import dbmsforeveread.foreveread.category.Category;
import dbmsforeveread.foreveread.inventory.Inventory;
import dbmsforeveread.foreveread.publisher.Publisher;
import dbmsforeveread.foreveread.review.ReviewDocument;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String isbn;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    private String language;
    private Integer pages;
    private String description;
    private BigDecimal price;
    private String imageUrl;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    @JsonIgnore
    private List<Author> authors;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @JsonIgnore
    private Set<Category> categories = new HashSet<>();

    @OneToOne(mappedBy = "book")
    @JsonIgnore
    private Inventory inventory;

    @Transient
    private Double avgRating;

    @Transient
    private List<ReviewDocument> reviews;
}