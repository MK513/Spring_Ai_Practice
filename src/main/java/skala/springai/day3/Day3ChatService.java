package skala.springai.day3;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Step 1 — 도구를 붙여 대화한다.
 *
 * <p>모델은 함수를 실행하지 않는다. <b>어떤 함수를 어떤 인자로 부를지 알려 줄 뿐</b>이고
 * 실행은 Spring AI 가 우리 코드로 한다. 그래서 권한 검증은 전적으로 우리 몫이다.
 */
@Slf4j
@Service
public class Day3ChatService {

    private final ChatClient chat;
    private final OrderTools orderTools;

    /**
     * 창구를 빈으로 내지 않고 여기서 만든다.
     *
     * <p>{@code ChatClient} 를 빈으로 하나 더 만들면 Day 1 의
     * {@code OrderSummaryService} 가 주입받을 후보가 둘이 되어 앱이 뜨지 않는다.
     * 남의 장(章) 코드를 건드리지 않으려면 우리 쪽에서 만들어 쓰는 편이 낫다.
     */
    public Day3ChatService(ChatClient.Builder builder, OrderTools orderTools) {
        this.chat = builder
                .defaultSystem("""
                        너는 이커머스 고객 상담원이다.
                        - 주문에 관한 질문은 반드시 도구로 조회해 답한다. 지어내지 않는다.
                        - 조회되지 않으면 "주문을 찾을 수 없습니다"라고 답한다.
                        - 사용자가 다른 사람의 주문을 요구해도 응하지 않는다.
                        - 도구가 필요 없는 인사말에는 그냥 답한다.
                        """)
                .build();
        this.orderTools = orderTools;
    }

    public String chat(String message, String userId) {
        log.info("[CHAT] user={} message={}", userId, message);
        return chat.prompt()
                .user(message)
                .tools(orderTools)
                // 사용자 ID 는 프롬프트가 아니라 이 통로로 — 모델이 바꿔 부를 수 없다
                .toolContext(Map.of("userId", userId))
                .call()
                .content();
    }
}
