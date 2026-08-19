package skala.springai.day1.controlleradvice;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import skala.springai.day1.exception.OrderNotFoundException;
import skala.springai.day1.web.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class Lab1ExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(OrderNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse("주문을 찾을 수 없습니다", null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception e) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[{}] 요약 실패", traceId, e);
        return ResponseEntity.status(503).body(new ErrorResponse(
            "요약을 만들지 못했습니다. 잠시 후 다시 시도해 주세요.", traceId));
    }
}
