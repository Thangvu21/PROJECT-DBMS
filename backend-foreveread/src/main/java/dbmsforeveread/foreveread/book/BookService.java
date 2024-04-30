package dbmsforeveread.foreveread.book;

import dbmsforeveread.foreveread.SyncData.BookSyncService;
import dbmsforeveread.foreveread.author.Author;
import dbmsforeveread.foreveread.author.AuthorDTO;
import dbmsforeveread.foreveread.author.AuthorRepository;
import dbmsforeveread.foreveread.bookCategory.BookAuthor;
import dbmsforeveread.foreveread.bookCategory.BookAuthorRepository;
import dbmsforeveread.foreveread.bookCategory.BookCategory;
import dbmsforeveread.foreveread.bookCategory.BookCategoryRepository;
import dbmsforeveread.foreveread.category.Category;
import dbmsforeveread.foreveread.category.CategoryDTO;
import dbmsforeveread.foreveread.category.CategoryRepository;
import dbmsforeveread.foreveread.exceptions.InsufficientInventoryException;
import dbmsforeveread.foreveread.exceptions.ResourceNotFoundException;
import dbmsforeveread.foreveread.inventory.Inventory;
import dbmsforeveread.foreveread.inventory.InventoryDTO;
import dbmsforeveread.foreveread.inventory.InventoryRepository;
import dbmsforeveread.foreveread.neo4j.RecommendationService;
import dbmsforeveread.foreveread.publisher.Publisher;
import dbmsforeveread.foreveread.publisher.PublisherDTO;
import dbmsforeveread.foreveread.publisher.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final RecommendationService recommendationService;
    private final BookSyncService bookSyncService;
    private final BookAuthorRepository bookAuthorRepository;
    private final BookCategoryRepository bookCategoryRepository;
    @Transactional(readOnly = true)
    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }


    public BigDecimal getBookPriceById(Long bookId) {
        Book book = getBookById(bookId);
        return book.getPrice();
    }

//    @Transactional
//    public Book addBook(BookDTO bookDTO) {
//        Book book = new Book();
//        book.setTitle(bookDTO.getTitle());
//        book.setIsbn(bookDTO.getIsbn());
//
//        Publisher publisher = publisherRepository.findById(bookDTO.getPublisherId())
//                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
//        book.setPublisher(publisher);
//
//        book.setPublicationDate(bookDTO.getPublicationDate());
//        book.setLanguage(bookDTO.getLanguage());
//        book.setPages(bookDTO.getPages());
//        book.setDescription(bookDTO.getDescription());
//        book.setPrice(bookDTO.getPrice());
//        book.setImageUrl(bookDTO.getImageUrl());
//
//        Book savedBook = bookRepository.save(book);
//
//        bookDTO.getAuthorIds().forEach(authorId -> {
//            BookAuthor bookAuthor = new BookAuthor();
//            bookAuthor.setBookId(savedBook.getId());
//            bookAuthor.setAuthorId(authorId);
//            bookAuthorRepository.save(bookAuthor);
//        });
//
//        bookDTO.getCategoryIds().forEach(categoryId -> {
//            BookCategory bookCategory = new BookCategory();
//            bookCategory.setBookId(savedBook.getId());
//            bookCategory.setCategoryId(categoryId);
//            bookCategoryRepository.save(bookCategory);
//        });
//
//        Inventory inventory = new Inventory();
//        inventory.setBookId(savedBook.getId());
//        inventory.setQuantity(bookDTO.getQuantity());
//        inventoryRepository.save(inventory);
//
//        //publish a kafka event for adding
//        bookSyncService.publishBookEvent(savedBook.getId(), "ADD");
//        return savedBook;
//    }
    public void addBookEvent(BookDTO bookDTO){
        bookSyncService.publishBookEvent(bookDTO, "ADD");
    }

    @Transactional
    public Book updateBook(Long id, Book book) {
        Book existingBook = getBookById(id);
        existingBook.setTitle(book.getTitle());
        existingBook.setIsbn(book.getIsbn());
//        bookSyncService.publishBookEvent(book, "UPDATE");
        return bookRepository.save(existingBook);
    }

    @Transactional()
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
//        bookSyncService.publishBookEvent(id, "DELETE");
    }

