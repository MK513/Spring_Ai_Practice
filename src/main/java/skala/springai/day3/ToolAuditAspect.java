package skala.springai.day3;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Step 6 · 완료 기준 7 — 도구 호출을 한 곳에서 기록하고 센다.
 *
 * <p>도구마다 로깅을 넣으면 반드시 빠뜨리는 곳이 생긴다. {@code @Tool} 이라는
 * <b>타입</b>이 붙은 메서드를 전부 잡아 한 자리에서 처리한다.
 *
 * <p>인자에 개인정보가 들어올 수 있어 흔한 형태는 가린다. 운영이라면 마스킹 규칙과
 * 보존 기간을 함께 정하고 시작해야 한다.
 */
@Slf4j
@Aspect
@Component
public class ToolAuditAspect {

    private final MeterRegistry registry;

    public ToolAuditAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object 감사(ProceedingJoinPoint pjp) throws Throwable {
        String tool = pjp.getSignature().getName();
        String args = 가린다(Arrays.toString(pjp.getArgs()));
        long started = System.nanoTime();

        try {
            Object result = pjp.proceed();
            registry.counter("ai.tool.calls", "tool", tool, "result", "ok").increment();
            log.info("[TOOL_AUDIT] tool={} args={} result=OK {}ms",
                    tool, args, (System.nanoTime() - started) / 1_000_000);
            return result;

        } catch (Throwable e) {
            registry.counter("ai.tool.calls", "tool", tool, "result", "fail").increment();
            log.warn("[TOOL_AUDIT] tool={} args={} result=FAIL error={}", tool, args, e.getMessage());
            throw e;
        }
    }

    private static String 가린다(String raw) {
        return raw.replaceAll("\\d{6}-\\d{7}", "******-*******")
                  .replaceAll("\\d{4}-\\d{4}-\\d{4}-\\d{4}", "****-****-****-****")
                  .replaceAll("[\\w.+-]+@[\\w-]+\\.[\\w.]+", "***@***");
    }
}
