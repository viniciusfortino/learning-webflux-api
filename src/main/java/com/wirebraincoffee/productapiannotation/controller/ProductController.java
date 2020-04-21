//package com.wirebraincoffee.productapiannotation.controller;
//
//import com.wirebraincoffee.productapiannotation.model.Product;
//import com.wirebraincoffee.productapiannotation.model.ProductEvent;
//import com.wirebraincoffee.productapiannotation.repository.ProductRepository;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//
//@RestController
//@RequestMapping("/products")
//public class ProductController {
//    private ProductRepository repository;
//
//    public ProductController(ProductRepository repository) {
//        this.repository = repository;
//    }
//
//    @GetMapping
//    public Flux<Product> getAllProducts() {
//        return repository.findAll();
//    }
//
//    @GetMapping("{id}")
//    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
//        return repository
//                .findById(id)
//                .map(product -> ResponseEntity.ok(product))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public Mono<Product> saveProduct(@RequestBody Product product) {
//        return repository.save(product);
//    }
//
//    @PutMapping("{id}")
//    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable(value = "id") String id,
//                                                       @RequestBody Product product) {
//        return repository.findById(id)
//                .flatMap(existingProduct -> {
//                    existingProduct.setName(product.getName());
//                    existingProduct.setPrice(product.getPrice());
//                    return repository.save(existingProduct);
//                })
//                .map(updateProduct -> ResponseEntity.ok(updateProduct))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping("{id}")
//    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable(value = "id") String id) {
//        return repository.findById(id)
//                .flatMap(existingProduct -> repository
//                        .delete(existingProduct)
//                        .then(Mono.just(ResponseEntity.ok().<Void>build()))
//                )
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping
//    public Mono<Void> deleteAllProdcut() {
//        return repository.deleteAll();
//    }
//
//    /*Since i have indicated, this method produces an event stream, i don't have to return a Flux of type
//    ServersideEvent. I can return a Flux of type ProductEvent.
//    Using Flux.interval i'm going to simulare a stream of events. this way, with a map operator, i'm
//    going to turn the produced long value to an object of type productevent with a fixed eventType for
//    simplicity
//     */
//    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ProductEvent> getProductEvents() {
//        return Flux.interval(Duration.ofSeconds(1))
//                .map(val ->
//                        new ProductEvent(val, "Product Event")
//                );
//    }
//}
