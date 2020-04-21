package com.wirebraincoffee.productapiannotation;

import com.wirebraincoffee.productapiannotation.handler.ProductHandler;
import com.wirebraincoffee.productapiannotation.model.Product;
import com.wirebraincoffee.productapiannotation.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ProductApiAnnotationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApiAnnotationApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository repository) {
        return args -> {
            Flux<Product> productFlux = Flux.just(
                    new Product(null, "Big Latte", 2.99),
                    new Product(null, "Big Decaf", 2.49),
                    new Product(null, "Green Tea", 1.99)
            ).flatMap(repository::save);

            //we use then/thenMany, because we want to the previues method ends to then, reun the findAll method
            productFlux
                    .thenMany(repository.findAll())
                    .subscribe(System.out::println);

//			operations.collectionExists(Product.class)
//					.flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(exists))
//					.thenMany(v -> operations.createCollection(Product.class))
//					.thenMany(productFlux)
//					.thenMany(repository.findAll())
//					.subscribe(System.out::println);
        };
    }


    /*
     *IF YOU WANT TO WORK WITH FUNCTIONAL ENDPOINTs
     *
     * This way, Spring Boot will configure webflux with the instance returned by this method.
     * We will cover two aprouches for defining routes, one with chained routes and another one
     * with nested routes.
     *
     * When we chaining all the routes to build a single router function, the order in which the routes are defined is not random.
     * For example, if I defined the route to get a product befeore the route to send server-side events,
     *  the events part of path will match the ID part of the route to get a product. And the getProduct handler will be
     * executed with events as the ID of the product
     * */
//	@Bean
//	RouterFunction<ServerResponse> routes(ProductHandler handler){
//		return route(GET("/products").and(accept(APPLICATION_JSON)), handler::getAllProducts)
//				.andRoute(POST("/products").and(contentType(APPLICATION_JSON)), handler::saveProduct)
//				.andRoute(DELETE("/products").and(accept(APPLICATION_JSON)), handler::deleteAllProducts)
//				.andRoute(GET("/products/events").and(accept(TEXT_EVENT_STREAM)), handler::getProductEvents)
//				.andRoute(GET("/products/{id}").and(accept(APPLICATION_JSON)), handler::getProduct)
//				.andRoute(PUT("/products/{id}").and(contentType(APPLICATION_JSON)), handler::updateProduct)
//				.andRoute(DELETE("/products/{id}").and(accept(APPLICATION_JSON)), handler::deleteProduct);
//	}

    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return nest(path("/products"),
                nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
                        route(GET("/"), handler::getAllProducts)))
                .andRoute(method(HttpMethod.POST), handler::saveProduct)
                .andRoute(DELETE("/"), handler::deleteAllProducts)
                .andRoute(GET("/events"), handler::getProductEvents)
                .andNest(path("/{id}"),
                        route(method(HttpMethod.GET), handler::getProduct)
                                .andRoute(method(HttpMethod.PUT), handler::updateProduct)
                                .andRoute(method(HttpMethod.DELETE), handler::deleteAllProducts)
                );
    }

}
