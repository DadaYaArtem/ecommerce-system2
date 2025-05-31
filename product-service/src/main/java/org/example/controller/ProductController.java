package org.example.controller;

import org.example.dto.ProductResponse;
import org.example.model.Product;
import org.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getPriceByProductId(productId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(productService.search(name, category));
    }
} 