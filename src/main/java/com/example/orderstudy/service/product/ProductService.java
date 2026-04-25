package com.example.orderstudy.service.product;

import com.example.orderstudy.domain.product.Product;
import com.example.orderstudy.dto.product.ProductDtos;
import com.example.orderstudy.repository.product.ProductRepository;
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
        Product product = productRepository.save(Product.create(request.name(), request.price(), request.stockQuantity()));
        return ProductDtos.ProductResponse.from(product);
    }
}
