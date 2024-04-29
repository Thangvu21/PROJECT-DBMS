package dbmsforeveread.foreveread.SearchEngine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbmsforeveread.foreveread.SearchEngine.domain.Product;
import dbmsforeveread.foreveread.config.BaseRedisServiceImpli;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Component
@Slf4j
public class RedisServiceElasticImpli extends BaseRedisServiceImpli implements RedisServiceElastic {
    private final ObjectMapper redisObjectMapper;

    @Autowired
    public RedisServiceElasticImpli(RedisTemplate<String, Object> redisTemplate, ObjectMapper redisObjectMapper) {
        super(redisTemplate);
        this.redisObjectMapper = redisObjectMapper;
    }

    // key tương ứng với title field có id  object
//    final String key = "Products";

    @Override
    public void addProductToRedis(Product product) {
        String keyProduct = product.getTitle();
        String field = product.getId();
        String json = (String) this.hashGet(keyProduct, field);
        // if null mới thêm không null thì thêm làm gì
        try {
            if (json == null) {
                String jsonString = redisObjectMapper.writeValueAsString(product);
                this.hashSet(keyProduct, field, jsonString);
                this.setTimeToLive(keyProduct, 10, TimeUnit.MINUTES);
            }
        } catch (JsonProcessingException e) {
            e.getLocation();
        }
    }

    @Override
    public void deleteProductFromRedis(String id, String title) {
//        String keyProduct = title;
//        String field = id;
        this.delete(title, id);
    }
    // hoặc ây tuwf t đã cái delete vơới update t từ h kiểu thêm vào redis đã

    // id lúc này là title đấy
    @Override
    public Product getProductFromRedis(String field, String keyProduct) {
//        String keyProduct = title;
//        String field = id;

        // check xem co ko
        String json = (String) this.hashGet(keyProduct, field);
        Product ProductResponse = null;

        try {
            if (json != null) {
                ProductResponse = redisObjectMapper.readValue(json, Product.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse book from Redis", e);
        }

        return ProductResponse;
    }

    // thêm
    @Override
    public void addListProductToRedisWithTitle (List<Product> productList) {
        for (Product product : productList) {
            addProductToRedis(product);
        }
        this.setTimeToLive(productList.get(0).getTitle(), 10, TimeUnit.MINUTES);
    }

    // lấy ra sản phẩm với title tương ứng
    @Override
    public List<Product> getListProductWithTitle(String keyTitle) {
        List<Product> productResponse = new ArrayList<>();
        Map<String, Object> hashEntries = this.getField(keyTitle);
        for (Map.Entry<String, Object> entry : hashEntries.entrySet()) {
            String json = (String) entry.getValue();
            try {
                if (json != null) {
                    Product ProductResponse = redisObjectMapper.readValue(json, Product.class);
                    productResponse.add(ProductResponse);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse book from Redis", e);
            }
        }
        return productResponse;
    }
}
