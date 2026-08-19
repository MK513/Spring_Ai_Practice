package skala.springai.day2;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import skala.springai.day1.Day1Application;
import skala.springai.day2.Lab2AskService.AnswerDto;

@Slf4j
@SpringBootTest(classes = Day1Application.class)
public class GoldenSetTest {

    /** 골든 세트 한 항목. must 는 답변에 반드시 들어가야 할 키워드, src 는 기대 출처. */
    record Golden(String q, List<String> must, String src) {}

    @Autowired
    private Lab2AskService service;

    @Autowired
    private Lab2IngestService ingestService;

    @Value("classpath:lab2-docs/*.md")
    private Resource[] docs;

    // Spring Boot 4 기본 ObjectMapper 빈은 Jackson 3(tools.jackson) 타입이라 주입 불가 — 직접 생성한다.
    private final ObjectMapper mapper = new ObjectMapper();

    private static InputStream resource(String name) {
        return GoldenSetTest.class.getClassLoader().getResourceAsStream(name);
    }

    /** SimpleVectorStore 는 인메모리라 컨텍스트가 새로 뜰 때마다 비어 있다 — 평가 전에 색인부터 한다. */
    @BeforeEach
    void 인제스트() {
        for (Resource d : docs) {
            String source = d.getFilename().replaceFirst("\\.md$", "");
            ingestService.ingest(d, source, "v1");
        }
        log.info("인제스트 완료: {} 개 문서", docs.length);
    }

    @Test
    void 골든_세트_평가() throws Exception {
        var golden = mapper.readValue(resource("golden.json"),
                        new TypeReference<List<Golden>>() {});

        int pass = 0;
        for (Golden g : golden) {
            AnswerDto a = service.ask(g.q());
            boolean hit = g.must().stream().allMatch(k -> a.answer().contains(k));
            boolean cite = g.src() == null
                            || a.sources().stream().anyMatch(s -> s.contains(g.src()));
            if (hit && cite) {
                pass++;
                log.info("통과: {}\n 답변: {}\n 출처: {}", g.q(), a.answer(), a.sources());
            } else {
                log.warn("실패: {}\n 답변: {}\n 출처: {}", g.q(), a.answer(), a.sources());
            }
        }
        log.info("통과 {}/{}", pass, golden.size());
        assertThat(pass).isGreaterThanOrEqualTo(8); 
    }
}
