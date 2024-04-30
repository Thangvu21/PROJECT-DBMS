package dbmsforeveread.foreveread.neo4j;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Author")
@Data
public class AuthorNode {
    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("author_id")
    private Long authorId;
}
