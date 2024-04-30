package dbmsforeveread.foreveread.SearchEngine.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.json.JsonData;
import dbmsforeveread.foreveread.SearchEngine.service.RedisServiceElasticImpli;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import dbmsforeveread.foreveread.SearchEngine.domain.Product;
import org.springframework.util.StringUtils;
import co.elastic.clients.elasticsearch.core.search.Hit;

@Repository
public class ProductRepository{

    public static final String PRODUCTS = "products";

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private RedisServiceElasticImpli redisServiceElacticImpli;

    public String createOrUpdateDocument( Product product ) throws IOException {
        IndexResponse response= elasticsearchClient.index(i->i
                .index(PRODUCTS)
                .id(product.getId())
                .document(product));

        return switch (response.result().name()) {
            case "Created" -> "Document has been created";
            case "Updated" -> "Document has been updated";
            default -> "Error has occurred";
        };
    }

    public Product findDocById(String productId) throws IOException {
        return elasticsearchClient.get(g->g.index(PRODUCTS).id(productId),Product.class).source();
    }

    public String deleteDocById(String productId) throws IOException {
        DeleteRequest deleteRequest = DeleteRequest.of(d->d.index(PRODUCTS).id(productId));
        DeleteResponse response =elasticsearchClient.delete(deleteRequest);

        return new StringBuffer(response.result().name().equalsIgnoreCase("NOT_FOUND")
                ?"Document not found with id" + productId
                :"Document has been deleted").toString();
    }

    public List<Product> findAll() throws IOException {
        SearchRequest request = SearchRequest.of(s->s.index(PRODUCTS));
        SearchResponse<Product> response = elasticsearchClient.search(request,Product.class);

        List<Product> products = new ArrayList<>();
        response.hits().hits().stream().forEach(object->{
            products.add(object.source());

        });
        return products;

    }

    public String bulkSave(List<Product> products) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        products.stream().forEach(product->br.operations(operation->
                operation.index(i->i
                        .index(PRODUCTS)
                        .id(product.getId())
                        .document(product))));

        BulkResponse response =elasticsearchClient.bulk(br.build());
        return response.errors() ? "Bulk has errors" : "Bulk save success";
    }

    public Product insertProduct(Product product) throws IOException {
        IndexResponse response = elasticsearchClient.index(i -> i
                .index(PRODUCTS)
                .document(product));

        if (response.result().equals(Result.Created) || response.result().equals(Result.Updated)) {
            // Cập nhật product với ID từ response, nếu ID ban đầu không được cung cấp.
            if (product.getId() == null) {
                product.setId(response.id());
            }
            return product; // Trả về đối tượng Product đã được cập nhật với ID.
        } else {
            throw new RuntimeException("Failed to insert product. Elasticsearch response: " + response.result().name());
        }
    }

        public List<Product> searchWithFilters(Map<String, Object> filters, Double minPrice, Double maxPrice) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(PRODUCTS)
                .query(q -> q
                        .bool(b -> {
                            List<Query> mustQueries = filters.entrySet().stream()
                                    .map(filter -> {
                                        if (filter.getKey().equalsIgnoreCase("price")) {
                                            return null; // Skip handling price here as it's handled below
                                        } else {
                                            return Query.of(m -> m
                                                    .matchPhrase(mp -> mp
                                                            .field(filter.getKey())
                                                            .query(filter.getValue().toString())
                                                    )
                                            );
                                        }
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                            // Handling range query for price
                            if (minPrice != null || maxPrice != null) {
                                Query priceRangeQuery = Query.of(r -> r
                                        .range(ro -> ro
                                                .field("price")
                                                .gte(minPrice != null ? JsonData.of(minPrice) : null)
                                                .lte(maxPrice != null ? JsonData.of(maxPrice) : null)
                                        )
                                );
                                mustQueries.add(priceRangeQuery);
                            }

                            b.must(mustQueries);
                            return b;
                        })
                )
        );
        SearchResponse<Product> response = elasticsearchClient.search(searchRequest, Product.class);
        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    public Page<Product> searchProducts(String title, String category, String publisher, Double minPrice, Double maxPrice, Pageable pageable) {
        try {
            List<Product> products = redisServiceElacticImpli.getListProductWithTitle(title);
            if (products != null && !products.isEmpty()) {
                return new PageImpl<>(products, pageable, products.size());
            }
            SearchRequest searchRequest = buildSearchRequest(title, category, publisher, minPrice, maxPrice, pageable);
            SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);
            products = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
            redisServiceElacticImpli.addListProductToRedisWithTitle(products);
            return new PageImpl<>(products, pageable, searchResponse.hits().total().value());
        } catch (IOException e) {
            throw new RuntimeException("Failed to search products", e);
        }
    }

    private SearchRequest buildSearchRequest(String title, String category,String publisher, Double minPrice, Double maxPrice, Pageable pageable) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        addTextMatchFilter(boolQueryBuilder, "title", title);
        addTextMatchFilter(boolQueryBuilder, "category", category);
        addTextMatchFilter(boolQueryBuilder, "publisher", publisher);
        addRangeFilter(boolQueryBuilder, "price", minPrice, true);
        addRangeFilter(boolQueryBuilder, "price", maxPrice, false);

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index("products")
                .from(pageable.getPageNumber() * pageable.getPageSize())
                .size(pageable.getPageSize())
                .query(boolQueryBuilder.build()._toQuery());

        addSorting(searchRequestBuilder, pageable);
        return searchRequestBuilder.build();
    }

    private void addTextMatchFilter(BoolQuery.Builder builder, String field, String value) {
        if (StringUtils.hasText(value)) {
            builder.filter(q -> q.match(m -> m.field(field).query(value)));
        }
    }

    private void addRangeFilter(BoolQuery.Builder builder, String field, Double value, boolean isGte) {
        if (value != null) {
            builder.filter(q -> q.range(r -> isGte ? r.field(field).gte(JsonData.of(value)) : r.field(field).lte(JsonData.of(value))));
        }
    }

    private void addSorting(SearchRequest.Builder builder, Pageable pageable) {
        pageable.getSort().stream()
                .forEach(order -> builder.sort(s -> s
                        .field(f -> f
                                .field(order.getProperty())
                                .order(order.getDirection().isAscending() ? SortOrder.Asc : SortOrder.Desc)
                        )
                ));
    }

}

