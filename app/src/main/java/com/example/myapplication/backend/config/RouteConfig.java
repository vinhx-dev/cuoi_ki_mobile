package com.example.myapplication.backend.config;

import com.example.myapplication.backend.controller.AuthController;
import com.example.myapplication.backend.controller.OrderController;
import com.example.myapplication.backend.controller.ProductController;
import com.example.myapplication.backend.controller.UserController;
import com.example.myapplication.backend.core.Router;
import com.example.myapplication.backend.repository.OrderRepository;
import com.example.myapplication.backend.repository.ProductRepository;
import com.example.myapplication.backend.repository.UserRepository;
import com.example.myapplication.backend.service.AuthService;
import com.example.myapplication.backend.service.OrderService;
import com.example.myapplication.backend.service.ProductService;

/**
 * Nơi "wiring" các tầng (dependency injection thủ công) và khai báo toàn bộ API.
 * Thêm API mới: tạo Repository -> Service -> Controller rồi đăng ký route ở đây.
 */
public final class RouteConfig {

    private RouteConfig() {}

    public static Router createRouter() {
        // Repository
        UserRepository userRepo = new UserRepository();
        ProductRepository productRepo = new ProductRepository();
        OrderRepository orderRepo = new OrderRepository();

        // Service
        AuthService authService = new AuthService(userRepo);
        ProductService productService = new ProductService(productRepo);
        OrderService orderService = new OrderService(orderRepo, productRepo);

        // Controller
        AuthController auth = new AuthController(authService);
        UserController user = new UserController(authService);
        ProductController product = new ProductController(productService);
        OrderController order = new OrderController(orderService, authService);

        // Routes
        return new Router()
                .post("/auth/login", auth::login)
                .post("/auth/register", auth::register)

                .get("/users/me", user::me)

                .get("/products", product::list)
                .get("/products/{id}", product::detail)

                .get("/orders", order::list)
                .get("/orders/{id}", order::detail)
                .post("/orders", order::create)
                .post("/orders/{id}/cancel", order::cancel);
    }
}
