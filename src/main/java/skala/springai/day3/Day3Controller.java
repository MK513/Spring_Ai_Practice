package skala.springai.day3;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Day 3 실습 — 상담 에이전트.
 *
 * <pre>
 * POST /lab3/chat?userId=&amp;message=   주문 질문이면 도구를 부른다
 * </pre>
 */
@RestController
@Tag(name = "Day3 실습 - 상담 에이전트")
public class Day3Controller {

    private final Day3ChatService chatService;

    public Day3Controller(Day3ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "상담 대화",
               description = "주문 질문이면 도구를 부른다. 남의 주문은 조회되지 않는다")
    @PostMapping("/lab3/chat")
    public String chat(@RequestParam String userId, @RequestParam String message) {
        return chatService.chat(message, userId);
    }
}
