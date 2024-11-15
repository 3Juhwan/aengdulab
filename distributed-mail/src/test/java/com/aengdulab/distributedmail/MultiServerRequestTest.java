package com.aengdulab.distributedmail;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.aengdulab.distributedmail.repository.SentMailEventRepository;
import io.restassured.RestAssured;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SuppressWarnings("NonAsciiCharacters")
@Sql(scripts = {"/data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MultiServerRequestTest {

    private static final Logger log = LoggerFactory.getLogger(MultiServerRequestTest.class);
    private static final int SUBSCRIBER_COUNT = 20;

    @Autowired
    private SentMailEventRepository sentMailEventRepository;

    @Test
    void 다중화된_서버에서_균등하고_중복_없이_모든_구독자에게_메일을_발송한다() throws Exception {
        RestAssured.baseURI = "http://localhost";

        try (ExecutorService threadPool = Executors.newFixedThreadPool(2)) {
            sendRequest(threadPool, 8081);
            sendRequest(threadPool, 8082);
        }

        Thread.sleep(5000);

        long sentMailCount = sentMailEventRepository.count();
        assertThat(sentMailCount).isEqualTo(SUBSCRIBER_COUNT);
    }

    private static void sendRequest(ExecutorService executorService, int portNumber) {
        executorService.submit(() -> {
            given()
                    .port(portNumber)
                    .when()
                    .post("/send-mail")
                    .then()
                    .statusCode(200);
        });
    }
}
