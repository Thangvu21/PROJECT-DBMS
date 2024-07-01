# PROJECT-DBMS

###This is our video demo
-https://www.youtube.com/watch?v=5DKjblFeoxI&t=192s

### Set up elasticsearch and logstash
-   Download : [Elasticsearch](https://www.elastic.co/fr/downloads/elasticsearch)
- Cài đặt elasticsearch theo video :
[Setup_Elastic](https://www.youtube.com/watch?v=kYXx0sq74Tc)

-   Cài đặt logstash:
    -   Tải file logstash : [Logstash](https://www.elastic.co/fr/downloads/logstash)  
    - Set up logstash theo video : [Setup_logstash](https://www.youtube.com/watch?v=PcSNVTBhe0w&t=783s)
    - Chú ý tạo file trong mục config với tên là product_config.conf
    - Nội dung file config như sau:  

```conf
input {
  jdbc {
    jdbc_driver_library => # Đường dẫn đến file mysql-connector-java 
    jdbc_driver_class => "com.mysql.cj.jdbc.Driver"         #"com.mysql.jdbc.Driver"
    jdbc_connection_string => "jdbc:mysql://localhost:3306/better_read" 
    jdbc_user => "root" 
    jdbc_password => # Mật khẩu mysql 
    jdbc_paging_enabled => true
    tracking_column => "last_update"
    use_column_value => true
    # tracking_column => "customerNumber"
    tracking_column_type => "numeric"
    # schedule => "*/5 * * * * *"
    statement => "
        with recursive cte as(
            select bc.book_id as book_id  , c.name as name ,c.parent_id as parent_id
            from book_category bc
            inner join category c on bc.category_id = c.id
            union all
            select cte.book_id, concat_ws('; ', cte.name, c1.name) as name, c1.parent_id as parent_id
            from cte 
            inner join category c1 on c1.id = cte.parent_id
            where c1.parent_id is not Null
        ), cte1 as(
            select cte.book_id as book_id,
                    concat_ws('; ',cte.name, c.name) as name
            from cte
            inner join category c on c.id = cte.parent_id 
            where c.parent_id is Null
            order by book_id
        )
        select b.id, b.title, b.isbn, b.language, b.price, b.layout_book, p.name as publisher, a.name as author , cte1.name as category, b.image_url
        from book b
        left join publisher p on p.id = b.publisher_id
        inner join book_author ba on ba.book_id = b.id
        inner join author a on a.id = ba.author_id 
        left join cte1 on cte1.book_id = b.id
        order by b.id
    "
  }
}
filter {
  mutate {
    convert => { "id" => "string" }
    remove_field => ["@version"]

  }
}
output {
  elasticsearch {
    hosts => ["https://localhost:9200/"]
    index => "products"
    document_id => "%{id}" 
    user => "elastic"
    password => # Mật khẩu elastic
    ssl => true
    ssl_certificate_verification => false

  }
}
```
### Chú ý kiểm tra lại các thông tin tài khoản và đường dẫn trong file config

### Chạy trên cmd trong mục bin của logstash với lệnh
```
logstash -f ./config/product_config.conf
