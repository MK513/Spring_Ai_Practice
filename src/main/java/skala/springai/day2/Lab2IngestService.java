package skala.springai.day2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Step 1 — 인제스트: 메타데이터가 절반.
 *
 * <p>읽기 → 분할 → 메타데이터 → 저장. 출처와 버전은 이 시점에만 넣을 수 있다.
 * 저장한 뒤에는 넣을 방법이 없다.
 */
@Service
public class Lab2IngestService {

    private static final Logger log = LoggerFactory.getLogger(Lab2IngestService.class);

    public record IngestResult(String source, int chunks) {}

    private final VectorStore vectorStore;

    public Lab2IngestService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public IngestResult ingest(Resource doc, String source, String version) {
        var reader = new TextReader(doc);                    // .md → Document

        var splitter = TokenTextSplitter.builder()
                .withChunkSize(400)              // 토큰 기준 — 문서 성격에 맞춘다
                .withMinChunkSizeChars(200)
                .build();

        // 출처·버전은 이 시점에만 넣을 수 있다. reader.getCustomMetadata() 에 넣으면
        // TextReader 가 source 를 제 파일명으로 덮어써서 아래 삭제 필터가 안 걸린다.
        // 그러면 넣을 때마다 중복이 쌓인다 — 조각을 만든 뒤 우리가 직접 넣는다.
        List<Document> chunks = splitter.apply(reader.get()).stream()
                .map(c -> {
                    Map<String, Object> meta = new HashMap<>(c.getMetadata());
                    meta.put("source", source);
                    meta.put("version", version);
                    return new Document(c.getText(), meta);
                })
                .toList();

        vectorStore.delete(new FilterExpressionBuilder()      // 재색인 —
                .eq("source", source).build());               //  같은 출처를 지우고
        vectorStore.add(chunks);                              //  다시 넣는다

        log.info("인제스트 source={} chunks={}", source, chunks.size());
        return new IngestResult(source, chunks.size());
    }
}
