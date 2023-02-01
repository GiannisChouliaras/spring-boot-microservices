package com.vodafone.springbootmicroservices.repository;

import com.vodafone.springbootmicroservices.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

}
