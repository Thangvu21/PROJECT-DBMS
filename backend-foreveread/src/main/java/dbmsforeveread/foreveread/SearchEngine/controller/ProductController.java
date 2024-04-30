package dbmsforeveread.foreveread.SearchEngine.controller;


import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dbmsforeveread.foreveread.SearchEngine.domain.Product;
import dbmsforeveread.foreveread.SearchEngine.repository.ProductRepository;
import dbmsforeveread.foreveread.SearchEngine.service.ProductService;
import dbmsforeveread.foreveread.SearchEngine.service.RedisServiceElasticImpli;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api/v1/products")
public class ProductController {

    @Autowired
    private ProductRepository elasticSearchQuery;

    @Autowired
    private RedisServiceElasticImpli redisServiceElacticImpli;

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    // không dùng thì thui
    @PostMapping
    public ResponseEntity<String> createOrUpdateDocument(@RequestBody Product product) throws IOException {
        String response = elasticSearchQuery.createOrUpdateDocument(product);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> bulk( @RequestBody List<Product> product) throws IOException {
        String response = elasticSearchQuery.bulkSave(product);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getDocumentById(@PathVariable String productId) throws IOException {
        Product product = elasticSearchQuery.findDocById(productId);
        log.info("Product Document has been successfully retrieved.");
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    // có thể xóa luôn trong redis
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteDocumentById( @PathVariable String productId) throws IOException {
        Product product = elasticSearchQuery.findDocById(productId);
        String message = "";
        if (product != null) {
             message = elasticSearchQuery.deleteDocById(productId);
        }

        String title = product.getTitle();
        product = redisServiceElacticImpli.getProductFromRedis(productId, title);
        if (product != null) {
            redisServiceElacticImpli.deleteProductFromRedis(productId, title);
        }
        // xóa car title trong redis nữa

        log.info("Product Document has been successfully deleted. Message: {}", message);
        return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }

    // nhieu qua thi thoi ko cache noi
    @GetMapping
    public ResponseEntity<List<Product>> findAll() throws IOException {
        List<Product> products = elasticSearchQuery.findAll();
        log.info("No of Product Documents has been successfully retrieved: {}", products.size());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    // insert thi thoi
    @PostMapping("/insert")
    public ResponseEntity<Product> insertProduct(@RequestBody Product product) {
        try {
            Product insertedProduct = elasticSearchQuery.insertProduct(product);
            // check title của nó có trong key không neeus ko có title đó thì thôi không
            return new ResponseEntity<>(insertedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // đây là thứ cần cache này
    @GetMapping("/matchSearch/{title}")
    public ResponseEntity<List<Product>> searchProductsByName(@PathVariable String title) {
        // tức là dùng key riêng
        // vì lấy thì lấy ra cả 1 đống luôn mà
        // cần lưu key cho cả 1 title luôn ấy nếu cái key của title này tồn tại lấy luôn cả list
        List<Product> products = redisServiceElacticImpli.getListProductWithTitle(title);
        try {
            if (products == null || products.isEmpty()) {
                // không thấy trong redis thì gọi db
                products = productService.searchProductsByName(title);
                // lưu lai vào redis
                redisServiceElacticImpli.addListProductToRedisWithTitle(products);
                // dùng cơ chế LRU để đâỷ d liệu ra
            }

            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // lấy ra 1 danh sách dựa trên từ có sắn lưu này không biết bao nhiêu mới đủ
    @GetMapping("/fuzzySearch/{approximateProductName}")
    List<Product> fuzzySearch( @PathVariable String approximateProductName ) throws IOException {
        SearchResponse<Product> searchResponse = productService.fuzzySearch(approximateProductName);
        List<Hit<Product>> hitList = searchResponse.hits().hits();
        System.out.println(hitList);
        List<Product> productList = new ArrayList<>();
        for(Hit<Product> hit :hitList){
            productList.add(hit.source());
        }
        return productList;
    }

    // Gợi ý chữ autocomplite
    @GetMapping("/autoSuggest/{partialProductName}")
    List<String> autoSuggestProductSearch(@PathVariable String partialProductName) throws IOException {
        try {
            SearchResponse<Product> searchResponse = productService.autoSuggestProduct(partialProductName);
            List<Hit<Product>> hitList = searchResponse.hits().hits();
            List<Product> productList = new ArrayList<>();
            for (Hit<Product> hit : hitList) {
                productList.add(hit.source());
            }
            List<String> listOfProductNames = new ArrayList<>();
            for (Product product : productList) {
                listOfProductNames.add(product.getTitle());
            }
            return listOfProductNames;
        }  catch (IOException e) {
            log.error("Error while retrieving auto suggestions", e);
            return (List<String>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // tìm theo tiêu chí tôi nghĩ cái này phụ hơn là cái search chính
    @GetMapping("/searchFilter")
    public List<Product> searchFilter(
            @RequestParam(required = false) Map<String, Object> filters,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) throws IOException {
        return productService.getProductsByFilters(filters, minPrice, maxPrice);
    }
}








