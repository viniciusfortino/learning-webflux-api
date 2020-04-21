package com.wirebraincoffee.productapiannotation.repository;

import com.wirebraincoffee.productapiannotation.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository
            extends ReactiveMongoRepository<Product, String> {
}
