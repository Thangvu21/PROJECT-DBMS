package dbmsforeveread.foreveread.SyncData;

import dbmsforeveread.foreveread.author.Author;
import dbmsforeveread.foreveread.book.Book;
import dbmsforeveread.foreveread.book.BookRepository;
import dbmsforeveread.foreveread.category.Category;
import dbmsforeveread.foreveread.exceptions.ResourceNotFoundException;
import dbmsforeveread.foreveread.neo4j.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Neo4jConsumer {
    private final Neo4jBookRepository neo4jBookRepository;
    private final Neo4jAuthorRepository neo4jAuthorRepository;
    private final Neo4jCategoryRepository neo4jCategoryRepository;
    private final BookRepository bookRepository;

    @KafkaListener(topics = "book-events", groupId = "better_read")
    public void consumeBookEvent(BookEvent bookEvent) {
        String eventType = bookEvent.getEventType();
        Long bookId = bookEvent.getBookId();
        Book book = bookRepository.findById(bookEvent.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookEvent.getBookId()));

        switch (eventType) {
            case "ADD":
            case "UPDATE":
                BookNode bookNode = neo4jBookRepository.findByBookId(bookId);
                if (bookNode == null) {
                    bookNode = new BookNode();
                    bookNode.setBookId(book.getId());
                }
                bookNode.setTitle(book.getTitle());
                bookNode.setImageUrl(book.getImageUrl());

                List<AuthorNode> authorNodes = new ArrayList<>();
                for (Author author : book.getAuthors()) {
                    AuthorNode authorNode = neo4jAuthorRepository.findByAuthorId(author.getId());
                    if (authorNode == null) {
                        authorNode = new AuthorNode();
                        authorNode.setAuthorId(author.getId());
                        authorNode.setName(author.getName());
                    }
                    authorNodes.add(authorNode);
                }
                bookNode.setAuthors(authorNodes);

                List<CategoryNode> categoryNodes = new ArrayList<>();
                for (Category category : book.getCategories()) {
                    CategoryNode categoryNode = neo4jCategoryRepository.findByCategoryId(category.getId());
                    if (categoryNode == null) {
                        categoryNode = new CategoryNode();
                        categoryNode.setCategoryId(category.getId());
                        categoryNode.setName(category.getName());
                    }
                    categoryNodes.add(categoryNode);
                }
                bookNode.setCategoryNodes(categoryNodes);

                neo4jBookRepository.save(bookNode);

//                for (AuthorNode authorNode : authorNodes) {
//                    neo4jBookRepository.createWrittenByRelationship(bookNode, authorNode);
//                }
//
//                for (CategoryNode categoryNode : categoryNodes) {
//                    neo4jBookRepository.createInCategoryRelationship(bookNode, categoryNode);
//                }

                break;
            case "DELETE":
                neo4jBookRepository.deleteByBookId(bookId);
                break;
        }
    }
}