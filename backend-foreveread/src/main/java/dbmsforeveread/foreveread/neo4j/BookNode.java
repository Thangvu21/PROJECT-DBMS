package dbmsforeveread.foreveread.neo4j;

import jakarta.persistence.GeneratedValue;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@Node("Book")
public class BookNode {
    @Id
    @GeneratedValue
    private Long id;
    @Property("title")
    private String title;

    @Property("book_id")
    private Long bookId;

    @Property("imageUrl")
    private String imageUrl;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private List<AuthorNode> authors;

    @Relationship(type = "IN_CATEGORY", direction = Relationship.Direction.OUTGOING)
    private List<CategoryNode> categoryNodes ;
}
