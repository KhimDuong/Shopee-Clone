package com.shopeeclone.shopee_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shopeeclone.shopee_api.model.Product;
import com.shopeeclone.shopee_api.service.ProductService;
import com.shopeeclone.shopee_api.repository.ProductRepository; // add this

@RestController
@RequestMapping("/api/products") // keep under /api
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository; // direct read for the public list

    @GetMapping("/public")
    public List<Product> getAllPublic() {
        return productRepository.findAll();
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts(); // your existing method
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return productService.createProduct(product);
    }
}