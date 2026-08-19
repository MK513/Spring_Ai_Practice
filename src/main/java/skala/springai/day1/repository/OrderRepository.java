package skala.springai.day1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import skala.springai.day1.domain.Order;

public interface OrderRepository extends JpaRepository<Order, String>{

    Optional<Order> findByIdAndOwnerId(String id, String ownerId);
}