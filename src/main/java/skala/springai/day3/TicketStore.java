package skala.springai.day3;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * 접수된 요청을 담아 두는 곳.
 *
 * <p>메모리에만 있다. 재시작하면 사라지므로 실습 범위에서만 쓴다.
 */
@Component
public class TicketStore {

    public record Ticket(String no, String type, String orderId, String reason,
                         String requestedBy, Instant requestedAt, String status) {}

    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1000);

    /** 접수만 한다. 상태는 언제나 PENDING 으로 시작한다. */
    public Ticket create(String type, String orderId, String userId, String reason) {
        String no = "AP-" + seq.incrementAndGet();
        Ticket t = new Ticket(no, type, orderId, reason, userId, Instant.now(), "PENDING");
        tickets.put(no, t);
        return t;
    }

    public Ticket find(String no) {
        return tickets.get(no);
    }

    public List<Ticket> pending() {
        return tickets.values().stream().filter(t -> "PENDING".equals(t.status())).toList();
    }

    /** 승인은 사람만 부른다 — 이 메서드에는 @Tool 이 붙어 있지 않다. */
    public Ticket approve(String no) {
        return tickets.computeIfPresent(no, (k, t) -> new Ticket(
                t.no(), t.type(), t.orderId(), t.reason(),
                t.requestedBy(), t.requestedAt(), "APPROVED"));
    }
}
