package skala.springai.day1.web.dto;

public record ErrorResponse(
    String response,
    String traceId
) {
    
}