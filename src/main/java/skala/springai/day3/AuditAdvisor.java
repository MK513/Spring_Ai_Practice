package skala.springai.day3;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Step 4 — 감사 Advisor. 체인의 가장 바깥에 둔다.
 *
 * <p>{@link BaseAdvisor} 를 구현하면 동기와 스트리밍 양쪽에 한 번에 걸린다.
 * {@code CallAdvisor} 만 구현하면 스트리밍 요청이 감사 로그에서 통째로 빠진다 —
 * 교재 305쪽이 「스트리밍에서 감사 누락」으로 적어 둔 함정이다.
 */
@Slf4j
@Component
public class AuditAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        log.info("[AUDIT] 요청 in={}", 한줄(request.prompt().getContents()));
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        String out = response.chatResponse() == null || response.chatResponse().getResult() == null
                ? "(없음)"
                : response.chatResponse().getResult().getOutput().getText();
        log.info("[AUDIT] 응답 out={}", 한줄(out));
        return response;
    }

    private static String 한줄(String s) {
        String t = String.valueOf(s).replaceAll("\\s+", " ");
        return t.length() <= 160 ? t : t.substring(0, 160) + "…";
    }

    /** 낮을수록 바깥. 감사는 무슨 일이 있어도 남아야 하므로 맨 바깥에 둔다. */
    @Override
    public int getOrder() {
        return 0;
    }
}
