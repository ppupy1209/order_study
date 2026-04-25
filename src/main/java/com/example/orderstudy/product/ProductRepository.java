package com.example.orderstudy.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product p
               set p.stockQuantity = p.stockQuantity - :quantity
             where p.id = :productId
               and p.stockQuantity >= :quantity
            """)
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") long quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product p
               set p.stockQuantity = p.stockQuantity + :quantity
             where p.id = :productId
            """)
    int increaseStock(@Param("productId") Long productId, @Param("quantity") long quantity);
}
