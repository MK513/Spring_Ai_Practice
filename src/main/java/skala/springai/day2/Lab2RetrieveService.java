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
     * @param threshold 이 점수 아래는 근거로 치지 않는다. 실제 점수를 보고 정해야 하는 값이라
     *                  밖에서 바꿀 수 있게 열어 둔다(교재 226쪽 실험표 E).
     *                  <p>없는 것을 거절하는 일은 이 값이 못 한다. 거절은 Step 3 의 프롬프트가 맡는다.
     */
    public List<Chunk> retrieve(String q, int topK, double threshold) {
        return search(q, topK, threshold).stream()
                .map(d -> new Chunk(source(d),
                                    d.getScore(),     // 점수를 노출한다
                                    snippet(d.getText(), 120)))
                .toList();
    }

    /**
     * 모델에 넣을 근거는 자르지 않은 것으로 준다.
     *
     * <p>{@link #retrieve}가 돌려주는 Chunk 는 눈으로 보려고 120자만 남긴 것이다.
     * 그걸 그대로 프롬프트에 넣으면 근거가 반토막 난 채로 들어간다.
     */
    public List<Document> search(String q, int topK, double threshold) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(q).topK(topK)
                .similarityThreshold(threshold)
                .build());
    }

    static String sourceOf(Document d) {
        return source(d);
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
