package skala.springai.day2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import skala.springai.day2.Lab2IngestService.IngestResult;
import skala.springai.day2.Lab2AskService.AnswerDto;
import skala.springai.day2.Lab2RetrieveService.Chunk;

import java.util.Arrays;
import java.util.List;

/**
 * Day 2 실습 — 사내 문서 Q&A.
 *
 * <pre>
 * POST /lab2/ingest              문서 3종을 읽어 넣는다. 두 번 눌러도 청크 수가 같아야 한다
 * GET  /lab2/retrieve?q=&topK=&threshold=   답변을 만들기 전에 검색부터 눈으로 본다
 * POST /lab2/ask?q=                         근거로만 답한다. 근거에 없으면 지어내지 않는다
 * </pre>
 */
@RestController
public class Lab2Controller {

    private final Lab2IngestService ingestService;
    private final Lab2RetrieveService retrieveService;
    private final Lab2AskService askService;
    private final Resource[] docs;

    public Lab2Controller(Lab2IngestService ingestService,
                          Lab2RetrieveService retrieveService,
                          Lab2AskService askService,
                          @Value("classpath:lab2-docs/*.md") Resource[] docs) {
        this.ingestService = ingestService;
        this.retrieveService = retrieveService;
        this.askService = askService;
        this.docs = docs;
    }

    @PostMapping("/lab2/ingest")
    public List<IngestResult> ingest() {
        return Arrays.stream(docs)
                .map(d -> ingestService.ingest(d, 이름(d), "v1"))
                .toList();
    }

    @GetMapping("/lab2/retrieve")            // 답변을 만들기 전에 이것부터 만든다
    public List<Chunk> retrieve(@RequestParam String q,
                                @RequestParam(defaultValue = "4") int topK,
                                @RequestParam(defaultValue = "0.5") double threshold) {
        return retrieveService.retrieve(q, topK, threshold);
    }

    @PostMapping("/lab2/ask")
    public AnswerDto ask(@RequestParam String q,
                         @RequestParam(defaultValue = "4") int topK,
                         @RequestParam(defaultValue = "0.5") double threshold) {
        return askService.ask(q, topK, threshold);
    }

    /** return-policy.md → return-policy. 이 이름이 그대로 답변의 출처가 된다. */
    private static String 이름(Resource d) {
        String f = d.getFilename();
        return f == null ? "unknown" : f.replaceFirst("\\.md$", "");
    }
}
