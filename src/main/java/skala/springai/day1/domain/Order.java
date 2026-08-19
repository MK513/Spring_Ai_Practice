package skala.springai.day1.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "orders")
@Getter
public class Order {
    
    @Id
    private String id;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDate eta;
}
