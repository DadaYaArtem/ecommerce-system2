package org.example.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.repository.OrderRepository;
import org.example.OrderServiceApplication;

import java.util.List;

@SpringBootTest(classes = OrderServiceApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerTestIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        Order order = new Order();
        order.setCustomerId("customer-test");
        order.setStatus("CREATED");

        OrderItem item = new OrderItem("product123", 3, order);
        order.setItems(List.of(item));

        order = orderRepository.save(order);

        mockMvc.perform(get("/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("Order status fetched successfully"));
    }
}

