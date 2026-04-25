package com.example.orderstudy.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDtos.ProductResponse create(ProductDtos.CreateProductRequest request) {
        Product product = productRepository.save(new Product(request.name(), request.price(), request.stockQuantity()));
        return ProductDtos.ProductResponse.from(product);
    }
}
