package io.pinksoft.opp.user;

import java.util.Map;

final class ApiMessages {

    private ApiMessages() {}

    static Map<String, String> body(String message) {
        String safe = (message == null || message.isBlank()) ? "요청 처리에 실패했습니다." : message;
        return Map.of("message", safe);
    }
}
