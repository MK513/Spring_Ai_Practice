package skala.springai.day2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 3 — 근거로 답하기.
 *
 * <p>세 가지가 이 클래스의 전부다.
 * <ul>
 *   <li>근거가 없으면 모델을 부르지 않는다 — 부를 이유가 없고, 부르면 지어낸다
 *   <li>프롬프트에 거절 지시를 반드시 넣는다 — 근거에 없으면 "확인되지 않습니다"
 *   <li>응답은 문자열이 아니라 객체로 받는다
 * </ul>
 */
@Service
public class Lab2AskService {

    /** 답변·출처·근거 사용 여부를 타입에 못 박는다. */
    public record AnswerDto(String answer, List<String> sources, boolean grounded) {
        static AnswerDto unknown() {
            return new AnswerDto("확인되지 않습니다.", List.of(), false);
        }
    }

    private static final String 지시 = """
            아래 [근거]만 사용해 답한다. 근거에 없으면 "확인되지 않습니다"라고 답한다.
            추측하지 않는다. 답변 끝에 사용한 출처를 [출처: 파일명] 형식으로 남긴다.
            """;

    private final Lab2RetrieveService retrieveService;
    private final ChatClient chatClient;

    public Lab2AskService(Lab2RetrieveService retrieveService, ChatClient.Builder builder) {
        this.retrieveService = retrieveService;
        this.chatClient = builder.build();
    }

    public AnswerDto ask(String question) {
        return ask(question, 4, 0.25);
    }

    public AnswerDto ask(String question, int topK, double threshold) {
        List<Document> docs = retrieveService.search(question, topK, threshold);
        if (docs.isEmpty()) {
            return AnswerDto.unknown();       // 근거가 없으면 모델을 부르지 않는다
        }
        return chatClient.prompt()
                .system(지시)
                .user(u -> u.text("[근거]\n{context}\n\n[질문] {question}")
                            .param("context", format(docs))
                            .param("question", question))
                .call()
                .entity(AnswerDto.class);     // 구조화 출력 — 문자열 파싱 금지(6장)
    }

    /** 어느 조각이 어느 문서에서 왔는지 모델이 알아야 출처를 붙일 수 있다. */
    private static String format(List<Document> docs) {
        return docs.stream()
                .map(d -> "[출처: %s]%n%s".formatted(Lab2RetrieveService.sourceOf(d), d.getText()))
                .collect(Collectors.joining("\n\n"));
    }
}
