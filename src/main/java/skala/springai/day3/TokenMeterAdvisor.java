package skala.springai.day3;

import java.util.concurrent.TimeUnit;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Step 6 — 토큰과 지연을 잰다.
 *
 * <p>보이지 않는 비용은 줄일 수도 없다. 태그를 붙여 둬야 나중에 기능별로 쪼개 볼 수 있다.
 *
 * <p>체인의 <b>가장 안쪽</b>(order 900)에 둔다. 여기서 재면 Advisor 들이 쓴 시간이 빠지고
 * 모델 호출 구간만 잡힌다 — 그래서 태그가 {@code phase=model} 이다.
 * 바깥에 두면 전체 왕복 시간이 잡히는데, 그건 그것대로 다른 지표다.
 *
 * <pre>
 * GET /actuator/metrics/ai.tokens?tag=type:prompt
 * GET /actuator/metrics/ai.latency
 * </pre>
 */
@Slf4j
@Component
public class TokenMeterAdvisor implements CallAdvisor {

    private final MeterRegistry registry;

    public TokenMeterAdvisor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long started = System.nanoTime();
        ChatClientResponse response = chain.nextCall(request);
        long elapsed = System.nanoTime() - started;

        registry.timer("ai.latency", "phase", "model").record(elapsed, TimeUnit.NANOSECONDS);

        Usage usage = response.chatResponse() == null || response.chatResponse().getMetadata() == null
                ? null : response.chatResponse().getMetadata().getUsage();
        if (usage != null) {
            registry.counter("ai.tokens", "type", "prompt", "feature", "chat")
                    .increment(nullSafe(usage.getPromptTokens()));
            registry.counter("ai.tokens", "type", "completion", "feature", "chat")
                    .increment(nullSafe(usage.getCompletionTokens()));
            log.info("[METER] 프롬프트 {} · 완성 {} 토큰 · {}ms",
                    usage.getPromptTokens(), usage.getCompletionTokens(), elapsed / 1_000_000);
        }
        return response;
    }

    private double nullSafe(Integer v) {
        return v == null ? 0 : v;
    }

    @Override
    public String getName() {
        return "tokenMeter";
    }

    /** 가장 안쪽 — 모델 호출 구간만 재려는 것이다. */
    @Override
    public int getOrder() {
        return 900;
    }
}
