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

import java.util.List;

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
        reader.getCustomMetadata().put("source", source);    // 나중에 못 넣는다
        reader.getCustomMetadata().put("version", version);

        var splitter = TokenTextSplitter.builder()
                .withChunkSize(400)              // 토큰 기준 — 문서 성격에 맞춘다
                .withMinChunkSizeChars(200)
                .build();
        List<Document> chunks = splitter.apply(reader.get());

        vectorStore.delete(new FilterExpressionBuilder()      // 재색인 —
                .eq("source", source).build());               //  같은 출처를 지우고
        vectorStore.add(chunks);                              //  다시 넣는다

        log.info("인제스트 source={} chunks={}", source, chunks.size());
        return new IngestResult(source, chunks.size());
    }
}
