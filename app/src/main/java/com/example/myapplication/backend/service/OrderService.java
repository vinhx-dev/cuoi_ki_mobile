package com.example.myapplication.backend.service;

import com.example.myapplication.backend.core.ApiException;
import com.example.myapplication.backend.entity.Order;
import com.example.myapplication.backend.entity.OrderItem;
import com.example.myapplication.backend.entity.Product;
import com.example.myapplication.backend.entity.User;
import com.example.myapplication.backend.repository.OrderRepository;
import com.example.myapplication.backend.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    /** Dữ liệu đầu vào khi tạo đơn (DTO nội bộ của backend). */
    public static class ItemInput {
        public final int productId;
        public final int quantity;

        public ItemInput(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getMyOrders(User user) {
        return orderRepository.findByUserId(user.getId());
    }

    public Order getMyOrder(User user, int orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) throw ApiException.notFound("Không tìm thấy đơn hàng #" + orderId);
        if (order.getUserId() != user.getId()) throw ApiException.forbidden("Bạn không có quyền xem đơn hàng này");
        return order;
    }

    public synchronized Order createOrder(User user, List<ItemInput> inputs, String address) {
        if (inputs == null || inputs.isEmpty()) throw ApiException.badRequest("Đơn hàng chưa có sản phẩm");

        String shipTo = (address == null || address.trim().isEmpty()) ? user.getAddress() : address.trim();
        if (shipTo == null || shipTo.isEmpty()) throw ApiException.badRequest("Vui lòng nhập địa chỉ giao hàng");

        // Validate toàn bộ trước, rồi mới trừ kho
        List<OrderItem> items = new ArrayList<>();
        for (ItemInput in : inputs) {
            if (in.quantity <= 0) throw ApiException.badRequest("Số lượng không hợp lệ");
            Product p = productRepository.findById(in.productId);
            if (p == null) throw ApiException.notFound("Không tìm thấy sản phẩm #" + in.productId);
            if (p.getStock() < in.quantity) throw ApiException.badRequest("'" + p.getName() + "' không đủ hàng");
            items.add(new OrderItem(p.getId(), p.getName(), p.getPrice(), in.quantity));
        }
        for (OrderItem item : items) {
            Product p = productRepository.findById(item.getProductId());
            p.setStock(p.getStock() - item.getQuantity());
        }

        Order order = new Order(0, user.getId(), items, shipTo, Order.STATUS_PENDING, System.currentTimeMillis());
        return orderRepository.save(order);
    }

    public synchronized Order cancelOrder(User user, int orderId) {
        Order order = getMyOrder(user, orderId);
        if (!Order.STATUS_PENDING.equals(order.getStatus())) {
            throw ApiException.badRequest("Chỉ có thể hủy đơn đang chờ xử lý");
        }
        // Hoàn kho
        for (OrderItem item : order.getItems()) {
            Product p = productRepository.findById(item.getProductId());
            if (p != null) p.setStock(p.getStock() + item.getQuantity());
        }
        order.setStatus(Order.STATUS_CANCELLED);
        return order;
    }
}
