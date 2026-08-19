package skala.springai.day1.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import skala.springai.day1.domain.Order;
import skala.springai.day1.exception.OrderNotFoundException;
import skala.springai.day1.repository.OrderRepository;
import skala.springai.day1.web.dto.SummaryResponse;


@Transactional(readOnly = true)
@Service
@AllArgsConstructor
public class OrderSummaryService {
    
    private final OrderRepository orders;
    private final ChatClient summaryChat;

    public SummaryResponse summarize(String orderId, String userId) {
        Order order = orders.findByIdAndOwnerId(orderId, userId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    
        String summary = summaryChat.prompt()
            .user(u -> u.text("주문번호 {id} - 상품 {item} - 상태 {status} - 도착예정 {eta}"
                + "\n위 정보를 세 문장으로 요약해 줘.")
                .param("id", order.getId())
                .param("item", order.getItem())
                .param("status", order.getStatus().label())
                .param("eta", order.getEta()))
            .call().content();
        
            return new SummaryResponse(order.getId(), summary);

    }

    
}
