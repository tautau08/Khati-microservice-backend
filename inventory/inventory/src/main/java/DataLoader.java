package com.example.inventory.config;

import com.example.inventory.entity.Category;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataLoader(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (categoryRepository.count() > 0) {
            return;
        }

        // Create categories
        Category dairy = new Category();
        dairy.setName("Dairy");
        dairy.setDescription("Fresh organic dairy products");
        categoryRepository.save(dairy);

        Category wool = new Category();
        wool.setName("Wool");
        wool.setDescription("Organic wool and fiber products");
        categoryRepository.save(wool);

        Category fruits = new Category();
        fruits.setName("Fruits");
        fruits.setDescription("Seasonal organic fruits");
        categoryRepository.save(fruits);

        Category vegetables = new Category();
        vegetables.setName("Vegetables");
        vegetables.setDescription("Fresh green organic vegetables");
        categoryRepository.save(vegetables);

        Category grains = new Category();
        grains.setName("Grains");
        grains.setDescription("Organic grains and cereals");
        categoryRepository.save(grains);

        // Create products
        Product milk = new Product();
        milk.setName("Organic Milk");
        milk.setDescription("Fresh cow's milk from organic farms");
        milk.setPrice(new BigDecimal("3.99"));
        milk.setStockQuantity(100);
        milk.setCategory(dairy);
        milk.setCreatedAt(LocalDateTime.now());
        productRepository.save(milk);

        Product cheese = new Product();
        cheese.setName("Organic Cheese");
        cheese.setDescription("Aged organic cheddar cheese");
        cheese.setPrice(new BigDecimal("8.99"));
        cheese.setStockQuantity(50);
        cheese.setCategory(dairy);
        cheese.setCreatedAt(LocalDateTime.now());
        productRepository.save(cheese);

        Product woolYarn = new Product();
        woolYarn.setName("Organic Wool Yarn");
        woolYarn.setDescription("Soft wool yarn for knitting");
        woolYarn.setPrice(new BigDecimal("12.99"));
        woolYarn.setStockQuantity(30);
        woolYarn.setCategory(wool);
        woolYarn.setCreatedAt(LocalDateTime.now());
        productRepository.save(woolYarn);

        Product apples = new Product();
        apples.setName("Organic Apples");
        apples.setDescription("Crisp red apples");
        apples.setPrice(new BigDecimal("2.49"));
        apples.setStockQuantity(200);
        apples.setCategory(fruits);
        apples.setCreatedAt(LocalDateTime.now());
        productRepository.save(apples);

        Product spinach = new Product();
        spinach.setName("Organic Spinach");
        spinach.setDescription("Fresh green spinach leaves");
        spinach.setPrice(new BigDecimal("1.99"));
        spinach.setStockQuantity(150);
        spinach.setCategory(vegetables);
        spinach.setCreatedAt(LocalDateTime.now());
        productRepository.save(spinach);

        Product quinoa = new Product();
        quinoa.setName("Organic Quinoa");
        quinoa.setDescription("Nutritious organic quinoa grains");
        quinoa.setPrice(new BigDecimal("4.99"));
        quinoa.setStockQuantity(75);
        quinoa.setCategory(grains);
        quinoa.setCreatedAt(LocalDateTime.now());
        productRepository.save(quinoa);

        System.out.println("Sample organic farm product data loaded successfully!");
    }
}
