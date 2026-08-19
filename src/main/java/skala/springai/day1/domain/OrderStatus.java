package skala.springai.day1.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum OrderStatus {
    PENDING("결제 대기"),
    PAID("결제 완료");

    private final String label;
}
