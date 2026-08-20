package skala.springai.day3;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import skala.springai.day3.TicketStore.Ticket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Day 3 실습 — 상담 에이전트.
 *
 * <pre>
 * POST /lab3/chat?userId=&amp;sessionId=&amp;message=  주문 질문이면 도구를 부른다
 * GET  /lab3/chat/history?userId=&amp;sessionId=    무엇이 기억에 남았는지 본다
 * GET  /lab3/admin/tickets/pending        접수된 요청 목록 (사람만 본다)
 * POST /lab3/admin/tickets/{no}/approve   승인 (사람만 누른다)
 * </pre>
 */
@RestController
@Tag(name = "Day3 실습 - 상담 에이전트")
public class Day3Controller {

    private final Day3ChatService chatService;
    private final TicketStore tickets;

    public Day3Controller(Day3ChatService chatService, TicketStore tickets) {
        this.chatService = chatService;
        this.tickets = tickets;
    }

    @Operation(summary = "상담 대화",
               description = "주문 질문이면 도구를 부른다. 남의 주문은 조회되지 않는다")
    @PostMapping("/lab3/chat")
    public String chat(@RequestParam String userId,
                       @RequestParam(defaultValue = "s1") String sessionId,
                       @RequestParam String message) {
        return chatService.chat(message, userId, sessionId);
    }

    @Operation(summary = "대화 이력", description = "차단된 문장이 저장됐는지 확인한다 — Advisor 순서 실험")
    @GetMapping("/lab3/chat/history")
    public List<String> history(@RequestParam String userId,
                                @RequestParam(defaultValue = "s1") String sessionId) {
        return chatService.history(userId, sessionId);
    }

    // ── 아래 둘은 담당자용이다. @Tool 이 없으니 모델은 존재를 모른다 ──────────────

    @Operation(summary = "접수된 요청 목록", description = "환불이 접수로만 남아 있는지 확인한다")
    @GetMapping("/lab3/admin/tickets/pending")
    public List<Ticket> pending() {
        return tickets.pending();
    }

    @Operation(summary = "승인", description = "실행 버튼은 사람이 누른다")
    @PostMapping("/lab3/admin/tickets/{no}/approve")
    public Ticket approve(@PathVariable String no) {
        return tickets.approve(no);
    }
}