//    public Page<BookSearchResultDTO> searchBooks(String query, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Book> books = bookRepository.findByTitleContainingIgnoreCase(query, pageable);
//        return books.map(this::convertToSearchResultDTO);
//    }
//    public Page<BookSearchResultDTO> searchBooks(BookSearchRequest request) {
//        String query = request.getQuery();
//        int page = request.getPage();
//        int size = request.getSize();
//        String priceRange = request.getPrice();
////        List<Category> categories = request.getCategory();
//
//        BigDecimal minPrice = null;
//        BigDecimal maxPrice = null;
//        if (priceRange != null && !priceRange.isEmpty()) {
//            String[] prices = priceRange.split("-");
//            minPrice = new BigDecimal(prices[0]);
//            maxPrice = prices.length > 1 ? new BigDecimal(prices[1]) : null;
//        }
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Book> bookPage = bookRepository.searchBooks(query, minPrice, maxPrice, pageable);
//        return bookPage.map(this::convertToSearchResultDTO);
//    }

    private BookSearchResultDTO convertToSearchResultDTO(Book book) {
        BookSearchResultDTO dto = new BookSearchResultDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setPrice(book.getPrice());
        dto.setImageUrl(book.getImageUrl());
        return dto;
    }

    public BookDTO getBookDetails(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        return convertToDTO(book);
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(book.getId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setIsbn(book.getIsbn());
        bookDTO.setPublisher(convertToPublisherDTO(book.getPublisher()));
        bookDTO.setPublicationDate(book.getPublicationDate());
        bookDTO.setLanguage(book.getLanguage());
        bookDTO.setPages(book.getPages());
        bookDTO.setDescription(book.getDescription());
        bookDTO.setPrice(book.getPrice());
        bookDTO.setImageUrl(book.getImageUrl());
//        bookDTO.setAuthors(convertToAuthorDTOs(book.getAuthors()));
        bookDTO.setCategory(convertToCategoryDTOs(book.getCategories()));
        bookDTO.setInventory(convertToInventoryDTO(book.getInventory()));
        return bookDTO;
    }
    private PublisherDTO convertToPublisherDTO(Publisher publisher) {
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(publisher.getId());
        publisherDTO.setName(publisher.getName());
        return publisherDTO;
    }

    private List<AuthorDTO> convertToAuthorDTOs(List<Author> authors) {
        return authors.stream()
                .map(this::convertToAuthorDTO)
                .collect(Collectors.toList());
    }

    private AuthorDTO convertToAuthorDTO(Author author) {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(author.getId());
        authorDTO.setName(author.getName());
        return authorDTO;
    }

    private Set<CategoryDTO> convertToCategoryDTOs(Set<Category> categories) {
        return categories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toSet());
    }

    private CategoryDTO convertToCategoryDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        categoryDTO.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        return categoryDTO;
    }



    private InventoryDTO convertToInventoryDTO(Inventory inventory) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setQuantity(inventory.getQuantity());
        return inventoryDTO;
    }

    public Set<Category> getBookCategories(Long bookId) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            return book.getCategories();
        }
        return Collections.emptySet();
    }

    public List<BookRecommendedResponse> getRecommendedBooks(Long bookId) {
        List<Map<String, Object>> recommendedBooksData = recommendationService.getRecommendations(bookId);
        return recommendedBooksData.stream()
                .map(bookData -> {
                    bookData.forEach((k, v) -> System.out.println(k + ": " + v));
                    BookRecommendedResponse bookRecommendedResponse = new BookRecommendedResponse();
                    bookRecommendedResponse.setBookId((Long) bookData.get("bookId"));
                    bookRecommendedResponse.setTitle((String) bookData.get("title"));
//                    bookDTO.setIsbn((String) bookData.get("isbn"));
//                    bookDTO.setPublicationDate((LocalDate) bookData.get("publicationDate"));
//                    bookDTO.setLanguage((String) bookData.get("language"));
//                    bookDTO.setPages((Integer) bookData.get("pages"));
//                    bookDTO.setDescription((String) bookData.get("description"));
//                    bookDTO.setPrice((BigDecimal) bookData.get("price"));
                    bookRecommendedResponse.setImageUrl((String) bookData.get("imageUrl"));
                    return bookRecommendedResponse;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateInventory(Long bookId, int quantityChange) {
        Book book = getBookById(bookId);
        Inventory inventory = book.getInventory();
        int newQuantity = inventory.getQuantity() + quantityChange;

        if (newQuantity < 0) {
            throw new InsufficientInventoryException("Insufficient inventory for book: " + book.getTitle());
        }

        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
    }
}
