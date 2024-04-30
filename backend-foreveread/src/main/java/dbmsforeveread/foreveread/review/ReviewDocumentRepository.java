package dbmsforeveread.foreveread.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReviewDocumentRepository extends MongoRepository<ReviewDocument, String> {
    Page<ReviewDocument> findByBookId(Long bookId, Pageable pageable);
    List<ReviewDocument> findByBookId(Long bookId);

    void deleteAllByBookId(Long bookId);
}
