package com.aengdulab.distributedmail;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.aengdulab.distributedmail.domain.SentMailEvent;
import com.aengdulab.distributedmail.repository.SentMailEventRepository;
import io.restassured.RestAssured;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@Slf4j
@SuppressWarnings("NonAsciiCharacters")
@Sql(scripts = {"/data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MultiServerRequestTest {

    private static final int SUBSCRIBER_COUNT = 20;

    /*
    서버를 다중화할 경우, 새로운 요청에 대한 추가 포트를 설정
     */
    @Value("#{T(java.util.Arrays).asList(8080, 9090)}")
    private List<Integer> serverPort;

    @Autowired
    private SentMailEventRepository sentMailEventRepository;

    @Test
    void 다중화된_서버에서_균등하고_중복_없이_모든_구독자에게_메일을_발송한다() throws Exception {
        RestAssured.baseURI = "http://localhost";

        try (ExecutorService threadPool = Executors.newFixedThreadPool(serverPort.size())) {
            serverPort.forEach(port -> sendRequest(threadPool, port));
        }

        Thread.sleep(5000);

        long sentMailCount = sentMailEventRepository.count();
        assertThat(sentMailCount).isEqualTo(SUBSCRIBER_COUNT);

        long uniqueMailReceivedSubscribeCount = getUniqueMailReceivedSubscribeCount();
        assertThat(uniqueMailReceivedSubscribeCount).isEqualTo(SUBSCRIBER_COUNT);
    }

    private void sendRequest(ExecutorService executorService, int portNumber) {
        executorService.submit(() -> {
            given()
                    .port(portNumber)
                    .when()
                    .post("/send-mail")
                    .then()
                    .statusCode(200);
        });
    }

    private long getUniqueMailReceivedSubscribeCount() {
        List<SentMailEvent> sentMailEvents = sentMailEventRepository.findAll();
        return sentMailEvents.stream()
                .map(SentMailEvent::getSubscribe)
                .distinct()
                .count();
    }
}
