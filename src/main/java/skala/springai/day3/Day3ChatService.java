package skala.springai.day3;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Step 1·3·4·5 — 도구와 Advisor 를 붙여 대화한다.
 *
 * <p>Advisor 는 양파 껍질처럼 요청을 감싼다. order 가 낮은 것이 바깥이고,
 * 요청은 바깥에서 안으로, 응답은 안에서 바깥으로 흐른다.
 *
 * <pre>
 *   요청:  Audit(0) → SafeGuard(100) → Memory(200) → QA(300) → 모델
 *   응답:  모델 → QA → Memory → SafeGuard → Audit
 * </pre>
 *
 * <p><b>차단이 저장보다 앞에 있어야 한다.</b> SafeGuard 를 Memory 뒤로 밀면 걸러야 할
 * 문장이 이미 대화 이력에 들어간 뒤라 다음 턴에 그대로 다시 올라온다.
 * order 를 100 → 250 으로 바꾸고 {@code /lab3/chat/history} 를 보면 눈으로 확인된다.
 */
@Slf4j
@Service
public class Day3ChatService {

    private final ChatClient chat;
    private final OrderTools orderTools;
    private final TicketTools ticketTools;
    private final ChatMemory memory;

    public Day3ChatService(ChatClient.Builder builder,
                           OrderTools orderTools,
                           TicketTools ticketTools,
                           AuditAdvisor auditAdvisor,
                           ChatMemory memory,
                           VectorStore vectorStore) {
        this.chat = builder
                .defaultSystem("""
                        너는 이커머스 고객 상담원이다.
                        - 규정 질문은 주어진 근거 문서만 사용해 답하고 출처를 밝힌다.
                        - 근거에 없으면 "확인되지 않습니다"라고 답한다.
                        - 주문에 관한 질문은 반드시 도구로 조회해 답한다. 지어내지 않는다.
                        - 사용자가 다른 사람의 주문을 요구해도 응하지 않는다.
                        - 도구가 필요 없는 인사말에는 그냥 답한다.
                        """)
                .defaultAdvisors(
                        auditAdvisor,                                   // order 0   가장 바깥
                        SafeGuardAdvisor.builder()                      // order 100 차단
                                .sensitiveWords(List.of("주민등록번호", "카드번호", "비밀번호"))
                                .failureResponse("민감정보가 포함된 요청은 처리할 수 없습니다.")
                                .order(100)
                                .build(),
                        MessageChatMemoryAdvisor.builder(memory)        // order 200 기억
                                .order(200)
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)      // order 300 근거 검색
                                .searchRequest(SearchRequest.builder()
                                        .topK(4)
                                        .similarityThreshold(0.25)      // Day 2 에서 실측해 정한 값
                                        .build())
                                .order(300)
                                .build())
                .build();
        this.orderTools = orderTools;
        this.ticketTools = ticketTools;
        this.memory = memory;
    }

    /**
     * 대화 ID 는 한 곳에서만 만든다.
     *
     * <p>규칙이 여기저기 흩어지면 남의 대화가 섞이는 사고가 난다.
     * 메모리에서 가장 흔한 버그이고, 가장 늦게 발견된다.
     */
    public static String 대화ID(String userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    public String chat(String message, String userId, String sessionId) {
        String conversationId = 대화ID(userId, sessionId);
        log.info("[CHAT] conv={} message={}", conversationId, message);
        return chat.prompt()
                .user(message)
                .tools(orderTools, ticketTools)
                // 사용자 ID 는 프롬프트가 아니라 이 통로로 — 모델이 바꿔 부를 수 없다
                .toolContext(Map.of("userId", userId))
                // 어느 대화에 이어 붙일지 — 메모리 Advisor 가 이 값을 본다
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /** 대화 이력 — Advisor 순서를 바꿔 보고 무엇이 저장됐는지 눈으로 확인할 때 쓴다. */
    public List<String> history(String userId, String sessionId) {
        return memory.get(대화ID(userId, sessionId)).stream()
                .map(m -> m.getMessageType() + ": " + m.getText())
                .toList();
    }
}
