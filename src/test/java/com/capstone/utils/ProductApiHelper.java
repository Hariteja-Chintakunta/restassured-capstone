package com.capstone.utils;

import java.util.List;
import java.util.Map;

public final class ProductApiHelper {

    public static final String PRODUCTS = "/products";
    public static final String PRODUCT_SEARCH = "/products/search";

    private ProductApiHelper() {
    }

    public static void logProducts(String label, List<Map<String, Object>> products) {
        System.out.println(label);
        for (Map<String, Object> product : products) {
            System.out.printf(
                    "Product ID: %s | Product Name: %s%n",
                    product.get("id"), product.get("title"));
        }
    }

    public static void logProduct(String label, Map<String, Object> product) {
        System.out.println(label);
        System.out.printf(
                "Product ID: %s | Product Name: %s | Details: %s%n",
                product.get("id"), product.get("title"), product);
    }
}
