package skala.springai.day2;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Step 2 — 검색을 먼저 눈으로 본다.
 *
 * <p>답변보다 검색을 먼저 만든다. 답이 틀렸을 때 검색이 문제인지 프롬프트가 문제인지
 * 가르는 것이 언제나 첫 번째 일이고, 그러려면 검색 결과를 볼 수 있어야 한다.
 */
@Service
public class Lab2RetrieveService {

    /** 검색 결과 한 조각. 점수를 반드시 노출한다 — 감으로 판단하지 않는다. */
    public record Chunk(String source, Double score, String text) {}

    private final VectorStore vectorStore;

    public Lab2RetrieveService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * @param threshold 이 점수 아래는 근거로 치지 않는다. 처음에는 0 으로 두고 점수 분포부터 본다 —
     *                  한국어 짧은 질문은 0.3~0.45 에 몰려서, 0.5 로 두면 전부 걸러진다.
     */
    public List<Chunk> retrieve(String q, int topK, double threshold) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                        .query(q).topK(topK)
                        .similarityThreshold(threshold)
                        .build())
                .stream()
                .map(d -> new Chunk(source(d),
                                    d.getScore(),     // 점수를 노출한다
                                    snippet(d.getText(), 120)))
                .toList();
    }

    private static String source(Document d) {
        Object s = d.getMetadata().get("source");
        return s == null ? "unknown" : s.toString();
    }

    private static String snippet(String text, int len) {
        String 한줄 = text.replaceAll("\\s+", " ").strip();
        return 한줄.length() <= len ? 한줄 : 한줄.substring(0, len) + "…";
    }
}
