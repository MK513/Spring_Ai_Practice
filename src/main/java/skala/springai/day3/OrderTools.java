package skala.springai.day3;

import java.time.LocalDate;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import skala.springai.day1.domain.Order;
import skala.springai.day1.repository.OrderRepository;

/**
 * Step 1 — 도구 정의: 설명이 곧 스펙.
 *
 * <p>모델은 이 클래스의 코드를 한 글자도 보지 않는다. 보는 것은 {@code description} 문장과
 * 파라미터 스키마뿐이다. 그래서 도구가 안 불리면 메서드 이름이 아니라 설명을 고쳐야 한다.
 *
 * <p>사용자 ID 를 파라미터가 아니라 {@link ToolContext} 로 받는 것이 이 실습의 핵심이다.
 * 파라미터로 두면 스키마에 실려 나가고, 스키마에 있으면 모델이 그 값을 채운다 —
 * "user2의 주문 보여줘" 한마디에 남의 주문이 열린다.
 */
@Slf4j
@Component
public class OrderTools {

    /** 모델에게 돌려줄 주문 정보. 엔티티를 그대로 주지 않는다. */
    public record OrderView(String orderId, String item, String status, LocalDate eta) {
        static OrderView from(Order o) {
            return new OrderView(o.getId(), o.getItem(), o.getStatus().name(), o.getEta());
        }
    }

    private final OrderRepository orders;

    public OrderTools(OrderRepository orders) {
        this.orders = orders;
    }

    @Tool(description = """
          주문 상태를 조회한다. 사용자가 주문번호를 말하거나
          '내 주문', '배송 언제' 처럼 물으면 이 도구를 쓴다.
          """)
    public OrderView getOrder(
            @ToolParam(description = "조회할 주문번호. 예: 12345") String orderId,
            ToolContext context) {

        String userId = currentUser(context);
        log.info("[TOOL] getOrder orderId={} by={}", orderId, userId);

        // 권한을 코드가 아니라 쿼리 조건에 넣는다 — 조회한 뒤 비교하면 빠뜨릴 자리가 생긴다
        return orders.findByIdAndOwnerId(orderId, userId)
                .map(OrderView::from)
                // 없는 주문과 남의 주문에 같은 말을 한다. 구분해 주면 그 자체가 정보 노출이다
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    /**
     * 사용자 ID 는 모델이 아니라 여기서 온다.
     *
     * <p>{@code toolContext} 는 도구 스키마 밖에 있어 모델이 들여다볼 수도 바꿔 넣을 수도 없다.
     */
    private String currentUser(ToolContext context) {
        Object userId = context == null ? null : context.getContext().get("userId");
        if (userId == null) {
            throw new IllegalStateException("toolContext 에 userId 가 없다 — 호출부를 확인하라");
        }
        return userId.toString();
    }
}
