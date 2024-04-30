package dbmsforeveread.foreveread.neo4j;

import jakarta.persistence.GeneratedValue;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@Node("Category")
public class CategoryNode {
    @Id
    @GeneratedValue
    private Long id;

    @Property("category_id")
    private Long categoryId;

    @Property("name")
    private String name;
}
