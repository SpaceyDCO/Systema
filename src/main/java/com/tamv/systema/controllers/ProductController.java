package com.tamv.systema.controllers;

import com.tamv.systema.entities.Product;
import com.tamv.systema.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductRepository productRepository;
    @Autowired
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * GET /api/v1/products
     * @return A list containing all products
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * GET /api/v1/products/{id}
     * Retrieves a product by ID
     * @param id The id of the product to retrieve
     * @return The product or 404 Not Found status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/products
     * Creates a product from a json
     * @param product The product to be created (json)
     * @return 201 Created status
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/products/{id}
     * Updates an existing product
     * @param id The id of the product to update
     * @param productDetails The details for the new product (json)
     * @return The updated product or 404 Not Found status
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setCategory(productDetails.getCategory());
            existingProduct.setDefaultPrice(productDetails.getDefaultPrice());
            Product updatedProduct = productRepository.save(existingProduct);
            return ResponseEntity.ok(updatedProduct);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes an existing product by its ID
     * @param id The id of the product to delete
     * @return 204 No Content on success, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if(!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
