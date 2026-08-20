package skala.springai.day3;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import skala.springai.day1.repository.OrderRepository;

/**
 * Step 3 — 승인 게이트: 접수까지만.
 *
 * <p>되돌리기 어려운 행동은 도구에게 <b>접수</b>까지만 준다. 실행 버튼은 사람이 누르고,
 * 그 경로에는 {@code @Tool} 을 붙이지 않는다 — 모델은 존재 자체를 모른다.
 *
 * <p>모델에게는 "접수됐다"고 알려 주면 대화가 자연스럽게 이어진다.
 */
@Slf4j
@Component
public class TicketTools {

    public record TicketView(String ticketNo, String message) {}

    private final OrderRepository orders;
    private final TicketStore tickets;

    public TicketTools(OrderRepository orders, TicketStore tickets) {
        this.orders = orders;
        this.tickets = tickets;
    }

    @Tool(description = """
          환불을 접수한다. 즉시 처리되지 않고 담당자 승인 후 처리된다.
          사용자가 환불이나 반품을 명시적으로 요청했을 때만 쓴다.
          """)
    public TicketView requestRefund(
            @ToolParam(description = "주문번호. 예: 12345") String orderId,
            @ToolParam(description = "환불 사유. 사용자가 말한 그대로 적는다") String reason,
            ToolContext ctx) {

        String userId = String.valueOf(ctx.getContext().get("userId"));

        // 권한 먼저 — 남의 주문은 접수 자체가 안 된다
        orders.findByIdAndOwnerId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        var t = tickets.create("REFUND", orderId, userId, reason);

        // 접수 사실도 감사 로그에 남긴다 — 누가·언제·무엇을 요청했는지
        log.warn("[APPROVAL] REFUND 접수 no={} order={} by={} reason={}",
                t.no(), orderId, userId, reason);

        return new TicketView(t.no(), "접수되었습니다. 담당자 승인 후 처리됩니다.");
    }

    @Tool(description = "접수된 요청의 처리 상태를 조회한다.")
    public String ticketStatus(
            @ToolParam(description = "접수 번호. 예: AP-1001") String ticketNo) {
        var t = tickets.find(ticketNo);
        return t == null
                ? "해당 접수 번호를 찾을 수 없습니다."
                : "요청 %s · 유형 %s · 상태 %s".formatted(t.no(), t.type(), t.status());
    }
}
