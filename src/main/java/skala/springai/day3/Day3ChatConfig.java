package skala.springai.day3;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Day 3 대화 메모리 설정.
 *
 * <p>{@code ChatClient} 는 여기서 빈으로 만들지 않는다 — Day 1 에 이미 하나 있어서
 * 후보가 둘이 되면 앱이 뜨지 않는다. 창구 조립은 {@link Day3ChatService} 안에서 한다.
 */
@Configuration
public class Day3ChatConfig {

    /** 인메모리라 재시작하면 대화가 사라진다. 인스턴스가 둘이 되면 JDBC·Redis 로 바꿔야 한다. */
    @Bean
    ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)            // 길어진 대화는 잘라 토큰을 통제한다
                .build();
    }
}
