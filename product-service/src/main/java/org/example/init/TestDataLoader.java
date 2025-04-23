package org.example.init;

import jakarta.annotation.PostConstruct;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class TestDataLoader {

    private final ProductRepository repository;

    public TestDataLoader(ProductRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        if (repository.count() == 0) {
            repository.save(new Product(
                    "p123",
                    "Samsung Galaxy A14",
                    "Смартфон 64ГБ, Android 13, LTE",
                    "Смартфони",
                    "Samsung",
                    "Samsung Україна",
                    6999.00,
                    "UAH",
                    true
            ));

            repository.save(new Product(
                    "p456",
                    "Apple AirPods Pro 2",
                    "Бездротові навушники з ANC, кейс MagSafe",
                    "Навушники",
                    "Apple",
                    "iStore",
                    10999.00,
                    "UAH",
                    true
            ));

            repository.save(new Product(
                    "p789",
                    "Dell Vostro 15",
                    "Ноутбук 15.6'' i5/16GB/512SSD/Win11",
                    "Ноутбуки",
                    "Dell",
                    "Dell Partner",
                    28999.00,
                    "UAH",
                    true
            ));

            System.out.println("✅ [TestDataLoader] Продукти успішно додані до бази даних");
        }
    }
}
