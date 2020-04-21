package com.wirebraincoffee.productapiannotation.handler;

import com.mongodb.util.JSON;
import com.wirebraincoffee.productapiannotation.model.Product;
import com.wirebraincoffee.productapiannotation.model.ProductEvent;
import com.wirebraincoffee.productapiannotation.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
public class ProductHandler {
    private ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        Flux<Product> products = repository.findAll();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        String id = request.pathVariable("id");

        /* I use de glatmap to give a user a response in case of i cannot find the product Id */
        Mono<Product> productMono = repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromObject(product)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono
                .flatMap(product ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(repository.save(product), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> existingProductMono = this.repository.findById(id);

        Mono<Product> productMono = request.bodyToMono(Product.class);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        /*As we are using two Monos, and insted to use flatmap or nested flatmap, we will
        use zipWith operator to combine the result of both Monos. Iy doesen't metter which one
        is the argument of the method, this only changed the order of the the arguments of the
        combinator funcion*/

        /*I can update a product withou create another one, but one rule of functional programming is to work
        with immutable objects, when you need to mutate an object, you create a new object instead of
        modifying an existing one*/
        return productMono.zipWith(existingProductMono, (prodcut, existingProduct) ->
                new Product(existingProduct.getId(), prodcut.getName(), prodcut.getPrice()))
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(repository.save(product), Product.class))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productMono = this.repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(existingProduct ->
                        ServerResponse.ok()
                                .build(repository.delete(existingProduct)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteAllProducts(ServerRequest request) {
        return ServerResponse.ok()
                .build(repository.deleteAll());
    }


    public Mono<ServerResponse> getProductEvents(ServerRequest request) {
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1)).map(val->
                new ProductEvent(val, "Product Event"));
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvent.class);
    }
}
