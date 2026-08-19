package skala.springai.day2;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Day 2 실습용 벡터 저장소 — 인메모리.
 *
 * <p>도커 없이 바로 돌리려고 이걸 쓴다. 재시작하면 사라지므로 뜰 때마다 다시 인제스트해야 한다.
 * pgvector 스타터를 넣으면 이 빈은 물러난다(확장 과제).
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
