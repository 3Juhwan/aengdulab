package com.aengdulab.distributedmail.support;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

public class MailHogClient implements TestMailClient {

    private static final String DELETE_ALL_MESSAGES_URL = "/api/v1/messages";
    private static final String GET_ALL_MESSAGES_URL = "/api/v2/messages";

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.console-port}")
    private String consolePort;

    /**
     * MailHog의 GET /api/v2/messages 엔드포인트 응답을 파싱합니다. 응답 JSON에서 `items.Raw.To`를 추출하여 발송된 메일의 모든 수신자를 파싱합니다.
     * <p>
     * 실제 응답 예:
     * <pre>
     * {
     *   "items": [
     *     {
     *       "Raw": {
     *         "To": ["user1@example.com", "user2@example.com"]
     *       }
     *     },
     *     {
     *       "Raw": {
     *         "To": ["user3@example.com"]
     *       }
     *     }
     *   ]
     * }
     * </pre>
     * 위 JSON에서 모든 `To` 값을 추출하여 결과로 수신자 목록을 반환합니다. 예: ["user1@example.com", "user2@example.com", "user3@example.com"]
     *
     * @see <a href="https://github.com/mailhog/MailHog/blob/master/docs/APIv2/swagger-2.0.json">
     * MailHog API 문서</a>
     */
    public List<String> getMailReceivedSubscribes() {
        ExtractableResponse<Response> response = RestAssured.when()
                .get(concatUrl(host, consolePort, GET_ALL_MESSAGES_URL)).then().statusCode(HttpStatus.OK.value()).and()
                .extract();

        return parseJsonMailReceivedSubscribes(response);
    }

    private List<String> parseJsonMailReceivedSubscribes(ExtractableResponse<Response> response) {
        return response.jsonPath().getList("items.Raw.To", List.class).stream()
                .flatMap(item -> ((List<String>) item).stream()).toList();
    }

    public void deleteAll() {
        RestAssured.when().delete(concatUrl(host, consolePort, DELETE_ALL_MESSAGES_URL)).then()
                .statusCode(HttpStatus.OK.value());
    }

    private String concatUrl(String host, String port, String path) {
        return "http://" + host + ":" + port + path;
    }
}
