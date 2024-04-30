package dbmsforeveread.foreveread.SyncData;

import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookDTO;
import dbmsforeveread.foreveread.book.BookRepository;
import dbmsforeveread.foreveread.bookCategory.BookAuthor;
import dbmsforeveread.foreveread.bookCategory.BookAuthorRepository;
import dbmsforeveread.foreveread.bookCategory.BookCategory;
import dbmsforeveread.foreveread.bookCategory.BookCategoryRepository;
import dbmsforeveread.foreveread.inventory.Inventory;
import dbmsforeveread.foreveread.inventory.InventoryRepository;
import dbmsforeveread.foreveread.publisher.Publisher;
import dbmsforeveread.foreveread.publisher.PublisherRepository;
import dbmsforeveread.foreveread.review.ReviewDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MySQLBookConsumer {
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final PublisherRepository publisherRepository;

    @KafkaListener(topics = "book-events", groupId = "better_read")
    public void consumeBookEvent(BookEvent bookEvent) {
        BookDTO bookDTO = bookEvent.getBookDTO();
        String eventType = bookEvent.getEventType();
        switch (eventType) {
            case "ADD":
                Book book = convertToBook(bookDTO);
                Book savedBook = bookRepository.save(book);

                bookDTO.getAuthorIds().forEach(authorId -> {
                    BookAuthor bookAuthor = new BookAuthor();
                    bookAuthor.setBookId(savedBook.getId());
                    bookAuthor.setAuthorId(authorId);
                    bookAuthorRepository.save(bookAuthor);
                });

                bookDTO.getCategoryIds().forEach(categoryId -> {
                    BookCategory bookCategory = new BookCategory();
                    bookCategory.setBookId(savedBook.getId());
                    bookCategory.setCategoryId(categoryId);
                    bookCategoryRepository.save(bookCategory);
                });

                Inventory inventory = new Inventory();
                inventory.setBookId(savedBook.getId());
                inventory.setQuantity(bookDTO.getQuantity());
                inventoryRepository.save(inventory);
                break;
            case "UPDATE":
                break;
            case "DELETE":
                bookAuthorRepository.deleteAllByBookId(bookDTO.getId());
                bookCategoryRepository.deleteAllByBookId(bookDTO.getId());
                inventoryRepository.deleteByBookId(bookDTO.getId());
                bookRepository.deleteById(bookDTO.getId());
                break;
        }
    }

    private Book convertToBook(BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setIsbn(bookDTO.getIsbn());

        Publisher publisher = publisherRepository.findById(bookDTO.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        book.setPublisher(publisher);

        book.setPublicationDate(bookDTO.getPublicationDate());
        book.setLanguage(bookDTO.getLanguage());
        book.setPages(bookDTO.getPages());
        book.setDescription(bookDTO.getDescription());
        book.setPrice(bookDTO.getPrice());
        book.setImageUrl(bookDTO.getImageUrl());

        Book savedBook = bookRepository.save(book);
        return savedBook;
    }
}