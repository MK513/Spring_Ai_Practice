package skala.springai.day2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import skala.springai.day2.Lab2IngestService.IngestResult;

import java.util.Arrays;
import java.util.List;

/**
 * Day 2 실습 — 사내 문서 Q&A.
 *
 * <pre>
 * POST /lab2/ingest   문서 3종을 읽어 넣는다. 두 번 눌러도 청크 수가 같아야 한다
 * </pre>
 */
@RestController
public class Lab2Controller {

    private final Lab2IngestService ingestService;
    private final Resource[] docs;

    public Lab2Controller(Lab2IngestService ingestService,
                          @Value("classpath:lab2-docs/*.md") Resource[] docs) {
        this.ingestService = ingestService;
        this.docs = docs;
    }

    @PostMapping("/lab2/ingest")
    public List<IngestResult> ingest() {
        return Arrays.stream(docs)
                .map(d -> ingestService.ingest(d, 이름(d), "v1"))
                .toList();
    }

    /** return-policy.md → return-policy. 이 이름이 그대로 답변의 출처가 된다. */
    private static String 이름(Resource d) {
        String f = d.getFilename();
        return f == null ? "unknown" : f.replaceFirst("\\.md$", "");
    }
}
