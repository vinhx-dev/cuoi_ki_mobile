package com.example.myapplication.backend.service;

import com.example.myapplication.backend.core.ApiException;
import com.example.myapplication.backend.entity.Product;
import com.example.myapplication.backend.repository.ProductRepository;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts(String keyword) {
        return productRepository.findAll(keyword);
    }

    public Product getProduct(int id) {
        Product product = productRepository.findById(id);
        if (product == null) throw ApiException.notFound("Không tìm thấy sản phẩm #" + id);
        return product;
    }
}
